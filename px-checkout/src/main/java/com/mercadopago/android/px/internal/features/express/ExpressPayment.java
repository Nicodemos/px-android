package com.mercadopago.android.px.internal.features.express;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpView;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.features.explode.ExplodeDecorator;
import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodAdapter;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.IPayment;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import java.util.List;

public interface ExpressPayment {

    interface View extends MvpView {

        void configureAdapters(@NonNull List<DrawableFragmentItem> items, @NonNull final Site site,
            final int selectedPayerCost, @NonNull PaymentMethodAdapter.Model paymentMethodViewModel);

        void cancel();

        void showCardFlow(@NonNull final Card card);

        void showPaymentProcessor();

        void finishLoading(@NonNull final ExplodeDecorator params);

        void cancelLoading();

        void startLoadingButton(final int paymentTimeout);

        //TODO shared with Checkout activity

        void showErrorScreen(@NonNull final MercadoPagoError error);

        void showPaymentResult(@NonNull final IPayment paymentResult);

        void onRecoverPaymentEscInvalid(final PaymentRecovery recovery);

        void startPayment();

        void enableToolbarBack();

        void disableToolbarBack();

        void showErrorSnackBar(@NonNull final MercadoPagoError error);

        void showInstallmentsDescriptionRow(final int paymentMethodIndex, final int payerCostSelected);

        void showInstallmentsList(List<PayerCost> payerCostList, final int payerCostSelected);

        void hideInstallmentsSelection();

        void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel);

        void collapseInstallmentsSelection();

    }

    interface Actions extends PaymentServiceHandler {

        void trackExpressView();

        void trackConfirmButton(final int paymentMethodSelectedIndex);

        void confirmPayment(final int paymentMethodSelectedIndex);

        void cancel();

        void onTokenResolved(final int paymentMethodSelectedIndex);

        void onViewResumed();

        void updateElementPosition(int paymentMethodIndex);

        void onViewPaused();

        void onInstallmentsRowPressed(int currentItem);

        void onInstallmentSelectionCanceled(final int currentItem);

        void onSliderOptionSelected(final int position);

        void onPayerCostSelected(final int paymentMethodIndex, final PayerCost payerCostSelected);

        void hasFinishPaymentAnimation();

        void manageNoConnection();
    }
}