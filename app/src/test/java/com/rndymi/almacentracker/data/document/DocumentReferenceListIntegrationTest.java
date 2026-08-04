package com.rndymi.almacentracker.data.document;

import static org.junit.Assert.assertEquals;

import com.rndymi.almacentracker.core.document.RecognizedTextElement;
import com.rndymi.almacentracker.core.document.RecognizedTextLine;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceMatch;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DocumentReferenceListIntegrationTest {

    private final DocumentLineReconstructor reconstructor =
            new DocumentLineReconstructor();

    private final WarehouseReferenceParser parser =
            new WarehouseReferenceParser();

    @Test
    public void twoColumnDocumentProducesTenReferenceCandidates() {
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

        List<RecognizedTextLine> lines =
                reconstructor.reconstruct(
                        elements,
                        420
                );

        List<WarehouseReferenceMatch> matches =
                new ArrayList<>();

        for (RecognizedTextLine line : lines) {
            matches.addAll(
                    parser.parseOcrLine(
                            line.getIndex(),
                            line.getReconstructedText(),
                            Collections.emptyList()
                    )
            );
        }

        assertEquals(11, lines.size());
        assertEquals(10, matches.size());

        assertEquals(
                "MA 710",
                matches.get(0)
                        .getReference()
                        .displayValue()
        );

        assertEquals(
                "MR 21232",
                matches.get(5)
                        .getReference()
                        .displayValue()
        );

        assertEquals(
                "MR 21502",
                matches.get(6)
                        .getReference()
                        .displayValue()
        );

        assertEquals(
                "MR 21211",
                matches.get(9)
                        .getReference()
                        .displayValue()
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
