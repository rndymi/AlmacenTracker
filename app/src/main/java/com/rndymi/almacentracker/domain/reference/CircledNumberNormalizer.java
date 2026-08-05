package com.rndymi.almacentracker.domain.reference;

final class CircledNumberNormalizer {

    private CircledNumberNormalizer() {
    }

    static String digitsFor(char value) {
        int number;

        if (value >= '\u2460' && value <= '\u2473') {
            number = value - '\u2460' + 1;
        } else if (value >= '\u3251' && value <= '\u325F') {
            number = value - '\u3251' + 21;
        } else if (value >= '\u32B1' && value <= '\u32BF') {
            number = value - '\u32B1' + 36;
        } else {
            return null;
        }

        return String.valueOf(number);
    }
}
