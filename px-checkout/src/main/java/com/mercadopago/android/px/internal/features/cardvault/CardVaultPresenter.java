package com.mercadopago.android.px.internal.features.cardvault;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.base.MvpPresenter;
import com.mercadopago.android.px.internal.callbacks.FailureRecovery;
import com.mercadopago.android.px.internal.callbacks.TaggedCallback;
import com.mercadopago.android.px.internal.controllers.PaymentMethodGuessingController;
import com.mercadopago.android.px.internal.datasource.MercadoPagoESC;
import com.mercadopago.android.px.internal.features.installments.PayerCostListener;
import com.mercadopago.android.px.internal.features.installments.PayerCostSolver;
import com.mercadopago.android.px.internal.features.providers.CardVaultProvider;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.ApiUtil;
import com.mercadopago.android.px.internal.util.EscUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardInfo;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentRecovery;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import java.util.List;

public class CardVaultPresenter extends MvpPresenter<CardVaultView, CardVaultProvider>
    implements PayerCostListener, CardVault.Actions {

    @NonNull /* default */ final MercadoPagoESC mercadoPagoESC;
    @NonNull /* default */ final PayerCostRepository payerCostRepository;
    @NonNull /* default */ final UserSelectionRepository userSelectionRepository;
    @NonNull /* default */ final PaymentSettingRepository paymentSettingRepository;

    private FailureRecovery failureRecovery;
    @NonNull private final PayerCostSolver payerCostSolver;
    private String bin;

    //Activity parameters
    private PaymentRecovery paymentRecovery;

    private boolean installmentsListShown;
    private boolean issuersListShown;

    //Activity result
    protected PaymentMethod paymentMethod;

    //Card Info
    private CardInfo cardInfo;
    /* default */ Token token;
    /* default */ Card card;

    //Security Code
    /* default */ @Nullable String esc;

    public CardVaultPresenter(@NonNull final UserSelectionRepository userSelectionRepository,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final MercadoPagoESC mercadoPagoESC,
        @NonNull final PayerCostRepository payerCostRepository,
        @NonNull final PayerCostSolver payerCostSolver) {
        this.userSelectionRepository = userSelectionRepository;
        this.paymentSettingRepository = paymentSettingRepository;
        this.mercadoPagoESC = mercadoPagoESC;
        this.payerCostRepository = payerCostRepository;
        this.payerCostSolver = payerCostSolver;
    }

    public void initialize() {
        installmentsListShown = false;
        issuersListShown = false;

        if (tokenRecoveryAvailable()) {
            startTokenRecoveryFlow();
        } else if (getCard() == null) {
            startNewCardFlow();
        } else {
            startSavedCardFlow();
        }
    }

    public void setPaymentRecovery(@Nullable final PaymentRecovery paymentRecovery) {
        this.paymentRecovery = paymentRecovery;
    }

    public void setCard(final Card card) {
        this.card = card;
    }

    public void setFailureRecovery(final FailureRecovery failureRecovery) {
        this.failureRecovery = failureRecovery;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token mToken) {
        token = mToken;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod mPaymentMethod) {
        paymentMethod = mPaymentMethod;
    }

    public PaymentRecovery getPaymentRecovery() {
        return paymentRecovery;
    }

    // TODO: can we kill this and use the selected card on user selection repository?
    public Card getCard() {
        return card;
    }

    public void setESC(final String esc) {
        this.esc = esc;
    }

    public void setCardInfo(final CardInfo cardInfo) {
        this.cardInfo = cardInfo;
        if (this.cardInfo == null) {
            bin = TextUtil.EMPTY;
        } else {
            bin = this.cardInfo.getFirstSixDigits();
        }
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public int getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(paymentMethod, bin);
    }

    public boolean isInstallmentsListShown() {
        return installmentsListShown;
    }

    public boolean isIssuersListShown() {
        return issuersListShown;
    }

    public void setInstallmentsListShown(final boolean installmentsListShown) {
        this.installmentsListShown = installmentsListShown;
    }

    public void setIssuersListShown(final boolean issuersListShown) {
        this.issuersListShown = issuersListShown;
    }

    private void checkStartInstallmentsActivity() {
        if (userSelectionRepository.getPayerCost() == null) {
            installmentsListShown = true;
            getView().askForInstallments();
        } else {
            getView().finishWithResult();
        }
    }

    private void checkStartIssuersActivity() {
        if (userSelectionRepository.getIssuer() == null) {
            issuersListShown = true;
            getView().startIssuersActivity();
        } else {
            checkStartInstallmentsActivity();
        }
    }

    public void recoverFromFailure() {
        if (failureRecovery != null) {
            failureRecovery.recover();
        }
    }

    public void resolveIssuersRequest() {
        issuersListShown = true;
        checkStartInstallmentsActivity();
    }

    public void resolveInstallmentsRequest() {
        installmentsListShown = true;

        if (getCard() == null) {
            getView().finishWithResult();
        } else {
            startSecurityCodeFlowIfNeeded();
        }
    }

    public void resolveSecurityCodeRequest() {
        setToken(paymentSettingRepository.getToken());
        getView().finishWithResult();
        // esto esta raro
        //if (tokenRecoveryAvailable()) {
        //    getView().askForInstallments();
        //}
    }

    public void resolveNewCardRequest() {
        setCardInfo(new CardInfo(paymentSettingRepository.getToken()));
        checkStartIssuersActivity();
    }

    public void onResultCancel() {
        getView().cancelCardVault();
    }

    @Override
    public void onResultFinishOnError() {
        getView().finishOnErrorResult();
    }

    private void startTokenRecoveryFlow() {
        setCardInfo(new CardInfo(paymentSettingRepository.getToken()));
        setPaymentMethod(userSelectionRepository.getPaymentMethod());
        setToken(paymentSettingRepository.getToken());
        getView().askForSecurityCodeFromTokenRecovery();
    }

    private void startSavedCardFlow() {
        // here is a saved card selected
        final Card card = getCard();
        setCardInfo(new CardInfo(card));
        setPaymentMethod(card.getPaymentMethod());

        if (userSelectionRepository.getPayerCost() == null) {
            payerCostSolver.solve(this, payerCostRepository.getCurrentConfiguration().getPayerCosts());
        } else {
            // This could happen on one tap flows
            onSelectedPayerCost();
        }
    }

    private void startNewCardFlow() {
        getView().askForCardInformation();
    }

    private boolean tokenRecoveryAvailable() {
        return getPaymentRecovery() != null && getPaymentRecovery().isTokenRecoverable();
    }

    private void startSecurityCodeFlowIfNeeded() {
        if (isESCSaved()) {
            createESCToken();
        } else {
            getView().startSecurityCodeActivity();
        }
    }

    private boolean isESCSaved() {
        if (!TextUtil.isEmpty(esc)) {
            return true;
        } else {
            setESC(mercadoPagoESC.getESC(card.getId()));
            return !TextUtil.isEmpty(esc);
        }
    }

    /* default */ void createESCToken() {
        getView().showProgressLayout();

        final SavedESCCardToken escCardToken = SavedESCCardToken.createWithEsc(card.getId(), esc);
        getResourcesProvider()
            .createESCTokenAsync(escCardToken, new TaggedCallback<Token>(ApiUtil.RequestOrigin.CREATE_TOKEN) {
                @Override
                public void onSuccess(final Token token) {
                    CardVaultPresenter.this.token = token;
                    CardVaultPresenter.this.token.setLastFourDigits(card.getLastFourDigits());
                    paymentSettingRepository.configure(CardVaultPresenter.this.token);
                    MPTracker.getInstance().trackTokenId(CardVaultPresenter.this.token.getId(),
                        paymentSettingRepository.getPublicKey(),
                        paymentSettingRepository.getCheckoutPreference().getSite());
                    mercadoPagoESC.saveESC(token.getCardId(), token.getEsc());
                    if (isViewAttached()) {
                        getView().finishWithResult();
                    }
                }

                @Override
                public void onFailure(final MercadoPagoError error) {
                    if (error.isApiException() &&
                        EscUtil.isInvalidEscForApiException(error.getApiException())) {
                        mercadoPagoESC.deleteESC(escCardToken.getCardId());
                        esc = null;
                        //Start CVV screen if fail
                        if (isViewAttached()) {
                            getView().startSecurityCodeActivity();
                        }
                    } else {
                        //Retry with error screen
                        recoverCreateESCToken(error);
                    }
                }
            });
    }

    /* default */ void recoverCreateESCToken(final MercadoPagoError error) {
        if (isViewAttached()) {
            getView().showError(error, ApiUtil.RequestOrigin.CREATE_TOKEN);
            setFailureRecovery(new FailureRecovery() {
                @Override
                public void recover() {
                    createESCToken();
                }
            });
        }
    }

    @Override
    public void onEmptyOptions() {
        getView().showEmptyPayerCostScreen();
    }

    @Override
    public void onSelectedPayerCost() {
        startSecurityCodeFlowIfNeeded();
    }

    @Override
    public void displayInstallments(final List<PayerCost> payerCosts) {
        getView().askForInstallments();
    }
}
