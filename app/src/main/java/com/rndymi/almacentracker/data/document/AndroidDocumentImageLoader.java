package com.rndymi.almacentracker.data.document;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.rndymi.almacentracker.core.document.DocumentImageLoader;
import com.rndymi.almacentracker.core.document.DocumentImageRotation;

import java.io.InputStream;
import java.util.Objects;

public final class AndroidDocumentImageLoader
        implements DocumentImageLoader<Bitmap> {

    private final ContentResolver contentResolver;

    public AndroidDocumentImageLoader(
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
    public boolean canOpen(String imageUri) {
        try (
                InputStream inputStream =
                        contentResolver.openInputStream(
                                Uri.parse(imageUri)
                        )
        ) {
            return inputStream != null;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public Bitmap loadPreview(
            String imageUri,
            int targetSize,
            int manualRotationDegrees
    ) {
        Objects.requireNonNull(
                imageUri,
                "imageUri"
        );

        if (targetSize <= 0) {
            throw new IllegalArgumentException(
                    "targetSize must be greater than zero"
            );
        }

        int normalizedManualRotation =
                DocumentImageRotation.normalize(
                        manualRotationDegrees
                );

        Uri uri = Uri.parse(imageUri);

        Bitmap decoded = null;
        Bitmap rotated = null;

        try {
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
            }

            if (bounds.outWidth <= 0
                    || bounds.outHeight <= 0) {
                return null;
            }

            BitmapFactory.Options options =
                    new BitmapFactory.Options();

            options.inSampleSize =
                    calculateSampleSize(
                            bounds.outWidth,
                            bounds.outHeight,
                            targetSize
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

                decoded =
                        BitmapFactory.decodeStream(
                                stream,
                                null,
                                options
                        );
            }

            if (decoded == null) {
                return null;
            }

            int effectiveRotation =
                    DocumentImageRotation.combine(
                            readExifRotation(uri),
                            normalizedManualRotation
                    );

            rotated =
                    rotateBitmap(
                            decoded,
                            effectiveRotation
                    );

            if (rotated != decoded
                    && !decoded.isRecycled()) {
                decoded.recycle();
            }

            return rotated;
        } catch (
                Exception
                | OutOfMemoryError error
        ) {
            recycleIfNecessary(rotated);
            recycleIfNecessary(decoded);
            return null;
        }
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

                default:
                    return 0;
            }
        } catch (Exception exception) {
            return 0;
        }
    }

    private Bitmap rotateBitmap(
            Bitmap source,
            int rotation
    ) {
        int normalizedRotation =
                DocumentImageRotation.normalize(rotation);

        if (normalizedRotation == 0) {
            return source;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(normalizedRotation);

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

    private int calculateSampleSize(
            int width,
            int height,
            int targetSize
    ) {
        int sampleSize = 1;

        while (
                Math.max(
                        width / sampleSize,
                        height / sampleSize
                ) > targetSize * 2
        ) {
            sampleSize *= 2;
        }

        return Math.max(1, sampleSize);
    }

    private void recycleIfNecessary(Bitmap bitmap) {
        if (bitmap != null
                && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
}