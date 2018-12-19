package com.mercadopago.android.px.internal.features;

import android.support.annotation.NonNull;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.OnSelectedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.features.providers.InstallmentsProvider;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.SummaryAmountRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.InstallmentsUtil;
import com.mercadopago.android.px.internal.view.AmountView;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PayerCostConfigurationModel;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.SummaryAmount;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.PaymentPreference;
import com.mercadopago.android.px.services.Callback;
import java.util.List;

public class InstallmentsPresenter extends MvpPresenter<InstallmentsActivityView, InstallmentsProvider> implements
    AmountView.OnClick {

    @NonNull
    private final AmountRepository amountRepository;
    @NonNull
    private final PaymentSettingRepository configuration;
    @NonNull
    private final UserSelectionRepository userSelectionRepository;
    @NonNull
    private final DiscountRepository discountRepository;
    @NonNull
    private final SummaryAmountRepository summaryAmountRepository;

    private FailureRecovery mFailureRecovery;

    //Card Info
    private String bin = "";

    private List<PayerCost> payerCosts;
    private PaymentPreference paymentPreference;
    private CardInfo cardInfo;

    public InstallmentsPresenter(@NonNull final AmountRepository amountRepository,
        @NonNull final PaymentSettingRepository configuration,
        @NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final DiscountRepository discountRepository,
        @NonNull final SummaryAmountRepository summaryAmountRepository) {
        this.amountRepository = amountRepository;
        this.configuration = configuration;
        this.userSelectionRepository = userSelectionRepository;
        this.discountRepository = discountRepository;
        this.summaryAmountRepository = summaryAmountRepository;
    }

    public void initialize() {
        initializeAmountRow();
        showSiteRelatedInformation();
        loadPayerCosts();
    }

    private void initializeAmountRow() {
        if (isViewAttached()) {
            getView().showAmount(discountRepository.getCurrentConfiguration(),
                amountRepository.getItemsPlusCharges(), configuration.getCheckoutPreference().getSite());
        }
    }

    private void showSiteRelatedInformation() {
        if (InstallmentsUtil.shouldWarnAboutBankInterests(configuration.getCheckoutPreference().getSite().getId())) {
            getView().warnAboutBankInterests();
        }
    }

    private void loadPayerCosts() {
        if (werePayerCostsSet()) {
            resolvePayerCosts(payerCosts);
        } else {
            getPayerCosts();
        }
    }

    private boolean werePayerCostsSet() {
        return payerCosts != null;
    }

    /* default */ void resolvePayerCosts(final List<PayerCost> payerCosts) {
        final PayerCost defaultPayerCost =
            paymentPreference == null ? null : paymentPreference.getDefaultInstallments(payerCosts);
        this.payerCosts =
            paymentPreference == null ? payerCosts : paymentPreference.getInstallmentsBelowMax(payerCosts);

        if (defaultPayerCost != null) {
            userSelectionRepository.select(defaultPayerCost);
            getView().finishWithResult(defaultPayerCost);
        } else if (this.payerCosts.isEmpty()) {
            getView().showError(getResourcesProvider().getNoPayerCostFoundError(), "");
        } else if (this.payerCosts.size() == 1) {
            final PayerCost payerCost = payerCosts.get(0);
            userSelectionRepository.select(payerCost);
            getView().finishWithResult(payerCost);
        } else {
            getView().showHeader();
            getView().showInstallments(this.payerCosts, getDpadSelectionCallback());
            getView().hideLoadingView();
        }
    }

    private void getPayerCosts() {
        getView().showLoadingView();
        summaryAmountRepository.getSummaryAmount(bin).enqueue(new Callback<SummaryAmount>() {
            @Override
            public void success(final SummaryAmount summaryAmount) {
                final String key = summaryAmount.getSelectedAmountConfiguration();
                final PayerCostConfigurationModel payerCostConfiguration =
                    summaryAmount.getPayerCostConfiguration(key);
                final List<PayerCost> payerCosts = payerCostConfiguration.getPayerCosts();

                resolvePayerCosts(payerCosts);
            }

            @Override
            public void failure(final ApiException apiException) {
                getView().hideLoadingView();
                getView()
                    .showApiException(apiException, ApiUtil.RequestOrigin.POST_SUMMARY_AMOUNT);
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        getPayerCosts();
                    }
                });
            }
        });
    }

    public void setCardInfo(final CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        if (this.cardInfo != null) {
            bin = this.cardInfo.getFirstSixDigits();
        }
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public int getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(userSelectionRepository.getPaymentMethod(), bin);
    }

    public void setPayerCosts(final List<PayerCost> payerCosts) {
        this.payerCosts = payerCosts;
    }

    public void setPaymentPreference(final PaymentPreference paymentPreference) {
        this.paymentPreference = paymentPreference;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        mFailureRecovery = failureRecovery;
    }

    public FailureRecovery getFailureRecovery() {
        return mFailureRecovery;
    }

    public PaymentMethod getPaymentMethod() {
        return userSelectionRepository.getPaymentMethod();
    }

    public boolean isRequiredCardDrawn() {
        return cardInfo != null && userSelectionRepository.getPaymentMethod() != null;
    }

    private OnSelectedCallback<Integer> getDpadSelectionCallback() {
        return new OnSelectedCallback<Integer>() {
            @Override
            public void onSelected(final Integer position) {
                onItemSelected(position);
            }
        };
    }

    public String getBin() {
        return bin;
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public void onItemSelected(final int position) {
        final PayerCost selectedPayerCost = payerCosts.get(position);
        userSelectionRepository.select(selectedPayerCost);
        getView().finishWithResult(selectedPayerCost);
    }

    @Override
    public void onDetailClicked() {
        getView().showDetailDialog();
    }
}
