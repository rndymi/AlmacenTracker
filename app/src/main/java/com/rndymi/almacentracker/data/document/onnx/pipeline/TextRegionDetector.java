package com.rndymi.almacentracker.data.document.onnx.pipeline;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.data.document.onnx.detection.TextDetectionException;
import com.rndymi.almacentracker.data.document.onnx.model.TextDetectionResult;

public interface TextRegionDetector {

    TextDetectionResult detect(
            Bitmap bitmap
    ) throws TextDetectionException;
}
