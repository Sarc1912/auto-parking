package com.autopay.parking.ui.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Indicador de progreso del flujo del kiosco (1 Ticket, 2 Confirmar,
 * 3 Pagar, 4 Comprobante). Cada pantalla resalta su paso con chips grandes,
 * pensados para pantalla táctil.
 */
public final class StepIndicator {

    private static final String[] LABELS = {"Ticket", "Confirmar", "Pagar", "Comprobante"};

    private StepIndicator() {
    }

    public static HBox of(int current) {
        HBox bar = new HBox(0);
        bar.getStyleClass().add("steps");
        bar.setAlignment(Pos.TOP_CENTER);
        for (int i = 1; i <= LABELS.length; i++) {
            if (i > 1) {
                Region line = new Region();
                line.getStyleClass().addAll("step-connector",
                        i <= current ? "step-connector-done" : "step-connector-upcoming");
                bar.getChildren().add(line);
            }
            bar.getChildren().add(step(i, current));
        }
        return bar;
    }

    private static VBox step(int position, int current) {
        VBox step = new VBox(6);
        step.setAlignment(Pos.CENTER);
        step.getStyleClass().addAll("step", styleFor(position, current));

        StackPane circle = new StackPane();
        circle.getStyleClass().add("step-circle");
        if (position < current) {
            circle.getChildren().add(Icons.icon(Icons.CHECK, 22));
        } else {
            circle.getChildren().add(new Label(String.valueOf(position)));
        }

        Label caption = new Label(LABELS[position - 1]);
        caption.getStyleClass().add("step-label");

        step.getChildren().addAll(circle, caption);
        return step;
    }

    private static String styleFor(int position, int current) {
        if (position < current) {
            return "step-done";
        }
        return position == current ? "step-active" : "step-upcoming";
    }
}
