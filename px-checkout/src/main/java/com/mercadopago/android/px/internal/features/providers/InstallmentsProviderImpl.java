package com.mercadopago.android.px.internal.features.providers;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.datasource.MercadoPagoServicesAdapter;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

public class InstallmentsProviderImpl implements InstallmentsProvider {

    private final Context context;
    private final MercadoPagoServicesAdapter mercadoPago;

    public InstallmentsProviderImpl(@NonNull final Context context) {
        this.context = context;
        mercadoPago = Session.getSession(context).getMercadoPagoServiceAdapter();
    }

        @Override
    public MercadoPagoError getNoPayerCostFoundError() {
        String message = getStandardErrorMessage();
        String detail = context.getString(R.string.px_error_message_detail_no_payer_cost_found);

        return new MercadoPagoError(message, detail, false);
    }

    public String getStandardErrorMessage() {
        return context.getString(R.string.px_standard_error_message);
    }
}
