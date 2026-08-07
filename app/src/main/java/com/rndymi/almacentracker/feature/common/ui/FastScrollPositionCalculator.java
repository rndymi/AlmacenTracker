package com.rndymi.almacentracker.feature.common.ui;

final class FastScrollPositionCalculator {

    private FastScrollPositionCalculator() {
    }

    static float fractionForViewport(
            int firstVisiblePosition,
            int lastVisiblePosition,
            int itemCount
    ) {
        if (itemCount <= 0
                || firstVisiblePosition < 0
                || lastVisiblePosition < firstVisiblePosition) {
            return 0f;
        }

        int visibleItemCount =
                lastVisiblePosition
                        - firstVisiblePosition
                        + 1;
        int maximumFirstPosition =
                Math.max(
                        0,
                        itemCount - visibleItemCount
                );

        if (maximumFirstPosition == 0) {
            return 0f;
        }

        return clamp(
                firstVisiblePosition
                        / (float) maximumFirstPosition
        );
    }

    static int positionForFraction(
            float fraction,
            int itemCount,
            int visibleItemCount
    ) {
        if (itemCount <= 0) {
            return 0;
        }

        int maximumFirstPosition =
                Math.max(
                        0,
                        itemCount
                                - Math.max(
                                1,
                                visibleItemCount
                        )
                );

        return Math.round(
                clamp(fraction)
                        * maximumFirstPosition
        );
    }

    static float clamp(float value) {
        return Math.max(
                0f,
                Math.min(1f, value)
        );
    }
}
