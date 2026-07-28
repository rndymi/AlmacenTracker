package com.rndymi.almacentracker.data.document;

import android.graphics.Rect;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.rndymi.almacentracker.core.document.DocumentImage;
import com.rndymi.almacentracker.core.document.DocumentImageSource;
import com.rndymi.almacentracker.core.document.DocumentRecognitionCallback;
import com.rndymi.almacentracker.core.document.DocumentTextRecognizer;
import com.rndymi.almacentracker.core.document.RecognizedDocument;
import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MlKitDocumentTextRecognizer
        implements DocumentTextRecognizer {

    private final TextRecognizer textRecognizer;

    private final DocumentLineReconstructor
            lineReconstructor;

    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public MlKitDocumentTextRecognizer() {
        this(new DocumentLineReconstructor());
    }

    public MlKitDocumentTextRecognizer(
            DocumentLineReconstructor lineReconstructor
    ) {
        this.lineReconstructor =
                Objects.requireNonNull(
                        lineReconstructor,
                        "lineReconstructor"
                );

        textRecognizer =
                TextRecognition.getClient(
                        TextRecognizerOptions.DEFAULT_OPTIONS
                );
    }

    @Override
    public void recognize(
            DocumentImage documentImage,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    ) {
        Objects.requireNonNull(
                documentImage,
                "documentImage"
        );
        Objects.requireNonNull(
                sourceType,
                "sourceType"
        );
        Objects.requireNonNull(
                callback,
                "callback"
        );

        if (!(documentImage
                instanceof AndroidDocumentImage)) {
            documentImage.close();
            callback.onRecognitionError();
            return;
        }

        AndroidDocumentImage androidDocumentImage =
                (AndroidDocumentImage) documentImage;

        if (closed.get()
                || androidDocumentImage.isClosed()) {
            androidDocumentImage.close();
            callback.onRecognitionError();
            return;
        }

        InputImage inputImage;

        try {
            inputImage =
                    InputImage.fromBitmap(
                            androidDocumentImage
                                    .getRecognitionBitmap(),
                            0
                    );
        } catch (
                IllegalArgumentException
                | IllegalStateException exception
        ) {
            androidDocumentImage.close();
            callback.onImageOpenError();
            return;
        }

        textRecognizer
                .process(inputImage)
                .addOnSuccessListener(
                        text -> {
                            try {
                                callback.onSuccess(
                                        mapDocument(
                                                text,
                                                sourceType,
                                                androidDocumentImage
                                                        .getRecognitionBitmap()
                                                        .getWidth()
                                        )
                                );
                            } finally {
                                androidDocumentImage.close();
                            }
                        }
                )
                .addOnFailureListener(
                        exception -> {
                            androidDocumentImage.close();
                            callback.onRecognitionError();
                        }
                );
    }

    private RecognizedDocument mapDocument(
            Text recognizedText,
            DocumentImageSource sourceType,
            int documentWidth
    ) {
        List<RecognizedTextElement> elements =
                new ArrayList<>();

        List<RecognizedTextLine> fallbackLines =
                new ArrayList<>();

        int fallbackIndex = 0;

        for (Text.TextBlock block
                : recognizedText.getTextBlocks()) {

            for (Text.Line line : block.getLines()) {
                String rawLine =
                        safeText(line.getText());

                Rect lineBox =
                        line.getBoundingBox();

                if (!rawLine.trim().isEmpty()) {
                    fallbackLines.add(
                            createFallbackLine(
                                    fallbackIndex++,
                                    rawLine,
                                    lineBox
                            )
                    );
                }

                for (Text.Element element
                        : line.getElements()) {
                    Rect elementBox =
                            element.getBoundingBox();

                    String elementText =
                            safeText(
                                    element.getText()
                            );

                    if (elementBox == null
                            || elementText
                            .trim()
                            .isEmpty()) {
                        continue;
                    }

                    elements.add(
                            new RecognizedTextElement(
                                    elementText,
                                    elementBox.left,
                                    elementBox.top,
                                    elementBox.right,
                                    elementBox.bottom
                            )
                    );
                }
            }
        }

        List<RecognizedTextLine> reconstructed =
                lineReconstructor.reconstruct(
                        elements,
                        documentWidth
                );

        List<RecognizedTextLine> finalLines =
                reconstructed.isEmpty()
                        ? fallbackLines
                        : reconstructed;

        return new RecognizedDocument(
                sourceType,
                finalLines,
                System.currentTimeMillis()
        );
    }

    private RecognizedTextLine createFallbackLine(
            int index,
            String rawText,
            Rect boundingBox
    ) {
        if (boundingBox == null) {
            return new RecognizedTextLine(
                    index,
                    rawText
            );
        }

        return new RecognizedTextLine(
                index,
                rawText,
                boundingBox.left,
                boundingBox.top,
                boundingBox.right,
                boundingBox.bottom
        );
    }

    private String safeText(String text) {
        return text == null
                ? ""
                : text.trim();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        textRecognizer.close();
    }
}
