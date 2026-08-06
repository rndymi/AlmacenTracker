package com.rndymi.almacentracker.evaluation.io;

import android.content.res.AssetManager;

import com.rndymi.almacentracker.evaluation.model.OcrEvaluationCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class OcrEvaluationCorpusLoader {

    private static final int SUPPORTED_FORMAT_VERSION = 1;

    private final AssetManager assetManager;

    public OcrEvaluationCorpusLoader(
            AssetManager assetManager
    ) {
        this.assetManager =
                Objects.requireNonNull(
                        assetManager,
                        "assetManager"
                );
    }

    public OcrEvaluationCorpus load(
            String manifestPath
    ) throws IOException, JSONException {
        String json = readTextAsset(
                requirePath(manifestPath)
        );

        JSONObject root = new JSONObject(json);

        int formatVersion =
                root.getInt("formatVersion");

        if (formatVersion
                != SUPPORTED_FORMAT_VERSION) {
            throw new JSONException(
                    "Unsupported corpus format version: "
                            + formatVersion
            );
        }

        String corpusVersion =
                root.getString("corpusVersion");

        JSONArray caseArray =
                root.getJSONArray("cases");

        List<OcrEvaluationCase> cases =
                new ArrayList<>(
                        caseArray.length()
                );

        Set<String> ids = new HashSet<>();

        for (int index = 0;
             index < caseArray.length();
             index++) {
            JSONObject caseObject =
                    caseArray.getJSONObject(index);

            OcrEvaluationCase evaluationCase =
                    parseCase(caseObject);

            if (!ids.add(evaluationCase.getId())) {
                throw new JSONException(
                        "Duplicated evaluation case id: "
                                + evaluationCase.getId()
                );
            }

            verifyAssetExists(
                    evaluationCase.getImagePath()
            );

            verifyAssetExists(
                    evaluationCase.getExpectedPath()
            );

            cases.add(evaluationCase);
        }

        return new OcrEvaluationCorpus(
                formatVersion,
                corpusVersion,
                cases
        );
    }

    private OcrEvaluationCase parseCase(
            JSONObject value
    ) throws JSONException {
        return new OcrEvaluationCase(
                value.getString("id"),
                value.getString("imagePath"),
                value.getString("expectedPath"),
                value.getString("description"),
                value.getString("sourceType"),
                value.getString("documentType"),
                value.getString("lighting"),
                value.getString("perspective"),
                value.getString("group"),
                value.getInt(
                        "expectedOrientationDegrees"
                ),
                value.getInt("columnCount"),
                value.getBoolean("handwritten"),
                value.getBoolean(
                        "publicRepositoryAllowed"
                ),
                value.getBoolean(
                        "containsPrivateData"
                ),
                value.getString("source"),
                value.getString("license")
        );
    }

    private void verifyAssetExists(
            String path
    ) throws IOException {
        try (InputStream ignored =
                     assetManager.open(path)) {
            // Opening is enough to validate the path.
        }
    }

    private String readTextAsset(
            String path
    ) throws IOException {
        try (InputStream inputStream =
                     assetManager.open(path);
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8 * 1024];

            int read;

            while ((read = inputStream.read(buffer))
                    != -1) {
                outputStream.write(
                        buffer,
                        0,
                        read
                );
            }

            return outputStream.toString(
                    StandardCharsets.UTF_8.name()
            );
        }
    }

    private String requirePath(
            String value
    ) {
        Objects.requireNonNull(value, "path");

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    "path cannot be blank"
            );
        }

        return trimmed;
    }
}
