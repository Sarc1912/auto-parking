package com.autopay.parking.ui.controller;

import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.PaymentService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private HBox plateChip;
    @FXML
    private Label plateLabel;
    @FXML
    private Label entryTimeLabel;
    @FXML
    private Label entryDateLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label tariffLabel;
    @FXML
    private Label tariffRateLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label amountUsdLabel;
    @FXML
    private Button payButton;

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
            navigator.show(MainController.SCAN_SCREEN, null);
            return;
        }
        ticketCodeLabel.setText(current.ticket().getCode());

        String plate = current.ticket().getLicensePlate();
        boolean hasPlate = plate != null && !plate.isBlank();
        plateChip.setVisible(hasPlate);
        plateChip.setManaged(hasPlate);
        plateLabel.setText(hasPlate ? "Placa " + plate : "");

        LocalDateTime entry = current.ticket().getEntryTime();
        entryTimeLabel.setText(FormatUtil.time(entry));
        entryDateLabel.setText(FormatUtil.date(entry));
        durationLabel.setText(FormatUtil.duration(current.minutes()));
        tariffLabel.setText(current.tariff().getName());
        tariffRateLabel.setText(FormatUtil.ves(current.tariff().getRatePerHour()) + " por hora");
        amountLabel.setText(FormatUtil.ves(current.amount()));

        ExchangeRate rate = paymentService.latestRate().orElse(null);
        BigDecimal usd = paymentService.convert(current.amount(), rate,
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
        amountUsdLabel.setText("Equivale a " + FormatUtil.usd(usd)
                + (rate != null ? "  ·  Tasa BCV " + rate.getBcvRate() : ""));
        Platform.runLater(payButton::requestFocus);
    }

    @FXML
    private void onPayClicked() {
        if (current != null) {
            navigator.show(PAYMENT_SCREEN, current);
        }
    }

    @FXML
    private void onCancelClicked() {
        navigator.show(MainController.SCAN_SCREEN, null);
    }
}
