package com.autopay.parking.repository;

import com.autopay.parking.model.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA de pagos. Expone además el contrato
 * {@link PaymentRecordStore} usado por los servicios y las pruebas.
 */
@Repository
public interface PaymentRecordRepository
        extends JpaRepository<PaymentRecord, Long>, PaymentRecordStore {
}
