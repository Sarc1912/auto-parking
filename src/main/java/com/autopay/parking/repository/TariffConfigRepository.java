package com.autopay.parking.repository;

import com.autopay.parking.model.TariffConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA de tarifas. Expone además el contrato
 * {@link TariffConfigStore} usado por los servicios y las pruebas.
 */
@Repository
public interface TariffConfigRepository
        extends JpaRepository<TariffConfig, Long>, TariffConfigStore {
}
