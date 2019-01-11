package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.commission.PaymentTypeRule;
import java.math.BigDecimal;

public interface ChargeRepository {

    @NonNull
    BigDecimal getChargeAmount();

    boolean shouldApply(PaymentTypeRule paymentTypeRule);
}
