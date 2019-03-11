package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 10/4/2017.
 */

public class DriverDetails implements Parcelable {
    public String crn_number = "", driver_id = "", driver_mobile = "", driver_name = "", driver_vehicle_id = "",vehicle_number="",vehicle_type="", drop_contact_name = "", drop_contact_number = "", drop_full_address = "", order_id = "", payment_required = "", pickup_contact_name = "", pickup_contact_number = "", pickup_full_address = "", transport_description = "", trip_type = "", vehicle_name = "";
    public String drop_latitude = "", drop_longitude = "", pickup_latitude = "", pickup_longitude = "",driver_image="";

    public DriverDetails() {
    }


    protected DriverDetails(Parcel in) {
        crn_number = in.readString();
        driver_id = in.readString();
        driver_mobile = in.readString();
        driver_name = in.readString();
        driver_vehicle_id = in.readString();
        vehicle_number = in.readString();
        vehicle_type = in.readString();
        drop_contact_name = in.readString();
        drop_contact_number = in.readString();
        drop_full_address = in.readString();
        order_id = in.readString();
        payment_required = in.readString();
        pickup_contact_name = in.readString();
        pickup_contact_number = in.readString();
        pickup_full_address = in.readString();
        transport_description = in.readString();
        trip_type = in.readString();
        vehicle_name = in.readString();
        drop_latitude = in.readString();
        drop_longitude = in.readString();
        pickup_latitude = in.readString();
        pickup_longitude = in.readString();
        driver_image = in.readString();
    }

    public static final Creator<DriverDetails> CREATOR = new Creator<DriverDetails>() {
        @Override
        public DriverDetails createFromParcel(Parcel in) {
            return new DriverDetails(in);
        }

        @Override
        public DriverDetails[] newArray(int size) {
            return new DriverDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(crn_number);
        dest.writeString(driver_id);
        dest.writeString(driver_mobile);
        dest.writeString(driver_name);
        dest.writeString(driver_vehicle_id);
        dest.writeString(vehicle_number);
        dest.writeString(vehicle_type);
        dest.writeString(drop_contact_name);
        dest.writeString(drop_contact_number);
        dest.writeString(drop_full_address);
        dest.writeString(order_id);
        dest.writeString(payment_required);
        dest.writeString(pickup_contact_name);
        dest.writeString(pickup_contact_number);
        dest.writeString(pickup_full_address);
        dest.writeString(transport_description);
        dest.writeString(trip_type);
        dest.writeString(vehicle_name);
        dest.writeString(drop_latitude);
        dest.writeString(drop_longitude);
        dest.writeString(pickup_latitude);
        dest.writeString(pickup_longitude);
        dest.writeString(driver_image);
    }
}
