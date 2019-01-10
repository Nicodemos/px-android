package com.mercadopago.android.px.tracking.internal.events;

import android.support.annotation.NonNull;

public class AbortCheckoutEventTracker extends EventTracker {

    public static final String PATH = BASE_PATH + "/router";

    @NonNull
    @Override
    public String getEventPath() {
        return PATH;
    }
}
