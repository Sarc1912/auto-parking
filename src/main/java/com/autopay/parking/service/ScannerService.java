package com.autopay.parking.service;

import com.autopay.parking.ui.util.ScannerHandler;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Frontera entre la UI y el hardware de escaneo.
 *
 * <p>El escáner USB en modo teclado ("pistola") envía los caracteres como si
 * fueran pulsaciones, terminando con un Enter. {@link ScannerHandler} captura
 * ese flujo y firma el evento cuando detecta el fin de lectura.</p>
 */
@Service
public class ScannerService {

    private final ScannerHandler handler = new ScannerHandler();
    private final EventHandler<KeyEvent> keyHandler = this::onKeyPressed;

    private Consumer<String> onScanConsumer;
    private Node attachedNode;

    /**
     * Vincula el escaneo a un nodo (pantalla) y define el callback al leer.
     */
    public void attach(Node focusedNode, Consumer<String> onScan) {
        detach();
        this.attachedNode = focusedNode;
        this.onScanConsumer = onScan;
        focusedNode.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
    }

    public void detach() {
        if (attachedNode != null) {
            attachedNode.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
            attachedNode = null;
        }
        onScanConsumer = null;
    }

    public boolean isAttached() {
        return attachedNode != null;
    }

    private void onKeyPressed(KeyEvent event) {
        handler.onKeyPressed(event, this::fireScan);
    }

    private void fireScan(String code) {
        Consumer<String> c = onScanConsumer;
        if (c != null && code != null) {
            c.accept(code);
        }
    }
}