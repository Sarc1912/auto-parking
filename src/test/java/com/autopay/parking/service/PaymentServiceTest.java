package com.autopay.parking.service;

import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.repository.ExchangeRateStore;
import com.autopay.parking.repository.PaymentRecordStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentServiceTest {

    private final PaymentRecordStore paymentRepository = new MemoryPaymentStore();
    private final ExchangeRateStore rateRepository = new MemoryRateStore();
    private final ParkingService parkingService = new FakeParkingService();

    private final PaymentService service =
            new PaymentService(paymentRepository, rateRepository, parkingService);

    @Test
    void procesaPagoYRegistraTransaccion() {
        PaymentRecord record = service.processPayment(
                activeTicket(),
                PaymentRecord.PaymentMethod.MOBILE_PAY,
                PaymentRecord.Currency.VES,
                new BigDecimal("3.00"),
                new BigDecimal("36.50"));

        assertThat(record.getStatus()).isEqualTo(PaymentRecord.Status.COMPLETED);
        assertThat(record.getAmount()).isEqualByComparingTo("3.00");
        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    void rechazaTicketYaPagado() {
        ParkingTicket paid = activeTicket();
        paid.setStatus(ParkingTicket.Status.PAID);

        assertThatThrownBy(() -> service.processPayment(
                paid, PaymentRecord.PaymentMethod.CARD_POS,
                PaymentRecord.Currency.VES, new BigDecimal("3.00"), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rechazaMontoInvalido() {
        assertThatThrownBy(() -> service.processPayment(
                activeTicket(), PaymentRecord.PaymentMethod.CASH_VES,
                PaymentRecord.Currency.VES, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void convierteDeVesAUsdConTasa() {
        ExchangeRate rate = new ExchangeRate();
        rate.setBcvRate(new BigDecimal("36.50"));

        BigDecimal usd = service.convert(new BigDecimal("73.00"), rate,
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
        assertThat(usd).isEqualByComparingTo("2.00");
    }

    @Test
    void convierteDeUsdAVesConTasa() {
        ExchangeRate rate = new ExchangeRate();
        rate.setBcvRate(new BigDecimal("36.50"));

        BigDecimal ves = service.convert(new BigDecimal("2.00"), rate,
                PaymentRecord.Currency.USD, PaymentRecord.Currency.VES);
        assertThat(ves).isEqualByComparingTo("73.00");
    }

    private ParkingTicket activeTicket() {
        ParkingTicket t = new ParkingTicket();
        t.setId(1L);
        t.setCode("TKT-1");
        t.setStatus(ParkingTicket.Status.ACTIVE);
        return t;
    }

    /** Stub de ParkingService: confirma el pago sin tocar BD. */
    private static class FakeParkingService extends ParkingService {
        FakeParkingService() {
            super(null, null);
        }

        @Override
        public void confirmPaid(ParkingTicket ticket) {
            ticket.setStatus(ParkingTicket.Status.PAID);
        }
    }

    /** Implementación en memoria del contrato de persistencia de pagos. */
    private static class MemoryPaymentStore implements PaymentRecordStore {
        private final List<PaymentRecord> data = new ArrayList<>();
        private long nextId = 1;

        @Override
        public PaymentRecord save(PaymentRecord record) {
            record.setId(nextId++);
            record.setTimestamp(LocalDateTime.now());
            data.add(record);
            return record;
        }

        @Override
        public List<PaymentRecord> findAll() {
            return new ArrayList<>(data);
        }

        @Override
        public List<PaymentRecord> findByTimestampBetween(LocalDateTime from, LocalDateTime to) {
            return data.stream()
                    .filter(r -> !r.getTimestamp().isBefore(from) && !r.getTimestamp().isAfter(to))
                    .toList();
        }

        @Override
        public long countByStatus(PaymentRecord.Status status) {
            return data.stream().filter(r -> r.getStatus() == status).count();
        }
    }

    /** Implementación en memoria del contrato de persistencia de tasas. */
    private static class MemoryRateStore implements ExchangeRateStore {
        private final List<ExchangeRate> data = new ArrayList<>();
        private long nextId = 1;

        @Override
        public ExchangeRate save(ExchangeRate rate) {
            rate.setId(nextId++);
            rate.setUpdatedAt(LocalDateTime.now());
            data.add(rate);
            return rate;
        }

        @Override
        public List<ExchangeRate> findAll() {
            return new ArrayList<>(data);
        }

        @Override
        public Optional<ExchangeRate> findFirstByOrderByUpdatedAtDesc() {
            return data.stream().reduce((a, b) -> b);
        }
    }
}
