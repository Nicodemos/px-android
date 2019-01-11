package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.math.BigDecimal;

public class PaymentTypeRule extends ChargeRule {

    @NonNull
    private final String paymentTypeId;

    /**
     * @param paymentTypeId to compare
     * @param charge the charge amount to apply for this rule
     */
    PaymentTypeRule(@NonNull final String paymentTypeId,
        @NonNull final BigDecimal charge) {
        super(charge);
        this.paymentTypeId = paymentTypeId;
    }

    @NonNull
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    @Override
    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }
}
