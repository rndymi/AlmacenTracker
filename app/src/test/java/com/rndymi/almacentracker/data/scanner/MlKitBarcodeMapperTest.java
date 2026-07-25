package com.rndymi.almacentracker.data.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.rndymi.almacentracker.core.scanner.ScannedCodeFormat;

import org.junit.Before;
import org.junit.Test;

public final class MlKitBarcodeMapperTest {

    private MlKitBarcodeMapper mapper;

    @Before
    public void setUp() {
        mapper = new MlKitBarcodeMapper();
    }

    @Test
    public void mapsAllSupportedFormats() {
        assertEquals(
                ScannedCodeFormat.CODE_128,
                mapper.mapFormat(
                        Barcode.FORMAT_CODE_128
                )
        );

        assertEquals(
                ScannedCodeFormat.CODE_39,
                mapper.mapFormat(
                        Barcode.FORMAT_CODE_39
                )
        );

        assertEquals(
                ScannedCodeFormat.EAN_13,
                mapper.mapFormat(
                        Barcode.FORMAT_EAN_13
                )
        );

        assertEquals(
                ScannedCodeFormat.EAN_8,
                mapper.mapFormat(
                        Barcode.FORMAT_EAN_8
                )
        );

        assertEquals(
                ScannedCodeFormat.UPC_A,
                mapper.mapFormat(
                        Barcode.FORMAT_UPC_A
                )
        );

        assertEquals(
                ScannedCodeFormat.UPC_E,
                mapper.mapFormat(
                        Barcode.FORMAT_UPC_E
                )
        );

        assertEquals(
                ScannedCodeFormat.ITF,
                mapper.mapFormat(
                        Barcode.FORMAT_ITF
                )
        );

        assertEquals(
                ScannedCodeFormat.CODABAR,
                mapper.mapFormat(
                        Barcode.FORMAT_CODABAR
                )
        );

        assertEquals(
                ScannedCodeFormat.QR_CODE,
                mapper.mapFormat(
                        Barcode.FORMAT_QR_CODE
                )
        );
    }

    @Test
    public void unsupportedFormatMapsToUnknown() {
        assertEquals(
                ScannedCodeFormat.UNKNOWN,
                mapper.mapFormat(
                        Barcode.FORMAT_DATA_MATRIX
                )
        );
    }

    @Test
    public void nullBarcodeProducesNoResult() {
        assertNull(
                mapper.toScannedCode(null)
        );
    }
}