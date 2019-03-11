package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 12/26/2017.
 */

public class EndFareDetails implements Parcelable{
    public String expense_id="",driver_id="",expense_description="",expense_image_url="",order_id="";
    public float amount;

    protected EndFareDetails(Parcel in) {
        expense_id = in.readString();
        driver_id = in.readString();
        expense_description = in.readString();
        expense_image_url = in.readString();
        order_id = in.readString();
        amount = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(expense_id);
        dest.writeString(driver_id);
        dest.writeString(expense_description);
        dest.writeString(expense_image_url);
        dest.writeString(order_id);
        dest.writeFloat(amount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EndFareDetails> CREATOR = new Creator<EndFareDetails>() {
        @Override
        public EndFareDetails createFromParcel(Parcel in) {
            return new EndFareDetails(in);
        }

        @Override
        public EndFareDetails[] newArray(int size) {
            return new EndFareDetails[size];
        }
    };
}
