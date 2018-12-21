package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.PayerCostModel;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;

public class PayerCostRepositoryImpl implements PayerCostRepository {

    /* default */ ConfigurationSolver configurationSolver;
    private final UserSelectionRepository userSelectionRepository;

    public PayerCostRepositoryImpl(@NonNull final GroupsRepository groupsRepository,
        @NonNull final UserSelectionRepository userSelectionRepository) {

        this.userSelectionRepository = userSelectionRepository;

        groupsRepository.getGroups().enqueue(new Callback<PaymentMethodSearch>() {
            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                configurationSolver =
                    new ConfigurationSolverImpl(paymentMethodSearch.getSelectedAmountConfiguration(),
                        paymentMethodSearch.getCustomSearchItems());
            }

            @Override
            public void failure(final ApiException apiException) {
                configurationSolver = new ConfigurationSolverImpl("", new ArrayList<CustomSearchItem>());
            }
        });
    }

    @Nullable
    @Override
    public PayerCostModel getCurrentConfiguration() {
        final Card card = userSelectionRepository.getCard();
        // Remember to prioritize the selected discount over the rest when the selector feature is added.

        if (card == null) {
            // Account money was selected, neither plugins nor off methods should apply payer costs
            return null;
        } else {
            return configurationSolver.getPayerCostConfigurationFor(card.getId());
        }
    }

    @Override
    @Nullable
    public PayerCostModel getConfigurationFor(@NonNull final String customOptionId) {
        final String configurationHash = configurationSolver.getConfigurationHashFor(customOptionId);
        return configurationSolver.getPayerCostConfigurationFor(customOptionId, configurationHash);
    }
}