package com.rndymi.almacentracker.data.document.onnx.detection;

import ai.onnxruntime.OnnxTensor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DetectorInput
        implements AutoCloseable {

    private final OnnxTensor tensor;
    private final DetectorImageTransform transform;
    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public DetectorInput(
            OnnxTensor tensor,
            DetectorImageTransform transform
    ) {
        this.tensor = Objects.requireNonNull(
                tensor,
                "tensor"
        );
        this.transform = Objects.requireNonNull(
                transform,
                "transform"
        );
    }

    public OnnxTensor getTensor() {
        ensureOpen();
        return tensor;
    }

    public DetectorImageTransform getTransform() {
        ensureOpen();
        return transform;
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
                    "Unable to close detector input tensor",
                    exception
            );
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "Detector input is already closed"
            );
        }
    }
}
