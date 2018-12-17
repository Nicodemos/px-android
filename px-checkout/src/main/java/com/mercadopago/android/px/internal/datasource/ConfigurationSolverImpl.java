package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.CustomSearchItem;
import java.util.List;

import static com.mercadopago.android.px.internal.util.TextUtil.isEmpty;

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
    public String getConfigurationFor(@Nullable final String id) {
        if (!isEmpty(id)) {
            for (final CustomSearchItem customSearchItem : customSearchItems) {
                if (customSearchItem.getId() != null && customSearchItem.getId().equals(id)) {
                    return customSearchItem.getSelectedAmountConfiguration();
                }
            }
        } else if (!isEmpty(selectedAmountConfiguration)) {
            return selectedAmountConfiguration;
        }
        return "";
    }
}
