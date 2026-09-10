package com.autopay.parking.repository;

import com.autopay.parking.model.PaymentRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato de persistencia para pagos, independiente del motor.
 *
 * <p>La implementación concreta la aporta {@link PaymentRecordRepository}
 * (Spring Data JPA) en producción, y una implementación en memoria en las
 * pruebas unitarias, lo que mantiene la capa de datos desacoplada.</p>
 */
public interface PaymentRecordStore {

    PaymentRecord save(PaymentRecord record);

    List<PaymentRecord> findAll();

    List<PaymentRecord> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    long countByStatus(PaymentRecord.Status status);
}
