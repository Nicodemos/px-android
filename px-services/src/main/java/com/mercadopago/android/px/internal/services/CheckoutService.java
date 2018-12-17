package com.mercadopago.android.px.internal.services;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.requests.PaymentMethodSearchBody;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.math.BigDecimal;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CheckoutService {

    String GROUPS_VERSION = "1.7";

    @GET("/{version}/px_mobile_api/payment_methods?api_version=" + GROUPS_VERSION)
    MPCall<PaymentMethodSearch> getPaymentMethodSearch(@Path(value = "version", encoded = true) String version,
        @Header("Accept-Language") String locale,
        @Query("public_key") String publicKey,
        @Query("amount") BigDecimal amount,
        @Query("excluded_payment_types") String excludedPaymentTypes,
        @Query("excluded_payment_methods") String excludedPaymentMethods,
        @Query("site_id") String siteId,
        @Query("processing_mode") String processingMode,
        @Query("cards_esc") String cardsWithEsc,
        @Query("support_plugins") String supportedPlugins,
        @Nullable @Query("differential_pricing_id") Integer differentialPricingId,
        @Nullable @Query("default_installments") final Integer defaultInstallments,
        @Query("express_enabled") final boolean expressEnabled,
        @Nullable @Query("access_token") String accessToken);

    //TODO remove "mercadopago/" from path when wrapper is OK
    @POST("mercadopago/{version}/px_mobile_api/payment_methods?api_version=" + GROUPS_VERSION)
    MPCall<PaymentMethodSearch> getPaymentMethodSearch(@Path(value = "version", encoded = true) String version,
        @Header("Accept-Language") String locale,
        @Query("public_key") String publicKey,
        @Query("amount") BigDecimal amount,
        @Query("excluded_payment_types") String excludedPaymentTypes,
        @Query("excluded_payment_methods") String excludedPaymentMethods,
        @Query("site_id") String siteId,
        @Query("processing_mode") String processingMode,
        @Query("cards_esc") String cardsWithEsc,
        @Query("support_plugins") String supportedPlugins,
        @Nullable @Query("differential_pricing_id") Integer differentialPricingId,
        @Nullable @Query("default_installments") final Integer defaultInstallments,
        @Query("express_enabled") final boolean expressEnabled,
        @Body PaymentMethodSearchBody paymentMethodSearchBody);

    @GET("/{version}/checkout/preferences/{preference_id}")
    MPCall<CheckoutPreference> getPreference(@Path(value = "version", encoded = true) String version,
        @Path(value = "preference_id", encoded = true) String checkoutPreferenceId,
        @Query("public_key") String publicKey);
}

