package com.autopay.parking.ui.controller;

import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.ScannerService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Pantalla de escaneo. Captura la lectura del escáner USB en modo teclado y
 * valida el ticket antes de pasar a la confirmación.
 */
@KioskController
public class ScanScreenController implements AppScreen {

    private static final Duration ERROR_VISIBLE = Duration.seconds(8);

    private final ParkingService parkingService;
    private final ScannerService scannerService;
    private final ViewNavigator navigator;

    @FXML
    private VBox scanRoot;
    @FXML
    private HBox stepsBox;
    @FXML
    private Circle pulseRing;
    @FXML
    private StackPane scanIconStage;
    @FXML
    private HBox statusPill;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox errorBox;
    @FXML
    private Label errorTitleLabel;
    @FXML
    private Label errorMessageLabel;
    @FXML
    private TextField manualCodeField;
    @FXML
    private Button manualSearchButton;

    private Animation pulse;
    private final PauseTransition errorTimer = new PauseTransition(ERROR_VISIBLE);

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

        manualCodeField.setTextFormatter(new TextFormatter<String>(change -> {
            change.setText(change.getText().toUpperCase());
            return change.getControlNewText().length() <= 32 ? change : null;
        }));
        manualCodeField.textProperty().addListener((obs, o, n) -> {
            manualSearchButton.setDisable(n.isBlank());
            if (!n.equals(o)) {
                hideError();
            }
        });
        manualSearchButton.setDisable(true);
        errorTimer.setOnFinished(e -> hideError());

        startPulse();
        scannerService.attach(scanRoot, this::onScanned);
        Platform.runLater(manualCodeField::requestFocus);
    }

    @Override
    public void onHidden() {
        if (pulse != null) {
            pulse.stop();
        }
        errorTimer.stop();
        scannerService.detach();
    }

    private void startPulse() {
        pulseRing.radiusProperty().bind(scanIconStage.widthProperty().divide(2));
        ScaleTransition grow = new ScaleTransition(Duration.seconds(1.8), pulseRing);
        grow.setFromX(0.85);
        grow.setFromY(0.85);
        grow.setToX(1.35);
        grow.setToY(1.35);
        grow.setInterpolator(Interpolator.EASE_OUT);
        FadeTransition fade = new FadeTransition(Duration.seconds(1.8), pulseRing);
        fade.setFromValue(0.55);
        fade.setToValue(0);
        pulse = new ParallelTransition(grow, fade);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    private void onScanned(String code) {
        setStatus("Leyendo ticket…", "status-reading");
        validateAndNavigate(code.trim().toUpperCase());
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
            showError(e.getMessage(), code);
        } catch (RuntimeException e) {
            showError(null, code);
        }
    }

    private void showError(String errorKey, String code) {
        setStatus("Lector listo", "status-ready");
        errorTitleLabel.setText(titleFor(errorKey));
        errorMessageLabel.setText(messageFor(errorKey, code));
        boolean wasVisible = errorBox.isVisible();
        errorBox.setVisible(true);
        errorBox.setManaged(true);
        if (!wasVisible) {
            shake();
        }
        errorTimer.playFromStart();
        manualCodeField.requestFocus();
        manualCodeField.selectAll();
    }

    private void hideError() {
        errorTimer.stop();
        errorBox.setVisible(false);
        errorBox.setManaged(false);
    }

    private void shake() {
        TranslateTransition t = new TranslateTransition(Duration.millis(60), errorBox);
        t.setFromX(0);
        t.setByX(8);
        t.setCycleCount(4);
        t.setAutoReverse(true);
        t.play();
    }

    private void setStatus(String text, String styleClass) {
        statusLabel.setText(text);
        statusPill.getStyleClass().removeAll("status-ready", "status-reading");
        statusPill.getStyleClass().add(styleClass);
    }

    private static String titleFor(String errorKey) {
        return switch (errorKey == null ? "UNKNOWN" : errorKey) {
            case "TICKET_NOT_FOUND" -> "Ticket no encontrado";
            case "TICKET_ALREADY_PAID" -> "Este ticket ya fue pagado";
            case "NO_TARIFF" -> "Servicio no disponible";
            default -> "No se pudo leer el ticket";
        };
    }

    private static String messageFor(String errorKey, String code) {
        return switch (errorKey == null ? "UNKNOWN" : errorKey) {
            case "TICKET_NOT_FOUND" -> "No existe un ticket con el código " + code
                    + ". Verifique el código e intente de nuevo.";
            case "TICKET_ALREADY_PAID" -> "El ticket " + code
                    + " ya está pagado. Puede dirigirse a la salida.";
            case "NO_TARIFF" -> "No hay una tarifa activa. Solicite ayuda en la taquilla.";
            default -> "Intente escanear nuevamente o escriba el código manualmente.";
        };
    }
}
