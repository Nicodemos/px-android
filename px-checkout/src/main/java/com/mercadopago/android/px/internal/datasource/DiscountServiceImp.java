package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import java.util.Map;

public class DiscountServiceImp implements DiscountRepository {

    /* default */ ConfigurationSolver configurationSolver;
    /* default */ Map<String, DiscountConfigurationModel> discountConfigurations;

    @Nullable private String defaultSelectedGuessingConfiguration;
    @NonNull private final GroupsRepository groupsRepository;
    private final UserSelectionRepository userSelectionRepository;

    public DiscountServiceImp(@NonNull final GroupsRepository groupsRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {
        this.groupsRepository = groupsRepository;
        this.userSelectionRepository = userSelectionRepository;
    }

    @NonNull
    @Override
    public DiscountConfigurationModel getCurrentConfiguration() {
        // TODO: remove
        init();

        final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
        final Card card = userSelectionRepository.getCard();
        // Remember to prioritize the selected discount over the rest when the selector feature is added.
        // TODO: refactor with solver.
        if (card == null) {
            if (paymentMethod == null) {
                // The user did not select any payment method, thus the dominant discount is the general config
                return getConfiguration(configurationSolver.getDefaultSelectedAmountConfiguration());
            } else {
                if (PaymentTypes.isCardPaymentType(paymentMethod.getPaymentTypeId())) {
                    // Guessing card config
                    return getConfiguration(defaultSelectedGuessingConfiguration);
                } else {
                    // The user select account money or an off payment method / everything else.
                    return getConfiguration(configurationSolver.getConfigurationHashFor(paymentMethod.getId()));
                }
            }
        } else {
            // The user has already selected a payment method, thus the dominant discount is the best between the
            // general discount and the discount associated to the payment method
            return getConfiguration(configurationSolver.getConfigurationHashFor(card.getId()));
        }
    }

    @Override
    public DiscountConfigurationModel getConfigurationFor(@NonNull final String customOptionId) {
        init();
        return getConfiguration(configurationSolver.getConfigurationHashFor(customOptionId));
    }



    private DiscountConfigurationModel getConfiguration(@Nullable final String hash) {
        // TODO: remove
        init();
        final DiscountConfigurationModel discountModel = discountConfigurations.get(hash);
        final DiscountConfigurationModel defaultConfig =
            discountConfigurations.get(configurationSolver.getDefaultSelectedAmountConfiguration());
        if(discountModel  == null && defaultConfig == null) return DiscountConfigurationModel.NONE;
        return discountModel == null ? defaultConfig : discountModel;
    }

    //TODO: remove init call.
    private void init() {
        if (configurationSolver != null && discountConfigurations != null) {
            return;
        }

        groupsRepository.getGroups().execute(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                configurationSolver =
                    new ConfigurationSolverImpl(paymentMethodSearch.getDefaultAmountConfiguration(),
                        paymentMethodSearch.getCustomSearchItems());
                discountConfigurations = paymentMethodSearch.getDiscountsConfigurations();
            }

            @Override
            public void failure(final ApiException apiException) {
                //TODO
            }
        });
    }

    @Override
    public void addConfigurations(@NonNull final SummaryAmount summaryAmount) {
        // TODO: remove
        init();
        discountConfigurations.putAll(summaryAmount.getDiscountConfigurations());
        defaultSelectedGuessingConfiguration = summaryAmount.getDefaultAmountConfiguration();
    }
}