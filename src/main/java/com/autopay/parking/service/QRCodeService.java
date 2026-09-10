package com.autopay.parking.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Generación de códigos QR mediante ZXing.
 *
 * <p>El QR contiene la información del ticket pagado (código, monto, método)
 * y se muestra en pantalla para que el usuario lo capture con su teléfono,
 * sin depender de conexión a internet ni hardware adicional.</p>
 */
@Service
public class QRCodeService {

    public static final int DEFAULT_SIZE = 512;

    /**
     * Genera un QR y lo devuelve codificado en Base64 (PNG).
     */
    public String toBase64(String content) {
        return toBase64(content, DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public String toBase64(String content, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("QR_GENERATION_FAILED", e);
        }
    }

    /**
     * Arma el contenido del QR para un ticket pagado.
     */
    public String buildTicketQrContent(String ticketCode, String paidAt,
                                       String amount, String method) {
        return "AUTOPAY|TICKET=" + ticketCode
                + "|PAID=" + paidAt
                + "|AMOUNT=" + amount
                + "|METHOD=" + method;
    }
}