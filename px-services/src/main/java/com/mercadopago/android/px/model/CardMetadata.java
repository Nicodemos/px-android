package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.Serializable;
import java.util.List;

public class CardMetadata implements Parcelable, Serializable {

    private final String id;
    private final CardDisplayInfo displayInfo;

    protected CardMetadata(final Parcel in) {
        id = in.readString();
        displayInfo = in.readParcelable(CardDisplayInfo.class.getClassLoader());
    }

    public static final Creator<CardMetadata> CREATOR = new Creator<CardMetadata>() {
        @Override
        public CardMetadata createFromParcel(final Parcel in) {
            return new CardMetadata(in);
        }

        @Override
        public CardMetadata[] newArray(final int size) {
            return new CardMetadata[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(id);
        dest.writeParcelable(displayInfo, flags);
    }

    public String getId() {
        return id;
    }

    public int getDefaultPayerCostIndex() {
        //TODO remove when PayerCostSolver is finished
        return 0;
    }

    public List<PayerCost> getPayerCosts() {
        //TODO remove when PayerCostSolver is finished
        return null;
    }

    public CardDisplayInfo getDisplayInfo() {
        return displayInfo;
    }

    public PayerCost getPayerCost(final int userSelectedPayerCost) {
        //TODO remove when PayerCostSolver is finished
        /*
        if (userSelectedPayerCost == -1) {
            return payerCosts.get(defaultPayerCostIndex);
        } else {
            return payerCosts.get(userSelectedPayerCost);
        }
        */
        return null;
    }
}
