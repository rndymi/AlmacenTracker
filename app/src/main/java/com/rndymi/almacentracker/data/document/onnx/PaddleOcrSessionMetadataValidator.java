package com.rndymi.almacentracker.data.document.onnx;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxJavaType;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;

import java.util.Map;
import java.util.Objects;

public final class PaddleOcrSessionMetadataValidator {

    public void validateDetector(
            OrtSession session,
            PaddleOcrModelManifest manifest
    ) {
        validateSession(
                session,
                manifest.getDetectorInputName(),
                manifest.getDetectorInputRank(),
                manifest.getDetectorOutputCount()
        );
    }

    public void validateRecognizer(
            OrtSession session,
            PaddleOcrModelManifest manifest,
            PaddleOcrDictionary dictionary
    ) {
        validateSession(
                session,
                manifest.getRecognizerInputName(),
                manifest.getRecognizerInputRank(),
                manifest.getRecognizerOutputCount()
        );

        int expectedDictionarySize =
                manifest.expectedDictionarySize();

        if (expectedDictionarySize <= 0) {
            throw new IllegalArgumentException(
                    "Recognizer token metadata is invalid"
            );
        }

        if (dictionary.size()
                != expectedDictionarySize) {
            throw new IllegalArgumentException(
                    "Recognition dictionary is incompatible"
            );
        }

        validateRecognizerOutputClassCount(
                session,
                manifest.getRecognizerClassCount()
        );
    }

    private void validateSession(
            OrtSession session,
            String expectedInputName,
            int expectedInputRank,
            int expectedOutputCount
    ) {
        Objects.requireNonNull(
                session,
                "session"
        );

        Map<String, NodeInfo> inputInfo =
                session.getInputInfo();

        NodeInfo inputNode =
                inputInfo.get(expectedInputName);

        if (inputNode == null) {
            throw new IllegalArgumentException(
                    "Expected model input is unavailable"
            );
        }

        if (!(inputNode.getInfo()
                instanceof TensorInfo)) {
            throw new IllegalArgumentException(
                    "Model input is not a tensor"
            );
        }

        TensorInfo inputTensor =
                (TensorInfo) inputNode.getInfo();

        if (inputTensor.type != OnnxJavaType.FLOAT) {
            throw new IllegalArgumentException(
                    "Model input must use FLOAT tensors"
            );
        }

        if (inputTensor.getShape().length
                != expectedInputRank) {
            throw new IllegalArgumentException(
                    "Model input rank is incompatible"
            );
        }

        if (session.getOutputInfo().size()
                != expectedOutputCount) {
            throw new IllegalArgumentException(
                    "Model output count is incompatible"
            );
        }
    }

    private void validateRecognizerOutputClassCount(
            OrtSession session,
            int expectedClassCount
    ) {
        if (session.getOutputInfo().isEmpty()) {
            throw new IllegalArgumentException(
                    "Recognizer has no output"
            );
        }

        NodeInfo outputNode =
                session.getOutputInfo()
                        .values()
                        .iterator()
                        .next();

        if (!(outputNode.getInfo()
                instanceof TensorInfo)) {
            throw new IllegalArgumentException(
                    "Recognizer output is not a tensor"
            );
        }

        TensorInfo outputTensor =
                (TensorInfo) outputNode.getInfo();

        if (outputTensor.type != OnnxJavaType.FLOAT) {
            throw new IllegalArgumentException(
                    "Recognizer output must use FLOAT tensors"
            );
        }

        long[] shape = outputTensor.getShape();

        if (shape.length < 2) {
            throw new IllegalArgumentException(
                    "Recognizer output rank is incompatible"
            );
        }

        long classDimension =
                shape[shape.length - 1];

        if (classDimension > 0
                && classDimension != expectedClassCount) {
            throw new IllegalArgumentException(
                    "Recognizer class count is incompatible"
            );
        }
    }
}
