package com.autopay.parking.ui.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFormValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"V-12345678", "V12345678", "E-1234567", "J-123456789", "v-12345678", "  V-12345678  "})
    void aceptaCedulaValida(String value) {
        assertThat(PaymentFormValidation.isCedula(value)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"V-12345", "V-1234567890", "12345678", "X-12345678", "V-12A45678"})
    void rechazaCedulaInvalida(String value) {
        assertThat(PaymentFormValidation.isCedula(value)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0412-1234567", "04121234567", "0424-7654321"})
    void aceptaTelefonoValido(String value) {
        assertThat(PaymentFormValidation.isPhone(value)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0212-1234567", "0412-123456", "0412-12345678", "412-1234567", "0412 1234567"})
    void rechazaTelefonoInvalido(String value) {
        assertThat(PaymentFormValidation.isPhone(value)).isFalse();
    }

    @Test
    void aceptaReferenciaDeSeisDigitos() {
        assertThat(PaymentFormValidation.isReference("123456")).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"12345", "1234567", "12A456", "123-456"})
    void rechazaReferenciaInvalida(String value) {
        assertThat(PaymentFormValidation.isReference(value)).isFalse();
    }

    @Test
    void pagoMovilRequiereBancoYCamposValidos() {
        assertThat(PaymentFormValidation.isMobilePayValid(
                "Banesco", "V-12345678", "0412-1234567", "123456")).isTrue();
        assertThat(PaymentFormValidation.isMobilePayValid(
                null, "V-12345678", "0412-1234567", "123456")).isFalse();
        assertThat(PaymentFormValidation.isMobilePayValid(
                "Banesco", "V-12", "0412-1234567", "123456")).isFalse();
        assertThat(PaymentFormValidation.isMobilePayValid(
                "Banesco", "V-12345678", "0212-1234567", "123456")).isFalse();
        assertThat(PaymentFormValidation.isMobilePayValid(
                "Banesco", "V-12345678", "0412-1234567", "12")).isFalse();
    }

    @Test
    void puntoDeVentaSoloPideCedula() {
        assertThat(PaymentFormValidation.isCardPosValid("V-12345678")).isTrue();
        assertThat(PaymentFormValidation.isCardPosValid("")).isFalse();
    }

    @Test
    void efectivoAceptaMontoIgualOMayor() {
        BigDecimal due = new BigDecimal("15.50");
        assertThat(PaymentFormValidation.coversAmount("15.50", due)).isTrue();
        assertThat(PaymentFormValidation.coversAmount("20", due)).isTrue();
        assertThat(PaymentFormValidation.coversAmount("15.49", due)).isFalse();
        assertThat(PaymentFormValidation.coversAmount("", due)).isFalse();
        assertThat(PaymentFormValidation.coversAmount(".", due)).isFalse();
        assertThat(PaymentFormValidation.coversAmount(null, due)).isFalse();
        assertThat(PaymentFormValidation.coversAmount("10", null)).isFalse();
    }

    @Test
    void parseAmountTrataVaciosComoCero() {
        assertThat(PaymentFormValidation.parseAmount(null)).isEqualByComparingTo("0");
        assertThat(PaymentFormValidation.parseAmount("")).isEqualByComparingTo("0");
        assertThat(PaymentFormValidation.parseAmount(".")).isEqualByComparingTo("0");
        assertThat(PaymentFormValidation.parseAmount("abc")).isEqualByComparingTo("0");
        assertThat(PaymentFormValidation.parseAmount("12.50")).isEqualByComparingTo("12.50");
    }
}
