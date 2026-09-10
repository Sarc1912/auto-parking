package com.autopay.parking.service;

import com.autopay.parking.model.AppUser;
import com.autopay.parking.repository.AppUserStore;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Optional;

/**
 * Autenticación del panel de administración.
 *
 * <p>Las contraseñas se verifican contra un hash PBKDF2 con sal individual.
 * Al arrancar, si no existe ningún usuario, se crea la cuenta por defecto
 * {@code admin / admin123} (solo para la etapa de demostración).</p>
 */
@Service
public class AuthService {

    public static final String DEFAULT_ADMIN_USER = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private static final int ITERATIONS = 100_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserStore userStore;

    public AuthService(AppUserStore userStore) {
        this.userStore = userStore;
    }

    @PostConstruct
    void ensureDefaultAdmin() {
        if (userStore.count() > 0) {
            return;
        }
        String salt = newSalt();
        String hash = hash(DEFAULT_ADMIN_PASSWORD, salt);
        userStore.save(new AppUser(DEFAULT_ADMIN_USER, hash, salt,
                "Administrador del estacionamiento", "ADMIN"));
    }

    /**
     * Verifica usuario y contraseña contra la base de datos.
     *
     * @return el usuario autenticado o vacío si las credenciales no coinciden.
     */
    public Optional<AppUser> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        return userStore.findByUsername(username.trim())
                .filter(user -> verify(password, user.getSalt(), user.getPasswordHash()));
    }

    /** Genera el hash PBKDF2-HMAC-SHA256 de una contraseña con su sal. */
    public static String hash(String rawPassword, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(
                    rawPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] derived = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(derived);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo derivar la contraseña", e);
        }
    }

    public static boolean verify(String rawPassword, String salt, String expectedHash) {
        String candidate = hash(rawPassword, salt);
        return MessageDigest.isEqual(
                candidate.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String newSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}