package com.autopay.parking.ui.controller;

import com.autopay.parking.model.ExchangeRate;
import com.autopay.parking.model.ParkingTicket;
import com.autopay.parking.model.PaymentRecord;
import com.autopay.parking.service.ParkingService;
import com.autopay.parking.service.PaymentService;
import com.autopay.parking.ui.AppScreen;
import com.autopay.parking.ui.ViewNavigator;
import com.autopay.parking.ui.util.FormatUtil;
import com.autopay.parking.ui.util.IconView;
import com.autopay.parking.ui.util.Icons;
import com.autopay.parking.ui.util.PaymentFormValidation;
import com.autopay.parking.ui.util.StepIndicator;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;

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
            "Banco de Venezuela", "Banesco", "Banco Mercantil", "BBVA Provincial",
            "Banco Nacional de Crédito", "Banco del Tesoro", "Banco Exterior",
            "Bancaribe", "Banco Sofitasa", "Bancrecer", "Banplus",
            "DelSur Banco Universal", "Banco Plaza", "Banco Activo");

    /** Datos del comercio para el débito por Pago Móvil (mock). */
    private static final String MERCHANT_BANK = "BBVA Provincial (0108)";
    private static final String MERCHANT_PHONE = "0412-1234567";
    private static final String MERCHANT_ID = "J-12345678-9";

    private static final int[] USD_BILLS = {1, 5, 10, 20, 50, 100};
    private static final int[] VES_ROUNDING = {10, 50, 100, 500, 1000};

    private final PaymentService paymentService;
    private final ViewNavigator navigator;

    @FXML
    private HBox stepsBox;
    @FXML
    private Label ticketCodeLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label amountUsdLabel;
    @FXML
    private HBox amountBar;
    @FXML
    private Label formTicketLabel;
    @FXML
    private VBox methodGridPane;
    @FXML
    private GridPane methodGrid;
    @FXML
    private StackPane methodFormPane;
    @FXML
    private StackPane formIconBox;
    @FXML
    private Label formTitleLabel;
    @FXML
    private Label formSubtitleLabel;
    @FXML
    private VBox formFieldsBox;
    @FXML
    private Label formAmountLabel;
    @FXML
    private HBox paymentErrorBox;
    @FXML
    private HBox validationHint;
    @FXML
    private Label validationHintLabel;
    @FXML
    private Button changeMethodButton;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private IconView confirmIcon;
    @FXML
    private StackPane processingBox;

    private ParkingService.TicketInfo current;
    private PaymentRecord.PaymentMethod selectedMethod;
    private BooleanSupplier formValid = () -> false;
    private String invalidHint = "";
    private boolean processing;
    private final ChangeListener<Boolean> orientationListener = (o, a, portrait) -> layoutMethods(portrait);

    public PaymentScreenController(PaymentService paymentService, ViewNavigator navigator) {
        this.paymentService = paymentService;
        this.navigator = navigator;
    }

    @Override
    public void onShown(Object context) {
        stepsBox.getChildren().add(StepIndicator.of(3));
        navigator.portraitProperty().addListener(orientationListener);
        layoutMethods(navigator.portraitProperty().get());
        current = (ParkingService.TicketInfo) context;
        if (current == null) {
            navigator.show(MainController.SCAN_SCREEN, null);
            return;
        }
        ticketCodeLabel.setText(current.ticket().getCode());
        amountLabel.setText(FormatUtil.ves(current.amount()));
        ExchangeRate rate = paymentService.latestRate().orElse(null);
        amountUsdLabel.setText("Equivale a " + FormatUtil.usd(usdEquivalent())
                + (rate != null ? "  ·  Tasa BCV " + rate.getBcvRate() : ""));
        backToMethods();
    }

    @Override
    public void onHidden() {
        navigator.portraitProperty().removeListener(orientationListener);
    }

    /** Horizontal: cuadrícula 2x2. Vertical (tótem): una columna de tarjetas altas. */
    private void layoutMethods(boolean portrait) {
        int columns = portrait ? 1 : 2;
        methodGrid.getColumnConstraints().clear();
        for (int c = 0; c < columns; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columns);
            methodGrid.getColumnConstraints().add(cc);
        }
        List<Node> cards = List.copyOf(methodGrid.getChildren());
        for (int i = 0; i < cards.size(); i++) {
            GridPane.setConstraints(cards.get(i), i % columns, i / columns);
        }
    }

    // ---------------------------------------------------------------
    // Selección de método
    // ---------------------------------------------------------------

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

    private void openForm(PaymentRecord.PaymentMethod method) {
        selectedMethod = method;
        formTitleLabel.setText(titleFor(method));
        formSubtitleLabel.setText(subtitleFor(method));
        formIconBox.getChildren().setAll(Icons.icon(iconFor(method), 32));
        formAmountLabel.setText(method == PaymentRecord.PaymentMethod.CASH_USD
                ? FormatUtil.usd(usdEquivalent()) : FormatUtil.ves(current.amount()));
        formFieldsBox.getChildren().clear();

        switch (method) {
            case MOBILE_PAY -> buildMobilePayForm();
            case CARD_POS -> buildCardPosForm();
            case CASH_VES -> buildCashForm(false);
            case CASH_USD -> buildCashForm(true);
        }
        formTicketLabel.setText("Ticket " + current.ticket().getCode());
        setShown(amountBar, false);
        setShown(methodGridPane, false);
        setShown(methodFormPane, true);
        setShown(paymentErrorBox, false);
        setProcessing(false);
        confirmPaymentButton.setText("Confirmar pago");
        confirmIcon.setIcon("CHECK");
        refreshValidity();
    }

    // ---------------------------------------------------------------
    // Formularios
    // ---------------------------------------------------------------

    private void buildMobilePayForm() {
        VBox merchant = infoPanel("Envíe su Pago Móvil a",
                infoRow(Icons.BANK, "Banco", MERCHANT_BANK),
                infoRow(Icons.SMARTPHONE, "Teléfono", MERCHANT_PHONE),
                infoRow(Icons.BADGE, "RIF", MERCHANT_ID),
                infoRow(Icons.TAG, "Monto exacto", FormatUtil.ves(current.amount())));
        merchant.getChildren().add(note("Datos de demostración."));

        ComboBox<String> bank = new ComboBox<>();
        bank.setPromptText("Seleccione su banco");
        bank.getItems().setAll(BANKS_VE);
        bank.setVisibleRowCount(7);
        stretch(bank);

        TextField cedula = textField("V-12345678", upperFilter("[VEJvej0-9-]", 11));
        TextField phone = textField("0412-1234567", charFilter("[0-9-]", 12));
        TextField reference = textField("6 últimos dígitos", charFilter("[0-9]", 6));

        watch(cedula, () -> PaymentFormValidation.isCedula(cedula.getText()));
        watch(phone, () -> PaymentFormValidation.isPhone(phone.getText()));
        watch(reference, () -> PaymentFormValidation.isReference(reference.getText()));
        bank.valueProperty().addListener((o, a, b) -> refreshValidity());

        VBox fields = new VBox(12,
                field("Banco desde el que pagó", bank, null),
                pair(field("Su cédula", cedula, "Ej: V-12345678"),
                        field("Su teléfono", phone, "Ej: 0414-7654321")),
                field("Número de referencia", reference, "Aparece en el comprobante de su banco"));
        HBox.setHgrow(fields, Priority.ALWAYS);

        formFieldsBox.getChildren().add(twoColumns(merchant, fields));

        formValid = () -> PaymentFormValidation.isMobilePayValid(
                bank.getValue(), cedula.getText(), phone.getText(), reference.getText());
        invalidHint = "Complete todos los datos del Pago Móvil para continuar.";
        Platform.runLater(bank::requestFocus);
    }

    private void buildCardPosForm() {
        VBox steps = infoPanel("Siga estos pasos",
                numberedStep(1, "Inserte o acerque su tarjeta al punto de venta."),
                numberedStep(2, "Indique el tipo de cuenta y su cédula aquí."),
                numberedStep(3, "Ingrese su clave únicamente en el punto de venta."));
        HBox security = new HBox(8, Icons.icon(Icons.LOCK, 18), note("Nunca le pediremos su clave en esta pantalla."));
        security.setAlignment(Pos.CENTER_LEFT);
        steps.getChildren().add(security);

        StackPane segmented = segmentedControl("Corriente", "Ahorro");

        TextField cedula = textField("V-12345678", upperFilter("[VEJvej0-9-]", 11));
        watch(cedula, () -> PaymentFormValidation.isCedula(cedula.getText()));

        VBox fields = new VBox(12,
                field("Tipo de cuenta", segmented, null),
                field("Cédula del titular", cedula, "Ej: V-12345678"));
        HBox.setHgrow(fields, Priority.ALWAYS);

        formFieldsBox.getChildren().add(twoColumns(steps, fields));

        formValid = () -> PaymentFormValidation.isCardPosValid(cedula.getText());
        invalidHint = "Ingrese la cédula del titular de la tarjeta.";
        Platform.runLater(cedula::requestFocus);
    }

    private void buildCashForm(boolean usd) {
        BigDecimal due = usd ? usdEquivalent() : current.amount();

        Label receivedValue = new Label(money(BigDecimal.ZERO, usd));
        Label changeCaption = new Label("Vuelto");
        Label changeValue = new Label(money(BigDecimal.ZERO, usd));
        receivedValue.getStyleClass().add("summary-value");
        changeCaption.getStyleClass().add("summary-label");
        changeValue.getStyleClass().addAll("summary-value", "summary-strong");

        HBox changeRow = summaryRow(changeCaption, changeValue);
        changeRow.getStyleClass().add("summary-total");
        VBox summary = infoPanel("Resumen",
                summaryRow(summaryLabel("A pagar"), summaryValue(money(due, usd))),
                summaryRow(summaryLabel("Recibido"), receivedValue),
                changeRow);

        TextField received = textField(usd ? "0.00 $" : "0.00 Bs.", amountFilter());
        received.getStyleClass().add("amount-input");

        FlowPane quick = new FlowPane(10, 10);
        for (BigDecimal option : quickAmounts(due, usd)) {
            Button chip = new Button(option.compareTo(due) == 0
                    ? "Exacto " + money(option, usd) : money(option, usd));
            chip.getStyleClass().add("chip-button");
            chip.setFocusTraversable(false);
            chip.setOnAction(e -> {
                received.setText(option.setScale(2, RoundingMode.HALF_UP).toPlainString());
                received.positionCaret(received.getText().length());
            });
            quick.getChildren().add(chip);
        }

        received.textProperty().addListener((obs, o, n) -> {
            BigDecimal value = PaymentFormValidation.parseAmount(n);
            receivedValue.setText(money(value, usd));
            changeRow.getStyleClass().removeAll("summary-ok", "summary-missing");
            if (value.signum() == 0) {
                changeCaption.setText("Vuelto");
                changeValue.setText(money(BigDecimal.ZERO, usd));
            } else if (value.compareTo(due) < 0) {
                changeCaption.setText("Falta");
                changeValue.setText(money(due.subtract(value), usd));
                changeRow.getStyleClass().add("summary-missing");
            } else {
                changeCaption.setText("Vuelto");
                changeValue.setText(money(value.subtract(due), usd));
                changeRow.getStyleClass().add("summary-ok");
            }
            refreshValidity();
        });

        VBox fields = new VBox(12,
                field("Monto recibido", received, null),
                field("Montos rápidos", quick, null));
        HBox.setHgrow(fields, Priority.ALWAYS);

        formFieldsBox.getChildren().add(twoColumns(summary, fields));

        formValid = () -> PaymentFormValidation.coversAmount(received.getText(), due);
        invalidHint = "El monto recibido debe cubrir el total a pagar.";
        Platform.runLater(received::requestFocus);
    }

    private List<BigDecimal> quickAmounts(BigDecimal due, boolean usd) {
        Set<BigDecimal> options = new LinkedHashSet<>();
        options.add(due.setScale(2, RoundingMode.HALF_UP));
        int[] steps = usd ? USD_BILLS : VES_ROUNDING;
        for (int step : steps) {
            BigDecimal s = BigDecimal.valueOf(step);
            BigDecimal value = usd ? s
                    : due.divide(s, 0, RoundingMode.CEILING).multiply(s);
            if (value.compareTo(due) > 0) {
                options.add(value.setScale(2, RoundingMode.HALF_UP));
            }
            if (options.size() >= 5) {
                break;
            }
        }
        return new ArrayList<>(options);
    }

    // ---------------------------------------------------------------
    // Validación
    // ---------------------------------------------------------------

    /** Marca el campo como inválido solo cuando ya tiene contenido. */
    private void watch(TextField field, BooleanSupplier valid) {
        field.textProperty().addListener((obs, o, n) -> {
            boolean showError = !n.isEmpty() && !valid.getAsBoolean();
            field.getStyleClass().remove("field-invalid");
            if (showError) {
                field.getStyleClass().add("field-invalid");
            }
            refreshValidity();
        });
    }

    private void refreshValidity() {
        boolean valid = formValid.getAsBoolean();
        confirmPaymentButton.setDisable(!valid || processing);
        validationHintLabel.setText(invalidHint);
        validationHint.setVisible(!valid);
        if (valid) {
            setShown(paymentErrorBox, false);
        }
    }

    // ---------------------------------------------------------------
    // Pago
    // ---------------------------------------------------------------

    @FXML
    private void onConfirmPayment() {
        if (!formValid.getAsBoolean() || processing) {
            return;
        }
        setShown(paymentErrorBox, false);
        setProcessing(true);
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
            setProcessing(false);
            setShown(paymentErrorBox, true);
            confirmPaymentButton.setText("Reintentar pago");
            confirmIcon.setIcon("REFRESH");
            refreshValidity();
        }
    }

    private void setProcessing(boolean value) {
        processing = value;
        processingBox.setVisible(value);
        changeMethodButton.setDisable(value);
        formFieldsBox.setDisable(value);
        confirmPaymentButton.setDisable(value || !formValid.getAsBoolean());
    }

    @FXML
    private void onCancelForm() {
        backToMethods();
    }

    @FXML
    private void onBackClicked() {
        navigator.show(ConfirmScreenController.SCREEN, current);
    }

    private void backToMethods() {
        formValid = () -> false;
        setShown(amountBar, true);
        setShown(methodGridPane, true);
        setShown(methodFormPane, false);
        setProcessing(false);
    }

    // ---------------------------------------------------------------
    // Construcción de controles
    // ---------------------------------------------------------------

    /** Alterna visibilidad SIN dejar el hueco en el layout. */
    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private static void stretch(Control control) {
        control.setMaxWidth(Double.MAX_VALUE);
    }

    private static TextField textField(String prompt, UnaryOperator<TextFormatter.Change> filter) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setTextFormatter(new TextFormatter<>(filter));
        stretch(field);
        return field;
    }

    private static UnaryOperator<TextFormatter.Change> charFilter(String allowed, int maxLength) {
        return change -> {
            if (!change.getText().matches(allowed + "*")) {
                return null;
            }
            return change.getControlNewText().length() <= maxLength ? change : null;
        };
    }

    private static UnaryOperator<TextFormatter.Change> upperFilter(String allowed, int maxLength) {
        UnaryOperator<TextFormatter.Change> base = charFilter(allowed, maxLength);
        return change -> {
            change.setText(change.getText().toUpperCase());
            return base.apply(change);
        };
    }

    private static UnaryOperator<TextFormatter.Change> amountFilter() {
        return change -> {
            change.setText(change.getText().replace(',', '.'));
            return change.getControlNewText().matches("\\d{0,7}(\\.\\d{0,2})?") ? change : null;
        };
    }

    private static VBox field(String label, Node control, String helper) {
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        VBox box = new VBox(6, l, control);
        if (helper != null) {
            Label h = new Label(helper);
            h.getStyleClass().add("field-helper");
            box.getChildren().add(h);
        }
        return box;
    }

    private static HBox pair(VBox left, VBox right) {
        HBox row = new HBox(12, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);
        left.setPrefWidth(1);
        right.setPrefWidth(1);
        return row;
    }

    private static HBox twoColumns(VBox side, VBox main) {
        side.setMinWidth(300);
        side.setPrefWidth(300);
        side.setMaxWidth(300);
        HBox row = new HBox(24, side, main);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private static VBox infoPanel(String title, Node... rows) {
        Label t = new Label(title);
        t.getStyleClass().add("panel-title");
        VBox panel = new VBox(12, t);
        panel.getChildren().addAll(rows);
        panel.getStyleClass().add("info-panel");
        return panel;
    }

    private static HBox infoRow(String icon, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("detail-label");
        Label v = new Label(value);
        v.getStyleClass().add("info-value");
        HBox row = new HBox(12, Icons.icon(icon, 20), new VBox(0, l, v));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private static HBox numberedStep(int number, String text) {
        Label n = new Label(String.valueOf(number));
        n.getStyleClass().add("step-number");
        n.setMinSize(28, 28);
        n.setAlignment(Pos.CENTER);
        Label t = new Label(text);
        t.getStyleClass().add("step-text");
        t.setWrapText(true);
        HBox row = new HBox(12, n, t);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private static Label note(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-helper");
        l.setWrapText(true);
        return l;
    }

    /**
     * Selector segmentado con un indicador que se desliza hacia la opción
     * elegida. La primera opción queda seleccionada.
     */
    private static StackPane segmentedControl(String... options) {
        ToggleGroup group = new ToggleGroup();
        HBox buttons = new HBox(0);
        for (String option : options) {
            ToggleButton b = new ToggleButton(option);
            b.setToggleGroup(group);
            b.getStyleClass().add("segment");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefWidth(1);
            HBox.setHgrow(b, Priority.ALWAYS);
            buttons.getChildren().add(b);
        }

        Region thumb = new Region();
        thumb.getStyleClass().add("segment-thumb");
        thumb.setMaxHeight(Double.MAX_VALUE);
        thumb.maxWidthProperty().bind(buttons.widthProperty().divide(options.length));
        thumb.setMouseTransparent(true);
        StackPane.setAlignment(thumb, Pos.CENTER_LEFT);

        StackPane control = new StackPane(thumb, buttons);
        control.getStyleClass().add("segmented");

        TranslateTransition slide = new TranslateTransition(Duration.millis(240), thumb);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        group.selectedToggleProperty().addListener((o, old, now) -> {
            // Evita que el grupo quede sin selección al tocar la opción activa.
            if (now == null) {
                old.setSelected(true);
                return;
            }
            slide.stop();
            slide.setToX(buttons.getChildren().indexOf((Node) now) * thumb.getWidth());
            slide.playFromStart();
        });
        // Mantiene el indicador en su sitio si el control cambia de ancho.
        thumb.widthProperty().addListener((o, a, w) -> {
            Toggle selected = group.getSelectedToggle();
            if (selected != null && slide.getStatus() != Animation.Status.RUNNING) {
                thumb.setTranslateX(buttons.getChildren().indexOf((Node) selected) * w.doubleValue());
            }
        });
        group.selectToggle((Toggle) buttons.getChildren().get(0));
        return control;
    }

    private static Label summaryLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("summary-label");
        return l;
    }

    private static Label summaryValue(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("summary-value");
        return l;
    }

    private static HBox summaryRow(Label label, Label value) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, label, spacer, value);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("summary-row");
        return row;
    }

    private static String money(BigDecimal value, boolean usd) {
        return usd ? FormatUtil.usd(value) : FormatUtil.ves(value);
    }

    private BigDecimal usdEquivalent() {
        ExchangeRate rate = paymentService.latestRate().orElse(null);
        return paymentService.convert(current.amount(), rate,
                PaymentRecord.Currency.VES, PaymentRecord.Currency.USD);
    }

    private static String titleFor(PaymentRecord.PaymentMethod method) {
        return switch (method) {
            case MOBILE_PAY -> "Pago Móvil";
            case CARD_POS -> "Punto de Venta";
            case CASH_VES -> "Efectivo en bolívares";
            case CASH_USD -> "Efectivo en dólares";
        };
    }

    private static String subtitleFor(PaymentRecord.PaymentMethod method) {
        return switch (method) {
            case MOBILE_PAY -> "Realice el pago desde su banco y registre la operación";
            case CARD_POS -> "Pague con tarjeta de débito o crédito";
            case CASH_VES, CASH_USD -> "Indique el monto entregado para calcular su vuelto";
        };
    }

    private static String iconFor(PaymentRecord.PaymentMethod method) {
        return switch (method) {
            case MOBILE_PAY -> Icons.SMARTPHONE;
            case CARD_POS -> Icons.CREDIT_CARD;
            case CASH_VES -> Icons.CURRENCY;
            case CASH_USD -> Icons.BANKNOTE;
        };
    }

    /** Resultado del pago para la pantalla del ticket. */
    public record PaidTicket(ParkingTicket ticket, PaymentRecord record) {
    }
}
