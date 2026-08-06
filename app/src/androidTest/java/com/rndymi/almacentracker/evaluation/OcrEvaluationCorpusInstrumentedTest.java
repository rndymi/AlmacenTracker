package com.rndymi.almacentracker.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.rndymi.almacentracker.evaluation.io.OcrEvaluationCorpus;
import com.rndymi.almacentracker.evaluation.io.OcrEvaluationCorpusLoader;
import com.rndymi.almacentracker.evaluation.io.OcrExpectedResultLoader;
import com.rndymi.almacentracker.evaluation.model.OcrEvaluationCase;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedReference;
import com.rndymi.almacentracker.evaluation.model.OcrExpectedResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public final class OcrEvaluationCorpusInstrumentedTest {

    private OcrEvaluationCorpus corpus;
    private OcrExpectedResultLoader expectedLoader;

    @Before
    public void setUp() throws Exception {
        Context context =
                InstrumentationRegistry
                        .getInstrumentation()
                        .getContext();

        corpus =
                new OcrEvaluationCorpusLoader(
                        context.getAssets()
                ).load(
                        "ocr/evaluation/corpus_manifest.json"
                );

        expectedLoader =
                new OcrExpectedResultLoader(
                        context.getAssets()
                );
    }

    @Test
    public void corpusUsesSupportedVersion() {
        assertEquals(
                1,
                corpus.getFormatVersion()
        );

        assertEquals(
                "baseline-v1",
                corpus.getCorpusVersion()
        );
    }

    @Test
    public void publicCorpusContainsNoPrivateData() {
        for (OcrEvaluationCase evaluationCase :
                corpus.getCases()) {
            if (evaluationCase
                    .isPublicRepositoryAllowed()) {
                assertFalse(
                        "Public case contains private data: "
                                + evaluationCase.getId(),
                        evaluationCase
                                .containsPrivateData()
                );
            }

            assertNotNull(
                    evaluationCase.getSource()
            );

            assertNotNull(
                    evaluationCase.getLicense()
            );
        }
    }

    @Test
    public void expectedFilesMatchTheirCases()
            throws Exception {
        for (OcrEvaluationCase evaluationCase :
                corpus.getCases()) {
            OcrExpectedResult expected =
                    expectedLoader.load(
                            evaluationCase
                                    .getExpectedPath()
                    );

            assertEquals(
                    evaluationCase.getId(),
                    expected.getCaseId()
            );
        }
    }

    @Test
    public void containsRequiredHu39AndHu40References()
            throws Exception {
        Set<String> expectedIdentities =
                new HashSet<>();

        Set<String> observedVariants =
                new HashSet<>();

        for (OcrEvaluationCase evaluationCase :
                corpus.getCases()) {
            OcrExpectedResult expected =
                    expectedLoader.load(
                            evaluationCase
                                    .getExpectedPath()
                    );

            for (OcrExpectedReference reference :
                    expected.getReferences()) {
                expectedIdentities.add(
                        reference.getIdentity()
                );

                if (reference
                        .getObservedOcrVariant()
                        != null) {
                    observedVariants.add(
                            reference
                                    .getObservedOcrVariant()
                    );
                }
            }
        }

        assertTrue(
                expectedIdentities.contains(
                        "MR8665"
                )
        );

        assertTrue(
                expectedIdentities.contains(
                        "MR21854"
                )
        );

        assertTrue(
                expectedIdentities.contains(
                        "MA1201"
                )
        );

        assertTrue(
                expectedIdentities.contains(
                        "MR21571"
                )
        );

        assertTrue(
                observedVariants.contains(
                        "MK866S"
                )
        );

        assertTrue(
                observedVariants.contains(
                        "MA201"
                )
        );

        assertTrue(
                observedVariants.contains(
                        "MR215H"
                )
        );
    }

    @Test
    public void containsFourColumnHandwrittenCase() {
        boolean found = false;

        for (OcrEvaluationCase evaluationCase :
                corpus.getCases()) {
            if (evaluationCase.isHandwritten()
                    && evaluationCase
                    .getColumnCount() == 4) {
                found = true;
                break;
            }
        }

        assertTrue(
                "A four-column handwritten case is required",
                found
        );
    }
}
