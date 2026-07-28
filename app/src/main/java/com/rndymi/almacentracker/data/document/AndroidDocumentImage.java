package com.rndymi.almacentracker.data.document;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.core.document.DocumentImage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

final class AndroidDocumentImage implements DocumentImage {

    private final Bitmap recognitionBitmap;
    private final int originalWidth;
    private final int originalHeight;
    private final int appliedRotation;
    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    AndroidDocumentImage(
            Bitmap recognitionBitmap,
            int originalWidth,
            int originalHeight,
            int appliedRotation
    ) {
        this.recognitionBitmap =
                Objects.requireNonNull(
                        recognitionBitmap,
                        "recognitionBitmap"
                );

        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IllegalArgumentException(
                    "Original dimensions must be positive"
            );
        }

        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.appliedRotation =
                normalizeRotation(appliedRotation);
    }

    Bitmap getRecognitionBitmap() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "Document image is already closed"
            );
        }

        return recognitionBitmap;
    }

    @Override
    public int getOriginalWidth() {
        return originalWidth;
    }

    @Override
    public int getOriginalHeight() {
        return originalHeight;
    }

    @Override
    public int getAppliedRotation() {
        return appliedRotation;
    }

    @Override
    public int getProcessedWidth() {
        return recognitionBitmap.getWidth();
    }

    @Override
    public int getProcessedHeight() {
        return recognitionBitmap.getHeight();
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        if (!recognitionBitmap.isRecycled()) {
            recognitionBitmap.recycle();
        }
    }

    private int normalizeRotation(int rotation) {
        int normalized = rotation % 360;

        if (normalized < 0) {
            normalized += 360;
        }

        return normalized;
    }
}
