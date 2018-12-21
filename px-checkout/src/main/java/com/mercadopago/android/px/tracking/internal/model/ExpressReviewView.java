package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.repository.PayerCostRepository;
import com.mercadopago.android.px.model.Campaign;
import com.mercadopago.android.px.model.Discount;
import com.mercadopago.android.px.model.ExpressMetadata;
import com.mercadopago.android.px.model.Item;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PayerCostModel;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExpressReviewView implements Serializable {

    private List<PaymentMethodInfo> availableMethods;
    private BigDecimal totalAmount;
    private String currencyId;
    private DiscountInfo discount;
    private List<ItemInfo> items;

    public ExpressReviewView(@NonNull final List<PaymentMethodInfo> availableMethods,
        @NonNull final BigDecimal totalAmount, @NonNull final String currencyId, @Nullable final DiscountInfo discount,
        @NonNull final List<ItemInfo> items) {
        this.availableMethods = availableMethods;
        this.totalAmount = totalAmount;
        this.currencyId = currencyId;
        this.discount = discount;
        this.items = items;
    }

    public static ExpressReviewView createFrom(@NonNull final List<ExpressMetadata> expressMetadataList,
        @NonNull final BigDecimal totalAmount, @NonNull final String currencyId,
        @Nullable final Discount discount, @Nullable final Campaign campaign, @NonNull final Iterable<Item> items, @NonNull
        final PayerCostRepository payerCostRepository) {
        DiscountInfo discountInfo = null;
        //A discount always comes with a campaign
        if (discount != null && campaign != null) {
            if (discount.hasPercentOff()) {
                discountInfo = new PercentageDiscountInfo(discount.getPercentOff(), discount.getCouponAmount(),
                    campaign.getMaxCouponAmount(), campaign.getMaxRedeemPerUser());
            } else {
                discountInfo = new FixedDiscountInfo(discount.getAmountOff(), discount.getCouponAmount(),
                    campaign.getMaxCouponAmount(), campaign.getMaxRedeemPerUser());
            }
        }
        final List<ItemInfo> itemInfoList = new ArrayList<>();
        for (final Item item : items) {
            final ItemInfo itemInfo =
                new ItemInfo(item.getId(), item.getDescription(), item.getUnitPrice(), item.getQuantity(),
                    currencyId);
            itemInfoList.add(itemInfo);
        }

        final List<PaymentMethodInfo> paymentMethodInfoList = new ArrayList<>();
        for (final ExpressMetadata expressMetadata : expressMetadataList) {
            paymentMethodInfoList.add(PaymentMethodInfo.createFrom(expressMetadata, currencyId, payerCostRepository));
        }

        return new ExpressReviewView(paymentMethodInfoList, totalAmount, currencyId, discountInfo, itemInfoList);
    }
}
