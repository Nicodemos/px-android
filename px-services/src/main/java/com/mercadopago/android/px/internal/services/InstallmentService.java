package com.mercadopago.android.px.internal.services;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.model.Installment;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.SummaryAmountBody;
import java.math.BigDecimal;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface InstallmentService {

    //TODO remove "mercadopago/" from path when wrapper is OK
    @POST("mercadopago/{version}/px_mobile_api/summary_amount")
    MPCall<SummaryAmount> createSummaryAmount(@Path(value = "version", encoded = true) String version,
        @Body SummaryAmountBody summaryAmountBody,
        @Query("public_key") String publicKey,
        @Nullable @Query("access_token") String privateKey);

    @GET("/{version}/checkout/payment_methods/installments")
    MPCall<List<Installment>> getInstallments(@Path(value = "version", encoded = true) String version,
        @Query("public_key") String publicKey, @Query("access_token") String privateKey, @Query("bin") String bin,
        @Query("amount") BigDecimal amount, @Query("issuer.id") Long issuerId,
        @Query("payment_method_id") String paymentMethodId,
        @Query("locale") String locale,
        @Query("processing_mode") String processingMode,
        @Query("differential_pricing_id") Integer differentialPricingId);
}