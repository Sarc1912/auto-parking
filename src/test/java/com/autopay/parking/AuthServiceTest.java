package com.autopay.parking;

import com.autopay.parking.repository.AppUserStore;
import com.autopay.parking.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private AppUserStore userStore;

    @Test
    void credencialesPorDefectoAutentican() {
        assertTrue(authService.authenticate("admin", "admin123").isPresent());
    }

    @Test
    void contrasenaIncorrectaFalla() {
        assertFalse(authService.authenticate("admin", "incorrecta").isPresent());
    }

    @Test
    void usuarioInexistenteFalla() {
        assertFalse(authService.authenticate("nadie", "admin123").isPresent());
    }

    @Test
    void adminExisteEnBaseDeDatos() {
        assertTrue(userStore.findByUsername("admin").isPresent());
    }

    @Test
    void hashDeterministaParaMismaSal() {
        String salt = "sal-de-prueba";
        assertEquals(AuthService.hash("clave", salt), AuthService.hash("clave", salt));
    }
}