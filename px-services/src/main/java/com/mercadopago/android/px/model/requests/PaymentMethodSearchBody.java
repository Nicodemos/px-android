package com.mercadopago.android.px.model.requests;

import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.Set;

@SuppressWarnings("unused")
public class PaymentMethodSearchBody {

    @SerializedName("access_token")
    private final String privateKey;
    private final String email;
    private final String marketplace;
    private final String productId;
    private final Set<String> labels;

    public PaymentMethodSearchBody(final Builder builder) {
        privateKey = builder.privateKey;
        email = builder.email;
        marketplace = builder.marketplace;
        productId = builder.productId;
        labels = builder.labels;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getEmail() {
        return email;
    }

    public String getMarketplace() {
        return marketplace;
    }

    public String getProductId() {
        return productId;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public static class Builder {
        /* default */ String privateKey;
        /* default */ String email;
        /* default */ String marketplace;
        /* default */ String productId;
        /* default */ Set<String> labels;

        public Builder setPrivateKey(@Nullable final String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder setPayerEmail(@Nullable final String email) {
            this.email = email;
            return this;
        }

        public Builder setMarketplace(@Nullable final String marketplace) {
            this.marketplace = marketplace;
            return this;
        }

        public Builder setProductId(@Nullable final String productId) {
            this.productId = productId;
            return this;
        }

        public Builder setLabels(@Nullable final Set<String> labels) {
            this.labels = labels;
            return this;
        }

        public PaymentMethodSearchBody build() {
            return new PaymentMethodSearchBody(this);
        }
    }
}
