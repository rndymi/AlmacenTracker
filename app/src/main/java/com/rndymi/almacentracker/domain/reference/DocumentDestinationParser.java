package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DocumentDestinationParser {

    private static final Pattern CIRCLED_DESTINATION =
            Pattern.compile(
                    "[①②③④⑤⑥⑦⑧⑨⑩]"
            );

    public List<String> parse(
            String sourceText
    ) {
        if (sourceText == null
                || sourceText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Matcher matcher =
                CIRCLED_DESTINATION.matcher(
                        sourceText
                );

        List<String> result =
                new ArrayList<>();

        while (matcher.find()) {
            String value = matcher.group();

            if (!result.contains(value)) {
                result.add(value);
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }
}
