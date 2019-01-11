package com.mercadopago.android.px.model.commission;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.ChargeRepository;
import java.math.BigDecimal;

public final class PaymentTypeChargeRule extends PaymentTypeRule {

    /**
     * @param paymentType the payment type associated with the charge to shouldBeTriggered.
     * @param charge the charge amount to apply for this rule
     */
    public PaymentTypeChargeRule(@NonNull final String paymentType,
        @NonNull final BigDecimal charge) {
        super(paymentType, charge);
    }

    @Override
    public boolean shouldBeTriggered(@NonNull final ChargeRepository chargeRepository) {
        return chargeRepository.shouldApply(this);
    }
}
