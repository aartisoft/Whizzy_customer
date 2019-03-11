package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 7/14/2017.
 */

public class NotificationData implements Parcelable {
    public String vehicle_name = "", crn_number = "", drop_contact_name = "", driver_id = "", allocation_id = "", pickup_contact_name = "", drop_contact_number = "", trip_type = "", pickup_contact_number = "", transport_description = "", drop_full_address = "", order_id = "", driver_name = "", driver_vehicle_id = "", driver_mobile = "", pickup_full_address = "", vehicle_id = "",vehicle_type="",vehicle_number="",driver_image="";
    public double drop_longitude, drop_latitude, pickup_longitude, pickup_latitude;
    public boolean payment_required;
    public String order_status = "";

    public NotificationData() {
    }


    protected NotificationData(Parcel in) {
        vehicle_name = in.readString();
        crn_number = in.readString();
        drop_contact_name = in.readString();
        driver_id = in.readString();
        allocation_id = in.readString();
        pickup_contact_name = in.readString();
        drop_contact_number = in.readString();
        trip_type = in.readString();
        pickup_contact_number = in.readString();
        transport_description = in.readString();
        drop_full_address = in.readString();
        order_id = in.readString();
        driver_name = in.readString();
        driver_vehicle_id = in.readString();
        driver_mobile = in.readString();
        pickup_full_address = in.readString();
        vehicle_id = in.readString();
        vehicle_type = in.readString();
        vehicle_number = in.readString();
        driver_image = in.readString();
        drop_longitude = in.readDouble();
        drop_latitude = in.readDouble();
        pickup_longitude = in.readDouble();
        pickup_latitude = in.readDouble();
        payment_required = in.readByte() != 0;
        order_status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicle_name);
        dest.writeString(crn_number);
        dest.writeString(drop_contact_name);
        dest.writeString(driver_id);
        dest.writeString(allocation_id);
        dest.writeString(pickup_contact_name);
        dest.writeString(drop_contact_number);
        dest.writeString(trip_type);
        dest.writeString(pickup_contact_number);
        dest.writeString(transport_description);
        dest.writeString(drop_full_address);
        dest.writeString(order_id);
        dest.writeString(driver_name);
        dest.writeString(driver_vehicle_id);
        dest.writeString(driver_mobile);
        dest.writeString(pickup_full_address);
        dest.writeString(vehicle_id);
        dest.writeString(vehicle_type);
        dest.writeString(vehicle_number);
        dest.writeString(driver_image);
        dest.writeDouble(drop_longitude);
        dest.writeDouble(drop_latitude);
        dest.writeDouble(pickup_longitude);
        dest.writeDouble(pickup_latitude);
        dest.writeByte((byte) (payment_required ? 1 : 0));
        dest.writeString(order_status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NotificationData> CREATOR = new Creator<NotificationData>() {
        @Override
        public NotificationData createFromParcel(Parcel in) {
            return new NotificationData(in);
        }

        @Override
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
        }
    };
}
