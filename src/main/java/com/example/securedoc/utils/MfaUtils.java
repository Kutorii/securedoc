package com.example.securedoc.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import de.taimos.totp.TOTP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.function.Supplier;

import static com.example.securedoc.constant.Constants.ISSUER;

@Slf4j
public class MfaUtils {

    public static Supplier<String> buildSecret = () ->  {
        var random = new SecureRandom();
        var bytes = new byte[20];
        random.nextBytes(bytes);

        return new Base32().encodeToString(bytes);
    };

    private static String getTOTPCode(String secret) {
        var bytes = new Base32().decode(secret);
        var hexKey = Hex.encodeHexString(bytes);

        return TOTP.getOTP(hexKey);
    }

    public static String getQrCodeUri(String secret, String email) {
        return "otpauth://totp/" + URLEncoder.encode(ISSUER + ":" + email, StandardCharsets.UTF_8) +
                "?secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8) +
                "&issuer=" + URLEncoder.encode(ISSUER, StandardCharsets.UTF_8);
    }

    public static BufferedImage getQRCodeImage(String uri, int width, int height) {
        try {
            var matrix = new QRCodeWriter().encode(uri, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (WriterException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    public static boolean isCodeValid(String code, String secret) {
        return code.equals(getTOTPCode(secret));
    }
}
