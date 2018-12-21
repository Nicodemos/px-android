package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.PayerCostModel;
import javax.annotation.Nonnull;

public interface ConfigurationSolver {

    /**
     * Retrieves the dominant discount hash for a custom option.
     *
     * @param customOptionId The custom option ID.
     * @return The hash associated to the discount configuration.
     */
    @NonNull
    String getConfigurationHashFor(@Nonnull final String customOptionId);

    /**
     * Retrieves the general discount hash
     *
     * @return The hash associated to the general discount configuration.
     */
    @Nonnull
    String getGenericConfigurationHash();

    /**
     * Retrieves the dominant payer cost model for a custom option.
     *
     * @param customOptionId The custom option ID.
     * @return The payer cost model associated to the custom option ID.
     */
    @Nullable
    PayerCostModel getPayerCostConfigurationFor(@NonNull final String customOptionId);

    /**
     * Retrieves the dominant payer cost model for a custom option and a particular configuration hash.
     *
     * @param customOptionId The custom option ID.
     * @param configurationHash The configuration hash.
     * @return The payer cost model associated to the custom option ID and configuration hash.
     */
    @Nullable
    PayerCostModel getPayerCostConfigurationFor(@NonNull final String customOptionId,
        @NonNull final String configurationHash);
}
