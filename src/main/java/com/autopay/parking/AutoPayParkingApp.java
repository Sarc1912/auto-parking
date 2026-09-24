package com.autopay.parking;

import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.ScaledRoot;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
/**
 * Punto de entrada de la aplicación.
 *
 * <p>JavaFX debe poseer el hilo principal, por lo que esta clase extiende
 * {@link Application} y arranca el contexto de Spring Boot dentro de
 * {@link #init()}.</p>
 */
@SpringBootApplication
public class AutoPayParkingApp extends Application {

    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        Application.launch(AutoPayParkingApp.class, args);
    }

    @Override
    public void init() {
        springContext = SpringApplication.run(AutoPayParkingApp.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main-view.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Region root = loader.load();
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        ScaledRoot scaledRoot = new ScaledRoot(root);
        springContext.getBean(ViewNavigator.class).portraitProperty()
                .bind(scaledRoot.portraitProperty());
        Scene scene = new Scene(scaledRoot, screen.getWidth(), screen.getHeight());
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Autopago Estacionamiento");
        primaryStage.setScene(scene);
        // Al salir de pantalla completa (menú de opciones o ESC) queda una ventana maximizada.
        primaryStage.setMaximized(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}
