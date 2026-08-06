package com.rndymi.almacentracker.evaluation.io;

import com.rndymi.almacentracker.evaluation.model.OcrEvaluationCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OcrEvaluationCorpus {

    private final int formatVersion;
    private final String corpusVersion;
    private final List<OcrEvaluationCase> cases;

    public OcrEvaluationCorpus(
            int formatVersion,
            String corpusVersion,
            List<OcrEvaluationCase> cases
    ) {
        if (formatVersion <= 0) {
            throw new IllegalArgumentException(
                    "formatVersion must be positive"
            );
        }

        Objects.requireNonNull(
                corpusVersion,
                "corpusVersion"
        );

        String normalizedVersion =
                corpusVersion.trim();

        if (normalizedVersion.isEmpty()) {
            throw new IllegalArgumentException(
                    "corpusVersion cannot be blank"
            );
        }

        Objects.requireNonNull(cases, "cases");

        List<OcrEvaluationCase> copiedCases =
                new ArrayList<>(cases.size());

        for (OcrEvaluationCase evaluationCase :
                cases) {
            copiedCases.add(
                    Objects.requireNonNull(
                            evaluationCase,
                            "cases cannot contain null"
                    )
            );
        }

        this.formatVersion = formatVersion;
        this.corpusVersion = normalizedVersion;
        this.cases = Collections.unmodifiableList(
                copiedCases
        );
    }

    public int getFormatVersion() {
        return formatVersion;
    }

    public String getCorpusVersion() {
        return corpusVersion;
    }

    public List<OcrEvaluationCase> getCases() {
        return cases;
    }
}
