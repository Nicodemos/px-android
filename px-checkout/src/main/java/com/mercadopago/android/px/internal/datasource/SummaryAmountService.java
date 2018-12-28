package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.constants.ProcessingModes;
import com.mercadopago.android.px.internal.core.Settings;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.services.PaymentService;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.services.InstallmentService;
import com.mercadopago.android.px.model.DifferentialPricing;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.internal.request.SummaryAmountBody;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Map;

public class SummaryAmountService implements SummaryAmountRepository {

    @NonNull private final InstallmentService installmentService;
    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final AdvancedConfiguration advancedConfiguration;
    @NonNull private final UserSelectionRepository userSelectionRepository;

    public SummaryAmountService(@NonNull final InstallmentService installmentService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final AdvancedConfiguration advancedConfiguration,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.installmentService = installmentService;
        this.paymentSettingRepository = paymentSettingRepository;
        this.advancedConfiguration = advancedConfiguration;
        this.userSelectionRepository = userSelectionRepository;
    }

    @NonNull
    @Override
    public MPCall<SummaryAmount> getSummaryAmount(@NonNull final String bin) {
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final DifferentialPricing differentialPricing = checkoutPreference.getDifferentialPricing();
        final Integer differentialPricingId = differentialPricing == null ? null : differentialPricing.getId();
        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        final Issuer issuer = userSelectionRepository.getIssuer();

        final SummaryAmountBody summaryAmountBody =
            new SummaryAmountBody(checkoutPreference.getSite().getId(), checkoutPreference.getTotalAmount(),
                checkoutPreference.getMarketplace(), checkoutPreference.getPayer().getEmail(),
                advancedConfiguration.getDiscountParamsConfiguration().getProductId(),
                paymentMethod.getId(),
                paymentMethod.getPaymentTypeId(), bin, issuer.getId(),
                advancedConfiguration.getDiscountParamsConfiguration().getLabels(),
                checkoutPreference.getDefaultInstallments(),
                differentialPricingId, ProcessingModes.AGGREGATOR,
                paymentSettingRepository.getPaymentConfiguration().getCharges());

        final Map<String, Object> body = JsonUtil.getInstance().getMapFromObject(summaryAmountBody);

        return installmentService.createSummaryAmount(Settings.servicesVersion, body,
            paymentSettingRepository.getPublicKey(), paymentSettingRepository.getPrivateKey());
    }
}
