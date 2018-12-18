package com.mercadopago.android.px.internal.repository;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import javax.annotation.Nonnull;

public interface DiscountRepository extends ResourcesProvider {

    /**
     * Clears the repository.
     */
    void reset();

    /**
     * Obtains the discount configuration that applies in a particular moment of the flow
     *
     * E.g. If the user did not select any payment method, the general discount is retrieved
     * otherwise you will retrieve the best discount between the general discount or the selected payment method.
     *
     * In the future, with a discount selector feature, the selected discount will be dominant over the best one.
     *
     * @return The current dominant configuration
     */
    DiscountConfigurationModel getCurrentConfiguration();

    /**
     * Obtains the complete discount configuration for a specif custom option.
     *
     * @param id The {@link com.mercadopago.android.px.model.CustomSearchItem} ID.
     * @return The discount configuration, returns null if the ID is invalid.
     */
    // TODO: review null return
    @Nullable
    DiscountConfigurationModel getConfigurationFor(@Nonnull final String id);

    /**
     * Retrieve the configuration for a no-discount scenario
     *
     * @return The discount configuration model
     */
    DiscountConfigurationModel getWithoutDiscountConfiguration();
}
