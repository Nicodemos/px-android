package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;

import com.mercadopago.android.px.internal.view.SummaryView;

import java.util.List;

public class SummaryViewAdapter implements PaymentMethodAdapter<List<SummaryView.Model>> {

    private List<SummaryView.Model> models;
    private SummaryView summaryView;

    public SummaryViewAdapter(final SummaryView summaryView) {
        this.summaryView = summaryView;
    }

    @Override
    public void setModels(final List<SummaryView.Model> models) {
        this.models = models;
    }

    @Override
    public void showInstallmentsList() {
        //Nothing to do here
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected) {
        summaryView.update(models.get(currentIndex));
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        //TODO: Acá va a ver si tiene que animar y decirle a la vista que lo haga
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        //Nothing to do here
    }
}
