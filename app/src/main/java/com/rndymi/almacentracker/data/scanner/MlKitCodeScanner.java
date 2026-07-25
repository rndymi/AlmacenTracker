package com.rndymi.almacentracker.data.scanner;

import androidx.annotation.NonNull;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.rndymi.almacentracker.core.scanner.ScannedCode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MlKitCodeScanner
        implements ImageAnalysis.Analyzer, AutoCloseable {

    public interface Listener {

        void onCodeDetected(ScannedCode scannedCode);

        void onScannerError(Throwable cause);
    }

    private final BarcodeScanner barcodeScanner;
    private final MlKitBarcodeMapper mapper;
    private final Listener listener;

    /*
     * ML Kit processes one ImageProxy at a time.
     * STRATEGY_KEEP_ONLY_LATEST will discard stale frames.
     */
    private final AtomicBoolean processingFrame =
            new AtomicBoolean(false);

    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public MlKitCodeScanner(
            MlKitBarcodeMapper mapper,
            Listener listener
    ) {
        this.mapper = Objects.requireNonNull(mapper);
        this.listener = Objects.requireNonNull(listener);

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_CODE_128,
                                Barcode.FORMAT_CODE_39,
                                Barcode.FORMAT_EAN_13,
                                Barcode.FORMAT_EAN_8,
                                Barcode.FORMAT_UPC_A,
                                Barcode.FORMAT_UPC_E,
                                Barcode.FORMAT_ITF,
                                Barcode.FORMAT_CODABAR,
                                Barcode.FORMAT_QR_CODE
                        )
                        .build();

        barcodeScanner = BarcodeScanning.getClient(options);
    }

    @ExperimentalGetImage
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (closed.get()) {
            imageProxy.close();
            return;
        }

        if (!processingFrame.compareAndSet(false, true)) {
            imageProxy.close();
            return;
        }

        android.media.Image mediaImage =
                imageProxy.getImage();

        if (mediaImage == null) {
            finishFrame(imageProxy);
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy
                        .getImageInfo()
                        .getRotationDegrees()
        );

        barcodeScanner
                .process(inputImage)
                .addOnSuccessListener(this::handleBarcodes)
                .addOnFailureListener(
                        listener::onScannerError
                )
                .addOnCompleteListener(
                        ignored -> finishFrame(imageProxy)
                );
    }

    private void handleBarcodes(List<Barcode> barcodes) {
        if (closed.get() || barcodes == null) {
            return;
        }

        for (Barcode barcode : barcodes) {
            ScannedCode scannedCode =
                    mapper.toScannedCode(barcode);

            if (scannedCode != null) {
                listener.onCodeDetected(scannedCode);
                return;
            }
        }
    }

    private void finishFrame(ImageProxy imageProxy) {
        imageProxy.close();
        processingFrame.set(false);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        barcodeScanner.close();
    }
}