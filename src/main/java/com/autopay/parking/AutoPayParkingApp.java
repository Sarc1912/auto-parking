package com.autopay.parking;

import com.autopay.parking.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("Autopago Estacionamiento");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }
}
