package com.mercadopago.android.px.tracking.internal.model;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.Issuer;
import com.mercadopago.android.px.model.PayerCost;

@SuppressWarnings("unused")
@Keep
public final class CardExtraInfo extends TrackingMapModel {

    @Nullable private final String cardId;
    @Nullable private final PayerCostInfo selectedInstallment;
    private final Long issuerId;
    private final boolean hasEsc;

    private CardExtraInfo(@Nullable final String cardId, final boolean hasEsc,
        @Nullable final Long issuerId,
        @Nullable final PayerCostInfo payerCostTrackModel) {
        this.cardId = cardId;
        this.hasEsc = hasEsc;
        selectedInstallment = payerCostTrackModel;
        this.issuerId = issuerId;
    }

    @NonNull
    public static CardExtraInfo savedCard(@NonNull final Card card, @NonNull final PayerCost payerCost,
        final boolean hasEsc) {
        return new CardExtraInfo(card.getId(), hasEsc,
            card.getIssuer().getId(),
            new PayerCostInfo(payerCost));
    }

    @NonNull
    public static CardExtraInfo nonSavedCardInfo(@Nullable final Issuer issuer,
        @Nullable final PayerCost payerCost) {
        return new CardExtraInfo(null, false, issuer != null ? issuer.getId() : null,
            payerCost == null ? null : new PayerCostInfo(payerCost));
    }

    @NonNull
    public static CardExtraInfo expressSavedCard(final CardMetadata card, final boolean hasEsc) {
        return new CardExtraInfo(card.getId(),
            hasEsc,
            card.getDisplayInfo().issuerId, null);
    }

    @NonNull
    public static CardExtraInfo selectedExpressSavedCard(final CardMetadata card, final int payerCost,
        final boolean hasEsc) {
        return new CardExtraInfo(card.getId(),
            hasEsc,
            card.getDisplayInfo().issuerId, new PayerCostInfo(card.getPayerCost(payerCost)));
    }

    @NonNull
    public static CardExtraInfo customOptions(final CustomSearchItem val, final boolean hasEsc) {
        return new CardExtraInfo(val.getId(), hasEsc, null, null);
    }
}
