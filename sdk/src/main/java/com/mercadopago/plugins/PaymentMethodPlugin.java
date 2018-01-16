package com.mercadopago.plugins;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mercadopago.plugins.model.PaymentMethodInfo;

import java.util.Map;

/**
 * Created by nfortuna on 12/11/17.
 */

public abstract class PaymentMethodPlugin {

    public static final String POSIION_TOP = "position_up";
    public static final String POSIION_BOTTOM = "position_down";

    private String id;

    public PaymentMethodPlugin(final String id) {
        this.id = id;
    }

    public String displayOrder() {
        return POSIION_TOP;
    }

    public boolean isEnabled(@NonNull final Map<String, Object> data) {
        return true;
    }

    public String getId() {
        return this.id;
    }

    public abstract @NonNull PaymentMethodInfo getPaymentMethodInfo(@NonNull final Context context);

    public abstract @Nullable PluginComponent createConfigurationComponent(@NonNull final PluginComponent.Props props,
                                                                           @NonNull final Context context);

    public boolean isConfigurationComponentEnabled(@NonNull final Map<String, Object> data) {
        return true;
    }
}