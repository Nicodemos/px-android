package com.mercadopago.android.px.tracking.internal.views;

import android.support.annotation.NonNull;

public class GuessingRootViewTracker extends ViewTracker {

    public final static String PATH = BASE_VIEW_PATH + ADD_PAYMENT_METHOD_PATH;

    @NonNull
    @Override
    public String getViewPath() {
        return PATH;
    }
}
