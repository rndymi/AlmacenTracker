package com.rndymi.almacentracker.adapter.in.ui.activity;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.Objects;
import java.util.function.Consumer;

final class SimpleTextWatcher implements TextWatcher {

    private final Consumer<String> consumer;

    private SimpleTextWatcher(
            Consumer<String> consumer
    ) {
        this.consumer = Objects.requireNonNull(consumer);
    }

    static SimpleTextWatcher afterTextChanged(
            Consumer<String> consumer
    ) {
        return new SimpleTextWatcher(consumer);
    }

    @Override
    public void beforeTextChanged(
            CharSequence value,
            int start,
            int count,
            int after
    ) {
    }

    @Override
    public void onTextChanged(
            CharSequence value,
            int start,
            int before,
            int count
    ) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        consumer.accept(
                editable == null
                        ? ""
                        : editable.toString()
        );
    }
}