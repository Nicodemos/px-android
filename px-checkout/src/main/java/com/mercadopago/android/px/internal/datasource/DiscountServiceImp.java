package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.DiscountConfiguration;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import java.util.Set;

public class DiscountServiceImp implements DiscountRepository {

    @NonNull /* default */ final DiscountStorageService discountStorageService;

    @NonNull /* default */ final PaymentSettingRepository paymentSettingRepository;

    public DiscountServiceImp(@NonNull final DiscountStorageService discountStorageService,
        @NonNull final PaymentSettingRepository paymentSettingRepository) {
        this.discountStorageService = discountStorageService;
        this.paymentSettingRepository = paymentSettingRepository;
    }

    @Override
    public void configureMerchantDiscountManually(@Nullable final PaymentConfiguration paymentConfiguration) {
        if (paymentConfiguration != null && paymentConfiguration.getDiscountConfiguration() != null) {
            //TODO refactor - new way to configure with flow - but supporting compatibility.
            final DiscountConfiguration discountConfiguration = paymentConfiguration.getDiscountConfiguration();
            discountStorageService.configureDiscountManually(discountConfiguration.getDiscount(),
                discountConfiguration.getCampaign(), discountConfiguration.isNotAvailable());
        }
    }

    @Override
    public void configureExtraData(@Nullable final DiscountParamsConfiguration discountParamsConfiguration) {
        if (discountParamsConfiguration != null) {
            discountStorageService
                .configureExtraData(discountParamsConfiguration.getLabels(),
                    discountParamsConfiguration.getProductId());
        }
    }

    @Override
    public void reset() {
        discountStorageService.reset();
    }

    @Nullable
    @Override
    public Discount getDiscount() {
        return discountStorageService.getDiscount();
    }

    @Nullable
    @Override
    public Campaign getCampaign() {
        return discountStorageService.getCampaign();
    }

    @Nullable
    @Override
    public Set<String> getLabels() {
        return discountStorageService.getLabels();
    }

    @Nullable
    @Override
    public String getFlow() {
        return discountStorageService.getProductId();
    }

    @Override
    public boolean isNotAvailableDiscount() {
        return discountStorageService.isNotAvailableDiscount();
    }

    @Override
    public boolean hasValidDiscount() {
        return getDiscount() != null && getCampaign() != null;
    }
}