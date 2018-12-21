package com.mercadopago.android.px.internal.features.express;

import com.mercadopago.android.px.internal.features.express.slider.PaymentMethodAdapter;
import com.mercadopago.android.px.internal.repository.AmountRepository;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.GroupsRepository;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.internal.repository.PaymentRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.PaymentMethodDescriptorView;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PayerCostModel;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.Sites;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import com.mercadopago.android.px.utils.StubSuccessMpCall;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpressPaymentPresenterTest {

    @Mock
    private ExpressPayment.View view;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentSettingRepository configuration;

    @Mock
    private GroupsRepository groupsRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private PayerCostRepository payerCostRepository;

    @Mock
    private AmountRepository amountRepository;

    @Mock
    private PaymentMethodSearch paymentMethodSearch;

    @Mock
    private ExpressMetadata expressMetadata;

    @Mock
    private CardMetadata cardMetadata;

    @Mock
    private PayerCostModel payerCostModel;

    private ExpressPaymentPresenter expressPaymentPresenter;

    private static final DiscountConfigurationModel WITHOUT_DISCOUNT =
        new DiscountConfigurationModel(null, null, false);

    @Before
    public void setUp() {
        //This is needed for the presenter constructor
        final CheckoutPreference preference = mock(CheckoutPreference.class);
        when(preference.getSite()).thenReturn(Sites.ARGENTINA);
        when(preference.getItems()).thenReturn(Collections.singletonList(mock(Item.class)));
        when(configuration.getCheckoutPreference()).thenReturn(preference);
        when(groupsRepository.getGroups())
            .thenReturn(new StubSuccessMpCall<>(paymentMethodSearch));
        when(paymentMethodSearch.getExpress()).thenReturn(Collections.singletonList(expressMetadata));
        when(expressMetadata.getCard()).thenReturn(cardMetadata);
        when(expressMetadata.isCard()).thenReturn(true);
        when(cardMetadata.getId()).thenReturn("123");
        when(discountRepository.getConfigurationFor("123")).thenReturn(WITHOUT_DISCOUNT);
        when(discountRepository.getWithoutDiscountConfiguration()).thenReturn(WITHOUT_DISCOUNT);
        when(payerCostRepository.getConfigurationFor("123")).thenReturn(payerCostModel);

        expressPaymentPresenter = new ExpressPaymentPresenter(paymentRepository, configuration, discountRepository,
            amountRepository, groupsRepository, payerCostRepository);

        verifyAttachView();
    }

    @Test
    public void whenCanceledThenCancelAndTrack() {
        expressPaymentPresenter.cancel();

        verify(view).cancel();
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenViewIsResumedThenPaymentRepositoryIsAttached() {
        verifyOnViewResumed();
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenViewIsPausedThenPaymentRepositoryIsDetached() {
        verifyOnViewResumed();
        expressPaymentPresenter.onViewPaused();
        verify(paymentRepository).detach(expressPaymentPresenter);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenViewIsResumedAndPaymentRepositoryHasPaymentThenCancelLoading() {
        when(paymentRepository.hasPayment()).thenReturn(true);

        verifyOnViewResumed();
        verify(view).enableToolbarBack();
        verify(view).cancelLoading();
        verifyNoMoreInteractions(paymentRepository);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenSliderOptionSelectedThenShowInstallmentsRow() {
        final int currentElementPosition = 1;

        expressPaymentPresenter.onSliderOptionSelected(currentElementPosition);

        verify(view).hideInstallmentsSelection();
        verify(view).showInstallmentsDescriptionRow(currentElementPosition,
            PaymentMethodDescriptorView.Model.SELECTED_PAYER_COST_NONE);
        verifyNoMoreInteractions(view);
    }

    @Test
    public void whenPayerCostSelectedThenItsReflectedOnView() {
        final int paymentMethodIndex = 0;
        final int selectedPayerCostIndex = 1;
        final PayerCost firstPayerCost = mock(PayerCost.class);
        final List<PayerCost> payerCostList =
            Arrays.asList(mock(PayerCost.class), firstPayerCost, mock(PayerCost.class));
        when(payerCostModel.getPayerCosts()).thenReturn(payerCostList);

        expressPaymentPresenter.onPayerCostSelected(paymentMethodIndex, payerCostList.get(selectedPayerCostIndex));

        verify(view).hideInstallmentsSelection();
        verify(view).showInstallmentsDescriptionRow(paymentMethodIndex, selectedPayerCostIndex);
        verify(view).collapseInstallmentsSelection();
        verifyNoMoreInteractions(view);
    }

    private void verifyAttachView() {
        expressPaymentPresenter.attachView(view);

        verify(view).showToolbarElementDescriptor(any(ElementDescriptorView.Model.class));
        verify(view).configureAdapters(anyListOf(DrawableFragmentItem.class), any(Site.class), anyInt(),
            any(PaymentMethodAdapter.Model.class));
    }

    private void verifyOnViewResumed() {
        expressPaymentPresenter.onViewResumed();

        verify(paymentRepository).hasPayment();
        verify(paymentRepository).attach(expressPaymentPresenter);
    }
}