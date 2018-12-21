package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCostModel;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class ExpressInstallmentsView implements Serializable {

    @NonNull private String paymentMethodType;
    @NonNull private String paymentMethodId;
    @NonNull private Long issuerId;
    @NonNull private String cardId;
    @NonNull private BigDecimal totalAmount;
    @NonNull private List<AvailableInstallment> availableInstallments;
    @NonNull private String currencyId;

    public ExpressInstallmentsView(@NonNull final String paymentMethodType, @NonNull final String paymentMethodId,
        @NonNull final Long issuerId,
        @NonNull final String cardId,
        @NonNull final BigDecimal totalAmount,
        @NonNull final List<AvailableInstallment> availableInstallments, @NonNull final String currencyId) {
        this.paymentMethodType = paymentMethodType;
        this.paymentMethodId = paymentMethodId;
        this.issuerId = issuerId;
        this.cardId = cardId;
        this.totalAmount = totalAmount;
        this.availableInstallments = availableInstallments;
        this.currencyId = currencyId;
    }

    public static ExpressInstallmentsView createFrom(@NonNull final ExpressMetadata expressMetadata,
        @NonNull final String currencyId, @NonNull final BigDecimal totalAmount,
        @NonNull final PayerCostRepository payerCostRepository) {

        final String paymentMethodType = expressMetadata.getPaymentTypeId();
        final String paymentMethodId = expressMetadata.getPaymentMethodId();
        final String cardId = expressMetadata.getCard().getId();
        final Long issuerId = expressMetadata.getCard().getDisplayInfo().issuerId;
        final PayerCostModel payerCostModel =
            payerCostRepository.getConfigurationFor(expressMetadata.getCard().getId());
        final List<AvailableInstallment> availableInstallments =
            AvailableInstallment.createFrom(payerCostModel.getPayerCosts(), currencyId);

        return new ExpressInstallmentsView(paymentMethodType, paymentMethodId, issuerId, cardId, totalAmount,
            availableInstallments, currencyId);
    }
}
