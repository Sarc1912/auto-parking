package com.autopay.parking.ui.util;

import com.autopay.parking.model.PaymentRecord;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilidades de formato para la UI (español, moneda, duración). */
public final class FormatUtil {

    private static final DecimalFormat DECIMAL = (DecimalFormat)
            NumberFormat.getNumberInstance(Locale.US);

    private static final Locale ES = Locale.forLanguageTag("es-VE");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm", ES);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES);

    static {
        DECIMAL.setMinimumFractionDigits(2);
        DECIMAL.setMaximumFractionDigits(2);
    }

    private FormatUtil() {
    }

    public static String ves(BigDecimal value) {
        return "Bs. " + DECIMAL.format(value != null ? value : BigDecimal.ZERO);
    }

    public static String usd(BigDecimal value) {
        return "$ " + DECIMAL.format(value != null ? value : BigDecimal.ZERO);
    }

    public static String duration(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        long h = minutes / 60;
        long m = minutes % 60;
        return h + " h" + (m > 0 ? " " + m + " min" : "");
    }

    /** Hora en formato de 12 horas: {@code 03:45 PM}. */
    public static String time(LocalDateTime value) {
        return value.format(TIME) + (value.getHour() < 12 ? " AM" : " PM");
    }

    public static String date(LocalDateTime value) {
        return value.format(DATE);
    }

    public static String dateTime(LocalDateTime value) {
        return date(value) + "  " + time(value);
    }

    public static String paymentMethod(PaymentRecord.PaymentMethod method) {
        return switch (method) {
            case MOBILE_PAY -> "Pago Móvil";
            case CARD_POS -> "Punto de Venta";
            case CASH_VES -> "Efectivo Bs";
            case CASH_USD -> "Efectivo USD";
        };
    }

    public static String currency(PaymentRecord.Currency currency) {
        return switch (currency) {
            case VES -> "Bs.";
            case USD -> "USD";
        };
    }
}
