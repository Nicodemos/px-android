package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfigurationModel;

public interface DiscountRepository extends ResourcesProvider {

    @Nullable
    Discount getDiscount();

    @Nullable
    Campaign getCampaign();

    boolean isNotAvailableDiscount();

    boolean hasValidDiscount();

    void reset();

    @Nullable
    DiscountConfigurationModel getConfigurationFor(@NonNull final String id);
}
