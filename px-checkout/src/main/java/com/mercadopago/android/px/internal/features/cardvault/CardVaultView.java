package com.mercadopago.android.px.internal.features.cardvault;

import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.exceptions.ApiException;

public interface CardVaultView extends MvpView {

    void finishWithResult();

    void showApiExceptionError(final ApiException exception, final String requestOrigin);

    void showError(final MercadoPagoError mercadoPagoError, final String requestOrigin);

    void showEmptyPayerCostScreen();

    void askForInstallments();

    void startIssuersActivity();

    void startSecurityCodeActivity();

    void showProgressLayout();

    void askForCardInformation();

    void askForSecurityCodeFromTokenRecovery();

    void cancelCardVault();

    void animateTransitionSlideInSlideOut();

    void finishOnErrorResult();
}
