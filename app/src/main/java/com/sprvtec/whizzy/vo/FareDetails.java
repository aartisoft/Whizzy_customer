package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sowjanya on 11/6/2017.
 */

public class FareDetails implements Parcelable{
    public String Base_Fare = "", CGST = "", SGST = "";
    public List<EndFareDetails> FareDetails=new ArrayList<>();

    protected FareDetails(Parcel in) {
        Base_Fare = in.readString();
        CGST = in.readString();
        SGST = in.readString();
    }

    public static final Creator<FareDetails> CREATOR = new Creator<FareDetails>() {
        @Override
        public FareDetails createFromParcel(Parcel in) {
            return new FareDetails(in);
        }

        @Override
        public FareDetails[] newArray(int size) {
            return new FareDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Base_Fare);
        dest.writeString(CGST);
        dest.writeString(SGST);
    }
}
