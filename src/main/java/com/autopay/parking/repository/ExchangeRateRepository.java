package com.autopay.parking.repository;

import com.autopay.parking.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA de tasas de cambio. Expone además el contrato
 * {@link ExchangeRateStore} usado por los servicios y las pruebas.
 */
@Repository
public interface ExchangeRateRepository
        extends JpaRepository<ExchangeRate, Long>, ExchangeRateStore {
}
