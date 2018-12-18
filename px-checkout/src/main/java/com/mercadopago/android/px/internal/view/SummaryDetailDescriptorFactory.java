package com.mercadopago.android.px.internal.view;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountAmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountDescriptionLocalized;
import com.mercadopago.android.px.internal.viewmodel.DiscountDetailColor;
import com.mercadopago.android.px.internal.viewmodel.DiscountDetailDrawable;
import com.mercadopago.android.px.internal.viewmodel.ItemDetailColor;
import com.mercadopago.android.px.internal.viewmodel.ItemLocalized;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.ArrayList;
import java.util.List;

public class SummaryDetailDescriptorFactory {

    @NonNull private final DiscountConfigurationModel discountModel;
    @NonNull private final CheckoutPreference checkoutPreference;

    public SummaryDetailDescriptorFactory(@NonNull final DiscountConfigurationModel discountModel,
        @NonNull final CheckoutPreference checkoutPreference) {
        this.discountModel = discountModel;
        this.checkoutPreference = checkoutPreference;
    }

    public List<AmountDescriptorView.Model> create() {
        final List<AmountDescriptorView.Model> list = new ArrayList<>();

        if (discountModel.getDiscount() != null) {
            list.add(new AmountDescriptorView.Model(new ItemLocalized(),
                new AmountLocalized(checkoutPreference.getTotalAmount(),
                    checkoutPreference.getSite().getCurrencyId()), new ItemDetailColor()));
            list.add(new AmountDescriptorView.Model(new DiscountDescriptionLocalized(discountModel.getDiscount()),
                new DiscountAmountLocalized(discountModel.getDiscount().getCouponAmount(),
                    checkoutPreference.getSite().getCurrencyId()),
                new DiscountDetailColor()).setDetailDrawable(new DiscountDetailDrawable()).enableListener());
        }
        return list;
    }
}
