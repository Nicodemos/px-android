package com.mercadopago.android.px.internal.tracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import com.mercadopago.android.px.BuildConfig;
import com.mercadopago.android.px.core.PaymentMethodPlugin;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.PaymentModel;
import com.mercadopago.android.px.internal.features.review_and_confirm.models.SummaryModel;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.internal.util.JsonUtil;
import com.mercadopago.android.px.model.ActionEvent;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PayerCostModel;
import com.mercadopago.android.px.model.PaymentMethodSearch;
import com.mercadopago.android.px.model.PaymentMethodSearchItem;
import com.mercadopago.android.px.model.PaymentResult;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.ScreenViewEvent;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.tracking.internal.MPTracker;
import com.mercadopago.android.px.tracking.internal.model.ErrorView;
import com.mercadopago.android.px.tracking.internal.model.ExpressInstallmentsView;
import com.mercadopago.android.px.tracking.internal.model.ExpressConfirmEvent;
import com.mercadopago.android.px.tracking.internal.model.ExpressReviewView;
import com.mercadopago.android.px.tracking.internal.utils.TrackingUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mercadopago.android.px.tracking.internal.StrategyMode.NOOP_STRATEGY;

public final class Tracker {

    private Tracker() {
    }

    public static void trackConfirmExpress(@NonNull final ExpressMetadata expressMetadata,
        final PayerCost selectedPayerCost,
        @NonNull final String currencyId) {
        final ExpressConfirmEvent expressConfirmEvent =
            ExpressConfirmEvent.createFrom(expressMetadata, selectedPayerCost, currencyId);
        final Map<String, Object> map = JsonUtil.getInstance().getMapFromObject(expressConfirmEvent);
        MPTracker.getInstance().trackEvent(TrackingUtil.EVENT_PATH_REVIEW_CONFIRM, map);
    }

    public static void trackSwipeExpress() {
        MPTracker.getInstance()
            .trackEvent(TrackingUtil.EVENT_PATH_SWIPE_EXPRESS, new HashMap<String, Object>());
    }

    public static void trackAbortExpress() {
        MPTracker.getInstance()
            .trackEvent(TrackingUtil.EVENT_PATH_ABORT_EXPRESS, new HashMap<String, Object>());
    }

    public static void trackExpressInstallmentsView(@NonNull final ExpressMetadata expressMetadata,
        @NonNull final String currencyId, @NonNull final BigDecimal totalAmount,
        @NonNull final PayerCostRepository payerCostRepository) {
        final ExpressInstallmentsView expressInstallmentsView =
            ExpressInstallmentsView.createFrom(expressMetadata, currencyId, totalAmount, payerCostRepository);
        final Map<String, Object> data = JsonUtil.getInstance().getMapFromObject(expressInstallmentsView);
        MPTracker.getInstance().trackView(TrackingUtil.VIEW_PATH_EXPRESS_INSTALLMENTS_VIEW, data);
    }

    public static void trackExpressView(@NonNull final BigDecimal totalAmount, @NonNull final String currencyId,
        @Nullable final Discount discount, @Nullable final Campaign campaign, @NonNull final Iterable<Item> items,
        @NonNull final List<ExpressMetadata> expressMetadataList, @NonNull final PayerCostRepository payerCostRepository) {

        final ExpressReviewView expressReviewView = ExpressReviewView.createFrom(expressMetadataList, totalAmount, currencyId, discount, campaign, items, payerCostRepository);
        final Map<String, Object> data = JsonUtil.getInstance().getMapFromObject(expressReviewView);
        MPTracker.getInstance().trackView(TrackingUtil.VIEW_PATH_EXPRESS_REVIEW_VIEW, data);
    }

    public static void trackExpressDiscountView() {
        MPTracker.getInstance()
            .trackView(TrackingUtil.VIEW_PATH_EXPRESS_DISCOUNT_VIEW, new HashMap<String, Object>());
    }

    public static void trackGenericError(@Nullable final String path, @NonNull final ErrorView.ErrorType errorType,
        @NonNull final MercadoPagoError mercadoPagoError, @NonNull final String visibleMessage) {
        final ErrorView errorView = ErrorView.createFrom(path, mercadoPagoError, errorType, visibleMessage);
        final Map<String, Object> data = JsonUtil.getInstance().getMapFromObject(errorView);
        MPTracker.getInstance().trackEvent(TrackingUtil.EVENT_PATH_FRICTION, data);
    }

    private static void addProperties(final ScreenViewEvent.Builder builder,
        @Nullable final Iterable<Pair<String, String>> propertyList) {
        if (propertyList != null) {
            for (final Pair<String, String> property : propertyList) {
                builder.addProperty(property.first, property.second);
            }
        }
    }

    private static MPTrackingContext getTrackerContext(@NonNull final Context context) {
        final String publicKey =
            Session.getSession(context).getConfigurationModule().getPaymentSettings().getPublicKey();
        final MPTrackingContext.Builder builder =
            new MPTrackingContext.Builder(context.getApplicationContext(), publicKey)
                .setVersion(BuildConfig.VERSION_NAME);

        builder.setTrackingStrategy(NOOP_STRATEGY);

        return builder.build();
    }

    public static void trackScreen(final String screenId,
        final String screenName,
        final Context context) {

        trackScreen(screenId, screenName, context, new ArrayList<Pair<String, String>>());
    }

    public static void trackScreen(final String screenId,
        final String screenName,
        final Context context,
        @Nullable final Iterable<Pair<String, String>> properties) {

        final MPTrackingContext mpTrackingContext = getTrackerContext(context);

        final ScreenViewEvent.Builder builder = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(screenId)
            .setScreenName(screenName);

        addProperties(builder, properties);
        final ScreenViewEvent event = builder.build();
        mpTrackingContext.trackEvent(event);
    }

    public static void trackReviewAndConfirmScreen(final Context context,
        final PaymentModel paymentModel) {

        final Collection<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new Pair<>(TrackingUtil.PROPERTY_SHIPPING_INFO, TrackingUtil.HAS_SHIPPING_DEFAULT_VALUE));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, paymentModel.getPaymentType()));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, paymentModel.paymentMethodId));
        properties.add(new Pair<>(TrackingUtil.PROPERTY_ISSUER_ID, String.valueOf(paymentModel.issuerId)));

        trackScreen(TrackingUtil.VIEW_PATH_REVIEW_AND_CONFIRM,
            TrackingUtil.VIEW_PATH_REVIEW_AND_CONFIRM,
            context, properties);
    }

    public static void trackReviewAndConfirmTermsAndConditions(@NonNull final Context context) {

        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context);

        final ScreenViewEvent.Builder builder = new ScreenViewEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setScreenId(TrackingUtil.VIEW_PATH_REVIEW_TERMS_AND_CONDITIONS)
            .setScreenName(TrackingUtil.VIEW_PATH_REVIEW_TERMS_AND_CONDITIONS);

        mpTrackingContext.trackEvent(builder.build());
    }

    public static void trackCheckoutConfirm(final Context context,
        final PaymentModel paymentModel,
        final SummaryModel summaryModel) {

        final MPTrackingContext mpTrackingContext =
            getTrackerContext(context);

        final ActionEvent.Builder builder = new ActionEvent.Builder()
            .setFlowId(FlowHandler.getInstance().getFlowId())
            .setAction(TrackingUtil.ACTION_CHECKOUT_CONFIRMED)
            .setScreenId(TrackingUtil.VIEW_PATH_REVIEW_AND_CONFIRM)
            .setScreenName(TrackingUtil.VIEW_PATH_REVIEW_AND_CONFIRM)
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_TYPE_ID, paymentModel.getPaymentType())
            .addProperty(TrackingUtil.PROPERTY_PAYMENT_METHOD_ID, paymentModel.paymentMethodId)
            .addProperty(TrackingUtil.PROPERTY_PURCHASE_AMOUNT, summaryModel.getAmountToPay().toString());

        if (PaymentTypes.isCreditCardPaymentType(summaryModel.getPaymentTypeId())) {
            builder.addProperty(TrackingUtil.PROPERTY_INSTALLMENTS, String.valueOf(summaryModel.getInstallments()));
        }

        //If is saved card
        final String cardId = paymentModel.getCardId();
        if (cardId != null) {
            builder.addProperty(TrackingUtil.PROPERTY_CARD_ID, cardId);
        }

        final ActionEvent actionEvent = builder.build();
        mpTrackingContext.trackEvent(actionEvent);
    }

    public static void trackPaymentVaultScreen(final Context context,
        final PaymentMethodSearch paymentMethodSearch,
        final Set<String> escCardIds) {

        final Collection<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new Pair<>(TrackingUtil.PROPERTY_OPTIONS,
            getFormattedPaymentMethodsForTracking(context, paymentMethodSearch, escCardIds)));

        trackScreen(TrackingUtil.VIEW_PATH_PAYMENT_VAULT,
            TrackingUtil.VIEW_PATH_PAYMENT_VAULT,
            context, properties);
    }

    public static void trackPaymentVaultChildrenScreen(@NonNull final Context context,
        @NonNull final PaymentMethodSearchItem selectedItem) {

        final String selectedItemId = selectedItem.getId();

        if (TrackingUtil.GROUP_CARDS.equals(selectedItemId)) {
            trackScreen(TrackingUtil.VIEW_PATH_PAYMENT_VAULT_CARDS, TrackingUtil.VIEW_PATH_PAYMENT_VAULT_CARDS,
                context, null);
        } else {
            trackScreen(TrackingUtil.VIEW_PATH_PAYMENT_VAULT_TICKET, TrackingUtil.VIEW_PATH_PAYMENT_VAULT_TICKET,
                context, null);
        }
    }

    private static String getFormattedPaymentMethodsForTracking(final Context context,
        @NonNull final PaymentMethodSearch paymentMethodSearch, final Set<String> escCardIds) {
        final Collection<PaymentMethodPlugin> paymentMethodPluginList =
            Session.getSession(context).getPluginRepository().getEnabledPlugins();
        return TrackingFormatter
            .getFormattedPaymentMethodsForTracking(paymentMethodSearch, paymentMethodPluginList, escCardIds);
    }

    public static void trackBusinessPaymentResultScreen(@NonNull final String paymentStatus,
        @NonNull final String paymentStatusDetail,
        @NonNull final Context context) {
        final PaymentResult paymentResult =
            new PaymentResult.Builder()
                .setPaymentStatus(paymentStatus)
                .setPaymentStatusDetail(paymentStatusDetail)
                .build();
        final String screenId = getScreenIdByPaymentResult(paymentResult);

        trackScreen(screenId, screenId, context);
    }

    @NonNull
    public static String getScreenIdByPaymentResult(@NonNull final PaymentResult paymentResult) {
        if (paymentResult.isApproved() || paymentResult.isInstructions()) {
            return TrackingUtil.VIEW_PATH_PAYMENT_RESULT_APPROVED;
        } else if (paymentResult.isRejected()) {
            return TrackingUtil.VIEW_PATH_PAYMENT_RESULT_REJECTED;
        } else if (paymentResult.isPending()) {
            return TrackingUtil.VIEW_PATH_PAYMENT_RESULT_PENDING;
        }
        return "";
    }
}
