package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 2/20/2019.
 */
public class SavedAddress implements Parcelable, Comparable<SavedAddress> {
    public String Label = "", Address = "", ContactName = "", ContactNumber = "", Landmark = "", id = "";
    public double Latitude, Longitude;
    public int priority;

    public SavedAddress() {
    }

    private SavedAddress(Parcel in) {
        Label = in.readString();
        Address = in.readString();
        ContactName = in.readString();
        ContactNumber = in.readString();
        Landmark = in.readString();
        id = in.readString();
        Latitude = in.readDouble();
        Longitude = in.readDouble();
    }

    public static final Creator<SavedAddress> CREATOR = new Creator<SavedAddress>() {
        @Override
        public SavedAddress createFromParcel(Parcel in) {
            return new SavedAddress(in);
        }

        @Override
        public SavedAddress[] newArray(int size) {
            return new SavedAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Label);
        dest.writeString(Address);
        dest.writeString(ContactName);
        dest.writeString(ContactNumber);
        dest.writeString(Landmark);
        dest.writeString(id);
        dest.writeDouble(Latitude);
        dest.writeDouble(Longitude);
    }

    @Override
    public int compareTo(SavedAddress address) {
        if (priority > address.priority) {
            return 1;
        }
        else if (priority <  address.priority) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
