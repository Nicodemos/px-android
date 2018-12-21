package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.tracking.internal.utils.TrackingUtil;
import java.io.Serializable;

public class ExpressConfirmEvent implements Serializable {

    private String paymentMethodType;
    private String paymentMethodId;
    private String reviewType;
    private ExtraInfo extraInfo;

    public ExpressConfirmEvent(@NonNull final String paymentMethodType, @NonNull final String paymentMethodId,
        @NonNull final ExtraInfo extraInfo) {
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodId = paymentMethodId;
        reviewType = TrackingUtil.PROPERTY_REVIEW_TYPE_ONE_TAP;
        this.extraInfo = extraInfo;
    }

    public static ExpressConfirmEvent createFrom(@NonNull final ExpressMetadata expressMetadata,
        final PayerCost selectedPayerCost, @NonNull final String currencyId) {
        final String paymentMethodType = expressMetadata.getPaymentTypeId();
        final String paymentMethodId = expressMetadata.getPaymentMethodId();
        final ExtraInfo extraInfo;
        if (expressMetadata.isCard()) {
            extraInfo = CardExtraInfo.createFrom(expressMetadata.getCard(), selectedPayerCost, currencyId);
        } else {
            extraInfo = new AccountMoneyInfo(expressMetadata.getAccountMoney().balance);
        }
        return new ExpressConfirmEvent(paymentMethodType, paymentMethodId, extraInfo);
    }
}
