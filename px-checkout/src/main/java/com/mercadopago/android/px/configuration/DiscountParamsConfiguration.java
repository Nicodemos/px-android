package com.mercadopago.android.px.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class DiscountParamsConfiguration {

    @NonNull private final Set<String> labels;
    @Nullable private final String productId;

    /* default */ DiscountParamsConfiguration(@NonNull final Builder builder) {
        labels = builder.labels;
        productId = builder.productId;
    }

    /**
     * Get additional data needed to apply a specific discount.
     *
     * @return set of labels needed to apply a specific discount.
     */
    @NonNull
    public Set<String> getLabels() {
        return labels;
    }

    /**
     * Let us know what the product id is
     *
     * @return product id
     */
    @Nullable
    public String getProductId() {
        return productId;
    }

    public static class Builder {
        /* default */ Set<String> labels;
        /* default */ String productId;

        public Builder() {
            labels = new HashSet<>();
            productId = "";
        }

        /**
         * Set additional data needed to apply a specific discount.
         *
         * @param labels are additional data needed to apply a specific discount.
         * @return builder to keep operating.
         */
        public Builder setLabels(@NonNull final Set<String> labels) {
            this.labels = labels;
            return this;
        }

        /**
         * Payment product id let us to enable discounts for the productId specified.
         *
         * @param productId payment product id.
         * @return builder to keep operating.
         */
        public Builder setProductId(@NonNull final String productId) {
            this.productId = productId;
            return this;
        }

        public DiscountParamsConfiguration build() {
            return new DiscountParamsConfiguration(this);
        }
    }
}
