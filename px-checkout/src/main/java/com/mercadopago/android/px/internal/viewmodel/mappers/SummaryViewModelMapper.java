package com.mercadopago.android.px.internal.viewmodel.mappers;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.view.AmountDescriptorView;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.SummaryDetailDescriptorFactory;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.viewmodel.AmountLocalized;
import com.mercadopago.android.px.internal.viewmodel.TotalDetailColor;
import com.mercadopago.android.px.internal.viewmodel.TotalLocalized;
import com.mercadopago.android.px.model.ExpressMetadata;
import java.util.ArrayList;
import java.util.List;

public class SummaryViewModelMapper extends Mapper<List<ExpressMetadata>, List<SummaryView.Model>> {

    @NonNull private final PaymentSettingRepository configuration;
    @NonNull private final DiscountRepository discountRepository;
    @NonNull private final AmountRepository amountRepository;
    @NonNull private final ElementDescriptorView.Model elementDescriptorModel;

    public SummaryViewModelMapper(@NonNull final PaymentSettingRepository configuration,
        @NonNull final DiscountRepository discountRepository, @NonNull final AmountRepository amountRepository,
        @NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        this.configuration = configuration;
        this.discountRepository = discountRepository;
        this.amountRepository = amountRepository;
        this.elementDescriptorModel = elementDescriptorModel;
    }

    @Override
    public List<SummaryView.Model> map(@NonNull final List<ExpressMetadata> expressMetadataList) {
        final List<SummaryView.Model> models = new ArrayList<>();

        for (final ExpressMetadata expressMetadata : expressMetadataList) {
            models.add(createModel(elementDescriptorModel));
        }

        //TODO: Last card is Add new payment method card, add one without discount
        models.add(createModel(elementDescriptorModel));

        return models;
    }

    private SummaryView.Model createModel(final ElementDescriptorView.Model elementDescriptorModel) {
        final List<AmountDescriptorView.Model> summaryDetailList =
            new SummaryDetailDescriptorFactory(discountRepository, configuration).create();

        final AmountDescriptorView.Model totalRow = new AmountDescriptorView.Model(
            new TotalLocalized(),
            new AmountLocalized(amountRepository.getAmountWithDiscount(),
                configuration.getCheckoutPreference().getSite().getCurrencyId()),
            new TotalDetailColor());

        return new SummaryView.Model(elementDescriptorModel, summaryDetailList, totalRow);
    }
}
