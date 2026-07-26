package com.rndymi.almacentracker.data.document;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Size;

import com.rndymi.almacentracker.core.document.DocumentImageLoader;

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
            int targetSize
    ) {
        Uri uri = Uri.parse(imageUri);

        if (Build.VERSION.SDK_INT >= 29) {
            try {
                return contentResolver.loadThumbnail(
                        uri,
                        new Size(targetSize, targetSize),
                        null
                );
            } catch (Exception ignored) {
                // Fall back when a provider has no thumbnail support.
            }
        }

        try {
            BitmapFactory.Options bounds =
                    new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;

            try (
                    InputStream stream =
                            contentResolver.openInputStream(uri)
            ) {
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

            try (
                    InputStream stream =
                            contentResolver.openInputStream(uri)
            ) {
                return BitmapFactory.decodeStream(
                        stream,
                        null,
                        options
                );
            }
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
            int targetSize
    ) {
        int sampleSize = 1;

        while (width / sampleSize > targetSize
                || height / sampleSize > targetSize) {
            sampleSize *= 2;
        }

        return Math.max(1, sampleSize);
    }
}
