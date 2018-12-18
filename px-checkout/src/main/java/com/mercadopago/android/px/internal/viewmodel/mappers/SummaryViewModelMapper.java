package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryDetailDescriptorFactory;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.TotalDetailColor;
import com.mercadopago.android.px.internal.viewmodel.TotalLocalized;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.ArrayList;
import java.util.List;

public class SummaryViewModelMapper extends Mapper<List<ExpressMetadata>, List<SummaryView.Model>> {

    @NonNull private final CheckoutPreference checkoutPreference;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final ElementDescriptorView.Model elementDescriptorModel;

    private static final String ACCOUNT_MONEY_ID = "account_money";

    public SummaryViewModelMapper(@NonNull final CheckoutPreference checkoutPreference,
        @NonNull final DiscountRepository discountRepository, @NonNull final AmountRepository amountRepository,
        @NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        this.checkoutPreference = checkoutPreference;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.elementDescriptorModel = elementDescriptorModel;
    }

    @Override
    public List<SummaryView.Model> map(@NonNull final List<ExpressMetadata> expressMetadataList) {
        final List<SummaryView.Model> models = new ArrayList<>();

        for (final ExpressMetadata expressMetadata : expressMetadataList) {
            final String customOptionId;
            if (expressMetadata.isCard()) {
                customOptionId = expressMetadata.getCard().getId();
            } else {
                customOptionId = ACCOUNT_MONEY_ID;
            }
            models.add(createModel(discountRepository.getConfigurationFor(customOptionId),
                    elementDescriptorModel));
        }

        models.add(createModel(discountRepository.getWithoutDiscountConfiguration(), elementDescriptorModel));

        return models;
    }

    private SummaryView.Model createModel(final DiscountConfigurationModel discountModel,
        final ElementDescriptorView.Model elementDescriptorModel) {
        final List<AmountDescriptorView.Model> summaryDetailList =
            new SummaryDetailDescriptorFactory(discountModel, checkoutPreference).create();

        final AmountDescriptorView.Model totalRow = new AmountDescriptorView.Model(
            new TotalLocalized(),
            new AmountLocalized(amountRepository.getAmountWithDiscount(),
                checkoutPreference.getSite().getCurrencyId()),
            new TotalDetailColor());

        return new SummaryView.Model(elementDescriptorModel, summaryDetailList, totalRow);
    }
}
