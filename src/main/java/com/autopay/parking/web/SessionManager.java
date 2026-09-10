package com.autopay.parking.web;

import com.autopay.parking.model.AppUser;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones del panel web de administración. Guarda el token emitido al iniciar
 * sesión junto al usuario autenticado (en memoria, suficiente para la etapa
 * de demostración y un kiosco offline).
 */
@Service
public class SessionManager {

    private final Map<String, AppUser> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String open(AppUser user) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, user);
        return token;
    }

    public Optional<AppUser> user(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public void close(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}