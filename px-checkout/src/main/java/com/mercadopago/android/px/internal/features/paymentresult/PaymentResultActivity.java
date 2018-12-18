package com.mercadopago.android.px.internal.features.paymentresult;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import com.mercadolibre.android.ui.widgets.MeliSnackbar;
import com.mercadopago.android.px.BuildConfig;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.configuration.PaymentResultScreenConfiguration;
import com.mercadopago.android.px.core.MercadoPagoCheckout;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.Constants;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationComment;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationCommentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationTime;
import com.mercadopago.android.px.internal.features.paymentresult.components.AccreditationTimeRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.Body;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyError;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyErrorRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.BodyRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionComponent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionComponentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionInteractionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionReferenceComponent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionReferenceRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.Instructions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsAction;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActionRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActions;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsActionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsContent;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsContentRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsReferences;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsReferencesRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSecondaryInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSecondaryInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSubtitle;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsSubtitleRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsTertiaryInfo;
import com.mercadopago.android.px.internal.features.paymentresult.components.InstructionsTertiaryInfoRenderer;
import com.mercadopago.android.px.internal.features.paymentresult.components.PaymentResultContainer;
import com.mercadopago.android.px.internal.features.paymentresult.props.PaymentResultProps;
import com.mercadopago.android.px.internal.repository.DiscountRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.tracker.MPTrackingContext;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.internal.view.Component;
import com.mercadopago.android.px.internal.view.ComponentManager;
import com.mercadopago.android.px.internal.view.LoadingComponent;
import com.mercadopago.android.px.internal.view.LoadingRenderer;
import com.mercadopago.android.px.internal.view.RendererFactory;
import com.mercadopago.android.px.internal.viewmodel.ChangePaymentMethodPostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.RecoverPaymentPostPaymentAction;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;

import static com.mercadopago.android.px.internal.features.Constants.RESULT_ACTION;
import static com.mercadopago.android.px.internal.features.Constants.RESULT_CUSTOM_EXIT;

public class PaymentResultActivity extends AppCompatActivity implements PaymentResultNavigator {

    public static final String CONGRATS_DISPLAY_BUNDLE = "congrats_display";
    public static final String PAYMENT_RESULT_BUNDLE = "payment_result";

    private static final String EXTRA_CONFIRM_PAYMENT_ORIGIN = "extra_confirm_payment_origin";
    private static final String EXTRA_PAYMENT_RESULT = "extra_payment_result";

    public static final String EXTRA_RESULT_CODE = "extra_result_code";

    private PaymentResultPresenter presenter;
    private Integer congratsDisplay;

    private PaymentResultPropsMutator mutator;

    public static Intent getIntent(@NonNull final Context context, @NonNull final PaymentResult result,
        @NonNull final PostPaymentAction.OriginAction confirmPaymentOrigin) {

        final Intent resultIntent = new Intent(context, PaymentResultActivity.class);
        //TODO remove
        resultIntent.putExtra(EXTRA_PAYMENT_RESULT, JsonUtil.getInstance().toJson(result));
        resultIntent.putExtra(EXTRA_CONFIRM_PAYMENT_ORIGIN, confirmPaymentOrigin.ordinal());
        return resultIntent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PaymentSettingRepository paymentSettings =
            Session.getSession(this).getConfigurationModule().getPaymentSettings();
        final PaymentResultScreenConfiguration paymentResultScreenConfiguration =
            paymentSettings.getAdvancedConfiguration().getPaymentResultScreenConfiguration();
        presenter = new PaymentResultPresenter(this,
            paymentSettings, Session.getSession(this).getInstructionsRepository());

        mutator = new PaymentResultPropsMutator(new PaymentResultProps.Builder(
            paymentResultScreenConfiguration).build());

        getActivityParameters();

        final PaymentResultProvider paymentResultProvider = new PaymentResultProviderImpl(this);

        presenter.attachResourcesProvider(paymentResultProvider);

        final ComponentManager componentManager = new ComponentManager(this);

        RendererFactory.register(Body.class, BodyRenderer.class);
        RendererFactory.register(LoadingComponent.class, LoadingRenderer.class);
        RendererFactory.register(Instructions.class, InstructionsRenderer.class);
        RendererFactory.register(InstructionsSubtitle.class, InstructionsSubtitleRenderer.class);
        RendererFactory.register(InstructionsContent.class, InstructionsContentRenderer.class);
        RendererFactory.register(InstructionsInfo.class, InstructionsInfoRenderer.class);
        RendererFactory.register(InstructionsReferences.class, InstructionsReferencesRenderer.class);
        RendererFactory.register(InstructionReferenceComponent.class, InstructionReferenceRenderer.class);
        RendererFactory.register(InstructionInteractionComponent.class, InstructionInteractionComponentRenderer.class);
        RendererFactory.register(InstructionInteractions.class, InstructionInteractionsRenderer.class);
        RendererFactory.register(AccreditationTime.class, AccreditationTimeRenderer.class);
        RendererFactory.register(AccreditationComment.class, AccreditationCommentRenderer.class);
        RendererFactory.register(InstructionsSecondaryInfo.class, InstructionsSecondaryInfoRenderer.class);
        RendererFactory.register(InstructionsTertiaryInfo.class, InstructionsTertiaryInfoRenderer.class);
        RendererFactory.register(InstructionsActions.class, InstructionsActionsRenderer.class);
        RendererFactory.register(InstructionsAction.class, InstructionsActionRenderer.class);
        RendererFactory.register(BodyError.class, BodyErrorRenderer.class);

        final Component root = new PaymentResultContainer(componentManager,
            new PaymentResultProps.Builder(
                paymentResultScreenConfiguration).build(),
            paymentResultProvider);
        componentManager.setActionsListener(presenter);
        componentManager.setComponent(root);
        mutator.setPropsListener(componentManager);
        mutator.renderDefaultProps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.attachView(mutator);
        presenter.initialize();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void showApiExceptionError(final ApiException exception, final String requestOrigin) {
        ErrorUtil.showApiExceptionError(this, exception, requestOrigin);
    }

    @Override
    public void showError(final MercadoPagoError error, final String requestOrigin) {
        if (error != null && error.isApiException()) {
            showApiExceptionError(error.getApiException(), requestOrigin);
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (presenter != null) {
            outState.putString(PAYMENT_RESULT_BUNDLE, JsonUtil.getInstance().toJson(presenter.getPaymentResult()));
        }

        outState.putInt(CONGRATS_DISPLAY_BUNDLE, congratsDisplay);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        final PaymentResult paymentResult =
            JsonUtil.getInstance().fromJson(savedInstanceState.getString(PAYMENT_RESULT_BUNDLE), PaymentResult.class);

        congratsDisplay = savedInstanceState.getInt(CONGRATS_DISPLAY_BUNDLE, -1);

        final Session session = Session.getSession(this);
        presenter = new PaymentResultPresenter(this,
            session.getConfigurationModule().getPaymentSettings(),
            session.getInstructionsRepository());
        presenter.setPaymentResult(paymentResult);

        final PaymentResultProvider provider = new PaymentResultProviderImpl(this);
        presenter.attachResourcesProvider(provider);

        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void getActivityParameters() {

        final Intent intent = getIntent();
        final PaymentResult paymentResult =
            JsonUtil.getInstance().fromJson(intent.getExtras().getString(EXTRA_PAYMENT_RESULT), PaymentResult.class);

        presenter.setPaymentResult(paymentResult);

        final int originIndex = intent.getIntExtra(EXTRA_CONFIRM_PAYMENT_ORIGIN, -1);
        if (originIndex != -1) {
            presenter.setOriginAction(PostPaymentAction.OriginAction.values()[originIndex]);
        }

        congratsDisplay = intent.getIntExtra(CONGRATS_DISPLAY_BUNDLE, -1);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.Activities.CONGRATS_REQUEST_CODE) {
            finishWithOkResult(resultCode, data);
        } else if (requestCode == Constants.Activities.PENDING_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.REJECTION_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.CALL_FOR_AUTHORIZE_REQUEST_CODE) {
            resolveRequest(resultCode, data);
        } else if (requestCode == Constants.Activities.INSTRUCTIONS_REQUEST_CODE) {
            finishWithOkResult(resultCode, data);
        } else {
            finishWithCancelResult(data);
        }
    }

    @Override
    public void onBackPressed() {
        finishWithResult(MercadoPagoCheckout.PAYMENT_RESULT_CODE);
    }

    private void resolveRequest(final int resultCode, final Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            finishWithCancelResult(data);
        } else {
            finishWithOkResult(resultCode, data);
        }
    }

    private void finishWithCancelResult(final Intent data) {
        setResult(RESULT_CANCELED, data);
        finish();
    }

    private void finishWithOkResult(final int resultCode, final Intent data) {
        setResult(resultCode, data);
        finish();
    }

    @Override
    public void openLink(final String url) {
        //TODO agregar try catch
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void finishWithResult(final int resultCode) {
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_RESULT_CODE, resultCode);
        setResult(RESULT_CUSTOM_EXIT, intent);
        finish();
    }

    @Override
    public void changePaymentMethod() {
        final Intent returnIntent = new Intent();
        new ChangePaymentMethodPostPaymentAction().addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @Override
    public void recoverPayment(@NonNull final PostPaymentAction.OriginAction originAction) {
        final Intent returnIntent = new Intent();
        new RecoverPaymentPostPaymentAction(originAction).addToIntent(returnIntent);
        setResult(RESULT_ACTION, returnIntent);
        finish();
    }

    @Override
    public void trackScreen(final ScreenViewEvent event) {
        final String publicKey = Session.getSession(this).getConfigurationModule().getPaymentSettings().getPublicKey();
        final MPTrackingContext mpTrackingContext = new MPTrackingContext.Builder(this, publicKey)
            .setVersion(BuildConfig.VERSION_NAME)
            .build();

        mpTrackingContext.trackEvent(event);
    }

    @Override
    public void copyToClipboard(@NonNull final String content) {
        final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText("", content);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            MeliSnackbar.make(findViewById(R.id.mpsdkPaymentResultContainer),
                getString(R.string.px_copied_to_clipboard_ack),
                Snackbar.LENGTH_SHORT, MeliSnackbar.SnackbarType.SUCCESS).show();
        }
    }
}
