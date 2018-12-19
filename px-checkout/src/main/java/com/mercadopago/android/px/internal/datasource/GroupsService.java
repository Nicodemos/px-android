package com.mercadopago.android.px.internal.datasource;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration;
import com.mercadopago.android.px.configuration.PaymentConfiguration;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.constants.ProcessingModes;
import com.mercadopago.android.px.internal.core.Settings;
import com.mercadopago.android.px.internal.datasource.cache.GroupsCache;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.CheckoutService;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.requests.PaymentMethodSearchBody;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class GroupsService implements GroupsRepository {

    private static final String SEPARATOR = ",";

    @NonNull private final PaymentSettingRepository paymentSettingRepository;
    @NonNull private final MercadoPagoESC mercadoPagoESC;
    @NonNull private final CheckoutService checkoutService;
    @NonNull private final String language;
    @NonNull /* default */ final GroupsCache groupsCache;

    public GroupsService(@NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final MercadoPagoESC mercadoPagoESC, @NonNull final CheckoutService checkoutService,
        @NonNull final String language, @NonNull final GroupsCache groupsCache) {
        this.paymentSettingRepository = paymentSettingRepository;
        this.mercadoPagoESC = mercadoPagoESC;
        this.checkoutService = checkoutService;
        this.language = language;
        this.groupsCache = groupsCache;
    }

    @NonNull
    @Override
    public MPCall<PaymentMethodSearch> getGroups() {
        if (groupsCache.isCached()) {
            return groupsCache.get();
        } else {
            return newCall();
        }
    }

    @NonNull
    private MPCall<PaymentMethodSearch> newCall() {
        return new MPCall<PaymentMethodSearch>() {

            @Override
            public void enqueue(final Callback<PaymentMethodSearch> callback) {
                newRequest().enqueue(getInternalCallback(callback));
            }

            @Override
            public void execute(final Callback<PaymentMethodSearch> callback) {
                newRequest().execute(getInternalCallback(callback));
            }

            @NonNull /* default */ Callback<PaymentMethodSearch> getInternalCallback(
                final Callback<PaymentMethodSearch> callback) {
                return new Callback<PaymentMethodSearch>() {
                    @Override
                    public void success(final PaymentMethodSearch paymentMethodSearch) {
                        groupsCache.put(paymentMethodSearch);
                        callback.success(paymentMethodSearch);
                    }

                    @Override
                    public void failure(final ApiException apiException) {
                        callback.failure(apiException);
                    }
                };
            }
        };
    }

    @NonNull
    /* default */ MPCall<PaymentMethodSearch> newRequest() {
        //TODO add preference service.
        final boolean expressPaymentEnabled =
            paymentSettingRepository.getAdvancedConfiguration().isExpressPaymentEnabled();
        final CheckoutPreference checkoutPreference = paymentSettingRepository.getCheckoutPreference();
        final Integer defaultInstallments = checkoutPreference.getPaymentPreference().getDefaultInstallments();

        final Collection<String> excludedPaymentTypesSet = new HashSet<>(checkoutPreference.getExcludedPaymentTypes());
        excludedPaymentTypesSet.addAll(getUnsupportedPaymentTypes(checkoutPreference.getSite()));

        final String excludedPaymentTypesAppended =
            getListAsString(new ArrayList<>(excludedPaymentTypesSet), SEPARATOR);
        final String supportedPluginsAppended = getListAsString(getPluginIds(), SEPARATOR);

        final String excludedPaymentMethodsAppended =
            getListAsString(checkoutPreference.getExcludedPaymentMethods(), SEPARATOR);
        final String cardsWithEscAppended = getListAsString(new ArrayList<>(mercadoPagoESC.getESCCardIds()), SEPARATOR);

        final Integer differentialPricingId =
            checkoutPreference.getDifferentialPricing() != null ? checkoutPreference.getDifferentialPricing()
                .getId() : null;

        final DiscountParamsConfiguration discountParamsConfiguration =
            paymentSettingRepository.getAdvancedConfiguration().getDiscountParamsConfiguration();

        final PaymentMethodSearchBody body = new PaymentMethodSearchBody.Builder()
            .setPrivateKey(paymentSettingRepository.getPrivateKey())
            .setPayerEmail(paymentSettingRepository.getCheckoutPreference().getPayer().getEmail())
            .setMarketplace(checkoutPreference.getMarketplace())
            .setProductId(discountParamsConfiguration.getProductId())
            .setLabels(discountParamsConfiguration.getLabels()).build();

        return checkoutService
            .getPaymentMethodSearch(Settings.servicesVersion, language, paymentSettingRepository.getPublicKey(),
                checkoutPreference.getTotalAmount(), excludedPaymentTypesAppended, excludedPaymentMethodsAppended,
                checkoutPreference.getSite().getId(), ProcessingModes.AGGREGATOR, cardsWithEscAppended,
                supportedPluginsAppended, differentialPricingId, defaultInstallments, expressPaymentEnabled, body);
    }

    @NonNull
    private List<String> getPluginIds() {
        final PaymentConfiguration paymentConfiguration = paymentSettingRepository.getPaymentConfiguration();
        if (paymentConfiguration != null) {
            final Collection<PaymentMethodPlugin> paymentMethodPluginList =
                paymentConfiguration.getPaymentMethodPluginList();
            final List<String> ids = new ArrayList<>();
            for (final PaymentMethodPlugin plugin : paymentMethodPluginList) {
                ids.add(plugin.getId());
            }
            return ids;
        } else {
            return new ArrayList<>();
        }
    }

    private Collection<String> getUnsupportedPaymentTypes(@NonNull final Site site) {
        final Collection<String> unsupportedTypesForSite = new ArrayList<>();
        if (Sites.CHILE.getId().equals(site.getId())
            || Sites.VENEZUELA.getId().equals(site.getId())
            || Sites.COLOMBIA.getId().equals(site.getId())) {

            unsupportedTypesForSite.add(PaymentTypes.TICKET);
            unsupportedTypesForSite.add(PaymentTypes.ATM);
            unsupportedTypesForSite.add(PaymentTypes.BANK_TRANSFER);
        }
        return unsupportedTypesForSite;
    }

    private String getListAsString(@NonNull final List<String> list, final String separator) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final String typeId : list) {
            stringBuilder.append(typeId);
            if (!typeId.equals(list.get(list.size() - 1))) {
                stringBuilder.append(separator);
            }
        }
        return stringBuilder.toString();
    }
}
