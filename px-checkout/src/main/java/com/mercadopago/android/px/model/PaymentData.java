package com.mercadopago.android.px.model;

import android.support.annotation.Nullable;
import com.mercadopago.android.px.internal.util.TextUtil;
import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentData implements Serializable {
    private BigDecimal transactionAmount;
    private PaymentMethod paymentMethod;
    private Issuer issuer;
    private PayerCost payerCost;
    private Token token;
    private Payer payer;

    /**
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    private String couponCode;

    @Nullable private Discount discount;
    @Nullable private Campaign campaign;

    @Nullable
    public Campaign getCampaign() {
        return campaign;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(final Issuer issuer) {
        this.issuer = issuer;
    }

    public PayerCost getPayerCost() {
        return payerCost;
    }

    public void setPayerCost(final PayerCost payerCost) {
        this.payerCost = payerCost;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(final Token token) {
        this.token = token;
    }

    public void setDiscount(@Nullable final Discount discount) {
        this.discount = discount;
    }

    @Nullable
    public Discount getDiscount() {
        return discount;
    }

    public Payer getPayer() {
        return payer;
    }

    public void setPayer(final Payer payer) {
        this.payer = payer;
    }

    /**
     * @param couponCode The coupon code
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    public void setCouponCode(final String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * @return code
     * @deprecated CouponCode discount is not supported anymore.
     */
    @Deprecated
    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(final BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public boolean containsCardInfo() {
        return getToken() != null && !TextUtil.isEmpty(getToken().getCardId());
    }

    public void setCampaign(@Nullable final Campaign campaign) {
        this.campaign = campaign;
    }
}
