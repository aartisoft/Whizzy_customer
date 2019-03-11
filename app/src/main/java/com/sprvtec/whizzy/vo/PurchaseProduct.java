package com.sprvtec.whizzy.vo;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 5/2/2018.
 */

public class PurchaseProduct implements Parcelable {
    public String title = "";
    public int count;

    public PurchaseProduct() {

    }

    public PurchaseProduct(Parcel in) {
        title = in.readString();
        count = in.readInt();
    }

    public static final Creator<PurchaseProduct> CREATOR = new Creator<PurchaseProduct>() {
        @Override
        public PurchaseProduct createFromParcel(Parcel in) {
            return new PurchaseProduct(in);
        }

        @Override
        public PurchaseProduct[] newArray(int size) {
            return new PurchaseProduct[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(count);
    }
}
