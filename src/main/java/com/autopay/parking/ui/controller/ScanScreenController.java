package com.autopay.parking.ui.controller;

import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.ScannerService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.Icons;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Pantalla de escaneo. Captura la lectura del escáner USB en modo teclado y
 * valida el ticket antes de pasar a la confirmación.
 */
@KioskController
public class ScanScreenController implements AppScreen {

    private static final String MSG_WAITING = "LISTO — espere la lectura";
    private static final String MSG_READING = "LEYENDO TICKET…";

    private final ParkingService parkingService;
    private final ScannerService scannerService;
    private final ViewNavigator navigator;

    @FXML
    private VBox scanRoot;
    @FXML
    private HBox stepsBox;
    @FXML
    private StackPane scanIconBox;
    @FXML
    private Label statusLabel;
    @FXML
    private Label detailsLabel;
    @FXML
    private TextField manualCodeField;
    @FXML
    private Button manualSearchButton;

    public ScanScreenController(ParkingService parkingService,
                                ScannerService scannerService,
                                ViewNavigator navigator) {
        this.parkingService = parkingService;
        this.scannerService = scannerService;
        this.navigator = navigator;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(1));
        scanIconBox.getChildren().add(Icons.icon(Icons.TICKET));
        resetState();
        scannerService.attach(scanRoot, this::onScanned);
        manualCodeField.requestFocus();
    }

    private void resetState() {
        statusLabel.setText(MSG_WAITING);
        detailsLabel.setText("");
        manualCodeField.setText("");
        manualSearchButton.setDisable(false);
    }

    private void onScanned(String code) {
        resetState();
        statusLabel.setText(MSG_READING);
        validateAndNavigate(code);
    }

    @FXML
    private void onManualSearch() {
        String code = manualCodeField.getText().trim();
        if (!code.isEmpty()) {
            validateAndNavigate(code);
        }
    }

    private void validateAndNavigate(String code) {
        try {
            ParkingService.TicketInfo info = parkingService.validateTicket(code);
            navigator.show(ConfirmScreenController.SCREEN, info);
        } catch (IllegalStateException e) {
            scannerService.detach();
            showError(e.getMessage(), code);
            scheduleReset();
        } catch (RuntimeException e) {
            scannerService.detach();
            showError(null, code);
            scheduleReset();
        }
    }

    private void showError(String errorKey, String code) {
        statusLabel.setText("Ticket no encontrado");
        detailsLabel.setText(resolveMessage(errorKey, code));
    }

    private String resolveMessage(String errorKey, String code) {
        return switch (errorKey == null ? "UNKNOWN" : errorKey) {
            case "TICKET_NOT_FOUND" -> "No existe un ticket con el código " + code + ".";
            case "TICKET_ALREADY_PAID" -> "El ticket " + code + " ya fue pagado.";
            case "NO_TARIFF" -> "No hay una tarifa activa configurada.";
            default -> "No se pudo procesar la lectura. Intente de nuevo.";
        };
    }

    private void scheduleReset() {
        PauseTransition wait = new PauseTransition(Duration.seconds(3));
        wait.setOnFinished(e -> {
            resetState();
            scannerService.attach(scanRoot, this::onScanned);
            manualCodeField.requestFocus();
        });
        wait.play();
    }
}