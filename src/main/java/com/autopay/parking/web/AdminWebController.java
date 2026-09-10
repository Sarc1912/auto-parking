package com.autopay.parking.web;

import com.autopay.parking.model.AppUser;
import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.model.TariffConfig;
import com.autopay.parking.service.AuthService;
import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.PaymentService;
import com.autopay.parking.service.TariffService;
import com.autopay.parking.ui.util.FormatUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * API REST del panel de administración web. Comparte la misma base de datos
 * que el kiosco (tarifas, tasa de cambio y pagos).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminWebController {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AuthService authService;
    private final SessionManager sessions;
    private final ParkingService parkingService;
    private final PaymentService paymentService;
    private final TariffService tariffService;

    public AdminWebController(AuthService authService, SessionManager sessions,
                              ParkingService parkingService,
                              PaymentService paymentService,
                              TariffService tariffService) {
        this.authService = authService;
        this.sessions = sessions;
        this.parkingService = parkingService;
        this.paymentService = paymentService;
        this.tariffService = tariffService;
    }

    // ---------- Sesión ----------

    /** Inicia sesión con las credenciales de administrador. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<AppUser> user = authService.authenticate(
                request.username(), request.password());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Usuario o contraseña incorrectos."));
        }
        String token = sessions.open(user.get());
        return ResponseEntity.ok(new LoginResponse(token, user.get().getFullName()));
    }

    /** Cierra la sesión del token enviado. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = AdminAuthFilter.TOKEN_HEADER, required = false) String token) {
        sessions.close(token);
        return ResponseEntity.ok().build();
    }

    // ---------- Resumen ----------

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        ParkingService.TicketSummary s = parkingService.summarize();
        List<PaymentRecord> records = paymentService.findAll();

        long todayCount = records.stream()
                .filter(r -> r.getTimestamp() != null
                        && r.getTimestamp().toLocalDate().equals(LocalDate.now()))
                .count();

        BigDecimal collected = records.stream()
                .filter(r -> r.getStatus() == PaymentRecord.Status.COMPLETED)
                .map(this::converted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ExchangeRate rate = paymentService.latestRate().orElse(null);
        return new DashboardResponse(
                s.active(),
                s.paid(),
                paymentService.countCompleted(),
                todayCount,
                FormatUtil.ves(collected),
                rate != null ? rate.getBcvRate().toPlainString() : null,
                rate != null && rate.getParallelRate() != null
                        ? rate.getParallelRate().toPlainString() : null);
    }

    // ---------- Tarifas ----------

    @GetMapping("/tariffs")
    public List<TariffPayload> tariffs() {
        return tariffService.findAll().stream()
                .map(t -> new TariffPayload(t.getId(), t.getName(),
                        t.getRatePerHour(), t.getMinimumMinutes(),
                        t.getMaxDailyRate(), t.isActive()))
                .collect(Collectors.toList());
    }

    // ---------- Tickets ----------

    /** Todos los tickets del sistema, con su pago asociado si existe. */
    @GetMapping("/tickets")
    public List<TicketPayload> tickets() {
        Map<Long, PaymentRecord> paymentsByTicket = paymentService.findAll().stream()
                .filter(r -> r.getTicket() != null)
                .collect(Collectors.toMap(
                        r -> r.getTicket().getId(), r -> r, (a, b) -> b));

        return parkingService.findAllTickets().stream()
                .map(t -> {
                    PaymentRecord p = paymentsByTicket.get(t.getId());
                    return new TicketPayload(
                            t.getCode(),
                            fmtDate(t.getEntryTime()),
                            t.getExitTime() != null ? fmtDate(t.getExitTime()) : null,
                            t.getLicensePlate(),
                            t.getAmountDue() != null ? t.getAmountDue().toPlainString() : null,
                            statusName(t.getStatus()),
                            p != null ? FormatUtil.paymentMethod(p.getPaymentMethod()) : null,
                            p != null ? FormatUtil.currency(p.getCurrency()) : null,
                            p != null ? p.getAmount().toPlainString() : null);
                })
                .collect(Collectors.toList());
    }

    private String fmtDate(LocalDateTime value) {
        return value != null ? value.format(FMT) : null;
    }

    /** Crea o actualiza una tarifa (si llega @{code id}). */
    @PostMapping("/tariffs")
    public ResponseEntity<?> saveTariff(@RequestBody TariffPayload payload) {
        if (payload.name() == null || payload.name().isBlank()
                || payload.ratePerHour() == null || payload.minimumMinutes() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Complete nombre, tarifa por hora y minutos mínimos."));
        }
        TariffConfig tariff;
        if (payload.id() != null) {
            Optional<TariffConfig> existing = tariffService.findAll().stream()
                    .filter(t -> t.getId().equals(payload.id())).findFirst();
            if (existing.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("La tarifa seleccionada ya no existe."));
            }
            tariff = existing.get();
        } else {
            tariff = new TariffConfig();
        }
        tariff.setName(payload.name().trim());
        tariff.setRatePerHour(payload.ratePerHour());
        tariff.setMinimumMinutes(payload.minimumMinutes());
        tariff.setMaxDailyRate(payload.maxDailyRate());
        tariff.setActive(payload.active());
        tariffService.save(tariff);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tariffs/{id}")
    public ResponseEntity<Void> deleteTariff(@PathVariable Long id) {
        tariffService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ---------- Tasa de cambio ----------

    @GetMapping("/rates")
    public RatePayload rates() {
        ExchangeRate rate = paymentService.latestRate().orElse(null);
        if (rate == null) {
            return new RatePayload(null, null);
        }
        return new RatePayload(rate.getBcvRate(), rate.getParallelRate());
    }

    @PutMapping("/rates")
    public ResponseEntity<?> saveRates(@RequestBody RatePayload payload) {
        if (payload.bcvRate() == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Indique la tasa BCV."));
        }
        ExchangeRate rate = paymentService.latestRate().orElse(new ExchangeRate());
        rate.setBcvRate(payload.bcvRate());
        rate.setParallelRate(payload.parallelRate());
        paymentService.saveRate(rate);
        return ResponseEntity.ok().build();
    }

    // ---------- Reportes ----------

    @GetMapping("/reports")
    public ReportsResponse reports() {
        List<ReportRow> rows = paymentService.findAll().stream()
                .sorted(Comparator.comparing(PaymentRecord::getTimestamp).reversed())
                .map(r -> new ReportRow(
                        r.getTimestamp() != null ? r.getTimestamp().format(FMT) : "—",
                        r.getTicket() != null ? r.getTicket().getCode() : "—",
                        FormatUtil.paymentMethod(r.getPaymentMethod()),
                        FormatUtil.currency(r.getCurrency()),
                        r.getAmount().toPlainString(),
                        statusName(r.getStatus())))
                .collect(Collectors.toList());

        BigDecimal total = paymentService.findAll().stream()
                .filter(r -> r.getStatus() == PaymentRecord.Status.COMPLETED)
                .map(this::converted)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportsResponse(rows, FormatUtil.ves(total),
                paymentService.findAll().size());
    }

    private BigDecimal converted(PaymentRecord r) {
        if (r.getCurrency() == PaymentRecord.Currency.USD && r.getExchangeRateUsed() != null) {
            return r.getAmount().multiply(r.getExchangeRateUsed());
        }
        return r.getAmount();
    }

    private String statusName(PaymentRecord.Status status) {
        return switch (status) {
            case COMPLETED -> "Pagado";
            case FAILED -> "Fallido";
            case PENDING -> "Pendiente";
        };
    }

    private String statusName(ParkingTicket.Status status) {
        return switch (status) {
            case ACTIVE -> "Activo";
            case PAID -> "Pagado";
            case CANCELLED -> "Cancelado";
        };
    }

    // ---------- DTOs ----------

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String fullName) {
    }

    public record ErrorResponse(String message) {
    }

    public record DashboardResponse(long activeTickets, long paidTickets,
                                    long paymentsCount, long todayPayments,
                                    String totalCollected,
                                    String bcvRate, String parallelRate) {
    }

    public record TariffPayload(Long id, String name, BigDecimal ratePerHour,
                                Integer minimumMinutes, BigDecimal maxDailyRate,
                                boolean active) {
    }

    public record RatePayload(BigDecimal bcvRate, BigDecimal parallelRate) {
    }

    public record TicketPayload(String code, String entry, String exit,
                                String plate, String amount, String status,
                                String method, String currency, String paymentAmount) {
    }

    public record ReportRow(String date, String ticket, String method,
                            String currency, String amount, String status) {
    }

    public record ReportsResponse(List<ReportRow> rows, String total, long count) {
    }
}