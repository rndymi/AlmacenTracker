package com.rndymi.almacentracker.data.scanner;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.rndymi.almacentracker.core.scanner.ScannedCode;
import com.rndymi.almacentracker.core.scanner.ScannedCodeFormat;

public final class MlKitBarcodeMapper {

    @Nullable
    public ScannedCode toScannedCode(Barcode barcode) {
        if (barcode == null) {
            return null;
        }

        String rawValue = barcode.getRawValue();

        if (rawValue == null) {
            return null;
        }

        String trimmedValue = rawValue.trim();

        if (trimmedValue.isEmpty()) {
            return null;
        }

        ScannedCodeFormat format = mapFormat(
                barcode.getFormat()
        );

        if (format == ScannedCodeFormat.UNKNOWN) {
            return null;
        }

        return new ScannedCode(
                trimmedValue,
                format
        );
    }

    public ScannedCodeFormat mapFormat(int mlKitFormat) {
        switch (mlKitFormat) {
            case Barcode.FORMAT_CODE_128:
                return ScannedCodeFormat.CODE_128;

            case Barcode.FORMAT_CODE_39:
                return ScannedCodeFormat.CODE_39;

            case Barcode.FORMAT_EAN_13:
                return ScannedCodeFormat.EAN_13;

            case Barcode.FORMAT_EAN_8:
                return ScannedCodeFormat.EAN_8;

            case Barcode.FORMAT_UPC_A:
                return ScannedCodeFormat.UPC_A;

            case Barcode.FORMAT_UPC_E:
                return ScannedCodeFormat.UPC_E;

            case Barcode.FORMAT_ITF:
                return ScannedCodeFormat.ITF;

            case Barcode.FORMAT_CODABAR:
                return ScannedCodeFormat.CODABAR;

            case Barcode.FORMAT_QR_CODE:
                return ScannedCodeFormat.QR_CODE;

            default:
                return ScannedCodeFormat.UNKNOWN;
        }
    }
}