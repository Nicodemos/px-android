package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public class PayerCostModel implements Serializable, Parcelable {

    public static final int NO_SELECTED = -1;

    public final int selectedPayerCostIndex;
    public final List<PayerCost> payerCosts;

    public PayerCostModel(final int selectedPayerCostIndex, @Nullable final List<PayerCost> payerCosts) {
        this.selectedPayerCostIndex = selectedPayerCostIndex;
        this.payerCosts = payerCosts;
    }

    protected PayerCostModel(final Parcel in) {
        selectedPayerCostIndex = in.readInt();
        payerCosts = in.createTypedArrayList(PayerCost.CREATOR);
    }

    public List<PayerCost> getPayerCosts() {
        return payerCosts;
    }

    public int getDefaultPayerCostIndex() {
        return selectedPayerCostIndex;
    }

    public PayerCost getPayerCost(final int userSelectedPayerCost) {
        if (userSelectedPayerCost == NO_SELECTED) {
            return payerCosts.get(selectedPayerCostIndex);
        } else {
            return payerCosts.get(userSelectedPayerCost);
        }
    }

    public static final Creator<PayerCostModel> CREATOR = new Creator<PayerCostModel>() {
        @Override
        public PayerCostModel createFromParcel(final Parcel in) {
            return new PayerCostModel(in);
        }

        @Override
        public PayerCostModel[] newArray(final int size) {
            return new PayerCostModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(selectedPayerCostIndex);
        dest.writeTypedList(payerCosts);
    }
}
