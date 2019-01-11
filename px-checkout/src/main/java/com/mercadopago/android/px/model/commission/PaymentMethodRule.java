package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.math.BigDecimal;

public class PaymentMethodRule extends ChargeRule {

    @NonNull
    private final String paymentTypeId;

    /**
     * @param paymentTypeId to compare
     * @param charge the charge amount to apply for this rule
     */
    PaymentMethodRule(@NonNull final String paymentTypeId,
        @NonNull final BigDecimal charge) {
        super(charge);
        this.paymentTypeId = paymentTypeId;
    }

    @NonNull
    public String getValue() {
        return paymentTypeId;
    }

    @Override
    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }
}
