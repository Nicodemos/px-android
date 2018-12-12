package com.mercadopago.android.px.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Set;

public class SummaryAmountBody {

    @NonNull private String siteId;
    @NonNull private BigDecimal transactionAmount;
    @NonNull private String marketplace;
    @NonNull private String email;
    @NonNull private String productId;
    @NonNull private String paymentMethodId;
    @NonNull private String paymentType;
    @NonNull private String bin;
    @NonNull private Long issuerId;
    @NonNull private Set<String> labels;
    @NonNull private Integer defaultInstallments;
    @Nullable private Integer differentialPricingId;
    @Nullable private String processingMode;

    public SummaryAmountBody(@NonNull final String siteId, @NonNull final BigDecimal transactionAmount,
        @NonNull final String marketplace, @NonNull final String email, @NonNull final String productId,
        @NonNull final String paymentMethodId,
        @NonNull final String paymentType, @NonNull final String bin, @NonNull final Long issuerId,
        @NonNull final Set<String> labels,
        @NonNull final Integer defaultInstallments, @Nullable final Integer differentialPricingId,
        @Nullable final String processingMode) {
        this.siteId = siteId;
        this.transactionAmount = transactionAmount;
        this.marketplace = marketplace;
        this.email = email;
        this.productId = productId;
        this.paymentMethodId = paymentMethodId;
        this.paymentType = paymentType;
        this.bin = bin;
        this.issuerId = issuerId;
        this.labels = labels;
        this.defaultInstallments = defaultInstallments;
        this.differentialPricingId = differentialPricingId;
        this.processingMode = processingMode;
    }
}
