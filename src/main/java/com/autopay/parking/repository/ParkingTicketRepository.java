package com.autopay.parking.repository;

import com.autopay.parking.model.ParkingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA de tickets. Expone además el contrato
 * {@link ParkingTicketStore} usado por los servicios y las pruebas.
 */
@Repository
public interface ParkingTicketRepository
        extends JpaRepository<ParkingTicket, Long>, ParkingTicketStore {
}
