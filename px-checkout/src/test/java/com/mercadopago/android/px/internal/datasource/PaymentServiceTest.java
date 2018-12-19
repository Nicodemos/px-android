package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import android.support.annotation.NonNull;
import com.mercadopago.android.px.core.PaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.EscManager;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.InstructionsRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.PluginRepository;
import com.mercadopago.android.px.internal.repository.TokenRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.utils.StubFailMpCall;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @Mock private PaymentServiceHandler handler;

    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private PluginRepository pluginRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private AmountRepository amountRepository;
    @Mock private PaymentProcessor paymentProcessor;
    @Mock private Context context;
    @Mock private EscManager escManager;
    @Mock private TokenRepository tokenRepository;
    @Mock private InstructionsRepository instructionsRepository;
    @Mock private GroupsRepository groupsRepository;
    @Mock private PaymentMethodSearch paymentMethodSearch;
    @Mock private List<ExpressMetadata> expressMetadata;

    @Mock private ExpressMetadata node;
    @Mock private CardMetadata cardMetadata;
    @Mock private PayerCost payerCost;

    private PaymentService paymentService;

    private static final DiscountConfigurationModel WITHOUT_DISCOUNT =
        new DiscountConfigurationModel(null, null, false);

    @Before
    public void setUp() {
        paymentService = new PaymentService(userSelectionRepository,
            paymentSettingRepository,
            pluginRepository,
            discountRepository,
            amountRepository,
            paymentProcessor,
            context,
            escManager,
            tokenRepository,
            instructionsRepository,
            groupsRepository);

        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(mock(CheckoutPreference.class));
        when(discountRepository.getCurrentConfiguration()).thenReturn(WITHOUT_DISCOUNT);
    }

    @Test
    public void whenOneTapPaymentIsCardSelectCard() {
        final Card card = creditCardPresetMock();

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(userSelectionRepository).select(card);
    }

    @Test
    public void whenOneTapPaymentIsCardSelectPayerCost() {
        creditCardPresetMock();

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenOneTapPaymentIsCardPayerCostAndCardSet() {
        final Card card = creditCardPresetMock();

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(userSelectionRepository).select(card);
        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenSavedCardAndESCSavedThenAskTokenButFailApiCallThenCVVIsRequiered() {
        final Card card = savedCreditCardOneTapPresent();
        when(escManager.hasEsc(card)).thenReturn(true);
        when(tokenRepository.createToken(card)).thenReturn(new StubFailMpCall(mock(ApiException.class)));

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(escManager).hasEsc(card);
        verifyNoMoreInteractions(escManager);

        verify(tokenRepository).createToken(card);
        verifyNoMoreInteractions(tokenRepository);

        // if api call to tokenize fails, then ask for CVV.
        verify(handler).onCvvRequired(card);
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void whenOneTapPaymentWhenSavedCardAndESCSavedThenAskTokenSuccess() {
        final Card card = savedCreditCardOneTapPresent();
        when(escManager.hasEsc(card)).thenReturn(true);
        final MPCall<Token> tokenMPCall = mock(MPCall.class);
        when(tokenRepository.createToken(card)).thenReturn(tokenMPCall);

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(escManager).hasEsc(card);
        verifyNoMoreInteractions(escManager);
        verifyNoMoreInteractions(handler);
        verify(tokenRepository).createToken(card);
        verifyNoMoreInteractions(tokenRepository);
    }

    @Test
    public void whenOneTapPaymentWhenNotSavedCardAndESCSavedThenAskCVV() {
        final Card card = savedCreditCardOneTapPresent();
        when(escManager.hasEsc(card)).thenReturn(false);

        paymentService.attach(handler);
        paymentService.startExpressPayment(expressMetadata.get(0), payerCost);

        verify(escManager).hasEsc(card);
        verifyNoMoreInteractions(escManager);
        verifyNoMoreInteractions(tokenRepository);
    }

    @NonNull
    private Card savedCreditCardOneTapPresent() {
        final Card card = creditCardPresetMock();
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(userSelectionRepository.getPaymentMethod()).thenReturn(paymentMethod);
        when(userSelectionRepository.hasCardSelected()).thenReturn(true);
        when(userSelectionRepository.getPayerCost()).thenReturn(mock(PayerCost.class));
        when(userSelectionRepository.getCard()).thenReturn(card);
        return card;
    }

    private Card creditCardPresetMock() {
        when(groupsRepository.getGroups()).thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(expressMetadata.get(0)).thenReturn(node);
        when(node.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(node.getCard()).thenReturn(cardMetadata);
        final Card card = mock(Card.class);
        when(paymentMethodSearch.getCardById(node.getCard().getId())).thenReturn(card);
        when(paymentMethodSearch.getPaymentMethodById(node.getPaymentMethodId())).thenReturn(mock(PaymentMethod.class));
        return card;
    }
}