package com.rndymi.almacentracker.data.document.onnx.detection;

import android.graphics.Bitmap;
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

            FloatBuffer inputBuffer =
                    createNchwBuffer(
                            resizedBitmap,
                            transform
                    );

            long[] inputShape = {
                    1L,
                    3L,
                    transform.getPaddedHeight(),
                    transform.getPaddedWidth()
            };

            OnnxTensor tensor =
                    OnnxTensor.createTensor(
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
            Bitmap resizedBitmap,
            DetectorImageTransform transform
    ) {
        Objects.requireNonNull(
                resizedBitmap,
                "resizedBitmap"
        );

        Objects.requireNonNull(
                transform,
                "transform"
        );

        int resizedWidth =
                resizedBitmap.getWidth();

        int resizedHeight =
                resizedBitmap.getHeight();

        if (resizedWidth
                != transform.getResizedWidth()
                || resizedHeight
                != transform.getResizedHeight()) {
            throw new IllegalArgumentException(
                    "Resized bitmap does not match detector transform"
            );
        }

        int[] pixels =
                new int[
                        resizedWidth
                                * resizedHeight
                        ];

        resizedBitmap.getPixels(
                pixels,
                0,
                resizedWidth,
                0,
                0,
                resizedWidth,
                resizedHeight
        );

        int paddedPixelCount =
                transform.getPaddedWidth()
                        * transform.getPaddedHeight();

        FloatBuffer buffer =
                FloatBuffer.allocate(
                        paddedPixelCount * 3
                );

        writeChannel(
                buffer,
                pixels,
                transform,
                Channel.RED
        );

        writeChannel(
                buffer,
                pixels,
                transform,
                Channel.GREEN
        );

        writeChannel(
                buffer,
                pixels,
                transform,
                Channel.BLUE
        );

        buffer.rewind();

        return buffer;
    }

    private void writeChannel(
            FloatBuffer buffer,
            int[] resizedPixels,
            DetectorImageTransform transform,
            Channel channel
    ) {
        int resizedWidth =
                transform.getResizedWidth();

        int resizedHeight =
                transform.getResizedHeight();

        int paddedWidth =
                transform.getPaddedWidth();

        int paddedHeight =
                transform.getPaddedHeight();

        int paddingLeft =
                transform.getPaddingLeft();

        int paddingTop =
                transform.getPaddingTop();

        for (int paddedY = 0;
             paddedY < paddedHeight;
             paddedY++) {

            int sourceY =
                    paddedY - paddingTop;

            for (int paddedX = 0;
                 paddedX < paddedWidth;
                 paddedX++) {

                int sourceX =
                        paddedX - paddingLeft;

                int component = 0;

                boolean insideResizedBitmap =
                        sourceX >= 0
                                && sourceX
                                < resizedWidth
                                && sourceY >= 0
                                && sourceY
                                < resizedHeight;

                if (insideResizedBitmap) {
                    int pixel =
                            resizedPixels[
                                    sourceY
                                            * resizedWidth
                                            + sourceX
                                    ];

                    component =
                            readComponent(
                                    pixel,
                                    channel
                            );
                }

                buffer.put(
                        normalizeComponent(
                                component
                        )
                );
            }
        }
    }

    private int readComponent(
            int pixel,
            Channel channel
    ) {
        switch (channel) {
            case RED:
                return Color.red(pixel);

            case GREEN:
                return Color.green(pixel);

            case BLUE:
                return Color.blue(pixel);

            default:
                throw new IllegalStateException(
                        "Unsupported image channel"
                );
        }
    }

    private float normalizeComponent(
            int component
    ) {
        return (
                component / 255.0f
                        - CHANNEL_MEAN
        ) / CHANNEL_SCALE;
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
