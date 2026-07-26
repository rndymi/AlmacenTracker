package com.rndymi.almacentracker.core.document;

public interface DocumentImageLoader<T> {

    boolean canOpen(String imageUri);

    T loadPreview(
            String imageUri,
            int targetSize
    );
}
