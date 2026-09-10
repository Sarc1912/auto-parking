package com.autopay.parking.ui.util;

import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Scale;

/**
 * Iconos vectoriales (familia consistente de trazo/fill) para el kiosco.
 *
 * <p>Los path-data corresponden a la familia Material Design (Apache 2.0),
 * renderizados como {@link SVGPath} con escala alrededor de su centro para
 * que la alineación del nodo se mantenga intacta.</p>
 */
public final class Icons {

    private static final double BASE = 24.0;

    private Icons() {
    }

    /** Recibo / ticket (pantalla de escaneo). */
    public static final String TICKET =
            "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22"
                    + "l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2"
                    + "l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z";

    /** Teléfono (Pago Móvil). */
    public static final String SMARTPHONE =
            "M17 1.01L7 1c-1.1 0-2 .9-2 2v18c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2V3c0-1.1-.9-1.99-2-1.99z"
                    + "M17 19H7V5h10v14z";

    /** Tarjeta (Punto de Venta). */
    public static final String CREDIT_CARD =
            "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2z"
                    + "m0 14H4v-6h16v6zm0-10H4V6h16v2z";

    /** Transferencia (flechas de intercambio). */
    public static final String TRANSFER =
            "M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z";

    /** Símbolo de moneda (Efectivo Bs). */
    public static final String CURRENCY =
            "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21"
                    + "c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46"
                    + "4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2"
                    + "c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";

    /** Billete con chip (Efectivo USD). */
    public static final String BANKNOTE =
            "M19 14V6c0-1.1-.9-2-2-2H3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2z"
                    + "m-9-1c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm13-6v11c0 1.1-.9 2-2 2H4v-2h17V7h2z";

    /** Confirmación exitosa. */
    public static final String CHECK_CIRCLE =
            "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"
                    + "m-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z";

    /** Configuración (engranaje). */
    public static final String SETTINGS =
            "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61"
                    + "l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54"
                    + "c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94"
                    + "l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63"
                    + "-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96"
                    + "c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24"
                    + "1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58z"
                    + "M12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z";

    /** Gráfica de barras (reportes). */
    public static final String BAR_CHART =
            "M5 9.2h3V19H5V9.2zM10.6 5h2.8v14h-2.8V5zm5.6 8H19v6h-2.8v-6z";

    /** Acción táctil (volver al kiosco). */
    public static final String TOUCH_APP =
            "M9 11.24V7.5C9 6.12 10.12 5 11.5 5S14 6.12 14 7.5v3.74c1.21-.81 2-2.18 2-3.74"
                    + "C16 5.01 13.99 3 11.5 3S7 5.01 7 7.5c0 1.56.79 2.93 2 3.74zm9.84 4.63l-4.54-2.26"
                    + "c-.17-.07-.35-.11-.54-.11H13v-6c0-.83-.67-1.5-1.5-1.5S10 6.67 10 7.5v10.74c-3.6-.76-3.54-.75"
                    + "-3.67-.75-.31 0-.59.13-.79.33l-.79.8 4.94 4.94c.27.27.65.44 1.06.44h6.79c.75 0 1.33-.55"
                    + "1.44-1.28l.75-5.27c.01-.07.02-.14.02-.2 0-.62-.38-1.16-.91-1.38z";

    /** Panel general (resumen del admin). */
    public static final String DASHBOARD =
            "M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z";

    /** Cerrar sesión. */
    public static final String LOGOUT =
            "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 "
                    + ".9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z";

    /** Candado (login). */
    public static final String LOCK =
            "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12"
                    + "c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2"
                    + "2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z";

    /**
     * Crea un icono SVG escalado desde una caja de 24 px alrededor de su
     * centro, de modo que el escalado no desalinea el nodo dentro de su
     * contenedor.
     */
    public static SVGPath icon(String pathData) {
        return icon(pathData, 1.6);
    }

    public static SVGPath icon(String pathData, double displaySize) {
        SVGPath p = new SVGPath();
        p.setContent(pathData);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.getStyleClass().add("icon-svg");
        double scale = displaySize / BASE;
        p.getTransforms().add(new Scale(scale, scale, BASE / 2.0, BASE / 2.0));
        return p;
    }
}