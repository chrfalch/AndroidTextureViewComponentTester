package com.androidtextureviewtest;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.view.ReactViewManager;

public class TestViewManager extends ReactViewManager {
    @NonNull
    @Override
    public String getName() {
        return "TestView";
    }

    @NonNull
    @Override
    public TestView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new TestView(reactContext);
    }
}
