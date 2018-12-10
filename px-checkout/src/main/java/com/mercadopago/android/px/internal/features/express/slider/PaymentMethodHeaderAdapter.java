package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.viewmodel.GoingToModel;

import java.util.List;

public class PaymentMethodHeaderAdapter implements PaymentMethodAdapter<List<PaymentMethodDescriptorView.Model>> {

    private static final int NO_SELECTED = -1;

    private int currentIndex = NO_SELECTED;
    private final PaymentMethodHeaderView paymentMethodHeaderView;
    private List<PaymentMethodDescriptorView.Model> models;

    public PaymentMethodHeaderAdapter(final PaymentMethodHeaderView paymentMethodHeaderView) {
        this.paymentMethodHeaderView = paymentMethodHeaderView;
    }

    @Override
    public void setModels(final List<PaymentMethodDescriptorView.Model> models) {
        this.models = models;
    }

    @Override
    public void showInstallmentsList() {
        paymentMethodHeaderView.showInstallmentsListTitle();
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected) {
        this.currentIndex = currentIndex;
        paymentMethodHeaderView.showTitlePager(models.get(currentIndex).hasPayerCostList());
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        final int nextIndex = goingTo == GoingToModel.BACKWARDS ? currentIndex - 1 : currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < models.size()) {
            final PaymentMethodDescriptorView.Model currentModel = models.get(currentIndex);
            final PaymentMethodDescriptorView.Model nextModel = models.get(nextIndex);
            final PaymentMethodHeaderView.Model viewModel = new PaymentMethodHeaderView.Model(goingTo,
                currentModel.hasPayerCostList(), nextModel.hasPayerCostList());
            paymentMethodHeaderView.updateArrowVisibility(positionOffset, viewModel);
        }
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        //Nothing to do here
    }
}
