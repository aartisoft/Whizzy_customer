package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 9/4/2018.
 */
public class Business implements Parcelable {
    public String bizitemid="",BusinessID="",address = "", address2 = "", id = "", address3 = "", category = "", createdDateFormatted = "", landmark = "", locality = "", name = "", pincode = "", status = "", subcategory = "", userid = "";
    private long createdDate;
    public float distance;
    public double latitude, longitude;

    public Business() {
    }

    private Business(Parcel in) {
        bizitemid = in.readString();
        BusinessID = in.readString();
        address = in.readString();
        address2 = in.readString();
        id = in.readString();
        address3 = in.readString();
        category = in.readString();
        createdDateFormatted = in.readString();
        landmark = in.readString();
        locality = in.readString();
        name = in.readString();
        pincode = in.readString();
        status = in.readString();
        subcategory = in.readString();
        userid = in.readString();
        createdDate = in.readLong();
        distance = in.readFloat();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bizitemid);
        dest.writeString(BusinessID);
        dest.writeString(address);
        dest.writeString(address2);
        dest.writeString(id);
        dest.writeString(address3);
        dest.writeString(category);
        dest.writeString(createdDateFormatted);
        dest.writeString(landmark);
        dest.writeString(locality);
        dest.writeString(name);
        dest.writeString(pincode);
        dest.writeString(status);
        dest.writeString(subcategory);
        dest.writeString(userid);
        dest.writeLong(createdDate);
        dest.writeFloat(distance);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Business> CREATOR = new Creator<Business>() {
        @Override
        public Business createFromParcel(Parcel in) {
            return new Business(in);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };
}
