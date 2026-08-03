package com.rndymi.almacentracker.data.document.onnx.pipeline;

import android.graphics.Bitmap;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextRegion;
import com.rndymi.almacentracker.data.document.onnx.model.TextRecognitionResult;
import com.rndymi.almacentracker.data.document.onnx.recognition.TextRecognitionException;

public interface TextRegionRecognizer {

    TextRecognitionResult recognize(
            Bitmap bitmap,
            DetectedTextRegion region
    ) throws TextRecognitionException;
}
