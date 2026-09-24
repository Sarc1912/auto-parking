package com.autopay.parking.ui;

/**
 * Contrato para las pantallas del kiosco.
 *
 * <p>Cada pantalla recibe un contexto opcional al momento de ser mostrada.
 * Los controladores deben re-inicializar su estado aquí, ya que el bean puede
 * reutilizarse entre navegaciones.</p>
 */
public interface AppScreen {

    /** Invocado cuando la pantalla pasa a ser visible. */
    void onShown(Object context);

    /** Invocado antes de reemplazar la pantalla: detener temporizadores y animaciones. */
    default void onHidden() {
    }
}
