package com.autopay.parking.ui.controller;

import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.QRCodeService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.Icons;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Pantalla de ticket pagado: muestra el comprobante en pantalla y el código QR
 * para que el usuario lo capture con su teléfono (transferencia offline).
 */
@KioskController
public class TicketScreenController implements AppScreen {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ViewNavigator navigator;
    private final QRCodeService qrCodeService;

    @FXML
    private HBox stepsBox;
    @FXML
    private Label ticketCodeLabel;
    @FXML
    private StackPane successIconBox;
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

    public TicketScreenController(ViewNavigator navigator, QRCodeService qrCodeService) {
        this.navigator = navigator;
        this.qrCodeService = qrCodeService;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(4));
        successIconBox.getChildren().add(Icons.icon(Icons.CHECK_CIRCLE));
        if (!(context instanceof PaymentScreenController.PaidTicket paid)) {
            navigator.show(MainController.SCAN_SCREEN, null);
            return;
        }
        render(paid);
    }

    private void render(PaymentScreenController.PaidTicket paid) {
        ParkingTicket ticket = paid.ticket();
        PaymentRecord record = paid.record();
        LocalDateTime paidAt = record.getTimestamp() != null
                ? record.getTimestamp() : LocalDateTime.now();

        ticketCodeLabel.setText("Ticket " + ticket.getCode());
        paidAtLabel.setText("Pagado el " + paidAt.format(FMT));
        amountLabel.setText(FormatUtil.currency(record.getCurrency()) != null
                ? formatAmount(record) : "");

        methodLabel.setText("Método: " + FormatUtil.paymentMethod(record.getPaymentMethod()));

        String qrContent = qrCodeService.buildTicketQrContent(
                ticket.getCode(),
                paidAt.format(FMT),
                record.getAmount().toPlainString(),
                record.getPaymentMethod().name());
        showQr(qrContent, paidAt);
        qrHintLabel.setText("Fotografía o escanee este código con su teléfono "
                + "para guardar el comprobante.");
    }

    private String formatAmount(PaymentRecord record) {
        BigDecimal a = record.getAmount();
        if (record.getCurrency() == PaymentRecord.Currency.USD) {
            return "$ " + a;
        }
        return "Bs. " + a;
    }

    private void showQr(String content, LocalDateTime paidAt) {
        String b64 = qrCodeService.toBase64(content);
        Image image = new Image(new ByteArrayInputStream(Base64.getDecoder().decode(b64)));
        qrImageView.setImage(image);
        qrImageView.setPreserveRatio(true);
        qrImageView.setFitWidth(280);
        qrImageView.setFitHeight(280);
    }

    @FXML
    private void onNewTicket() {
        navigator.show(MainController.SCAN_SCREEN, null);
    }
}