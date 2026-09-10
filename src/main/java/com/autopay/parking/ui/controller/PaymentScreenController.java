package com.autopay.parking.ui.controller;

import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.PaymentService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.Icons;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pantalla de selección de método de pago (mockup adaptado al contexto
 * venezolano). No procesa pagos reales: simula la confirmación y registra la
 * transacción.
 */
@KioskController
public class PaymentScreenController implements AppScreen {

    public static final String SCREEN = "/fxml/payment-screen.fxml";
    public static final String TICKET_SCREEN = "/fxml/ticket-screen.fxml";

    private static final List<String> BANKS_VE = List.of(
            "Banco de Venezuela", "Banesco", "Banco Mercantil", "Banco Provincial",
            "Banco Nacional de Crédito", "Banco Sabadell", "Banco del Tesoro",
            "Banco Exterior", "Venezuela", "Bancaribe", "BBVA Provincial",
            "Banco Sofitasa", "Bancrecer", "Banplus", "DelSur Banco Universal");

    /** Teléfono del comercio para el débito por Pago Móvil (mock). */
    private static final String MERCHANT_PHONE = "0412-1234567";

    /** RIF/Cédula del comercio (mock). */
    private static final String MERCHANT_ID = "J-12345678-9";

    private final PaymentService paymentService;
    private final ViewNavigator navigator;

    @FXML
    private Label amountLabel;
    @FXML
    private HBox stepsBox;
    @FXML
    private Label amountUsdLabel;
    @FXML
    private StackPane paymentRoot;
    @FXML
    private VBox methodGridPane;
    @FXML
    private StackPane movilIconBox;
    @FXML
    private StackPane posIconBox;
    @FXML
    private StackPane cashVesIconBox;
    @FXML
    private StackPane cashUsdIconBox;
    @FXML
    private VBox methodFormPane;
    @FXML
    private Label formTitleLabel;
    @FXML
    private VBox formFieldsBox;
    @FXML
    private Label formAmountLabel;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private StackPane processingBox;

    private ParkingService.TicketInfo current;
    private PaymentRecord.PaymentMethod selectedMethod;
    private TextField recibidoField;
    private Label vueltoLabel;

    public PaymentScreenController(PaymentService paymentService, ViewNavigator navigator) {
        this.paymentService = paymentService;
        this.navigator = navigator;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(3));
        movilIconBox.getChildren().add(Icons.icon(Icons.SMARTPHONE));
        posIconBox.getChildren().add(Icons.icon(Icons.CREDIT_CARD));
        cashVesIconBox.getChildren().add(Icons.icon(Icons.CURRENCY));
        cashUsdIconBox.getChildren().add(Icons.icon(Icons.BANKNOTE));
        current = (ParkingService.TicketInfo) context;
        if (current == null) {
            navigator.show(MainController.SCAN_SCREEN, null);
            return;
        }
        amountLabel.setText(FormatUtil.ves(current.amount()));
        ExchangeRate rate = paymentService.latestRate().orElse(null);
        BigDecimal usd = paymentService.convert(current.amount(), rate,
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
        amountUsdLabel.setText("≈ " + FormatUtil.usd(usd) + " (tasa BCV: "
                + (rate != null ? rate.getBcvRate() : "—") + ")");
        backToMethods();
    }

    @FXML
    private void onPagoMovilClicked() {
        openForm(PaymentRecord.PaymentMethod.MOBILE_PAY);
    }

    @FXML
    private void onCardPosClicked() {
        openForm(PaymentRecord.PaymentMethod.CARD_POS);
    }

    @FXML
    private void onCashVesClicked() {
        openForm(PaymentRecord.PaymentMethod.CASH_VES);
    }

    @FXML
    private void onCashUsdClicked() {
        openForm(PaymentRecord.PaymentMethod.CASH_USD);
    }

    private static boolean activate(KeyEvent e) {
        return e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE;
    }

    @FXML
    private void onPagoMovilKey(KeyEvent e) {
        if (activate(e)) {
            onPagoMovilClicked();
        }
    }

    @FXML
    private void onCardPosKey(KeyEvent e) {
        if (activate(e)) {
            onCardPosClicked();
        }
    }

    @FXML
    private void onCashVesKey(KeyEvent e) {
        if (activate(e)) {
            onCashVesClicked();
        }
    }

    @FXML
    private void onCashUsdKey(KeyEvent e) {
        if (activate(e)) {
            onCashUsdClicked();
        }
    }

    private void openForm(PaymentRecord.PaymentMethod method) {
        selectedMethod = method;
        formTitleLabel.setText(titleFor(method));
        formAmountLabel.setText(FormatUtil.ves(current.amount()));
        formFieldsBox.getChildren().clear();
        vueltoLabel = null;
        recibidoField = null;

        switch (method) {
            case MOBILE_PAY -> buildMobilePayForm();
            case CARD_POS -> buildCardPosForm();
            case CASH_VES -> buildCashForm(false);
            case CASH_USD -> {
                formAmountLabel.setText(FormatUtil.usd(usdEquivalent()));
                buildCashForm(true);
            }
        }
        setShown(methodGridPane, false);
        setShown(methodFormPane, true);
        processingBox.setVisible(false);
        confirmPaymentButton.setText("Confirmar pago");
        confirmPaymentButton.setDisable(false);
    }

    private void buildMobilePayForm() {
        ComboBox<String> bank = fieldCombo();
        bank.setPromptText("Seleccione el banco");
        bank.getItems().setAll(BANKS_VE);

        TextField cedula = fieldText();
        cedula.setPromptText("Su cédula (V-12345678)");

        TextField phone = fieldText();
        phone.setPromptText("Su teléfono (0412-0000000)");

        TextField reference = fieldText();
        reference.setPromptText("Número de referencia (6 dígitos)");

        formFieldsBox.getChildren().addAll(
                label("Pague a (datos ficticios)"),
                mockTarget("Banco Provincial", MERCHANT_PHONE, MERCHANT_ID),
                label("Banco desde el que pagó"), bank,
                label("Su cédula"), cedula,
                label("Su teléfono"), phone,
                label("Número de referencia"), reference,
                hint("Al confirmar se registra el débito Pago Móvil. "
                        + "En esta versión es simulado."));
    }

    private VBox mockTarget(String bank, String phone, String id) {
        VBox box = new VBox(2);
        box.getStyleClass().add("mock-target");
        box.getChildren().addAll(
                mockRow("Banco: " + bank),
                mockRow("Teléfono: " + phone),
                mockRow("RIF/Cédula: " + id));
        return box;
    }

    private Label mockRow(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("mock-row");
        return l;
    }

    private void buildCardPosForm() {
        ComboBox<String> accountType = fieldCombo();
        accountType.getItems().setAll("Corriente", "Ahorro");
        accountType.setValue("Corriente");

        TextField cedula = fieldText();
        cedula.setPromptText("Cédula de identidad (V-12345678)");

        PasswordField clave = fieldPassword();
        clave.setPromptText("Clave del punto de venta");

        formFieldsBox.getChildren().addAll(
                label("Tipo de cuenta"), accountType,
                label("Cédula de identidad"), cedula,
                label("Clave"), clave,
                hint("Inserte o deslice la tarjeta en el punto de venta. "
                        + "El cobro en esta versión es simulado."));
    }

    private ComboBox<String> fieldCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.setPrefWidth(360);
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private TextField fieldText() {
        TextField field = new TextField();
        field.setPrefWidth(360);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private PasswordField fieldPassword() {
        PasswordField field = new PasswordField();
        field.setPrefWidth(360);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private void buildCashForm(boolean usd) {
        recibidoField = new TextField();
        recibidoField.setPromptText("Monto recibido");
        recibidoField.setPrefWidth(360);

        vueltoLabel = new Label();
        vueltoLabel.getStyleClass().add("amount-usd");
        vueltoLabel.setText(usd ? "Vuelto: —" : "Vuelto: —");

        formFieldsBox.getChildren().addAll(label("Monto recibido"), recibidoField, vueltoLabel);
        recibidoField.setOnAction(e -> updateVuelto(usd));
        recibidoField.textProperty().addListener((obs, o, n) -> updateVuelto(usd));
    }

    private void updateVuelto(boolean usd) {
        if (vueltoLabel == null || recibidoField == null) {
            return;
        }
        try {
            BigDecimal received = new BigDecimal(recibidoField.getText().trim());
            BigDecimal due = usd ? usdEquivalent() : current.amount();
            if (received.compareTo(due) < 0) {
                vueltoLabel.setText("Falta " + (usd ? FormatUtil.usd(due.subtract(received))
                        : FormatUtil.ves(due.subtract(received))));
            } else {
                vueltoLabel.setText("Vuelto: "
                        + (usd ? FormatUtil.usd(received.subtract(due))
                        : FormatUtil.ves(received.subtract(due))));
            }
        } catch (NumberFormatException e) {
            vueltoLabel.setText("Vuelto: —");
        }
    }

    @FXML
    private void onConfirmPayment() {
        confirmPaymentButton.setDisable(true);
        processingBox.setVisible(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> executeMockPayment());
        delay.play();
    }

    private void executeMockPayment() {
        try {
            BigDecimal amount;
            PaymentRecord.Currency currency;
            ExchangeRate rate = paymentService.latestRate().orElse(null);
            if (selectedMethod == PaymentRecord.PaymentMethod.CASH_USD) {
                amount = usdEquivalent();
                currency = PaymentRecord.Currency.USD;
            } else {
                amount = current.amount();
                currency = PaymentRecord.Currency.VES;
            }
            PaymentRecord record = paymentService.processPayment(
                    current.ticket(), selectedMethod, currency, amount,
                    rate != null ? rate.getBcvRate() : null);
            navigator.show(TICKET_SCREEN, new PaidTicket(current.ticket(), record));
        } catch (RuntimeException e) {
            processingBox.setVisible(false);
            confirmPaymentButton.setDisable(false);
            confirmPaymentButton.setText("Reintentar pago");
        }
    }

    @FXML
    private void onCancelForm() {
        backToMethods();
    }

    @FXML
    private void onBackClicked() {
        navigator.show(MainController.SCAN_SCREEN, null);
    }

    private void backToMethods() {
        setShown(methodGridPane, true);
        setShown(methodFormPane, false);
        processingBox.setVisible(false);
    }

    /** Alterna visibilidad SIN dejar el hueco en el layout. */
    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private BigDecimal usdEquivalent() {
        ExchangeRate rate = paymentService.latestRate().orElse(null);
        return paymentService.convert(current.amount(), rate,
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
    }

    private String titleFor(PaymentRecord.PaymentMethod method) {
        return switch (method) {
            case MOBILE_PAY -> "Pago Móvil";
            case CARD_POS -> "Punto de Venta";
            case CASH_VES -> "Efectivo en Bolívares";
            case CASH_USD -> "Efectivo en Dólares";
        };
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-label");
        return l;
    }

    private Label hint(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("form-hint");
        l.setWrapText(true);
        return l;
    }

    /** Resultado del pago para la pantalla del ticket. */
    public record PaidTicket(ParkingTicket ticket, PaymentRecord record) {
    }
}