package com.autopay.parking.repository;

import com.autopay.parking.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a los usuarios del panel de administración.
 */
public interface AppUserStore extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}