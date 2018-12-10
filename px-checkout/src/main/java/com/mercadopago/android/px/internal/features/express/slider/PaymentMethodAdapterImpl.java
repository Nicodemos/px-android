package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;

import com.mercadolibre.android.ui.widgets.MeliButton;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdapterImpl implements PaymentMethodAdapter<PaymentMethodAdapter.Model> {

    private final List<PaymentMethodAdapter> adapters;
    private final TitlePagerAdapter titlePagerAdapter;
    private final PaymentMethodHeaderAdapter paymentMethodHeaderAdapter;
    private final ConfirmButtonAdapter confirmButtonAdapter;
    private final SummaryViewAdapter summaryViewAdapter;

    public PaymentMethodAdapterImpl(final TitlePager titlePager,
        final PaymentMethodHeaderView paymentMethodHeaderView, final MeliButton confirmButton,
            final SummaryView summaryView) {
        adapters = new ArrayList<>();
        titlePagerAdapter = new TitlePagerAdapter(titlePager);
        adapters.add(titlePagerAdapter);
        paymentMethodHeaderAdapter = new PaymentMethodHeaderAdapter(paymentMethodHeaderView);
        adapters.add(paymentMethodHeaderAdapter);
        confirmButtonAdapter = new ConfirmButtonAdapter(confirmButton);
        adapters.add(confirmButtonAdapter);
        summaryViewAdapter = new SummaryViewAdapter(summaryView);
        adapters.add(summaryViewAdapter);
    }

    @Override
    public void setModels(final PaymentMethodAdapter.Model model) {
        titlePagerAdapter.setModels(model.getPaymentMethodDescriptorModels());
        paymentMethodHeaderAdapter.setModels(model.getPaymentMethodDescriptorModels());
        confirmButtonAdapter.setModels(model.getPaymentMethodDescriptorModels());
        summaryViewAdapter.setModels(model.getSummaryViewModels());
    }

    @Override
    public void showInstallmentsList() {
        for (final PaymentMethodAdapter adapter : adapters) {
            adapter.showInstallmentsList();
        }
    }

    @Override
    public void updateData(final int currentIndex, final int payerCostSelected) {
        for (final PaymentMethodAdapter adapter : adapters) {
            adapter.updateData(currentIndex, payerCostSelected);
        }
    }

    @Override
    public void updatePosition(final float positionOffset, final int position) {
        for (final PaymentMethodAdapter adapter : adapters) {
            adapter.updatePosition(positionOffset, position);
        }
    }

    @Override
    public void updateViewsOrder(final View previousView, final View currentView, final View nextView) {
        for (final PaymentMethodAdapter adapter : adapters) {
            adapter.updateViewsOrder(previousView, currentView, nextView);
        }
    }
}
