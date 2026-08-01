package com.rndymi.almacentracker.data.repository;

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
