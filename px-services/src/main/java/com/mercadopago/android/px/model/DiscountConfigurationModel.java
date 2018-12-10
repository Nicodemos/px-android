package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class DiscountConfigurationModel implements Parcelable {

    private final Discount discount;
    private final Campaign campaign;
    private final boolean isAvailable;

    public DiscountConfigurationModel(@NonNull final Discount discount, @NonNull final Campaign campaign,
        final boolean isAvailable) {
        this.discount = discount;
        this.campaign = campaign;
        this.isAvailable = isAvailable;
    }

    protected DiscountConfigurationModel(final Parcel in) {
        discount = in.readParcelable(Discount.class.getClassLoader());
        campaign = in.readParcelable(Campaign.class.getClassLoader());
        isAvailable = in.readByte() != 0;
    }

    public static final Creator<DiscountConfigurationModel> CREATOR = new Creator<DiscountConfigurationModel>() {
        @Override
        public DiscountConfigurationModel createFromParcel(final Parcel in) {
            return new DiscountConfigurationModel(in);
        }

        @Override
        public DiscountConfigurationModel[] newArray(final int size) {
            return new DiscountConfigurationModel[size];
        }
    };

    public Discount getDiscount() {
        return discount;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(discount, flags);
        dest.writeParcelable(campaign, flags);
        dest.writeByte((byte) (isAvailable ? 1 : 0));
    }
}

