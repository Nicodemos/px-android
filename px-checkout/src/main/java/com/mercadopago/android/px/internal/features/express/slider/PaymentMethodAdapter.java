package com.mercadopago.android.px.internal.features.express.slider;

import android.view.View;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryView;
import java.util.List;

public interface PaymentMethodAdapter<T> {
    void setModels(T models);
    void showInstallmentsList();
    void updateData(int currentIndex, int payerCostSelected);
    void updatePosition(float positionOffset, int position);
    void updateViewsOrder(View previousView, View currentView, View nextView);

    class Model {
        private final List<PaymentMethodDescriptorView.Model> paymentMethodDescriptorModels;
        private final List<SummaryView.Model> summaryViewModels;

        public Model(final List<PaymentMethodDescriptorView.Model> paymentMethodDescriptorModels,
            final List<SummaryView.Model> summaryViewModels) {
            this.paymentMethodDescriptorModels = paymentMethodDescriptorModels;
            this.summaryViewModels = summaryViewModels;
        }

        public List<PaymentMethodDescriptorView.Model> getPaymentMethodDescriptorModels() {
            return paymentMethodDescriptorModels;
        }

        public List<SummaryView.Model> getSummaryViewModels() {
            return summaryViewModels;
        }
    }
}
