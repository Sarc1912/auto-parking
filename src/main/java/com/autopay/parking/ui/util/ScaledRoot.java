package com.autopay.parking.ui.util;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/**
 * Contenedor raíz que escala toda la interfaz para ocupar la pantalla
 * completa, sin importar su resolución (laptop, monitor o tótem).
 *
 * <p>La interfaz se diseña sobre un lienzo lógico: 1280x820 en horizontal y
 * 960x1500 en vertical. El lienzo se estira en el eje sobrante para que no
 * queden franjas vacías, y el contenido recibe la pseudo-clase
 * {@code :portrait} cuando la pantalla es más alta que ancha.</p>
 */
public class ScaledRoot extends Pane {

    private static final double LANDSCAPE_W = 1280;
    private static final double LANDSCAPE_H = 820;
    private static final double PORTRAIT_W = 960;
    private static final double PORTRAIT_H = 1500;

    private static final PseudoClass PORTRAIT = PseudoClass.getPseudoClass("portrait");

    private final Region content;
    private final Scale scale = new Scale(1, 1, 0, 0);
    private final ReadOnlyBooleanWrapper portrait = new ReadOnlyBooleanWrapper(false);

    public ScaledRoot(Region content) {
        this.content = content;
        content.getTransforms().add(scale);
        getChildren().add(content);
        getStyleClass().add("scaled-root");
    }

    /** Verdadero cuando la pantalla es vertical (tótem). */
    public ReadOnlyBooleanProperty portraitProperty() {
        return portrait.getReadOnlyProperty();
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        boolean isPortrait = h > w;
        double s = isPortrait
                ? Math.min(w / PORTRAIT_W, h / PORTRAIT_H)
                : Math.min(w / LANDSCAPE_W, h / LANDSCAPE_H);
        scale.setX(s);
        scale.setY(s);
        portrait.set(isPortrait);
        content.pseudoClassStateChanged(PORTRAIT, isPortrait);
        content.resizeRelocate(0, 0, w / s, h / s);
    }
}
