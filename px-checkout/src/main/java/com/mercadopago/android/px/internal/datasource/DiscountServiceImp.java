package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import java.util.Map;
import java.util.Set;

public class DiscountServiceImp implements DiscountRepository {
    @NonNull /* default */ final DiscountStorageService discountStorageService;
    @NonNull /* default */ final PaymentSettingRepository paymentSettingRepository;
    /* default */ ConfigurationSolver configurationSolver;
    /* default */ Map<String, DiscountConfigurationModel> discountConfigurations;

    public DiscountServiceImp(@NonNull final DiscountStorageService discountStorageService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final GroupsRepository groupsRepository) {

        this.discountStorageService = discountStorageService;
        this.paymentSettingRepository = paymentSettingRepository;

        groupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                configurationSolver =
                    new ConfigurationSolverImpl(paymentMethodSearch.getSelectedAmountConfiguration(),
                        paymentMethodSearch.getCustomSearchItems());
                discountConfigurations = paymentMethodSearch.getDiscountConfigurations();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO
            }
        });
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

    @Override
    public boolean isNotAvailableDiscount() {
        return discountStorageService.isNotAvailableDiscount();
    }

    @Override
    public boolean hasValidDiscount() {
        return getDiscount() != null && getCampaign() != null;
    }

    @Nullable
    @Override
    public DiscountConfigurationModel getConfigurationFor(@NonNull final String id) {
        final String hashConfiguration = configurationSolver.getConfigurationFor(id);
        return discountConfigurations.get(hashConfiguration);
    }
}