package com.autopay.parking.ui.controller;

import com.autopay.parking.ui.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador de la ventana principal del kiosco: encabezado, área de
 * contenido y acceso a la administración web (se abre en el navegador).
 */
@KioskController
public class MainController implements Initializable {

    public static final String SCAN_SCREEN = "/fxml/scan-screen.fxml";

    private static final String ADMIN_URL = "http://localhost:8080/admin/index.html";

    private final ViewNavigator navigator;

    @FXML
    private StackPane contentArea;

    public MainController(ViewNavigator navigator) {
        this.navigator = navigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        navigator.setContentRoot(contentArea);
        navigator.show(SCAN_SCREEN, null);
    }

    @FXML
    private void onAdminClicked() {
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