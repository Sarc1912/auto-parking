package com.autopay.parking.service;

import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.TariffConfig;
import com.autopay.parking.repository.ParkingTicketStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del estacionamiento: buscar tickets, calcular montos,
 * emitir/completar tickets y reportar el estado.
 */
@Service
@Transactional
public class ParkingService {

    private final ParkingTicketStore ticketRepository;
    private final TariffService tariffService;

    public ParkingService(ParkingTicketStore ticketRepository,
                          TariffService tariffService) {
        this.ticketRepository = ticketRepository;
        this.tariffService = tariffService;
    }

    public ParkingTicket findByCode(String code) {
        return ticketRepository.findByCode(code).orElse(null);
    }

    /**
     * Valida el ticket escaneado y devuelve la información para confirmación:
     * tiempo transcurrido y monto a pagar.
     *
     * @throws IllegalStateException si el ticket no existe o ya fue pagado.
     */
    public TicketInfo validateTicket(String code) {
        ParkingTicket ticket = findByCode(code);
        if (ticket == null) {
            throw new IllegalStateException("TICKET_NOT_FOUND");
        }
        if (ticket.getStatus() != ParkingTicket.Status.ACTIVE) {
            throw new IllegalStateException("TICKET_ALREADY_PAID");
        }
        TariffConfig tariff = tariffService.findActiveTariff()
                .orElseThrow(() -> new IllegalStateException("NO_TARIFF"));

        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.max(1, Duration.between(ticket.getEntryTime(), now).toMinutes());
        BigDecimal amount = tariffService.calculate(ticket.getEntryTime(), now, tariff);

        ticket.setAmountDue(amount);
        return new TicketInfo(ticket, tariff, minutes, amount);
    }

    /**
     * Marca el ticket como pagado (salida).
     */
    public void confirmPaid(ParkingTicket ticket) {
        if (ticket.getStatus() != ParkingTicket.Status.ACTIVE) {
            throw new IllegalStateException("TICKET_ALREADY_PAID");
        }
        ticket.setExitTime(LocalDateTime.now());
        ticket.setStatus(ParkingTicket.Status.PAID);
        ticketRepository.save(ticket);
    }

public List<ParkingTicket> findActiveTickets() {
        return ticketRepository.findByStatus(ParkingTicket.Status.ACTIVE);
    }

    /** Todos los tickets, ordenados por código (para el panel de administración). */
    public List<ParkingTicket> findAllTickets() {
        return ticketRepository.findAll().stream()
                .sorted(Comparator.comparing(ParkingTicket::getCode))
                .collect(Collectors.toList());
    }

    public long countActiveTickets() {
        return ticketRepository.countByStatus(ParkingTicket.Status.ACTIVE);
    }

    /**
     * Resumen de tickets agrupados por estado para el dashboard.
     */
    public TicketSummary summarize() {
        List<ParkingTicket> all = ticketRepository.findAll();
        return new TicketSummary(
                all.stream().filter(t -> t.getStatus() == ParkingTicket.Status.ACTIVE).count(),
                all.stream().filter(t -> t.getStatus() == ParkingTicket.Status.PAID).count(),
                all.stream().filter(t -> t.getStatus() == ParkingTicket.Status.CANCELLED).count(),
                all.stream()
                        .filter(t -> t.getStatus() == ParkingTicket.Status.PAID && t.getAmountDue() != null)
                        .map(ParkingTicket::getAmountDue)
                        .collect(Collectors.summingDouble(BigDecimal::doubleValue))
        );
    }

    /** Contenedor de la información mostrada en la pantalla de confirmación. */
    public record TicketInfo(ParkingTicket ticket, TariffConfig tariff,
                             long minutes, BigDecimal amount) {
    }

    /** Resumen agregado para el panel de administración. */
    public record TicketSummary(long active, long paid, long cancelled, double totalCollected) {
    }
}



