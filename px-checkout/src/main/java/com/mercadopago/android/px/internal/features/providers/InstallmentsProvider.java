package com.mercadopago.android.px.internal.features.providers;

import com.mercadopago.android.px.internal.base.ResourcesProvider;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public interface InstallmentsProvider extends ResourcesProvider {
    MercadoPagoError getNoPayerCostFoundError();
}
