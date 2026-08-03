package com.rndymi.almacentracker.data.document.onnx.detection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;

import java.nio.FloatBuffer;
import java.util.Objects;

public final class PaddleTextDetectorPreprocessor {

    private static final float CHANNEL_MEAN = 0.5f;
    private static final float CHANNEL_SCALE = 0.5f;

    private final OrtEnvironment environment;
    private final PaddleTextDetectorConfiguration configuration;

    public PaddleTextDetectorPreprocessor(
            OrtEnvironment environment,
            PaddleTextDetectorConfiguration configuration
    ) {
        this.environment = Objects.requireNonNull(
                environment,
                "environment"
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );
    }

    public DetectorInput prepare(
            Bitmap sourceBitmap
    ) throws TextDetectionException {
        validateBitmap(sourceBitmap);

        Bitmap resizedBitmap = null;
        Bitmap paddedBitmap = null;

        try {
            DetectorImageTransform transform =
                    createTransform(
                            sourceBitmap.getWidth(),
                            sourceBitmap.getHeight()
                    );

            resizedBitmap = Bitmap.createScaledBitmap(
                    sourceBitmap,
                    transform.getResizedWidth(),
                    transform.getResizedHeight(),
                    true
            );

            paddedBitmap = Bitmap.createBitmap(
                    transform.getPaddedWidth(),
                    transform.getPaddedHeight(),
                    Bitmap.Config.ARGB_8888
            );

            Canvas canvas = new Canvas(paddedBitmap);
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(
                    resizedBitmap,
                    transform.getPaddingLeft(),
                    transform.getPaddingTop(),
                    null
            );

            FloatBuffer inputBuffer =
                    createNchwBuffer(paddedBitmap);

            long[] inputShape = {
                    1L,
                    3L,
                    transform.getPaddedHeight(),
                    transform.getPaddedWidth()
            };

            OnnxTensor tensor = OnnxTensor.createTensor(
                    environment,
                    inputBuffer,
                    inputShape
            );

            return new DetectorInput(
                    tensor,
                    transform
            );
        } catch (OutOfMemoryError error) {
            throw new TextDetectionException(
                    TextDetectionException.Error.MEMORY_ERROR,
                    "Insufficient memory while preparing detector input",
                    error
            );
        } catch (OrtException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error
                            .INPUT_SHAPE_INCOMPATIBLE,
                    "Unable to create detector input tensor",
                    exception
            );
        } catch (IllegalArgumentException
                 | IllegalStateException exception) {
            throw new TextDetectionException(
                    TextDetectionException.Error.INVALID_IMAGE,
                    "Unable to preprocess detector image",
                    exception
            );
        } finally {
            recycleOwnedBitmap(
                    paddedBitmap,
                    sourceBitmap
            );
            recycleOwnedBitmap(
                    resizedBitmap,
                    sourceBitmap
            );
        }
    }

    DetectorImageTransform createTransform(
            int sourceWidth,
            int sourceHeight
    ) {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            throw new IllegalArgumentException(
                    "Source dimensions must be positive"
            );
        }

        int maximumSide = Math.max(
                sourceWidth,
                sourceHeight
        );

        float scale = maximumSide
                > configuration.getMaximumSide()
                ? (float) configuration.getMaximumSide()
                  / maximumSide
                : 1.0f;

        int resizedWidth = Math.max(
                1,
                Math.round(sourceWidth * scale)
        );
        int resizedHeight = Math.max(
                1,
                Math.round(sourceHeight * scale)
        );

        int paddedWidth = roundUp(
                resizedWidth,
                configuration.getDimensionMultiple()
        );
        int paddedHeight = roundUp(
                resizedHeight,
                configuration.getDimensionMultiple()
        );

        return new DetectorImageTransform(
                sourceWidth,
                sourceHeight,
                resizedWidth,
                resizedHeight,
                paddedWidth,
                paddedHeight,
                0,
                0,
                paddedWidth - resizedWidth,
                paddedHeight - resizedHeight
        );
    }

    FloatBuffer createNchwBuffer(
            Bitmap bitmap
    ) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int pixelCount = width * height;

        int[] pixels = new int[pixelCount];

        bitmap.getPixels(
                pixels,
                0,
                width,
                0,
                0,
                width,
                height
        );

        FloatBuffer buffer = FloatBuffer.allocate(
                pixelCount * 3
        );

        writeChannel(
                buffer,
                pixels,
                Channel.RED
        );
        writeChannel(
                buffer,
                pixels,
                Channel.GREEN
        );
        writeChannel(
                buffer,
                pixels,
                Channel.BLUE
        );

        buffer.rewind();
        return buffer;
    }

    private void writeChannel(
            FloatBuffer buffer,
            int[] pixels,
            Channel channel
    ) {
        for (int pixel : pixels) {
            int component;

            switch (channel) {
                case RED:
                    component = Color.red(pixel);
                    break;
                case GREEN:
                    component = Color.green(pixel);
                    break;
                case BLUE:
                    component = Color.blue(pixel);
                    break;
                default:
                    throw new IllegalStateException(
                            "Unsupported image channel"
                    );
            }

            float normalized =
                    (component / 255.0f - CHANNEL_MEAN)
                            / CHANNEL_SCALE;

            buffer.put(normalized);
        }
    }

    private int roundUp(
            int value,
            int multiple
    ) {
        int remainder = value % multiple;

        if (remainder == 0) {
            return value;
        }

        return value + multiple - remainder;
    }

    private void validateBitmap(
            Bitmap bitmap
    ) throws TextDetectionException {
        if (bitmap == null
                || bitmap.isRecycled()
                || bitmap.getWidth() <= 0
                || bitmap.getHeight() <= 0) {
            throw new TextDetectionException(
                    TextDetectionException.Error.INVALID_IMAGE,
                    "Detector requires a valid bitmap"
            );
        }
    }

    private void recycleOwnedBitmap(
            Bitmap bitmap,
            Bitmap sourceBitmap
    ) {
        if (bitmap == null
                || bitmap == sourceBitmap
                || bitmap.isRecycled()) {
            return;
        }

        bitmap.recycle();
    }

    private enum Channel {
        RED,
        GREEN,
        BLUE
    }
}
