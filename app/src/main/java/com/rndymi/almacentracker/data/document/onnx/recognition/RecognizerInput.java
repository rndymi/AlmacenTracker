package com.rndymi.almacentracker.data.document.onnx.recognition;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.onnxruntime.OnnxTensor;

public final class RecognizerInput
        implements AutoCloseable {

    private final OnnxTensor tensor;
    private final DetectedTextRegion sourceRegion;
    private final int contentWidth;
    private final int paddedWidth;
    private final int fixedHeight;
    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public RecognizerInput(
            OnnxTensor tensor,
            DetectedTextRegion sourceRegion,
            int contentWidth,
            int paddedWidth,
            int fixedHeight
    ) {
        this.tensor = Objects.requireNonNull(
                tensor,
                "tensor"
        );
        this.sourceRegion = Objects.requireNonNull(
                sourceRegion,
                "sourceRegion"
        );

        if (contentWidth <= 0
                || paddedWidth < contentWidth
                || fixedHeight <= 0) {
            throw new IllegalArgumentException(
                    "Recognizer dimensions are invalid"
            );
        }

        this.contentWidth = contentWidth;
        this.paddedWidth = paddedWidth;
        this.fixedHeight = fixedHeight;
    }

    public OnnxTensor getTensor() {
        ensureOpen();
        return tensor;
    }

    public DetectedTextRegion getSourceRegion() {
        ensureOpen();
        return sourceRegion;
    }

    public int getContentWidth() {
        ensureOpen();
        return contentWidth;
    }

    public int getPaddedWidth() {
        ensureOpen();
        return paddedWidth;
    }

    public int getFixedHeight() {
        ensureOpen();
        return fixedHeight;
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        try {
            tensor.close();
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                    "Unable to close recognizer input tensor",
                    exception
            );
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "Recognizer input is already closed"
            );
        }
    }
}
