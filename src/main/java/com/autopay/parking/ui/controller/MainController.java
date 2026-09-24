package com.autopay.parking.ui.controller;

import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.Icons;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.geometry.Side;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador de la ventana principal del kiosco: encabezado con reloj, área
 * de contenido, aviso de inactividad y acceso a la administración web (se abre
 * en el navegador).
 */
@KioskController
public class MainController implements Initializable {

    public static final String SCAN_SCREEN = "/fxml/scan-screen.fxml";

    private static final String ADMIN_URL = "http://localhost:8080/admin/index.html";

    private static final Locale ES = Locale.forLanguageTag("es-VE");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm", ES);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ES);

    /** Tiempo sin interacción antes de preguntar si el usuario sigue presente. */
    private static final Duration IDLE_TIMEOUT = Duration.seconds(75);
    /** Segundos que se espera una respuesta antes de volver al inicio. */
    private static final int IDLE_GRACE_SECONDS = 15;

    private final ViewNavigator navigator;

    @FXML
    private StackPane contentArea;
    @FXML
    private Label clockLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private StackPane idleOverlay;
    @FXML
    private Label idleMessageLabel;
    @FXML
    private Button idleContinueButton;
    @FXML
    private Button settingsButton;

    private final PauseTransition idleTimer = new PauseTransition(IDLE_TIMEOUT);
    private Timeline graceCountdown;
    private int graceRemaining;

    public MainController(ViewNavigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startClock();
        setupIdleWatch();
        navigator.setContentRoot(contentArea);
        navigator.show(SCAN_SCREEN, null);
    }

    private void startClock() {
        updateClock();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        clockLabel.setText(now.format(TIME) + (now.getHour() < 12 ? " AM" : " PM"));
        String date = now.format(DATE);
        dateLabel.setText(Character.toUpperCase(date.charAt(0)) + date.substring(1));
    }

    // ---------------------------------------------------------------
    // Inactividad: el kiosco nunca debe quedar con datos de otra persona
    // ---------------------------------------------------------------

    private void setupIdleWatch() {
        idleTimer.setOnFinished(e -> onIdle());
        contentArea.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onUserActivity);
        contentArea.addEventFilter(KeyEvent.KEY_PRESSED, this::onUserActivity);
        contentArea.addEventFilter(TouchEvent.TOUCH_PRESSED, this::onUserActivity);
        contentArea.addEventFilter(ScrollEvent.SCROLL, this::onUserActivity);
        idleTimer.play();
    }

    private void onUserActivity(Event event) {
        if (!idleOverlay.isVisible()) {
            idleTimer.playFromStart();
        }
    }

    private void onIdle() {
        String path = navigator.currentPath();
        // En el escaneo no hay datos que proteger; el comprobante tiene su propio cierre.
        if (path == null || SCAN_SCREEN.equals(path)
                || PaymentScreenController.TICKET_SCREEN.equals(path)) {
            idleTimer.playFromStart();
            return;
        }
        showIdleOverlay();
    }

    private void showIdleOverlay() {
        graceRemaining = IDLE_GRACE_SECONDS;
        updateIdleMessage();
        idleOverlay.setOpacity(0);
        idleOverlay.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(200), idleOverlay);
        fade.setToValue(1);
        fade.play();
        idleContinueButton.requestFocus();

        graceCountdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            graceRemaining--;
            if (graceRemaining <= 0) {
                onIdleRestart();
            } else {
                updateIdleMessage();
            }
        }));
        graceCountdown.setCycleCount(IDLE_GRACE_SECONDS);
        graceCountdown.play();
    }

    private void updateIdleMessage() {
        idleMessageLabel.setText("Por su seguridad, la operación se cancelará en "
                + graceRemaining + " segundos si no hay actividad.");
    }

    private void hideIdleOverlay() {
        if (graceCountdown != null) {
            graceCountdown.stop();
            graceCountdown = null;
        }
        idleOverlay.setVisible(false);
        idleTimer.playFromStart();
    }

    @FXML
    private void onIdleContinue() {
        hideIdleOverlay();
    }

    @FXML
    private void onIdleRestart() {
        hideIdleOverlay();
        navigator.show(SCAN_SCREEN, null);
    }

    // ---------------------------------------------------------------
    // Menú de opciones (engranaje)
    // ---------------------------------------------------------------

    @FXML
    private void onSettingsClicked() {
        Stage stage = (Stage) settingsButton.getScene().getWindow();

        CheckMenuItem fullScreen = new CheckMenuItem("Pantalla completa", Icons.icon(Icons.FULLSCREEN, 20));
        fullScreen.setSelected(stage.isFullScreen());
        fullScreen.setOnAction(e -> stage.setFullScreen(fullScreen.isSelected()));

        MenuItem admin = new MenuItem("Administración web", Icons.icon(Icons.OPEN_IN_NEW, 20));
        admin.setOnAction(e -> openAdmin());

        ContextMenu menu = new ContextMenu(fullScreen, new SeparatorMenuItem(), admin);
        menu.getStyleClass().add("options-menu");
        menu.show(settingsButton, Side.BOTTOM, 0, 6);
    }

    private void openAdmin() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(ADMIN_URL));
                return;
            }
        } catch (Exception ignored) {
            // sin navegador o sin entorno gráfico: se muestra la URL.
        }
        showUrlDialog();
    }

    private void showUrlDialog() {
        TextArea area = new TextArea(ADMIN_URL);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(360, 70);
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "", ButtonType.OK);
        alert.setTitle("Administración web");
        alert.setHeaderText("Abra esta dirección en el navegador:");
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }
}
