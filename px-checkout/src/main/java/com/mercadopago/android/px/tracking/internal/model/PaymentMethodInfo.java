package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PayerCostModel;

public class PaymentMethodInfo {

    @SerializedName("extra_info")
    private ExtraInfo extraInfo;

    private String paymentMethodType;

    private String paymentMethodId;

    public PaymentMethodInfo(@NonNull final ExtraInfo extraInfo, @NonNull final String paymentMethodType,
        @NonNull final String paymentMethodId) {

        this.extraInfo = extraInfo;
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodId = paymentMethodId;
    }

    public static PaymentMethodInfo createFrom(@NonNull final ExpressMetadata expressMetadata,
        @NonNull final String currencyId, @NonNull final PayerCostRepository payerCostRepository) {
        final ExtraInfo extraInfo;

        if (expressMetadata.isCard()) {
            final PayerCostModel payerCostModel =
                payerCostRepository.getConfigurationFor(expressMetadata.getCard().getId());
            final int expressInstallmentIndex = payerCostModel.getDefaultPayerCostIndex();
            final PayerCost payerCost = payerCostModel.getPayerCost(expressInstallmentIndex);
            extraInfo = CardExtraInfo.createFrom(expressMetadata.getCard(), payerCost, currencyId);
        } else {
            extraInfo = new AccountMoneyInfo(expressMetadata.getAccountMoney().balance);
        }
        return new PaymentMethodInfo(extraInfo, expressMetadata.getPaymentTypeId(),
            expressMetadata.getPaymentMethodId());
    }
}
