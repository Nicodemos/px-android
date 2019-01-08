package com.mercadopago.android.px.internal.features.express.slider;

import android.support.annotation.NonNull;
import android.view.View;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.viewmodel.GoingToModel;
import java.util.List;

public class PaymentMethodHeaderAdapter
    extends PaymentMethodAdapter<List<PaymentMethodDescriptorView.Model>, PaymentMethodHeaderView> {

    private static final int NO_SELECTED = -1;

    private int currentIndex = NO_SELECTED;

    public PaymentMethodHeaderAdapter(@NonNull final List<PaymentMethodDescriptorView.Model> data,
        @NonNull final PaymentMethodHeaderView view) {
        super(data, view);
    }

    @Override
    public void showInstallmentsList() {
        view.showInstallmentsListTitle();
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected) {
        this.currentIndex = currentIndex;
        view.showTitlePager(data.get(currentIndex).hasPayerCostList());
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        final GoingToModel goingTo = position < currentIndex ? GoingToModel.BACKWARDS : GoingToModel.FORWARD;
        final int nextIndex = goingTo == GoingToModel.BACKWARDS ? currentIndex - 1 : currentIndex + 1;
        if (nextIndex >= 0 && nextIndex < data.size()) {
            final PaymentMethodDescriptorView.Model currentModel = data.get(currentIndex);
            final PaymentMethodDescriptorView.Model nextModel = data.get(nextIndex);
            final PaymentMethodHeaderView.Model viewModel = new PaymentMethodHeaderView.Model(goingTo,
                currentModel.hasPayerCostList(), nextModel.hasPayerCostList());
            view.updateArrowVisibility(positionOffset, viewModel);
        }
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        //Nothing to do here
    }
}
