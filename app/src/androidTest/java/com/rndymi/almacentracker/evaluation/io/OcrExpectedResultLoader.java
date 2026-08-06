package com.rndymi.almacentracker.evaluation.io;

import android.content.res.AssetManager;

import com.rndymi.almacentracker.evaluation.metrics.NormalizedBox;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedLine;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedReference;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedRegion;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class OcrExpectedResultLoader {

    private final AssetManager assetManager;

    public OcrExpectedResultLoader(
            AssetManager assetManager
    ) {
        this.assetManager =
                Objects.requireNonNull(
                        assetManager,
                        "assetManager"
                );
    }

    public OcrExpectedResult load(
            String path
    ) throws IOException, JSONException {
        JSONObject root =
                new JSONObject(
                        readTextAsset(path)
                );

        return new OcrExpectedResult(
                root.getString("caseId"),
                parseRegions(
                        root.getJSONArray(
                                "expectedRegions"
                        )
                ),
                parseLines(
                        root.getJSONArray(
                                "expectedLines"
                        )
                ),
                parseReferences(
                        root.getJSONArray(
                                "expectedReferences"
                        )
                ),
                optionalString(
                        root,
                        "expectedTitle"
                ),
                optionalString(
                        root,
                        "expectedBuyerOrStore"
                ),
                optionalString(
                        root,
                        "expectedGlobalDestination"
                )
        );
    }

    private List<OcrExpectedRegion> parseRegions(
            JSONArray values
    ) throws JSONException {
        List<OcrExpectedRegion> result =
                new ArrayList<>(values.length());

        for (int index = 0;
             index < values.length();
             index++) {
            JSONObject value =
                    values.getJSONObject(index);

            result.add(
                    new OcrExpectedRegion(
                            value.getString("id"),
                            value.getString("text"),
                            new NormalizedBox(
                                    value.getDouble("left"),
                                    value.getDouble("top"),
                                    value.getDouble("right"),
                                    value.getDouble("bottom")
                            ),
                            value.optBoolean(
                                    "optional",
                                    false
                            )
                    )
            );
        }

        return result;
    }

    private List<OcrExpectedLine> parseLines(
            JSONArray values
    ) throws JSONException {
        List<OcrExpectedLine> result =
                new ArrayList<>(values.length());

        for (int index = 0;
             index < values.length();
             index++) {
            JSONObject value =
                    values.getJSONObject(index);

            Integer columnIndex =
                    value.isNull("columnIndex")
                            ? null
                            : value.getInt(
                            "columnIndex"
                    );

            result.add(
                    new OcrExpectedLine(
                            value.getInt("lineIndex"),
                            value.getString("text"),
                            columnIndex,
                            value.optBoolean(
                                    "globalLine",
                                    false
                            )
                    )
            );
        }

        return result;
    }

    private List<OcrExpectedReference> parseReferences(
            JSONArray values
    ) throws JSONException {
        List<OcrExpectedReference> result =
                new ArrayList<>(values.length());

        for (int index = 0;
             index < values.length();
             index++) {
            JSONObject value =
                    values.getJSONObject(index);

            result.add(
                    new OcrExpectedReference(
                            value.getString("category"),
                            value.getString("code"),
                            value.getInt(
                                    "sourceLineIndex"
                            ),
                            value.getString(
                                    "expectedMatchStatus"
                            ),
                            optionalString(
                                    value,
                                    "expectedQuantity"
                            ),
                            optionalString(
                                    value,
                                    "expectedUnit"
                            ),
                            stringList(
                                    value.optJSONArray(
                                            "expectedDestinations"
                                    )
                            ),
                            optionalString(
                                    value,
                                    "observedOcrVariant"
                            ),
                            optionalString(
                                    value,
                                    "expectedSuggestion"
                            ),
                            value.optBoolean(
                                    "knownInRoom",
                                    false
                            )
                    )
            );
        }

        return result;
    }

    private List<String> stringList(
            JSONArray values
    ) throws JSONException {
        List<String> result =
                new ArrayList<>();

        if (values == null) {
            return result;
        }

        for (int index = 0;
             index < values.length();
             index++) {
            result.add(values.getString(index));
        }

        return result;
    }

    private String optionalString(
            JSONObject value,
            String key
    ) throws JSONException {
        if (!value.has(key)
                || value.isNull(key)) {
            return null;
        }

        String result = value.getString(key);

        return result.trim().isEmpty()
                ? null
                : result;
    }

    private String readTextAsset(
            String path
    ) throws IOException {
        Objects.requireNonNull(path, "path");

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
}
