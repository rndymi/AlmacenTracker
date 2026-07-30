package com.rndymi.almacentracker.app;

import android.app.Application;

public final class AlmacenTrackerApplication extends Application {
    private AppContainer appContainer;
    private boolean referenceListExperimentalNoticeShown;

    @Override
    public void onCreate() {
        super.onCreate();

        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        if (appContainer == null) {
            throw new IllegalStateException(
                    "AppContainer has not been initialized"
            );
        }

        return appContainer;
    }

    public void startUserSession() {
        referenceListExperimentalNoticeShown = false;
    }

    public boolean consumeReferenceListExperimentalNotice() {
        if (referenceListExperimentalNoticeShown) {
            return false;
        }

        referenceListExperimentalNoticeShown = true;
        return true;
    }
}
