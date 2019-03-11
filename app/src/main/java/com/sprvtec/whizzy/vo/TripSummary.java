package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 7/31/2017.
 */

public class TripSummary implements Parcelable {
    public String message = "", order_id = "", distance = "", pickup_full_address = "", drop_full_address = "", trip_type = "", time = "", created_time = "", order_status = "",  delivery_charge = "", transport_description = "",pickup_contact_name="",pickup_contact_number="",pickup_contact_more_info="";
    public float fare,extra_expenses_amount;
    public boolean monthly_billing;


    protected TripSummary(Parcel in) {
        message = in.readString();
        order_id = in.readString();
        distance = in.readString();
        pickup_full_address = in.readString();
        drop_full_address = in.readString();
        trip_type = in.readString();
        time = in.readString();
        created_time = in.readString();
        order_status = in.readString();
        delivery_charge = in.readString();
        transport_description = in.readString();
        pickup_contact_name = in.readString();
        pickup_contact_number = in.readString();
        pickup_contact_more_info = in.readString();
        fare = in.readFloat();
        extra_expenses_amount = in.readFloat();
        monthly_billing = in.readByte() != 0;
    }

    public static final Creator<TripSummary> CREATOR = new Creator<TripSummary>() {
        @Override
        public TripSummary createFromParcel(Parcel in) {
            return new TripSummary(in);
        }

        @Override
        public TripSummary[] newArray(int size) {
            return new TripSummary[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(message);
        parcel.writeString(order_id);
        parcel.writeString(distance);
        parcel.writeString(pickup_full_address);
        parcel.writeString(drop_full_address);
        parcel.writeString(trip_type);
        parcel.writeString(time);
        parcel.writeString(created_time);
        parcel.writeString(order_status);
        parcel.writeString(delivery_charge);
        parcel.writeString(transport_description);
        parcel.writeString(pickup_contact_name);
        parcel.writeString(pickup_contact_number);
        parcel.writeString(pickup_contact_more_info);
        parcel.writeFloat(fare);
        parcel.writeFloat(extra_expenses_amount);
        parcel.writeByte((byte) (monthly_billing ? 1 : 0));
    }
}
