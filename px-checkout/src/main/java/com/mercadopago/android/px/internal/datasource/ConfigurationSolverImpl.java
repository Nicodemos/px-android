package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.PayerCostModel;
import java.util.List;
import javax.annotation.Nonnull;

public class ConfigurationSolverImpl implements ConfigurationSolver {

    @NonNull private final String selectedAmountConfiguration;
    @NonNull private final List<CustomSearchItem> customSearchItems;

    public ConfigurationSolverImpl(
        @NonNull final String selectedAmountConfiguration,
        @NonNull final List<CustomSearchItem> customSearchItems) {
        this.selectedAmountConfiguration = selectedAmountConfiguration;
        this.customSearchItems = customSearchItems;
    }

    @Override
    @NonNull
    public String getConfigurationHashFor(@Nonnull final String customOptionId) {
        for (final CustomSearchItem customSearchItem : customSearchItems) {
            if (customSearchItem.getId() != null && customSearchItem.getId().equals(customOptionId)) {
                return customSearchItem.getSelectedAmountConfiguration();
            }
        }
        return "";
    }

    @Override
    @NonNull
    public String getGenericConfigurationHash() {
        return selectedAmountConfiguration;
    }

    @Override
    @Nullable
    public PayerCostModel getPayerCostConfigurationFor(@NonNull final String customOptionId) {
        for (final CustomSearchItem customSearchItem : customSearchItems) {
            if (customSearchItem.getId() != null && customSearchItem.getId().equals(customOptionId)) {
                return customSearchItem.getPayerCostConfiguration(customSearchItem.getSelectedAmountConfiguration());
            }
        }
        return null;
    }

    @Override
    @Nullable
    public PayerCostModel getPayerCostConfigurationFor(@NonNull final String customOptionId,
        @NonNull final String configurationHash) {
        for (final CustomSearchItem customSearchItem : customSearchItems) {
            if (customSearchItem.getId() != null && customSearchItem.getId().equals(customOptionId)) {
                return customSearchItem.getPayerCostConfiguration(configurationHash);
            }
        }
        return null;
    }
}

