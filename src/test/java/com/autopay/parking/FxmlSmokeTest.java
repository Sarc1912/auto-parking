package com.autopay.parking;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FxmlSmokeTest {

    @Autowired
    ApplicationContext ctx;

    @BeforeAll
    static void initFx() throws Exception {
        Platform.startup(() -> { });
    }

    @AfterAll
    static void stopFx() {
        Platform.exit();
    }

    @Test
    @Timeout(30)
    void todasLasPantallasCargan() throws Exception {
        List<String> screens = List.of(
                "/fxml/main-view.fxml",
                "/fxml/scan-screen.fxml",
                "/fxml/confirm-screen.fxml",
                "/fxml/payment-screen.fxml",
                "/fxml/ticket-screen.fxml");

        for (String path : screens) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setControllerFactory(ctx::getBean);
            Object root = loader.load();
            assertNotNull(root, "No se pudo cargar " + path);
            Object controller = loader.getController();
            assertNotNull(controller, "Sin controlador " + path);
            System.out.println("OK " + path + " -> " + controller.getClass().getSimpleName());
        }
    }
}