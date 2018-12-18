package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.math.BigDecimal;
import java.util.List;

public interface InstallmentsActivityView extends MvpView {
    void showInstallments(List<PayerCost> payerCostList, OnSelectedCallback<Integer> onSelectedCallback);

    void finishWithResult(PayerCost payerCost);

    void showLoadingView();

    void hideLoadingView();

    void showError(MercadoPagoError error, String requestOrigin);

    void showHeader();

    void showInstallmentsRecyclerView();

    void warnAboutBankInterests();

    void showDetailDialog();

    void showAmount(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final BigDecimal itemsPlusCharges,
        @NonNull final Site site);
}
