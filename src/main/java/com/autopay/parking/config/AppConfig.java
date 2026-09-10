package com.autopay.parking.config;

import com.autopay.parking.ui.ViewNavigator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración principal de la aplicación.
 *
 * <p>Reservado para beans que no pertenecen claramente a una capa de servicio
 * o UI. La configuración de persistencia se realiza mediante
 * {@code application.properties}.</p>
 */
@Configuration
public class AppConfig {

    /** Navegador de las pantallas del kiosco. */
    @Bean
    public ViewNavigator viewNavigator(ConfigurableApplicationContext context) {
        return new ViewNavigator(context);
    }
}
