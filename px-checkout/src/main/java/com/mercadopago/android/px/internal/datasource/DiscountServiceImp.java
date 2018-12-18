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
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import java.util.Map;
import javax.annotation.Nonnull;

public class DiscountServiceImp implements DiscountRepository {

    /* default */ ConfigurationSolver configurationSolver;
    /* default */ Map<String, DiscountConfigurationModel> discountConfigurations;
    private final UserSelectionRepository userSelectionRepository;

    private static final DiscountConfigurationModel WITHOUT_DISCOUNT;

    static {
        WITHOUT_DISCOUNT = new DiscountConfigurationModel(null, null, false);
    }

    public DiscountServiceImp(@NonNull final GroupsRepository groupsRepository,
        @Nonnull final UserSelectionRepository userSelectionRepository) {
        this.userSelectionRepository = userSelectionRepository;

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
        discountConfigurations = null;
        configurationSolver = null;
    }

    @Nullable
    @Override
    public DiscountConfigurationModel getCurrentConfiguration() {
        final Card card = userSelectionRepository.getCard();
        // Remember to prioritize the selected discount over the rest when the selector feature is added.

        if (card == null) {
            final PaymentMethod paymentMethod = userSelectionRepository.getPaymentMethod();
            if (paymentMethod == null) {
                // The user did not select any payment method, thus the dominant discount is the general config
                return discountConfigurations.get(configurationSolver.getGenericConfigurationHash());
            } else {
                // The user select account money or an off payment method
                return discountConfigurations.get(configurationSolver.getConfigurationHashFor(paymentMethod.getId()));
            }
        } else {
            // The user has already selected a payment method, thus the dominant discount is the best between the
            // general discount and the discount associated to the payment method
            return discountConfigurations.get(configurationSolver.getConfigurationHashFor(card.getId()));
        }
    }

    @Override
    public DiscountConfigurationModel getConfigurationFor(@NonNull final String customOptionId) {
        final String hashConfiguration = configurationSolver.getConfigurationHashFor(customOptionId);
        return getConfiguration(hashConfiguration);
    }

    private DiscountConfigurationModel getConfiguration(@Nonnull final String hash) {
        final DiscountConfigurationModel discountModel = discountConfigurations.get(hash);

        if (discountModel == null) {
            return WITHOUT_DISCOUNT;
        }

        return discountModel;
    }

    @Override
    public DiscountConfigurationModel getWithoutDiscountConfiguration() {
        return WITHOUT_DISCOUNT;
    }
}