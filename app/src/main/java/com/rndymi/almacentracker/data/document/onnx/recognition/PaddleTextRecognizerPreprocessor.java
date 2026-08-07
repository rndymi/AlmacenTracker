package com.rndymi.almacentracker.data.document.onnx.recognition;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;

import java.nio.FloatBuffer;
import java.util.Objects;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;

public final class PaddleTextRecognizerPreprocessor {

    private static final float NORMALIZATION_MEAN = 0.5f;
    private static final float NORMALIZATION_SCALE = 2.0f;
    private static final int CHANNEL_COUNT = 3;

    private final OrtEnvironment environment;
    private final PaddleTextRecognizerConfiguration configuration;

    public PaddleTextRecognizerPreprocessor(
            OrtEnvironment environment,
            PaddleTextRecognizerConfiguration configuration
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

    public RecognizerInput prepare(
            Bitmap sourceBitmap,
            DetectedTextRegion sourceRegion
    ) throws TextRecognitionException {
        validateBitmap(sourceBitmap);
        Objects.requireNonNull(
                sourceRegion,
                "sourceRegion"
        );

        Bitmap croppedBitmap = null;
        Bitmap orientedBitmap = null;
        Bitmap resizedBitmap = null;
        Bitmap paddedBitmap = null;

        try {
            croppedBitmap = crop(
                    sourceBitmap,
                    sourceRegion
            );

            orientedBitmap = normalizeOrientation(
                    croppedBitmap
            );

            int contentWidth = calculateContentWidth(
                    orientedBitmap.getWidth(),
                    orientedBitmap.getHeight()
            );

            int paddedWidth = alignWidth(
                    contentWidth
            );

            resizedBitmap = Bitmap.createScaledBitmap(
                    orientedBitmap,
                    contentWidth,
                    configuration.getFixedHeight(),
                    true
            );

            paddedBitmap = createPaddedBitmap(
                    resizedBitmap,
                    paddedWidth
            );

            OnnxTensor tensor =
                    createTensor(paddedBitmap);

            return new RecognizerInput(
                    tensor,
                    sourceRegion,
                    contentWidth,
                    paddedWidth,
                    configuration.getFixedHeight()
            );
        } catch (TextRecognitionException exception) {
            throw exception;
        } catch (OutOfMemoryError error) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.MEMORY_ERROR,
                    "Insufficient memory while preparing text region",
                    error
            );
        } catch (RuntimeException | OrtException exception) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .PREPROCESSING_ERROR,
                    "Unable to prepare region for recognition",
                    exception
            );
        } finally {
            recycleCreatedBitmap(
                    paddedBitmap,
                    sourceBitmap
            );
            recycleCreatedBitmap(
                    resizedBitmap,
                    sourceBitmap,
                    paddedBitmap
            );
            recycleCreatedBitmap(
                    orientedBitmap,
                    sourceBitmap,
                    resizedBitmap,
                    paddedBitmap
            );
            recycleCreatedBitmap(
                    croppedBitmap,
                    sourceBitmap,
                    orientedBitmap,
                    resizedBitmap,
                    paddedBitmap
            );
        }
    }

    int calculateContentWidth(
            int sourceWidth,
            int sourceHeight
    ) throws TextRecognitionException {
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.INVALID_REGION,
                    "Region dimensions must be positive"
            );
        }

        long proportionalWidth = Math.round(
                sourceWidth
                        * (double) configuration.getFixedHeight()
                        / sourceHeight
        );

        int boundedWidth = (int) Math.max(
                configuration.getMinimumWidth(),
                Math.min(
                        proportionalWidth,
                        configuration.getMaximumWidth()
                )
        );

        return boundedWidth;
    }

    int alignWidth(
            int contentWidth
    ) {
        int multiple =
                configuration.getWidthMultiple();

        int aligned =
                ((contentWidth + multiple - 1)
                        / multiple)
                        * multiple;

        return Math.min(
                configuration.getMaximumWidth(),
                Math.max(
                        configuration.getMinimumWidth(),
                        aligned
                )
        );
    }

    private Bitmap crop(
            Bitmap sourceBitmap,
            DetectedTextRegion region
    ) throws TextRecognitionException {
        int left = Math.max(
                0,
                (int) Math.floor(region.getLeft())
        );
        int top = Math.max(
                0,
                (int) Math.floor(region.getTop())
        );
        int right = Math.min(
                sourceBitmap.getWidth(),
                (int) Math.ceil(region.getRight())
        );
        int bottom = Math.min(
                sourceBitmap.getHeight(),
                (int) Math.ceil(region.getBottom())
        );

        int width = right - left;
        int height = bottom - top;

        if (width <= 0 || height <= 0) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.INVALID_REGION,
                    "Detected region is outside the bitmap"
            );
        }

        return Bitmap.createBitmap(
                sourceBitmap,
                left,
                top,
                width,
                height
        );
    }

    private Bitmap normalizeOrientation(
            Bitmap croppedBitmap
    ) {
        if (croppedBitmap.getHeight()
                <= croppedBitmap.getWidth()
                * configuration.getVerticalAspectThreshold()) {
            return croppedBitmap;
        }

        Matrix rotation = new Matrix();
        rotation.postRotate(90.0f);

        return Bitmap.createBitmap(
                croppedBitmap,
                0,
                0,
                croppedBitmap.getWidth(),
                croppedBitmap.getHeight(),
                rotation,
                true
        );
    }

    private Bitmap createPaddedBitmap(
            Bitmap resizedBitmap,
            int paddedWidth
    ) {
        if (resizedBitmap.getWidth() == paddedWidth) {
            return resizedBitmap;
        }

        Bitmap padded = Bitmap.createBitmap(
                paddedWidth,
                configuration.getFixedHeight(),
                Bitmap.Config.ARGB_8888
        );

        padded.eraseColor(0xFFFFFFFF);

        android.graphics.Canvas canvas =
                new android.graphics.Canvas(padded);

        canvas.drawBitmap(
                resizedBitmap,
                0.0f,
                0.0f,
                null
        );

        return padded;
    }

    private OnnxTensor createTensor(
            Bitmap bitmap
    ) throws OrtException {
        int width =
                bitmap.getWidth();

        int height =
                bitmap.getHeight();

        int pixelCount =
                width * height;

        int[] pixels =
                new int[pixelCount];

        bitmap.getPixels(
                pixels,
                0,
                width,
                0,
                0,
                width,
                height
        );

        FloatBuffer buffer =
                FloatBuffer.allocate(
                        CHANNEL_COUNT
                                * pixelCount
                );

        writeNormalizedChannel(
                buffer,
                pixels,
                16
        );

        writeNormalizedChannel(
                buffer,
                pixels,
                8
        );

        writeNormalizedChannel(
                buffer,
                pixels,
                0
        );

        buffer.rewind();

        return OnnxTensor.createTensor(
                environment,
                buffer,
                new long[]{
                        1L,
                        CHANNEL_COUNT,
                        height,
                        width
                }
        );
    }

    private void writeNormalizedChannel(
            FloatBuffer buffer,
            int[] pixels,
            int bitShift
    ) {
        for (int pixel : pixels) {
            float component =
                    (
                            (pixel >> bitShift)
                                    & 0xFF
                    ) / 255.0f;

            buffer.put(
                    normalize(component)
            );
        }
    }

    private float normalize(
            float value
    ) {
        return (value - NORMALIZATION_MEAN)
                * NORMALIZATION_SCALE;
    }

    private void validateBitmap(
            Bitmap bitmap
    ) throws TextRecognitionException {
        if (bitmap == null
                || bitmap.isRecycled()
                || bitmap.getWidth() <= 0
                || bitmap.getHeight() <= 0) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error.INVALID_IMAGE,
                    "Recognizer requires a valid bitmap"
            );
        }
    }

    private void recycleCreatedBitmap(
            Bitmap bitmap,
            Bitmap source,
            Bitmap... protectedBitmaps
    ) {
        if (bitmap == null
                || bitmap == source
                || bitmap.isRecycled()) {
            return;
        }

        for (Bitmap protectedBitmap : protectedBitmaps) {
            if (bitmap == protectedBitmap) {
                return;
            }
        }

        bitmap.recycle();
    }
}
