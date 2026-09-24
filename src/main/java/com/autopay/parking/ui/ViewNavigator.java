package com.autopay.parking.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static final Duration TRANSITION = Duration.millis(260);

    private final ConfigurableApplicationContext springContext;
    private StackPane contentRoot;
    private AppScreen currentScreen;
    private String currentPath;
    private final BooleanProperty portrait = new SimpleBooleanProperty(false);

    public ViewNavigator(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    /** Orientación de la pantalla; las vistas la usan para reorganizar su contenido. */
    public BooleanProperty portraitProperty() {
        return portrait;
    }

    /** Ruta FXML de la pantalla visible, o {@code null} si aún no hay ninguna. */
    public String currentPath() {
        return currentPath;
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

            if (currentScreen != null) {
                currentScreen.onHidden();
                currentScreen = null;
            }
            contentRoot.getChildren().setAll(view);
            currentPath = fxmlPath;
            view.requestFocus();
            playEnter(view);

            // Se invoca con la vista ya en escena para que pueda enfocar sus controles.
            if (loader.getController() instanceof AppScreen screen) {
                currentScreen = screen;
                screen.onShown(screenContext);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la pantalla: " + fxmlPath, e);
        }
    }

    private static void playEnter(Parent view) {
        FadeTransition fade = new FadeTransition(TRANSITION, view);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        TranslateTransition slide = new TranslateTransition(TRANSITION, view);
        slide.setFromY(14);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }
}
