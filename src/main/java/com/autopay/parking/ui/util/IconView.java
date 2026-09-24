package com.autopay.parking.ui.util;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

/**
 * Icono vectorial de tamaño fijo, utilizable desde código y desde FXML:
 * {@code <IconView icon="SEARCH" size="20"/>}.
 *
 * <p>El color se controla por CSS con la clase {@code .icon-svg}.</p>
 */
public class IconView extends StackPane {

    private static final double BASE = 24.0;

    private final SVGPath path = new SVGPath();
    private final Scale scale = new Scale(1, 1, 0, 0);
    private double size = 24;
    private String iconName;

    public IconView() {
        getStyleClass().add("icon-view");
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        path.getStyleClass().add("icon-svg");

        // La caja transparente de 24 px fija el área del icono para que todos
        // los iconos queden alineados con el mismo tamaño, sin importar su forma.
        Rectangle box = new Rectangle(BASE, BASE, Color.TRANSPARENT);
        Group glyph = new Group(box, path);
        glyph.getTransforms().add(scale);
        getChildren().add(new Group(glyph));
        setSize(size);
    }

    public IconView(String pathData, double size) {
        this();
        path.setContent(pathData);
        setSize(size);
    }

    public void setIcon(String name) {
        this.iconName = name;
        path.setContent(Icons.byName(name));
    }

    public String getIcon() {
        return iconName;
    }

    public void setSize(double size) {
        this.size = size;
        scale.setX(size / BASE);
        scale.setY(size / BASE);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
    }

    public double getSize() {
        return size;
    }
}
