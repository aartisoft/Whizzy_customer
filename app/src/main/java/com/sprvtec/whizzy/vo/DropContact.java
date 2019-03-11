package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 11/19/2018.
 */
public class DropContact implements Parcelable {
    public String drop_contact_name = "", drop_contact_number = "", drop_contact_more_info = "", received_time = "", receiver_name = "", receiver_number = "", receiver_signature = "";
    public String id = "";

    public DropContact() {
    }

    public DropContact(Parcel in) {
        drop_contact_name = in.readString();
        drop_contact_number = in.readString();
        drop_contact_more_info = in.readString();
        received_time = in.readString();
        receiver_name = in.readString();
        receiver_number = in.readString();
        receiver_signature = in.readString();
        id = in.readString();
    }

    public static final Creator<DropContact> CREATOR = new Creator<DropContact>() {
        @Override
        public DropContact createFromParcel(Parcel in) {
            return new DropContact(in);
        }

        @Override
        public DropContact[] newArray(int size) {
            return new DropContact[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(drop_contact_name);
        dest.writeString(drop_contact_number);
        dest.writeString(drop_contact_more_info);
        dest.writeString(received_time);
        dest.writeString(receiver_name);
        dest.writeString(receiver_number);
        dest.writeString(receiver_signature);
        dest.writeString(id);
    }
}
