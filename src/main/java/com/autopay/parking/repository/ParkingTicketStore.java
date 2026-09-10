package com.autopay.parking.repository;

import com.autopay.parking.model.ParkingTicket;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia para tickets, independiente del motor.
 *
 * <p>La implementación concreta la aporta {@link ParkingTicketRepository}
 * (Spring Data JPA) en producción, y una implementación en memoria en las
 * pruebas unitarias.</p>
 */
public interface ParkingTicketStore {

    ParkingTicket save(ParkingTicket ticket);

    Optional<ParkingTicket> findByCode(String code);

    List<ParkingTicket> findAll();

    List<ParkingTicket> findByStatus(ParkingTicket.Status status);

    long countByStatus(ParkingTicket.Status status);
}
