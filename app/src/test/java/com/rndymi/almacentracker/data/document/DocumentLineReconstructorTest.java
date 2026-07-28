package com.rndymi.almacentracker.data.document;

import static org.junit.Assert.assertEquals;

import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class DocumentLineReconstructorTest {

    private DocumentLineReconstructor reconstructor;

    @Before
    public void setUp() {
        reconstructor =
                new DocumentLineReconstructor();
    }

    @Test
    public void reconstructJoinsElementsPlacedOnSameRow() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MR",
                                10,
                                10,
                                40,
                                35
                        ),
                        element(
                                "21570",
                                55,
                                11,
                                130,
                                36
                        ),
                        element(
                                "-",
                                145,
                                12,
                                153,
                                35
                        ),
                        element(
                                "5pcs",
                                165,
                                10,
                                220,
                                36
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(elements);

        assertEquals(1, result.size());
        assertEquals(
                "MR 21570 - 5pcs",
                result.get(0)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructOrdersElementsByHorizontalPosition() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "5008",
                                60,
                                20,
                                120,
                                45
                        ),
                        element(
                                "M5",
                                10,
                                20,
                                40,
                                45
                        ),
                        element(
                                "3pcs",
                                160,
                                20,
                                215,
                                45
                        ),
                        element(
                                "-",
                                140,
                                20,
                                148,
                                45
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(elements);

        assertEquals(
                "M5 5008 - 3pcs",
                result.get(0)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructKeepsDifferentRowsSeparated() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MR",
                                10,
                                10,
                                40,
                                35
                        ),
                        element(
                                "21570",
                                55,
                                10,
                                130,
                                35
                        ),
                        element(
                                "ML",
                                10,
                                80,
                                40,
                                105
                        ),
                        element(
                                "3723",
                                55,
                                80,
                                120,
                                105
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(elements);

        assertEquals(2, result.size());

        assertEquals(
                "MR 21570",
                result.get(0)
                        .getReconstructedText()
        );

        assertEquals(
                "ML 3723",
                result.get(1)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructOrdersRowsVertically() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "ML",
                                10,
                                90,
                                40,
                                115
                        ),
                        element(
                                "3723",
                                50,
                                90,
                                115,
                                115
                        ),
                        element(
                                "Elena",
                                20,
                                10,
                                90,
                                40
                        ),
                        element(
                                "MR",
                                10,
                                55,
                                40,
                                80
                        ),
                        element(
                                "21570",
                                50,
                                55,
                                125,
                                80
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(elements);

        assertEquals(3, result.size());

        assertEquals(
                "Elena",
                result.get(0)
                        .getReconstructedText()
        );

        assertEquals(
                "MR 21570",
                result.get(1)
                        .getReconstructedText()
        );

        assertEquals(
                "ML 3723",
                result.get(2)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructIgnoresEmptyElements() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "",
                                10,
                                10,
                                20,
                                30
                        ),
                        element(
                                "MR",
                                30,
                                10,
                                60,
                                30
                        ),
                        element(
                                "21570",
                                70,
                                10,
                                140,
                                30
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(elements);

        assertEquals(1, result.size());

        assertEquals(
                "MR 21570",
                result.get(0)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructSeparatesReferencesFromTwoColumns() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MA",
                                40,
                                100,
                                80,
                                130
                        ),
                        element(
                                "710",
                                95,
                                100,
                                150,
                                130
                        ),
                        element(
                                "-",
                                165,
                                100,
                                175,
                                130
                        ),
                        element(
                                "4pcs",
                                190,
                                100,
                                250,
                                130
                        ),
                        element(
                                "MR",
                                650,
                                100,
                                700,
                                130
                        ),
                        element(
                                "21502",
                                720,
                                100,
                                810,
                                130
                        ),
                        element(
                                "-",
                                825,
                                100,
                                835,
                                130
                        ),
                        element(
                                "2pqts",
                                850,
                                100,
                                930,
                                130
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        1000
                );

        assertEquals(2, result.size());

        assertEquals(
                "MA 710 - 4pcs",
                result.get(0)
                        .getReconstructedText()
        );

        assertEquals(
                "MR 21502 - 2pqts",
                result.get(1)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructReadsLeftColumnBeforeRightColumn() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MA",
                                40,
                                100,
                                80,
                                130
                        ),
                        element(
                                "710",
                                95,
                                100,
                                150,
                                130
                        ),
                        element(
                                "MA",
                                40,
                                170,
                                80,
                                200
                        ),
                        element(
                                "900",
                                95,
                                170,
                                150,
                                200
                        ),
                        element(
                                "MR",
                                650,
                                105,
                                700,
                                135
                        ),
                        element(
                                "21502",
                                720,
                                105,
                                810,
                                135
                        ),
                        element(
                                "MR",
                                650,
                                175,
                                700,
                                205
                        ),
                        element(
                                "21505",
                                720,
                                175,
                                810,
                                205
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        1000
                );

        assertEquals(4, result.size());

        assertEquals(
                "MA 710",
                result.get(0)
                        .getReconstructedText()
        );

        assertEquals(
                "MA 900",
                result.get(1)
                        .getReconstructedText()
        );

        assertEquals(
                "MR 21502",
                result.get(2)
                        .getReconstructedText()
        );

        assertEquals(
                "MR 21505",
                result.get(3)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructDoesNotSplitNormalSingleColumnRow() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MR",
                                40,
                                100,
                                80,
                                130
                        ),
                        element(
                                "21231",
                                100,
                                100,
                                190,
                                130
                        ),
                        element(
                                "-",
                                210,
                                100,
                                220,
                                130
                        ),
                        element(
                                "4pqts",
                                240,
                                100,
                                320,
                                130
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        1000
                );

        assertEquals(1, result.size());

        assertEquals(
                "MR 21231 - 4pqts",
                result.get(0)
                        .getReconstructedText()
        );
    }

    private RecognizedTextElement element(
            String text,
            int left,
            int top,
            int right,
            int bottom
    ) {
        return new RecognizedTextElement(
                text,
                left,
                top,
                right,
                bottom
        );
    }
}