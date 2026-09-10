package com.autopay.parking.repository;

import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.model.TariffConfig;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para tarifas, independiente del motor.
 *
 * <p>La implementación concreta la aporta {@link TariffConfigRepository}
 * (Spring Data JPA) en producción, y una implementación en memoria en las
 * pruebas unitarias.</p>
 */
public interface TariffConfigStore {

    TariffConfig save(TariffConfig tariff);

    List<TariffConfig> findAll();

    Optional<TariffConfig> findFirstByActiveTrue();

    void deleteById(Long id);
}
