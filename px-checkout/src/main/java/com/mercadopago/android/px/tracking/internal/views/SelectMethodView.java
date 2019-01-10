package com.mercadopago.android.px.tracking.internal.views;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.tracking.internal.mapper.FromCustomItemToAvailableMethod;
import com.mercadopago.android.px.tracking.internal.mapper.FromItemToItemInfo;
import com.mercadopago.android.px.tracking.internal.mapper.FromPaymentMethodSearchItemToAvailableMethod;
import com.mercadopago.android.px.tracking.internal.model.AvailableMethod;
import com.mercadopago.android.px.tracking.internal.model.ItemInfo;
import com.mercadopago.android.px.tracking.internal.model.SelectMethodData;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectMethodView extends ViewTracker {

    private static final String CONTEXT_VALUE = BASE_VIEW_PATH + PAYMENTS_PATH;
    public static final String PATH_PAYMENT_VAULT = CONTEXT_VALUE + "/select_method";
    @NonNull private final List<AvailableMethod> availableMethods;
    @NonNull private final List<ItemInfo> items;
    private final BigDecimal totalAmount;

    public SelectMethodView(@NonNull final PaymentMethodSearch paymentMethodSearch,
        @NonNull final Set<String> escCardIds,
        @NonNull final CheckoutPreference preference) {
        availableMethods =
            new ArrayList<>(
                new FromCustomItemToAvailableMethod(escCardIds).map(paymentMethodSearch.getCustomSearchItems()));
        availableMethods.addAll(
            new FromPaymentMethodSearchItemToAvailableMethod(paymentMethodSearch).map(paymentMethodSearch.getGroups()));
        items = new FromItemToItemInfo().map(preference.getItems());
        totalAmount = preference.getTotalAmount();
    }

    @NonNull
    @Override
    public String getViewPath() {
        return PATH_PAYMENT_VAULT;
    }

    @NonNull
    @Override
    public Map<String, Object> getData() {
        return new SelectMethodData(availableMethods, items,
            totalAmount).toMap();
    }
}
