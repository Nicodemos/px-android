package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;

import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import java.util.List;

public class ConfirmButtonAdapter implements PaymentMethodAdapter<List<PaymentMethodDescriptorView.Model>> {

    private List<PaymentMethodDescriptorView.Model> models;
    private final MeliButton confirmButton;

    public ConfirmButtonAdapter(final MeliButton confirmButton) {
        this.confirmButton = confirmButton;
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
        if (isLastElement(currentIndex)) {
            confirmButton.setState(MeliButton.State.DISABLED);
        } else {
            confirmButton.setState(MeliButton.State.NORMAL);
        }
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        //Nothing to do here
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        //Nothing to do here
    }

    private boolean isLastElement(final int position) {
        return position >= models.size() - 1;
    }
}
