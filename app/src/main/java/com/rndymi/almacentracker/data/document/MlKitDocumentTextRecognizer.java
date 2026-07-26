package com.rndymi.almacentracker.data.document;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MlKitDocumentTextRecognizer
        implements DocumentTextRecognizer {

    private final Context applicationContext;
    private final TextRecognizer textRecognizer;
    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public MlKitDocumentTextRecognizer(
            Context context
    ) {
        applicationContext =
                Objects.requireNonNull(
                        context,
                        "context"
                ).getApplicationContext();

        textRecognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );
    }

    @Override
    public void recognize(
            String imageUri,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    ) {
        Objects.requireNonNull(
                imageUri,
                "imageUri"
        );
        Objects.requireNonNull(
                sourceType,
                "sourceType"
        );
        Objects.requireNonNull(
                callback,
                "callback"
        );

        if (closed.get()) {
            callback.onRecognitionError();
            return;
        }

        InputImage inputImage;

        try {
            inputImage =
                    InputImage.fromFilePath(
                            applicationContext,
                            Uri.parse(imageUri)
                    );
        } catch (
                IOException
                | IllegalArgumentException
                | SecurityException exception
        ) {
            callback.onImageOpenError();
            return;
        }

        textRecognizer
                .process(inputImage)
                .addOnSuccessListener(
                        text -> callback.onSuccess(
                                mapDocument(
                                        text,
                                        sourceType
                                )
                        )
                )
                .addOnFailureListener(
                        exception ->
                                callback.onRecognitionError()
                );
    }

    @NonNull
    private RecognizedDocument mapDocument(
            Text recognizedText,
            DocumentImageSource sourceType
    ) {
        List<RecognizedTextLine> lines =
                new ArrayList<>();

        int index = 0;

        for (Text.TextBlock block
                : recognizedText.getTextBlocks()) {

            for (Text.Line line : block.getLines()) {
                String rawText = line.getText();

                if (rawText == null) {
                    rawText = "";
                }

                Rect boundingBox =
                        line.getBoundingBox();

                if (boundingBox == null) {
                    lines.add(
                            new RecognizedTextLine(
                                    index,
                                    rawText
                            )
                    );
                } else {
                    lines.add(
                            new RecognizedTextLine(
                                    index,
                                    rawText,
                                    boundingBox.left,
                                    boundingBox.top,
                                    boundingBox.right,
                                    boundingBox.bottom
                            )
                    );
                }

                index++;
            }
        }

        return new RecognizedDocument(
                sourceType,
                lines,
                System.currentTimeMillis()
        );
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        textRecognizer.close();
    }
}