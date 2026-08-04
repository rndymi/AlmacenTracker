package com.rndymi.almacentracker.data.document;

import static org.junit.Assert.assertEquals;

import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class DocumentColumnDetectorTest {

    private DocumentColumnDetector detector;

    @Before
    public void setUp() {
        detector =
                new DocumentColumnDetector();
    }

    @Test
    public void orderByColumnsReadsThreeColumnsFromLeftToRight() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                line(
                                        0,
                                        "MR 1210",
                                        40,
                                        100,
                                        180,
                                        130
                                ),
                                line(
                                        1,
                                        "MA 2300",
                                        350,
                                        105,
                                        490,
                                        135
                                ),
                                line(
                                        2,
                                        "ML 4170",
                                        670,
                                        110,
                                        810,
                                        140
                                ),
                                line(
                                        3,
                                        "MZ 1300",
                                        45,
                                        170,
                                        185,
                                        200
                                ),
                                line(
                                        4,
                                        "MD 2400",
                                        355,
                                        175,
                                        495,
                                        205
                                ),
                                line(
                                        5,
                                        "MI 5100",
                                        675,
                                        180,
                                        815,
                                        210
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "MR 1210",
                "MZ 1300",
                "MA 2300",
                "MD 2400",
                "ML 4170",
                "MI 5100"
        );
    }

    @Test
    public void orderByColumnsReadsFourColumnsWithoutFixedMaximum() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                line(
                                        0,
                                        "A1",
                                        30,
                                        100,
                                        120,
                                        130
                                ),
                                line(
                                        1,
                                        "B1",
                                        260,
                                        100,
                                        350,
                                        130
                                ),
                                line(
                                        2,
                                        "C1",
                                        490,
                                        100,
                                        580,
                                        130
                                ),
                                line(
                                        3,
                                        "D1",
                                        720,
                                        100,
                                        810,
                                        130
                                ),
                                line(
                                        4,
                                        "A2",
                                        35,
                                        170,
                                        125,
                                        200
                                ),
                                line(
                                        5,
                                        "B2",
                                        265,
                                        170,
                                        355,
                                        200
                                ),
                                line(
                                        6,
                                        "C2",
                                        495,
                                        170,
                                        585,
                                        200
                                ),
                                line(
                                        7,
                                        "D2",
                                        725,
                                        170,
                                        815,
                                        200
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "A1",
                "A2",
                "B1",
                "B2",
                "C1",
                "C2",
                "D1",
                "D2"
        );
    }

    @Test
    public void orderByColumnsKeepsGlobalTitleBeforeColumnBlock() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                line(
                                        0,
                                        "LISTA DE REPOSICION",
                                        80,
                                        20,
                                        820,
                                        60
                                ),
                                line(
                                        1,
                                        "A1",
                                        40,
                                        100,
                                        160,
                                        130
                                ),
                                line(
                                        2,
                                        "B1",
                                        390,
                                        100,
                                        510,
                                        130
                                ),
                                line(
                                        3,
                                        "A2",
                                        45,
                                        170,
                                        165,
                                        200
                                ),
                                line(
                                        4,
                                        "B2",
                                        395,
                                        170,
                                        515,
                                        200
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "LISTA DE REPOSICION",
                "A1",
                "A2",
                "B1",
                "B2"
        );
    }

    @Test
    public void orderByColumnsPreservesGlobalLineBetweenBlocks() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                line(
                                        0,
                                        "A1",
                                        40,
                                        100,
                                        160,
                                        130
                                ),
                                line(
                                        1,
                                        "B1",
                                        390,
                                        100,
                                        510,
                                        130
                                ),
                                line(
                                        2,
                                        "A2",
                                        45,
                                        170,
                                        165,
                                        200
                                ),
                                line(
                                        3,
                                        "B2",
                                        395,
                                        170,
                                        515,
                                        200
                                ),
                                line(
                                        4,
                                        "SEGUNDO BLOQUE",
                                        80,
                                        250,
                                        820,
                                        290
                                ),
                                line(
                                        5,
                                        "C1",
                                        40,
                                        340,
                                        160,
                                        370
                                ),
                                line(
                                        6,
                                        "D1",
                                        390,
                                        340,
                                        510,
                                        370
                                ),
                                line(
                                        7,
                                        "C2",
                                        45,
                                        410,
                                        165,
                                        440
                                ),
                                line(
                                        8,
                                        "D2",
                                        395,
                                        410,
                                        515,
                                        440
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "A1",
                "A2",
                "B1",
                "B2",
                "SEGUNDO BLOQUE",
                "C1",
                "C2",
                "D1",
                "D2"
        );
    }

    @Test
    public void orderByColumnsFallsBackWhenOneDisplacedLineHasNoRepeatedEvidence() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                line(
                                        0,
                                        "A1",
                                        40,
                                        100,
                                        160,
                                        130
                                ),
                                line(
                                        1,
                                        "A2",
                                        45,
                                        170,
                                        165,
                                        200
                                ),
                                line(
                                        2,
                                        "A3",
                                        50,
                                        240,
                                        170,
                                        270
                                ),
                                line(
                                        3,
                                        "AUXILIAR",
                                        650,
                                        310,
                                        800,
                                        340
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "A1",
                "A2",
                "A3",
                "AUXILIAR"
        );
    }

    @Test
    public void orderByColumnsKeepsStableVerticalOrderWhenBoundingBoxesAreMissing() {
        List<RecognizedTextLine> result =
                detector.orderByColumns(
                        Arrays.asList(
                                new RecognizedTextLine(
                                        2,
                                        "TERCERA"
                                ),
                                new RecognizedTextLine(
                                        0,
                                        "PRIMERA"
                                ),
                                new RecognizedTextLine(
                                        1,
                                        "SEGUNDA"
                                )
                        ),
                        900
                );

        assertTexts(
                result,
                "PRIMERA",
                "SEGUNDA",
                "TERCERA"
        );
    }

    private RecognizedTextLine line(
            int index,
            String text,
            int left,
            int top,
            int right,
            int bottom
    ) {
        return new RecognizedTextLine(
                index,
                text,
                left,
                top,
                right,
                bottom
        );
    }

    private void assertTexts(
            List<RecognizedTextLine> lines,
            String... expected
    ) {
        assertEquals(
                expected.length,
                lines.size()
        );

        for (int index = 0;
             index < expected.length;
             index++) {

            assertEquals(
                    expected[index],
                    lines.get(index)
                            .getDisplayText()
            );
        }
    }
}
