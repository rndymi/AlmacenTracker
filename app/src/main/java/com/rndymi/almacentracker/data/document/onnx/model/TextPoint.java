package com.rndymi.almacentracker.data.document.onnx.model;

public final class TextPoint {

    private final float x;
    private final float y;

    public TextPoint(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Text point coordinates must be finite"
            );
        }

        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
