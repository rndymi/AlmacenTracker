package com.rndymi.almacentracker.data.document.onnx;

import static androidx.test.core.app.ApplicationProvider
        .getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class PaddleOcrRuntimeInstrumentedTest {

    @Test
    public void bundledResources_createAndCloseBothSessions()
            throws OrtException {
        Context context = getApplicationContext();

        PaddleOcrRuntimeInitializer initializer =
                new PaddleOcrRuntimeInitializer(
                        OrtEnvironment.getEnvironment(),
                        new OnnxModelAssetLoader(
                                context.getAssets()
                        ),
                        PaddleOcrModelConfiguration.bundled(),
                        new PaddleOcrSessionMetadataValidator()
                );

        PaddleOcrInitializationResult result =
                initializer.initialize();

        assertTrue(
                "Initialization failed with: "
                        + result.getError(),
                result.isReady()
        );

        PaddleOcrSessionBundle sessions =
                result.getSessionBundle();

        assertNotNull(sessions);
        assertNotNull(
                sessions.getDetectorSession()
        );
        assertNotNull(
                sessions.getRecognizerSession()
        );
        assertFalse(
                sessions.getDetectorSession()
                        .getInputInfo()
                        .isEmpty()
        );
        assertFalse(
                sessions.getDetectorSession()
                        .getOutputInfo()
                        .isEmpty()
        );
        assertFalse(
                sessions.getRecognizerSession()
                        .getInputInfo()
                        .isEmpty()
        );
        assertFalse(
                sessions.getRecognizerSession()
                        .getOutputInfo()
                        .isEmpty()
        );
        assertTrue(
                sessions.getDictionary().size() > 0
        );

        sessions.close();
        sessions.close();

        assertTrue(sessions.isClosed());
    }

    @Test
    public void bundledResources_matchManifestHashes()
            throws Exception {
        Context context = getApplicationContext();

        PaddleOcrModelConfiguration configuration =
                PaddleOcrModelConfiguration.bundled();

        OnnxModelAssetLoader loader =
                new OnnxModelAssetLoader(
                        context.getAssets()
                );

        PaddleOcrModelManifest manifest =
                PaddleOcrModelManifest.from(
                        loader.loadProperties(
                                configuration
                                        .getManifestAssetPath()
                        )
                );

        byte[] detector =
                loader.loadRequiredBytes(
                        configuration
                                .getDetectorAssetPath()
                );
        byte[] recognizer =
                loader.loadRequiredBytes(
                        configuration
                                .getRecognizerAssetPath()
                );
        byte[] dictionary =
                loader.loadRequiredBytes(
                        configuration
                                .getDictionaryAssetPath()
                );

        assertEquals(
                manifest.getDetectorSha256(),
                loader.calculateSha256(detector)
        );
        assertEquals(
                manifest.getRecognizerSha256(),
                loader.calculateSha256(recognizer)
        );
        assertEquals(
                manifest.getDictionarySha256(),
                loader.calculateSha256(dictionary)
        );
    }
}
