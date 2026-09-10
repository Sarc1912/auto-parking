package com.autopay.parking.ui.controller;

import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.PaymentService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

/**
 * Pantalla de confirmación: muestra el ticket escaneado, la permanencia y el
 * monto a pagar antes de decidir el método de pago.
 */
@KioskController
public class ConfirmScreenController implements AppScreen {

    public static final String SCREEN = "/fxml/confirm-screen.fxml";
    public static final String PAYMENT_SCREEN = "/fxml/payment-screen.fxml";

    private final ViewNavigator navigator;
    private final PaymentService paymentService;

    @FXML
    private HBox stepsBox;
    @FXML
    private Label ticketCodeLabel;
    @FXML
    private Label entryTimeLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label tariffLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label amountUsdLabel;

    private ParkingService.TicketInfo current;

    public ConfirmScreenController(ViewNavigator navigator, PaymentService paymentService) {
        this.navigator = navigator;
        this.paymentService = paymentService;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(2));
        current = (ParkingService.TicketInfo) context;
        if (current == null) {
            navigator.show(SCAN_SCREEN_PATH(), null);
            return;
        }
        ticketCodeLabel.setText(current.ticket().getCode());
        entryTimeLabel.setText(current.ticket().getEntryTime().toString().replace("T", " "));
        durationLabel.setText(FormatUtil.duration(current.minutes()));
        tariffLabel.setText(current.tariff().getName() + " · "
                + current.tariff().getRatePerHour() + " Bs/h");
        amountLabel.setText(FormatUtil.ves(current.amount()));

        BigDecimal usd = paymentService.convert(current.amount(),
                paymentService.latestRate().orElse(null),
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
        amountUsdLabel.setText("≈ " + FormatUtil.usd(usd));
    }

    @FXML
    private void onPayClicked() {
        if (current != null) {
            navigator.show(PAYMENT_SCREEN, current);
        }
    }

    @FXML
    private void onCancelClicked() {
        navigator.show(SCAN_SCREEN_PATH(), null);
    }

    private static String SCAN_SCREEN_PATH() {
        return MainController.SCAN_SCREEN;
    }
}