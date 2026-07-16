package com.rndymi.almacentracker.adapter.in.ui.state;

public final class UiEvent<T> {

    private final T content;
    private boolean handled;

    public UiEvent(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (handled) {
            return null;
        }

        handled = true;
        return content;
    }

    public T peekContent() {
        return content;
    }
}