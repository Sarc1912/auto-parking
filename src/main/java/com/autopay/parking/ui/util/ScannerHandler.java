package com.autopay.parking.ui.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Consumer;

/**
 * Detección de lecturas de un escáner USB en modo teclado.
 *
 * <p>Un escáner "pistola" emite pulsaciones casi instantáneas (menos de
 * {@link #MAX_SCAN_INTERVAL_MS} entre caracteres) y cierra la lectura con
 * Enter. El buffer se vacía si el intervalo entre teclas es demasiado largo,
 * lo que distingue la lectura del tecleo humano.</p>
 */
public class ScannerHandler {

    static final long MAX_SCAN_INTERVAL_MS = 80;
    private static final int MAX_SCAN_LENGTH = 64;

    private final StringBuilder buffer = new StringBuilder();
    private long lastKeyTime = 0;
    private boolean capturing = false;

    /**
     * Procesa una pulsación y, cuando detecta el Enter final de una lectura,
     * invoca {@code onScan} con el código capturado.
     */
    public void onKeyPressed(KeyEvent event, Consumer<String> onScan) {
        long now = System.currentTimeMillis();

        if (event.getCode() == KeyCode.ENTER) {
            if (capturing) {
                String scanned = buffer.toString();
                reset();
                if (!scanned.isEmpty()) {
                    onScan.accept(scanned);
                }
            }
            event.consume();
            return;
        }

        if (event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
            return;
        }

        if (!capturing) {
            capturing = true;
            buffer.setLength(0);
        } else if (now - lastKeyTime > MAX_SCAN_INTERVAL_MS) {
            // Pausa demasiado larga: no es lectura de escáner, se descarta.
            reset();
            capturing = true;
            buffer.setLength(0);
        }

        if (buffer.length() < MAX_SCAN_LENGTH) {
            if (event.getText() != null && !event.getText().isEmpty()) {
                buffer.append(event.getText());
            } else if (isPrintableKey(event.getCode())) {
                buffer.append(printableFor(event.getCode()));
            }
        }
        lastKeyTime = now;
        event.consume();
    }

    private void reset() {
        buffer.setLength(0);
        capturing = false;
        lastKeyTime = 0;
    }

    private boolean isPrintableKey(KeyCode keyCode) {
        return (keyCode.isLetterKey() || keyCode.isDigitKey()
                || switch (keyCode) {
                    case MINUS, EQUALS, BACK_SPACE -> true;
                    default -> false;
                });
    }

    private String printableFor(KeyCode keyCode) {
        if (keyCode.isDigitKey()) {
            return keyCode.getChar();
        }
        return switch (keyCode) {
            case MINUS -> "-";
            case EQUALS -> "=";
            default -> keyCode.getChar().toLowerCase();
        };
    }
}