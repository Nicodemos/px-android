package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;

import com.mercadopago.android.px.internal.viewmodel.GoingToModel;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.TitlePager;

import java.util.List;

public class TitlePagerAdapter implements PaymentMethodAdapter<List<PaymentMethodDescriptorView.Model>> {

    private static final int NO_SELECTED = -1;

    private PaymentMethodDescriptorView previousView;
    private PaymentMethodDescriptorView currentView;
    private PaymentMethodDescriptorView nextView;
    private final TitlePager titlePager;
    private int currentIndex = NO_SELECTED;
    private List<PaymentMethodDescriptorView.Model> models;

    public TitlePagerAdapter(final TitlePager titlePager) {
        this.titlePager = titlePager;
    }

    @Override
    public void setModels(final List<PaymentMethodDescriptorView.Model> models) {
        this.models = models;
    }

    @Override
    public void showInstallmentsList() {
        //Nothing to do here
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected) {
        if (this.currentIndex != currentIndex) {
            final GoingToModel goingTo =
                this.currentIndex < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
            titlePager.orderViews(goingTo);
            this.currentIndex = currentIndex;
        }
        refreshData(currentIndex, payerCostSelected);
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        titlePager.updatePosition(positionOffset, goingTo);
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        this.previousView = (PaymentMethodDescriptorView) previousView;
        this.currentView = (PaymentMethodDescriptorView) currentView;
        this.nextView = (PaymentMethodDescriptorView) nextView;
    }

    private void refreshData(final int currentIndex, final int payerCostSelected) {
        if (currentIndex > 0) {
            final PaymentMethodDescriptorView.Model previousModel = models.get(currentIndex - 1);
            previousView.update(previousModel);
        }

        final PaymentMethodDescriptorView.Model currentModel = models.get(currentIndex);
        currentModel.setCurrentPayerCost(payerCostSelected);
        currentView.update(currentModel);

        if (currentIndex + 1 < models.size()) {
            final PaymentMethodDescriptorView.Model nextModel = models.get(currentIndex + 1);
            nextView.update(nextModel);
        }
    }
}
