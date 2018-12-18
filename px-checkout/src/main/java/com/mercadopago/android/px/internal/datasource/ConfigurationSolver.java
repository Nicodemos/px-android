package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
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
}
