package com.autopay.parking.ui;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

/**
 * Cambia la pantalla actual dentro del área de contenido de la ventana
 * principal ({@code main-view}). Cada pantalla es un archivo FXML con su
 * controlador gestionado por Spring.
 */
public class ViewNavigator {

    private static final Duration FADE = Duration.millis(240);

    private final ConfigurableApplicationContext springContext;
    private StackPane contentRoot;

    public ViewNavigator(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    /**
     * Carga el FXML indicado (ruta de clase) dentro del área de contenido y
     * le entrega el contexto opcional al controlador.
     */
    public void show(String fxmlPath, Object screenContext) {
        if (contentRoot == null) {
            throw new IllegalStateException("No se ha establecido el área de contenido.");
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AppScreen screen) {
                screen.onShown(screenContext);
            }
            contentRoot.getChildren().setAll(view);
            view.requestFocus();
            fadeIn(view);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la pantalla: " + fxmlPath, e);
        }
    }

    private static void fadeIn(Parent view) {
        FadeTransition fade = new FadeTransition(FADE, view);
        fade.setFromValue(0.4);
        fade.setToValue(1.0);
        fade.play();
    }
}