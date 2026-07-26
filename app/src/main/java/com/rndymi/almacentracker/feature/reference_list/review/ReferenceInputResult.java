package com.rndymi.almacentracker.feature.reference_list.review;

public final class ReferenceInputResult {

    public enum Status {
        SUCCESS,
        INVALID,
        DUPLICATE,
        NOT_FOUND
    }

    private final Status status;
    private final boolean categoryValid;
    private final boolean codeValid;

    private ReferenceInputResult(
            Status status,
            boolean categoryValid,
            boolean codeValid
    ) {
        this.status = status;
        this.categoryValid = categoryValid;
        this.codeValid = codeValid;
    }

    public static ReferenceInputResult success() {
        return new ReferenceInputResult(
                Status.SUCCESS,
                true,
                true
        );
    }

    public static ReferenceInputResult invalid(
            boolean categoryValid,
            boolean codeValid
    ) {
        return new ReferenceInputResult(
                Status.INVALID,
                categoryValid,
                codeValid
        );
    }

    public static ReferenceInputResult duplicate() {
        return new ReferenceInputResult(
                Status.DUPLICATE,
                true,
                true
        );
    }

    public static ReferenceInputResult notFound() {
        return new ReferenceInputResult(
                Status.NOT_FOUND,
                true,
                true
        );
    }

    public Status getStatus() {
        return status;
    }

    public boolean isCategoryValid() {
        return categoryValid;
    }

    public boolean isCodeValid() {
        return codeValid;
    }
}