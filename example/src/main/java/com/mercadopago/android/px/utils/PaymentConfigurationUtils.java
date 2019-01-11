package com.mercadopago.android.px.utils;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DiscountConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.internal.features.plugins.SamplePaymentMethodPlugin;
import com.mercadopago.android.px.internal.features.plugins.SamplePaymentProcessor;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.Sites;
import java.math.BigDecimal;

public final class PaymentConfigurationUtils {
    private PaymentConfigurationUtils() {
        //Do nothing
    }

    public static PaymentConfiguration create(
        @NonNull final PaymentProcessor paymentProcessor) {
        return new PaymentConfiguration.Builder(paymentProcessor).build();
    }

    public static PaymentConfiguration create() {
        return create(new SamplePaymentProcessor());
    }

    public static PaymentConfiguration create(
        @NonNull final PaymentProcessor paymentProcessor,
        @NonNull final PaymentMethodPlugin paymentMethodPlugin) {

        return new PaymentConfiguration.Builder(paymentProcessor)
            .addPaymentMethodPlugin(paymentMethodPlugin)
            .setDiscountConfiguration(
                DiscountConfiguration.withDiscount(new Discount.Builder("1", Sites.ARGENTINA.getCurrencyId(),
                        new BigDecimal("10.5")).setAmountOff(new BigDecimal("10.5")).build(),
                    new Campaign.Builder("1").setMaxCouponAmount(new BigDecimal("200")).build()))
            .build();
    }

    public static PaymentConfiguration createWithPlugin(
        @NonNull final PaymentProcessor paymentProcessor) {
        return create(paymentProcessor, new SamplePaymentMethodPlugin());
    }
}
