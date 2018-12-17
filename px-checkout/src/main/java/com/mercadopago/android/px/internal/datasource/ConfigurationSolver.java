package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ConfigurationSolver {

    @NonNull
    String getConfigurationFor(@Nullable final String id);
}
