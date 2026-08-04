package com.rndymi.almacentracker.data.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

        assertEquals(1, result.size());
        assertEquals(
                "M5 5008 - 3pcs",
                result.get(0).getReconstructedText()
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

    @Test
    public void reconstructDoesNotSplitAlphabeticCodeQualifier() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "ML",
                                30,
                                100,
                                60,
                                130
                        ),
                        element(
                                "4170",
                                75,
                                100,
                                130,
                                130
                        ),
                        element(
                                "DARK",
                                145,
                                100,
                                200,
                                130
                        ),
                        element(
                                "BLUE",
                                215,
                                100,
                                270,
                                130
                        ),
                        element(
                                "-",
                                285,
                                100,
                                295,
                                130
                        ),
                        element(
                                "4pqts",
                                310,
                                100,
                                375,
                                130
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        500
                );

        assertEquals(1, result.size());

        assertEquals(
                "ML 4170 DARK BLUE - 4pqts",
                result.get(0)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructSeparatesRealTwoColumnReferenceList() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "UTOPYA",
                                40,
                                20,
                                115,
                                48
                        ),

                        element(
                                "MA710-4pcs",
                                25,
                                90,
                                185,
                                120
                        ),
                        element(
                                "MA900-3pcs",
                                25,
                                135,
                                185,
                                165
                        ),
                        element(
                                "MA901-5pqts",
                                25,
                                180,
                                190,
                                210
                        ),
                        element(
                                "MA930-2pqts",
                                25,
                                225,
                                190,
                                255
                        ),
                        element(
                                "MR21231-4pqts",
                                25,
                                270,
                                195,
                                300
                        ),
                        element(
                                "MR21232-2pqts",
                                25,
                                315,
                                195,
                                345
                        ),

                        element(
                                "MR21502-2pqts",
                                225,
                                92,
                                390,
                                122
                        ),
                        element(
                                "MR21505-1pqts",
                                225,
                                137,
                                390,
                                167
                        ),
                        element(
                                "MR21111-2pqts",
                                225,
                                182,
                                390,
                                212
                        ),
                        element(
                                "MR21211-1pqt",
                                225,
                                227,
                                385,
                                257
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        420
                );

        assertEquals(11, result.size());

        assertEquals(
                "UTOPYA",
                result.get(0)
                        .getReconstructedText()
        );

        assertEquals(
                "MA710-4pcs",
                result.get(1)
                        .getReconstructedText()
        );

        assertEquals(
                "MA900-3pcs",
                result.get(2)
                        .getReconstructedText()
        );

        assertEquals(
                "MA901-5pqts",
                result.get(3)
                        .getReconstructedText()
        );

        assertEquals(
                "MA930-2pqts",
                result.get(4)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21231-4pqts",
                result.get(5)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21232-2pqts",
                result.get(6)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21502-2pqts",
                result.get(7)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21505-1pqts",
                result.get(8)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21111-2pqts",
                result.get(9)
                        .getReconstructedText()
        );

        assertEquals(
                "MR21211-1pqt",
                result.get(10)
                        .getReconstructedText()
        );
    }

    @Test
    public void reconstructReadsThreeColumnsWithoutMixingSeparateBoxes() {
        List<RecognizedTextElement> resultElements =
                Arrays.asList(
                        element("C2", 650, 170, 760, 200),
                        element("A1", 40, 100, 150, 130),
                        element("B2", 345, 170, 455, 200),
                        element("C1", 645, 100, 755, 130),
                        element("A2", 45, 170, 155, 200),
                        element("B1", 340, 100, 450, 130)
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(resultElements, 900);

        assertReconstructedTexts(
                result,
                "A1", "A2", "B1", "B2", "C1", "C2"
        );
    }

    @Test
    public void reconstructReadsFourColumnsTopToBottom() {
        List<RecognizedTextElement> resultElements =
                Arrays.asList(
                        element("D2", 720, 170, 810, 200),
                        element("B1", 260, 100, 350, 130),
                        element("A2", 35, 170, 125, 200),
                        element("C1", 490, 100, 580, 130),
                        element("D1", 715, 100, 805, 130),
                        element("A1", 30, 100, 120, 130),
                        element("C2", 495, 170, 585, 200),
                        element("B2", 265, 170, 355, 200)
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(resultElements, 900);

        assertReconstructedTexts(
                result,
                "A1", "A2", "B1", "B2",
                "C1", "C2", "D1", "D2"
        );
    }

    @Test
    public void reconstructSplitsSingleMergedRegionWithoutCorrectingCharacters() {
        RecognizedTextElement merged =
                element("mR22139mA98王", 30, 100, 360, 130);

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(merged),
                        900
                );

        assertReconstructedTexts(result, "mR22139", "mA98王");

        for (RecognizedTextLine line : result) {
            assertEquals(1, line.getElements().size());
            assertEquals(merged, line.getElements().get(0));
        }
    }

    @Test
    public void reconstructSplitsObservedFusionsIntoFourColumnOrder() {
        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(
                                element("MR21855", 800, 162, 930, 192),
                                element("AKGPORTOmA988", 20, 20, 440, 55),
                                element("MA1319", 550, 102, 670, 132),
                                element("MR22S47mA87", 20, 100, 440, 130),
                                element("MD803", 550, 162, 660, 192),
                                element("MR9280", 800, 22, 920, 52),
                                element("mR22139mA98王", 20, 160, 440, 190),
                                element("MA1318", 550, 22, 670, 52),
                                element("MR218S4", 800, 102, 930, 132)
                        ),
                        1000
                );

        assertReconstructedTexts(
                result,
                "AKGPORTO", "MR22S47", "mR22139",
                "mA988", "mA87", "mA98王",
                "MA1318", "MA1319", "MD803",
                "MR9280", "MR218S4", "MR21855"
        );
    }

    @Test
    public void reconstructDoesNotSplitSingleReferenceWithSuffixAndQuantity() {
        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(
                                element(
                                        "MR21387X40p",
                                        30,
                                        100,
                                        260,
                                        130
                                )
                        ),
                        900
                );

        assertReconstructedTexts(result, "MR21387X40p");
    }

    @Test
    public void reconstructKeepsSeparatedOcrFragmentsInSameReferenceLine() {
        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(
                                element("mR2138", 30, 100, 150, 130),
                                element("fX4p", 230, 102, 300, 132),
                                element("mR2", 30, 170, 90, 200),
                                element("Ol06", 180, 172, 260, 202),
                                element("mR2", 30, 240, 90, 270),
                                element("lS7o", 180, 242, 260, 272)
                        ),
                        1000
                );

        assertReconstructedTexts(
                result,
                "mR2138 fX4p",
                "mR2 Ol06",
                "mR2 lS7o"
        );
    }

    @Test
    public void reconstructDoesNotAttachPartialReferenceToPreviousColumn() {
        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(
                                element("MR10001", 30, 100, 140, 130),
                                element("MA20001", 280, 100, 390, 130),
                                element("MD30001", 530, 100, 640, 130),
                                element("MR40001", 780, 100, 890, 130),
                                element("MR10002", 35, 170, 145, 200),
                                element("MA20002", 285, 170, 395, 200),
                                element("MD30002", 535, 170, 645, 200),
                                element("MR40002", 785, 170, 895, 200),
                                element("MR10003", 40, 240, 150, 270),
                                element("MA20003", 290, 240, 400, 270),
                                element("mR2", 540, 240, 600, 270),
                                element("Ol06", 680, 242, 760, 272),
                                element("MR40003", 790, 240, 900, 270)
                        ),
                        1000
                );

        assertReconstructedTexts(
                result,
                "MR10001", "MR10002", "MR10003",
                "MA20001", "MA20002", "MA20003",
                "MD30001", "MD30002", "mR2 Ol06",
                "MR40001", "MR40002", "MR40003"
        );
    }

    @Test
    public void reconstructKeepsObservedReferencesInTheirGeometricColumns() {
        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        Arrays.asList(
                                element("MA871", 230, 102, 335, 132),
                                element("MR22139", 25, 160, 165, 190),
                                element("MA987", 230, 162, 335, 192),
                                element("MR22547", 25, 100, 165, 130)
                        ),
                        420
                );

        assertReconstructedTexts(
                result,
                "MR22547", "MR22139", "MA871", "MA987"
        );
    }

    @Test
    public void reconstructNeverKeepsTwoReferenceStartsInSameLine() {
        List<RecognizedTextElement> elements =
                Arrays.asList(
                        element(
                                "MA900-3pcs",
                                20,
                                100,
                                185,
                                130
                        ),
                        element(
                                "M221505-1pqts",
                                220,
                                102,
                                390,
                                132
                        ),
                        element(
                                "MA901-5pqts",
                                20,
                                155,
                                185,
                                185
                        ),
                        element(
                                "MR21111-2p9ts",
                                220,
                                157,
                                390,
                                187
                        )
                );

        List<RecognizedTextLine> result =
                reconstructor.reconstruct(
                        elements,
                        420
                );

        assertEquals(4, result.size());

        for (RecognizedTextLine line : result) {
            String text =
                    line.getReconstructedText()
                            .toUpperCase();

            boolean containsLeftAndRightReference =
                    (
                            text.contains("MA900")
                                    && text.contains("M221505")
                    )
                            || (
                            text.contains("MA901")
                                    && text.contains("MR21111")
                    );

            assertFalse(
                    containsLeftAndRightReference
            );
        }
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

    private void assertReconstructedTexts(
            List<RecognizedTextLine> lines,
            String... expected
    ) {
        assertEquals(expected.length, lines.size());

        for (int index = 0; index < expected.length; index++) {
            assertEquals(
                    expected[index],
                    lines.get(index).getReconstructedText()
            );
            assertEquals(index, lines.get(index).getIndex());
        }
    }
}
