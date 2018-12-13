package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.GoingToModel;
import java.util.List;

public class SummaryViewAdapter implements PaymentMethodAdapter<List<SummaryView.Model>> {

    private static final int NO_SELECTED = -1;

    private List<SummaryView.Model> models;
    private final SummaryView summaryView;
    private int currentIndex = NO_SELECTED;

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
        this.currentIndex = currentIndex;
        //TODO Only update if showing a new discount model
        summaryView.update(models.get(currentIndex));
    }

    @Override
    public void updatePosition(float positionOffset, final int position) {
        if (positionOffset <= 0.0f || positionOffset > 1.0f) {
            return;
        }
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        final int nextIndex = goingTo == GoingToModel.BACKWARDS ? currentIndex - 1 : currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < models.size()) {
            if (goingTo == GoingToModel.BACKWARDS) {
                positionOffset = 1.0f - positionOffset;
            }
            summaryView.animateElementList(positionOffset);
        }
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        //Nothing to do here
    }
}
