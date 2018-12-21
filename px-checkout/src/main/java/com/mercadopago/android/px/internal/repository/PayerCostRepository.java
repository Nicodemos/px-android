package com.mercadopago.android.px.internal.repository;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.model.PayerCostModel;
import javax.annotation.Nonnull;

public interface PayerCostRepository extends ResourcesProvider {

    /**
     * Obtains the payer costs configuration that applies in a particular moment of the flow
     * <p>
     * In the future, with a discount selector feature, the selected discount will define the associated payer cost.
     *
     * @return The current dominant configuration,
     * null if there is no installments configuration (e.g. for account money)
     */
    @Nullable
    PayerCostModel getCurrentConfiguration();

    /**
     * Obtains the complete payer cost configuration for a specif custom option.
     *
     * @param customOptionId The {@link com.mercadopago.android.px.model.CustomSearchItem} ID.
     * @return The payer cost configuration, returns null if don't have a configuration or ID is invalid.
     */
    @Nullable
    PayerCostModel getConfigurationFor(@Nonnull final String customOptionId);
}
