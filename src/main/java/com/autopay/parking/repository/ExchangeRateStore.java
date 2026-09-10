package com.autopay.parking.repository;

import com.autopay.parking.model.ExchangeRate;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para la tasa de cambio, independiente del motor.
 *
 * <p>La implementación concreta la aporta {@link ExchangeRateRepository}
 * (Spring Data JPA) en producción, y una implementación en memoria en las
 * pruebas unitarias.</p>
 */
public interface ExchangeRateStore {

    ExchangeRate save(ExchangeRate rate);

    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findFirstByOrderByUpdatedAtDesc();
}
