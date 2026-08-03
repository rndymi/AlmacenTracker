package com.rndymi.almacentracker.data.document.onnx.recognition;

import com.rndymi.almacentracker.data.document.onnx.PaddleOcrDictionary;

import java.util.Objects;

public final class PaddleCtcDecoder {

    private final PaddleTextRecognizerConfiguration configuration;
    private final PaddleOcrTokenMapper tokenMapper;

    public PaddleCtcDecoder(
            PaddleTextRecognizerConfiguration configuration,
            PaddleOcrDictionary dictionary
    ) {
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration"
        );

        this.tokenMapper = new PaddleOcrTokenMapper(
                Objects.requireNonNull(
                        dictionary,
                        "dictionary"
                ),
                configuration.getBlankIndex(),
                configuration.getClassCount(),
                configuration.getAdditionalSpecialTokenCount()
        );
    }

    public CtcDecodingResult decode(
            float[][] logits
    ) throws TextRecognitionException {
        validateLogits(logits);

        StringBuilder text = new StringBuilder();

        int previousIndex = -1;
        double confidenceSum = 0.0d;
        int acceptedTokenCount = 0;

        for (float[] timeStep : logits) {
            ProbabilitySelection selection =
                    selectMaximumProbability(
                            timeStep
                    );

            int classIndex =
                    selection.getClassIndex();

            boolean repeated =
                    classIndex == previousIndex;

            previousIndex = classIndex;

            if (tokenMapper.isBlank(classIndex)
                    || repeated) {
                continue;
            }

            if (tokenMapper.isSpecialToken(classIndex)) {
                continue;
            }

            String token =
                    tokenMapper.map(classIndex);

            if (token.isEmpty()) {
                continue;
            }

            text.append(token);
            confidenceSum += selection.getProbability();
            acceptedTokenCount++;
        }

        float confidence =
                acceptedTokenCount == 0
                        ? 0.0f
                        : (float) (
                        confidenceSum
                        / acceptedTokenCount
                );

        return new CtcDecodingResult(
                text.toString(),
                confidence
        );
    }

    private ProbabilitySelection
    selectMaximumProbability(
            float[] logits
    ) throws TextRecognitionException {
        if (logits == null
                || logits.length
                != configuration.getClassCount()) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .CLASS_COUNT_MISMATCH,
                    "Recognizer time step has an invalid "
                            + "class count"
            );
        }

        float maximumLogit =
                Float.NEGATIVE_INFINITY;

        for (float logit : logits) {
            if (!Float.isFinite(logit)) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .DECODING_ERROR,
                        "Recognizer output contains a non-finite value"
                );
            }

            maximumLogit = Math.max(
                    maximumLogit,
                    logit
            );
        }

        double denominator = 0.0d;
        int bestIndex = -1;
        double bestExponent = -1.0d;

        for (int index = 0;
             index < logits.length;
             index++) {
            double exponent = Math.exp(
                    logits[index] - maximumLogit
            );

            denominator += exponent;

            if (exponent > bestExponent) {
                bestExponent = exponent;
                bestIndex = index;
            }
        }

        if (bestIndex < 0
                || denominator <= 0.0d
                || !Double.isFinite(denominator)) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .DECODING_ERROR,
                    "Unable to calculate recognition probabilities"
            );
        }

        float probability =
                (float) (
                        bestExponent / denominator
                );

        return new ProbabilitySelection(
                bestIndex,
                probability
        );
    }

    private void validateLogits(
            float[][] logits
    ) throws TextRecognitionException {
        if (logits == null) {
            throw new TextRecognitionException(
                    TextRecognitionException.Error
                            .OUTPUT_SHAPE_INCOMPATIBLE,
                    "Recognizer logits cannot be null"
            );
        }

        for (float[] timeStep : logits) {
            if (timeStep == null) {
                throw new TextRecognitionException(
                        TextRecognitionException.Error
                                .OUTPUT_SHAPE_INCOMPATIBLE,
                        "Recognizer contains a null time step"
                );
            }
        }
    }

    private static final class ProbabilitySelection {

        private final int classIndex;
        private final float probability;

        private ProbabilitySelection(
                int classIndex,
                float probability
        ) {
            this.classIndex = classIndex;
            this.probability = probability;
        }

        private int getClassIndex() {
            return classIndex;
        }

        private float getProbability() {
            return probability;
        }
    }
}
