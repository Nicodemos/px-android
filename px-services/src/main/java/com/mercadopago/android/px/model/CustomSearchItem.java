package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CustomSearchItem implements Serializable, Parcelable {

    //TODO make final when deprecate custom search item constructor.
    private String description;
    private String id;

    //TODO change name / serialize name - signature V5
    @SerializedName("payment_type_id")
    private String type;
    private String paymentMethodId;

    @Nullable private String discountInfo;

    @Deprecated
    public CustomSearchItem() {
    }

    protected CustomSearchItem(final Parcel in) {
        description = in.readString();
        id = in.readString();
        type = in.readString();
        paymentMethodId = in.readString();
        discountInfo = in.readString();
    }

    public static final Creator<CustomSearchItem> CREATOR = new Creator<CustomSearchItem>() {
        @Override
        public CustomSearchItem createFromParcel(final Parcel in) {
            return new CustomSearchItem(in);
        }

        @Override
        public CustomSearchItem[] newArray(final int size) {
            return new CustomSearchItem[size];
        }
    };

    public String getDescription() {
        return description;
    }

    @Nullable
    public String getDiscountInfo() {
        return discountInfo;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(paymentMethodId);
        dest.writeString(discountInfo);
    }

    @Deprecated
    public void setDescription(final String description) {
        this.description = description;
    }

    @Deprecated
    public void setId(final String id) {
        this.id = id;
    }

    @Deprecated
    public void setType(final String type) {
        this.type = type;
    }

    @Deprecated
    public void setPaymentMethodId(final String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public boolean hasDiscountInfo() {
        return !TextUtils.isEmpty(discountInfo);
    }
}
