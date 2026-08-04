package com.rndymi.almacentracker.data.document;

import com.rndymi.almacentracker.core.document.RecognizedTextLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DocumentMergedLineSplitter {

    private static final Pattern OBSERVED_REFERENCE_START =
            Pattern.compile(
                    "(?iu)(?=[a-z]{2}[0-9(][a-z0-9()王]{1,})"
            );

    List<RecognizedTextLine> split(
            List<RecognizedTextLine> sourceLines
    ) {
        if (sourceLines == null
                || sourceLines.isEmpty()) {
            return Collections.emptyList();
        }

        List<RecognizedTextLine> result =
                new ArrayList<>();

        for (RecognizedTextLine line : sourceLines) {
            if (line == null) {
                continue;
            }

            result.addAll(
                    splitLine(line)
            );
        }

        return result;
    }

    private List<RecognizedTextLine> splitLine(
            RecognizedTextLine line
    ) {
        String text =
                line.getDisplayText();

        if (text == null
                || text.trim().isEmpty()
                || !line.hasBoundingBox()) {
            return Collections.singletonList(line);
        }

        List<Integer> starts =
                findReferenceStarts(text);

        boolean containsLeadingDocumentText =
                starts.size() == 1
                        && starts.get(0) > 0;

        boolean containsSeveralReferences =
                starts.size() > 1;

        if (!containsLeadingDocumentText
                && !containsSeveralReferences) {
            return Collections.singletonList(line);
        }

        List<Fragment> fragments =
                buildFragments(
                        text,
                        starts
                );

        if (fragments.size() < 2) {
            return Collections.singletonList(line);
        }

        List<RecognizedTextLine> result =
                new ArrayList<>(
                        fragments.size()
                );

        for (Fragment fragment : fragments) {
            result.add(
                    toRecognizedLine(
                            line,
                            text.length(),
                            fragment
                    )
            );
        }

        return result;
    }

    private List<Integer> findReferenceStarts(
            String text
    ) {
        List<Integer> result =
                new ArrayList<>();

        Matcher matcher =
                OBSERVED_REFERENCE_START.matcher(text);

        while (matcher.find()) {
            result.add(
                    matcher.start()
            );
        }

        return result;
    }

    private List<Fragment> buildFragments(
            String text,
            List<Integer> starts
    ) {
        List<Fragment> result =
                new ArrayList<>();

        int firstReferenceStart =
                starts.get(0);

        if (firstReferenceStart > 0) {
            addFragment(
                    result,
                    text,
                    0,
                    firstReferenceStart
            );
        }

        for (int index = 0;
             index < starts.size();
             index++) {

            int start =
                    starts.get(index);

            int end =
                    index + 1 < starts.size()
                            ? starts.get(index + 1)
                            : text.length();

            addFragment(
                    result,
                    text,
                    start,
                    end
            );
        }

        return result;
    }

    private void addFragment(
            List<Fragment> destination,
            String source,
            int start,
            int end
    ) {
        int trimmedStart = start;
        int trimmedEnd = end;

        while (trimmedStart < trimmedEnd
                && isSeparator(
                source.charAt(trimmedStart)
        )) {
            trimmedStart++;
        }

        while (trimmedEnd > trimmedStart
                && isSeparator(
                source.charAt(trimmedEnd - 1)
        )) {
            trimmedEnd--;
        }

        if (trimmedStart >= trimmedEnd) {
            return;
        }

        destination.add(
                new Fragment(
                        source.substring(
                                trimmedStart,
                                trimmedEnd
                        ),
                        trimmedStart,
                        trimmedEnd
                )
        );
    }

    private boolean isSeparator(char value) {
        return Character.isWhitespace(value)
                || value == '-'
                || value == '|'
                || value == ':';
    }

    private RecognizedTextLine toRecognizedLine(
            RecognizedTextLine source,
            int sourceLength,
            Fragment fragment
    ) {
        int sourceWidth =
                source.getRight()
                        - source.getLeft();

        int fragmentLeft =
                source.getLeft()
                        + Math.round(
                        sourceWidth
                                * fragment.start
                                / (float) sourceLength
                );

        int fragmentRight =
                source.getLeft()
                        + Math.round(
                        sourceWidth
                                * fragment.end
                                / (float) sourceLength
                );

        fragmentRight =
                Math.max(
                        fragmentLeft,
                        fragmentRight
                );

        return new RecognizedTextLine(
                source.getIndex(),
                fragment.text,
                fragment.text,
                fragmentLeft,
                source.getTop(),
                fragmentRight,
                source.getBottom(),
                Collections.emptyList()
        );
    }

    private static final class Fragment {

        private final String text;
        private final int start;
        private final int end;

        private Fragment(
                String text,
                int start,
                int end
        ) {
            this.text = text;
            this.start = start;
            this.end = end;
        }
    }
}
