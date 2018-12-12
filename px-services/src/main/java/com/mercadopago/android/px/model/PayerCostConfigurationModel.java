package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class PayerCostConfigurationModel implements Parcelable {

    private List<PayerCost> payerCosts;

    public PayerCostConfigurationModel(final List<PayerCost> payerCosts) {
        this.payerCosts = payerCosts;
    }

    protected PayerCostConfigurationModel(final Parcel in) {
        payerCosts = in.createTypedArrayList(PayerCost.CREATOR);
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeTypedList(payerCosts);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PayerCostConfigurationModel> CREATOR = new Creator<PayerCostConfigurationModel>() {
        @Override
        public PayerCostConfigurationModel createFromParcel(final Parcel in) {
            return new PayerCostConfigurationModel(in);
        }

        @Override
        public PayerCostConfigurationModel[] newArray(final int size) {
            return new PayerCostConfigurationModel[size];
        }
    };

    public List<PayerCost> getPayerCosts() {
        return payerCosts;
    }
}
