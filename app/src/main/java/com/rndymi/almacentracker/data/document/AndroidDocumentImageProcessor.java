package com.rndymi.almacentracker.data.document;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.rndymi.almacentracker.core.document.DocumentImageProcessingCallback;
import com.rndymi.almacentracker.core.document.DocumentImageProcessor;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AndroidDocumentImageProcessor
        implements DocumentImageProcessor {

    private static final int OCR_MAXIMUM_SIDE = 2200;

    private static final float CONTRAST = 1.18f;

    private final ContentResolver contentResolver;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public AndroidDocumentImageProcessor(
            Context context
    ) {
        contentResolver =
                Objects.requireNonNull(
                                context,
                                "context"
                        ).getApplicationContext()
                        .getContentResolver();
    }

    @Override
    public void process(
            String imageUri,
            DocumentImageProcessingCallback callback
    ) {
        Objects.requireNonNull(
                imageUri,
                "imageUri"
        );
        Objects.requireNonNull(
                callback,
                "callback"
        );

        if (closed.get()) {
            callback.onProcessingError();
            return;
        }

        executor.execute(
                () -> processInBackground(
                        imageUri,
                        callback
                )
        );
    }

    private void processInBackground(
            String imageUri,
            DocumentImageProcessingCallback callback
    ) {
        Uri uri;

        try {
            uri = Uri.parse(imageUri);
        } catch (Exception exception) {
            callback.onImageOpenError();
            return;
        }

        ImageMetadata metadata =
                readMetadata(uri);

        if (metadata == null) {
            callback.onImageOpenError();
            return;
        }

        Bitmap decoded =
                decodeScaledBitmap(
                        uri,
                        metadata.width,
                        metadata.height
                );

        if (decoded == null) {
            callback.onImageOpenError();
            return;
        }

        Bitmap oriented = null;
        Bitmap processed = null;

        try {
            oriented =
                    rotateBitmap(
                            decoded,
                            metadata.rotation
                    );

            if (oriented != decoded
                    && !decoded.isRecycled()) {
                decoded.recycle();
            }

            processed =
                    createOcrBitmap(oriented);

            if (processed != oriented
                    && !oriented.isRecycled()) {
                oriented.recycle();
            }

            callback.onSuccess(
                    new AndroidDocumentImage(
                            processed,
                            metadata.width,
                            metadata.height,
                            metadata.rotation
                    )
            );
        } catch (
                Exception
                | OutOfMemoryError error
        ) {
            recycleIfNecessary(processed);
            recycleIfNecessary(oriented);
            recycleIfNecessary(decoded);

            callback.onProcessingError();
        }
    }

    private ImageMetadata readMetadata(Uri uri) {
        BitmapFactory.Options bounds =
                new BitmapFactory.Options();

        bounds.inJustDecodeBounds = true;

        try (
                InputStream stream =
                        contentResolver.openInputStream(uri)
        ) {
            if (stream == null) {
                return null;
            }

            BitmapFactory.decodeStream(
                    stream,
                    null,
                    bounds
            );
        } catch (Exception exception) {
            return null;
        }

        if (bounds.outWidth <= 0
                || bounds.outHeight <= 0) {
            return null;
        }

        int rotation = readExifRotation(uri);

        return new ImageMetadata(
                bounds.outWidth,
                bounds.outHeight,
                rotation
        );
    }

    private int readExifRotation(Uri uri) {
        try (
                InputStream stream =
                        contentResolver.openInputStream(uri)
        ) {
            if (stream == null) {
                return 0;
            }

            ExifInterface exif =
                    new ExifInterface(stream);

            int orientation =
                    exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                    );

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    return 270;

                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    return 0;
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private Bitmap decodeScaledBitmap(
            Uri uri,
            int originalWidth,
            int originalHeight
    ) {
        BitmapFactory.Options options =
                new BitmapFactory.Options();

        options.inSampleSize =
                calculateSampleSize(
                        originalWidth,
                        originalHeight,
                        OCR_MAXIMUM_SIDE
                );

        options.inPreferredConfig =
                Bitmap.Config.ARGB_8888;

        try (
                InputStream stream =
                        contentResolver.openInputStream(uri)
        ) {
            if (stream == null) {
                return null;
            }

            return BitmapFactory.decodeStream(
                    stream,
                    null,
                    options
            );
        } catch (
                Exception
                | OutOfMemoryError error
        ) {
            return null;
        }
    }

    private int calculateSampleSize(
            int width,
            int height,
            int maximumSide
    ) {
        int sampleSize = 1;

        while (
                Math.max(
                        width / sampleSize,
                        height / sampleSize
                ) > maximumSide * 2
        ) {
            sampleSize *= 2;
        }

        return Math.max(1, sampleSize);
    }

    private Bitmap rotateBitmap(
            Bitmap source,
            int rotation
    ) {
        if (rotation == 0) {
            return source;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        return Bitmap.createBitmap(
                source,
                0,
                0,
                source.getWidth(),
                source.getHeight(),
                matrix,
                true
        );
    }

    private Bitmap createOcrBitmap(
            Bitmap source
    ) {
        Bitmap scaled =
                scaleToMaximumSide(
                        source,
                        OCR_MAXIMUM_SIDE
                );

        Bitmap output =
                Bitmap.createBitmap(
                        scaled.getWidth(),
                        scaled.getHeight(),
                        Bitmap.Config.ARGB_8888
                );

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(
                Paint.ANTI_ALIAS_FLAG
                        | Paint.FILTER_BITMAP_FLAG
        );

        ColorMatrix saturationMatrix =
                new ColorMatrix();

        saturationMatrix.setSaturation(0.0f);

        float translate =
                (-0.5f * CONTRAST + 0.5f)
                        * 255.0f;

        ColorMatrix contrastMatrix =
                new ColorMatrix(
                        new float[]{
                                CONTRAST, 0f, 0f, 0f, translate,
                                0f, CONTRAST, 0f, 0f, translate,
                                0f, 0f, CONTRAST, 0f, translate,
                                0f, 0f, 0f, 1f, 0f
                        }
                );

        saturationMatrix.postConcat(
                contrastMatrix
        );

        paint.setColorFilter(
                new ColorMatrixColorFilter(
                        saturationMatrix
                )
        );

        canvas.drawBitmap(
                scaled,
                0f,
                0f,
                paint
        );

        if (scaled != source
                && !scaled.isRecycled()) {
            scaled.recycle();
        }

        return output;
    }

    private Bitmap scaleToMaximumSide(
            Bitmap source,
            int maximumSide
    ) {
        int width = source.getWidth();
        int height = source.getHeight();

        int currentMaximum =
                Math.max(width, height);

        if (currentMaximum <= maximumSide) {
            return source;
        }

        float scale =
                maximumSide
                        / (float) currentMaximum;

        int targetWidth =
                Math.max(
                        1,
                        Math.round(width * scale)
                );

        int targetHeight =
                Math.max(
                        1,
                        Math.round(height * scale)
                );

        return Bitmap.createScaledBitmap(
                source,
                targetWidth,
                targetHeight,
                true
        );
    }

    private void recycleIfNecessary(Bitmap bitmap) {
        if (bitmap != null
                && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        executor.shutdownNow();
    }

    private static final class ImageMetadata {

        private final int width;
        private final int height;
        private final int rotation;

        private ImageMetadata(
                int width,
                int height,
                int rotation
        ) {
            this.width = width;
            this.height = height;
            this.rotation = rotation;
        }
    }
}
