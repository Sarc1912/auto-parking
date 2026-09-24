package com.autopay.parking.ui.controller;

import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.QRCodeService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Pantalla de ticket pagado: muestra el comprobante en pantalla y el código QR
 * para que el usuario lo capture con su teléfono (transferencia offline).
 * Vuelve sola al inicio para dejar el kiosco listo para el siguiente cliente.
 */
@KioskController
public class TicketScreenController implements AppScreen {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int AUTO_RETURN_SECONDS = 30;

    private final ViewNavigator navigator;
    private final QRCodeService qrCodeService;

    @FXML
    private HBox stepsBox;
    @FXML
    private StackPane successIcon;
    @FXML
    private Label ticketCodeLabel;
    @FXML
    private Label paidAtLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label methodLabel;
    @FXML
    private ImageView qrImageView;
    @FXML
    private Label qrHintLabel;
    @FXML
    private Button finishButton;
    @FXML
    private Label countdownLabel;
    @FXML
    private HBox receiptCard;
    @FXML
    private VBox receiptColumn;
    @FXML
    private VBox qrPanel;

    private Timeline countdown;
    private int remaining;
    private final ChangeListener<Boolean> orientationListener = (o, a, portrait) -> layoutReceipt(portrait);

    public TicketScreenController(ViewNavigator navigator, QRCodeService qrCodeService) {
        this.navigator = navigator;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(4));
        if (!(context instanceof PaymentScreenController.PaidTicket paid)) {
            navigator.show(MainController.SCAN_SCREEN, null);
            return;
        }
        render(paid);
        navigator.portraitProperty().addListener(orientationListener);
        layoutReceipt(navigator.portraitProperty().get());
        popSuccessIcon();
        startCountdown();
        Platform.runLater(finishButton::requestFocus);
    }

    @Override
    public void onHidden() {
        navigator.portraitProperty().removeListener(orientationListener);
        if (countdown != null) {
            countdown.stop();
        }
    }

    /** Horizontal: QR a la derecha. Vertical (tótem): QR más grande debajo del detalle. */
    private void layoutReceipt(boolean portrait) {
        receiptCard.getChildren().remove(qrPanel);
        receiptColumn.getChildren().remove(qrPanel);
        double qrSize = portrait ? 280 : 180;
        qrImageView.setFitWidth(qrSize);
        qrImageView.setFitHeight(qrSize);
        qrHintLabel.setMaxWidth(portrait ? 360 : 200);
        if (portrait) {
            qrPanel.setMaxWidth(Double.MAX_VALUE);
            receiptColumn.getChildren().add(qrPanel);
        } else {
            receiptCard.getChildren().add(qrPanel);
        }
    }

    private void render(PaymentScreenController.PaidTicket paid) {
        ParkingTicket ticket = paid.ticket();
        PaymentRecord record = paid.record();
        LocalDateTime paidAt = record.getTimestamp() != null
                ? record.getTimestamp() : LocalDateTime.now();

        ticketCodeLabel.setText(ticket.getCode());
        paidAtLabel.setText(FormatUtil.dateTime(paidAt));
        amountLabel.setText(record.getCurrency() == PaymentRecord.Currency.USD
                ? FormatUtil.usd(record.getAmount()) : FormatUtil.ves(record.getAmount()));
        methodLabel.setText(FormatUtil.paymentMethod(record.getPaymentMethod()));

        String qrContent = qrCodeService.buildTicketQrContent(
                ticket.getCode(),
                paidAt.format(FMT),
                record.getAmount().toPlainString(),
                record.getPaymentMethod().name());
        showQr(qrContent);
        qrHintLabel.setText("Escanee este código con la cámara de su teléfono.");
    }

    private void showQr(String content) {
        String b64 = qrCodeService.toBase64(content);
        Image image = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(b64)));
        qrImageView.setImage(image);
    }

    private void popSuccessIcon() {
        ScaleTransition pop = new ScaleTransition(Duration.millis(420), successIcon);
        pop.setFromX(0.6);
        pop.setFromY(0.6);
        pop.setToX(1);
        pop.setToY(1);
        pop.setInterpolator(Interpolator.EASE_OUT);
        pop.play();
    }

    private void startCountdown() {
        remaining = AUTO_RETURN_SECONDS;
        updateCountdown();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining--;
            if (remaining <= 0) {
                onNewTicket();
            } else {
                updateCountdown();
            }
        }));
        countdown.setCycleCount(AUTO_RETURN_SECONDS);
        countdown.play();
    }

    private void updateCountdown() {
        countdownLabel.setText("Volverá al inicio automáticamente en " + remaining + " s");
    }

    @FXML
    private void onNewTicket() {
        navigator.show(MainController.SCAN_SCREEN, null);
    }
}
