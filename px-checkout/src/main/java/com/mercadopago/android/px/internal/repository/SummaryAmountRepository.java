package com.mercadopago.android.px.internal.repository;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.SummaryAmount;

public interface SummaryAmountRepository {

    @NonNull
    MPCall<SummaryAmount> getSummaryAmount(@NonNull String bin);
}
