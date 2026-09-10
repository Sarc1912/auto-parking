package com.autopay.parking.ui.util;

import com.autopay.parking.model.PaymentRecord;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/** Utilidades de formato para la UI (español, moneda, duración). */
public final class FormatUtil {

    private static final DecimalFormat DECIMAL = (DecimalFormat)
            NumberFormat.getNumberInstance(Locale.US);

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
        return h + " h " + (m > 0 ? m + " min" : "");
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