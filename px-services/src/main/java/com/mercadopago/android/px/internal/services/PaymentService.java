package com.mercadopago.android.px.internal.services;

import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Installment;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.Payment;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.SummaryAmountBody;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface PaymentService {

    @GET("/v1/payment_methods")
    MPCall<List<PaymentMethod>> getPaymentMethods(@Query("public_key") String publicKey,
        @Query("access_token") String privateKey);

    @GET("/{version}/px_mobile_api/payment_methods/cards")
    MPCall<List<PaymentMethod>> getCardPaymentMethods(@Path(value = "version", encoded = true) String version,
        @Query("access_token") String accessToken);

    @POST("/{version}/px_mobile_api/summary_amount")
    MPCall<SummaryAmount> createSummaryAmount(@Path(value = "version", encoded = true) String version,
        @Body SummaryAmountBody summaryAmountBody,
        @Query("public_key") String publicKey,
        @Query("access_token") String privateKey);

    @GET("/{version}/checkout/payment_methods/installments")
    MPCall<List<Installment>> getInstallments(@Path(value = "version", encoded = true) String version,
        @Query("public_key") String publicKey, @Query("access_token") String privateKey, @Query("bin") String bin,
        @Query("amount") BigDecimal amount, @Query("issuer.id") Long issuerId,
        @Query("payment_method_id") String paymentMethodId,
        @Query("locale") String locale,
        @Query("processing_mode") String processingMode,
        @Query("differential_pricing_id") Integer differentialPricingId);

    @GET("/{version}/checkout/payment_methods/card_issuers")
    MPCall<List<Issuer>> getIssuers(@Path(value = "version", encoded = true) String version,
        @Query("public_key") String publicKey, @Query("access_token") String privateKey,
        @Query("payment_method_id") String paymentMethodId, @Query("bin") String bin,
        @Query("processing_mode") String processingMode);

    @POST("/v1/checkout/payments")
    MPCall<Payment> createPayment(@Header("X-Idempotency-Key") String transactionId,
        @Body Map<String, Object> additionalInfo,
        @QueryMap Map<String, String> query);
}