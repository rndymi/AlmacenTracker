package com.rndymi.almacentracker.data.document.onnx;

import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class OnnxModelAssetLoader {

    private static final int BUFFER_SIZE = 16 * 1024;

    private final AssetManager assetManager;

    public OnnxModelAssetLoader(
            AssetManager assetManager
    ) {
        this.assetManager =
                Objects.requireNonNull(
                        assetManager,
                        "assetManager"
                );
    }

    public byte[] loadRequiredBytes(
            String assetPath
    ) throws IOException {
        Objects.requireNonNull(
                assetPath,
                "assetPath"
        );

        try (InputStream inputStream =
                     assetManager.open(assetPath);
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            byte[] bytes = outputStream.toByteArray();

            if (bytes.length == 0) {
                throw new IOException(
                        "Required asset is empty"
                );
            }

            return bytes;
        }
    }

    public Properties loadProperties(
            String assetPath
    ) throws IOException {
        Objects.requireNonNull(
                assetPath,
                "assetPath"
        );

        Properties properties = new Properties();

        try (InputStream inputStream =
                     assetManager.open(assetPath)) {
            properties.load(inputStream);
        }

        if (properties.isEmpty()) {
            throw new IOException(
                    "Model manifest is empty"
            );
        }

        return properties;
    }

    public String calculateSha256(
            byte[] bytes
    ) {
        Objects.requireNonNull(bytes, "bytes");

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );
            byte[] hash = digest.digest(bytes);
            StringBuilder value =
                    new StringBuilder(hash.length * 2);

            for (byte current : hash) {
                value.append(
                        String.format(
                                Locale.ROOT,
                                "%02x",
                                current & 0xff
                        )
                );
            }

            return value.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    exception
            );
        }
    }

    public void verifySha256(
            byte[] bytes,
            String expectedSha256
    ) {
        String actualSha256 =
                calculateSha256(bytes);

        if (!actualSha256.equalsIgnoreCase(
                Objects.requireNonNull(
                        expectedSha256,
                        "expectedSha256"
                )
        )) {
            throw new IllegalArgumentException(
                    "Asset integrity verification failed"
            );
        }
    }
}
