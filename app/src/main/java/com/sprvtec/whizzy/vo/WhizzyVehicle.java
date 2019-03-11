package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 5/30/2017.
 */
public class WhizzyVehicle implements Parcelable {

    public String vehicle_id = "", vehicle_name = "", vehicle_description = "", vehicle_height = "", vehicle_image = "", vehicle_selected_image = "", vehicle_length = "", vehicle_max_weight = "", vehicle_width = "", distance = "";
    public double minimum_price, kilometer_price, approximate_fare;
    public int dist;

    public WhizzyVehicle() {
    }


    protected WhizzyVehicle(Parcel in) {
        vehicle_id = in.readString();
        vehicle_name = in.readString();
        vehicle_description = in.readString();
        vehicle_height = in.readString();
        vehicle_image = in.readString();
        vehicle_selected_image = in.readString();
        vehicle_length = in.readString();
        vehicle_max_weight = in.readString();
        vehicle_width = in.readString();
        distance = in.readString();
        minimum_price = in.readDouble();
        kilometer_price = in.readDouble();
        approximate_fare = in.readDouble();
        dist = in.readInt();
    }

    public static final Creator<WhizzyVehicle> CREATOR = new Creator<WhizzyVehicle>() {
        @Override
        public WhizzyVehicle createFromParcel(Parcel in) {
            return new WhizzyVehicle(in);
        }

        @Override
        public WhizzyVehicle[] newArray(int size) {
            return new WhizzyVehicle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicle_id);
        dest.writeString(vehicle_name);
        dest.writeString(vehicle_description);
        dest.writeString(vehicle_height);
        dest.writeString(vehicle_image);
        dest.writeString(vehicle_selected_image);
        dest.writeString(vehicle_length);
        dest.writeString(vehicle_max_weight);
        dest.writeString(vehicle_width);
        dest.writeString(distance);
        dest.writeDouble(minimum_price);
        dest.writeDouble(kilometer_price);
        dest.writeDouble(approximate_fare);
        dest.writeInt(dist);
    }
}
