package com.autopay.parking.ui.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reglas de los formularios de pago. Sin JavaFX para poder probarlas
 * como unitarias.
 */
public final class PaymentFormValidation {

    private static final Pattern CEDULA = Pattern.compile("^[VEJ]-?\\d{6,9}$");
    private static final Pattern PHONE = Pattern.compile("^04\\d{2}-?\\d{7}$");
    private static final Pattern REFERENCE = Pattern.compile("^\\d{6}$");

    private PaymentFormValidation() {
    }

    public static boolean isCedula(String value) {
        return CEDULA.matcher(normalize(value).toUpperCase()).matches();
    }

    public static boolean isPhone(String value) {
        return PHONE.matcher(normalize(value)).matches();
    }

    public static boolean isReference(String value) {
        return REFERENCE.matcher(normalize(value)).matches();
    }

    public static boolean hasBank(String bank) {
        return bank != null && !bank.isBlank();
    }

    public static boolean isMobilePayValid(String bank, String cedula, String phone, String reference) {
        return hasBank(bank) && isCedula(cedula) && isPhone(phone) && isReference(reference);
    }

    public static boolean isCardPosValid(String cedula) {
        return isCedula(cedula);
    }

    public static boolean coversAmount(String received, BigDecimal due) {
        if (due == null) {
            return false;
        }
        return parseAmount(received).compareTo(due) >= 0;
    }

    public static BigDecimal parseAmount(String text) {
        try {
            return text == null || text.isBlank() || text.equals(".")
                    ? BigDecimal.ZERO : new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
