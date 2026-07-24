package com.rndymi.almacentracker.app;

import android.app.Application;

public final class AlmacenTrackerApplication extends Application {
    private AppContainer appContainer;

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
}