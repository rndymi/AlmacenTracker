package com.rndymi.almacentracker.data.document.onnx;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PaddleOcrDictionary {

    private final List<String> characters;

    private PaddleOcrDictionary(
            List<String> characters
    ) {
        this.characters =
                Collections.unmodifiableList(
                        new ArrayList<>(characters)
                );
    }

    public static PaddleOcrDictionary fromUtf8(
            byte[] bytes
    ) throws IOException {
        Objects.requireNonNull(bytes, "bytes");

        List<String> characters =
                new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new ByteArrayInputStream(
                                             bytes
                                     ),
                                     StandardCharsets.UTF_8
                             )
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {
                String character =
                        removeTrailingCarriageReturn(line);

                if (character.isEmpty()) {
                    throw new IOException(
                            "Dictionary contains an empty entry"
                    );
                }

                characters.add(character);
            }
        }

        if (characters.isEmpty()) {
            throw new IOException(
                    "Recognition dictionary is empty"
            );
        }

        return new PaddleOcrDictionary(characters);
    }

    public int size() {
        return characters.size();
    }

    public String get(
            int index
    ) {
        if (index < 0 || index >= characters.size()) {
            throw new IndexOutOfBoundsException(
                    "Dictionary index out of range: "
                            + index
            );
        }

        return characters.get(index);
    }

    public List<String> asList() {
        return characters;
    }

    private static String removeTrailingCarriageReturn(
            String value
    ) {
        if (value.endsWith("\r")) {
            return value.substring(
                    0,
                    value.length() - 1
            );
        }

        return value;
    }
}
