package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;

public class ExpressMetadata implements Parcelable, Serializable {

    private final String paymentMethodId;
    private final String paymentTypeId;
    private final CardMetadata card;
    private final AccountMoneyMetadata accountMoney;

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public CardMetadata getCard() {
        return card;
    }

    public AccountMoneyMetadata getAccountMoney() {
        return accountMoney;
    }

    public boolean isCard() {
        return card != null;
    }

    protected ExpressMetadata(final Parcel in) {
        paymentMethodId = in.readString();
        paymentTypeId = in.readString();
        card = in.readParcelable(CardMetadata.class.getClassLoader());
        accountMoney = in.readParcelable(AccountMoneyMetadata.class.getClassLoader());
    }

    public static final Creator<ExpressMetadata> CREATOR = new Creator<ExpressMetadata>() {
        @Override
        public ExpressMetadata createFromParcel(final Parcel in) {
            return new ExpressMetadata(in);
        }

        @Override
        public ExpressMetadata[] newArray(final int size) {
            return new ExpressMetadata[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(paymentMethodId);
        dest.writeString(paymentTypeId);
        dest.writeParcelable(card, flags);
        dest.writeParcelable(accountMoney, flags);
    }
}
