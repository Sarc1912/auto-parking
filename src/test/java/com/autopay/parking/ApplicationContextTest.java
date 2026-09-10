package com.autopay.parking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que el contexto de Spring arranca con la persistencia embebida
 * (H2 + JPA) y los datos iniciales de {@code data.sql}.
 */
@SpringBootTest
class ApplicationContextTest {

    @Test
    void contextLoads() {
        // El arranque del contexto ya valida la configuración.
    }
}