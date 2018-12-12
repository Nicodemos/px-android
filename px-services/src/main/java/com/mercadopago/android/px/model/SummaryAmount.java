package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;

public class SummaryAmount implements Parcelable {

    private String selectedAmountConfiguration;
    private Map<String, DiscountConfigurationModel> discountConfigurations;
    private Map<String, PayerCostConfigurationModel> payerCostConfigurations;

    protected SummaryAmount(final Parcel in) {
        selectedAmountConfiguration = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(selectedAmountConfiguration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SummaryAmount> CREATOR = new Creator<SummaryAmount>() {
        @Override
        public SummaryAmount createFromParcel(final Parcel in) {
            return new SummaryAmount(in);
        }

        @Override
        public SummaryAmount[] newArray(final int size) {
            return new SummaryAmount[size];
        }
    };

    public String getSelectedAmountConfiguration() {
        return selectedAmountConfiguration;
    }

    public PayerCostConfigurationModel getPayerCostConfiguration(final String key) {
        return payerCostConfigurations.get(key);
    }

    public DiscountConfigurationModel getDiscountConfiguration(final String key) {
        return discountConfigurations.get(key);
    }
}
