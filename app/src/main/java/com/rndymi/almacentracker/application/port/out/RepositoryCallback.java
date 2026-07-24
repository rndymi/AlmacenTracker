package com.rndymi.almacentracker.application.port.out;

/**
 * Common asynchronous repository callback.
 *
 * @param <T> successful operation value; use {@link Void} when no value is
 *            produced
 */
public interface RepositoryCallback<T> {

    void onSuccess(T value);

    void onError(Throwable cause);

    default void onNotFound() {
        throw unsupportedOutcome("not found");
    }

    default void onDuplicate(Throwable cause) {
        throw unsupportedOutcome("duplicate");
    }

    private UnsupportedOperationException unsupportedOutcome(
            String outcome
    ) {
        return new UnsupportedOperationException(
                "Repository callback does not support outcome: "
                        + outcome
        );
    }
}
