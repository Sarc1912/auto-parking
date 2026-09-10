package com.autopay.parking.service;

import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.repository.ExchangeRateStore;
import com.autopay.parking.repository.PaymentRecordStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Mock del procesador de pagos.
 *
 * <p>No integra pasarela real. Simula la confirmación de un pago y registra la
 * transacción en {@link PaymentRecord}, de modo que el flujo y la UX puedan
 * validarse antes de conectar una pasarela (Pago Móvil, POS, etc.).</p>
 */
@Service
@Transactional
public class PaymentService {

    private final PaymentRecordStore paymentRepository;
    private final ExchangeRateStore exchangeRateRepository;
    private final ParkingService parkingService;

    public PaymentService(PaymentRecordStore paymentRepository,
                          ExchangeRateStore exchangeRateRepository,
                          ParkingService parkingService) {
        this.paymentRepository = paymentRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.parkingService = parkingService;
    }

    /**
     * Simula el procesamiento de un pago y deja el ticket pagado.
     *
     * @return el registro de pago creado.
     */
    public PaymentRecord processPayment(ParkingTicket ticket,
                                        PaymentRecord.PaymentMethod method,
                                        PaymentRecord.Currency currency,
                                        BigDecimal amount) {
        return processPayment(ticket, method, currency, amount, null);
    }

    public PaymentRecord processPayment(ParkingTicket ticket,
                                        PaymentRecord.PaymentMethod method,
                                        PaymentRecord.Currency currency,
                                        BigDecimal amount,
                                        BigDecimal exchangeRate) {
        if (ticket == null || ticket.getStatus() != ParkingTicket.Status.ACTIVE) {
            throw new IllegalStateException("TICKET_NOT_ACTIVE");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalStateException("INVALID_AMOUNT");
        }

        PaymentRecord record = new PaymentRecord();
        record.setTicket(ticket);
        record.setPaymentMethod(method);
        record.setCurrency(currency);
        record.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        record.setExchangeRateUsed(exchangeRate != null
                ? exchangeRate.setScale(2, RoundingMode.HALF_UP) : null);

        // Simulación de pasarela: tras unos segundos el pago se confirma.
        record.setStatus(PaymentRecord.Status.COMPLETED);
        record.setTimestamp(LocalDateTime.now());
        paymentRepository.save(record);

        parkingService.confirmPaid(ticket);
        return record;
    }

    /**
     * Convierte un monto a una moneda objetivo usando la última tasa guardada.
     */
    public BigDecimal convert(BigDecimal amount, ExchangeRate rate,
                              PaymentRecord.Currency from,
                              PaymentRecord.Currency to) {
        if (from == to) {
            return amount;
        }
        BigDecimal r = (rate != null ? rate.getBcvRate() : BigDecimal.ONE);
        if (from == PaymentRecord.Currency.USD && to == PaymentRecord.Currency.VES) {
            return amount.multiply(r).setScale(2, RoundingMode.HALF_UP);
        }
        if (from == PaymentRecord.Currency.VES && to == PaymentRecord.Currency.USD) {
            if (r.signum() == 0) {
                throw new IllegalStateException("INVALID_RATE");
            }
            return amount.divide(r, 2, RoundingMode.HALF_UP);
        }
        throw new IllegalStateException("UNSUPPORTED_CONVERSION");
    }

    public Optional<ExchangeRate> latestRate() {
        return exchangeRateRepository.findFirstByOrderByUpdatedAtDesc();
    }

    public void saveRate(ExchangeRate rate) {
        rate.setUpdatedAt(LocalDateTime.now());
        exchangeRateRepository.save(rate);
    }

    public List<PaymentRecord> findAll() {
        return paymentRepository.findAll();
    }

    public long countCompleted() {
        return paymentRepository.countByStatus(PaymentRecord.Status.COMPLETED);
    }
}





