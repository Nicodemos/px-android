package com.mercadopago.android.px.internal.view;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.util.textformatter.TextFormatter;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import java.util.Locale;
import javax.annotation.Nonnull;

public class DiscountDetail extends CompactComponent<DiscountDetail.Props, Void> {

    public static class Props {

        @NonNull
        /* default */ final DiscountConfigurationModel discountModel;

        public Props(@NonNull final DiscountConfigurationModel discountModel) {
            this.discountModel = discountModel;
        }
    }

    public DiscountDetail(final Props props) {
        super(props);
    }

    @Override
    public View render(@Nonnull final ViewGroup parent) {
        final View mainContainer = inflate(parent, R.layout.px_view_discount_detail);
        configureSubtitleMessage(mainContainer);
        configureDetailMessage(mainContainer);
        configureSubDetailsMessage(mainContainer);
        return mainContainer;
    }

    private void configureSubDetailsMessage(final View mainContainer) {
        if (!props.discountModel.isAvailable()) {
            mainContainer.findViewById(R.id.px_discount_detail_line).setVisibility(View.GONE);
            mainContainer.findViewById(R.id.px_discount_sub_details).setVisibility(View.GONE);
        }
    }

    private void configureSubtitleMessage(final View mainContainer) {
        final TextView subtitleMessage = mainContainer.findViewById(R.id.subtitle);
        if (isMaxCouponAmountApplicable(props.discountModel)) {
            TextFormatter.withCurrencyId(props.discountModel.getDiscount().getCurrencyId())
                .withSpace()
                .amount(props.discountModel.getCampaign().getMaxCouponAmount())
                .normalDecimals()
                .into(subtitleMessage)
                .holder(R.string.px_max_coupon_amount);
        } else {
            subtitleMessage.setVisibility(View.GONE);
        }
    }

    private void configureDetailMessage(final View mainContainer) {
        final TextView detailTextView = mainContainer.findViewById(R.id.detail);
        if (!props.discountModel.isAvailable()) {
            configureNotAvailableDiscountDetail(detailTextView, mainContainer);
        } else if (isMaxCouponAmountApplicable(props.discountModel)) {
            if (isAlwaysOnApplicable(props.discountModel)) {
                setDetailMessage(detailTextView, R.string.px_always_on_discount_detail, mainContainer);
            } else {
                setDetailMessage(detailTextView, R.string.px_one_shot_discount_detail, mainContainer);
            }
        } else {
            detailTextView.setVisibility(View.GONE);
        }
    }

    private void configureNotAvailableDiscountDetail(final TextView detailTextView, final View mainContainer) {
        setDetailMessage(detailTextView, R.string.px_used_up_discount_detail, mainContainer);
        ViewUtils.setMarginBottomInView(detailTextView,
            mainContainer.getContext().getResources().getDimensionPixelSize(R.dimen.px_xxs_margin));
    }

    private void setDetailMessage(final TextView detailTextView, final int detailId, final View view) {
        final String detailMessage = view.getResources().getString(detailId);

        if (isEndDateApplicable(props.discountModel)) {
            final String endDateMessage = view.getResources().getString(R.string.px_discount_detail_end_date,
                props.discountModel.getCampaign().getPrettyEndDate());
            detailTextView.setText(String.format(Locale.getDefault(), "%s %s", detailMessage, endDateMessage));
        } else {
            detailTextView.setText(detailMessage);
        }
    }

    @VisibleForTesting
    private boolean isEndDateApplicable(final DiscountConfigurationModel discountModel) {
        return discountModel.hasValidDiscount() && (discountModel.getCampaign().hasEndDate() &&
            discountModel.isAvailable());
    }

    @VisibleForTesting
    private boolean isMaxCouponAmountApplicable(final DiscountConfigurationModel discountModel) {
        return discountModel.hasValidDiscount() && (discountModel.getCampaign().hasMaxCouponAmount() &&
            discountModel.isAvailable());
    }

    @VisibleForTesting
    private boolean isAlwaysOnApplicable(final DiscountConfigurationModel discountModel) {
        return discountModel.hasValidDiscount() && (discountModel.getCampaign().isAlwaysOnDiscount() &&
            discountModel.isAvailable());
    }
}
