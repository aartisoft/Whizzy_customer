package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Sowjanya on 8/9/2017.
 */

public class Order implements Parcelable {
    public String order_id = "", created_time_ist = "", driver_id = "", cancel_option = "", created_time1 = "", cancelled_by = "", created_time = "", updated_time = "", cancel_time = "", crn_number = "", driver_comments_on_package = "", drop_contact_name = "", drop_contact_number = "", drop_customer_signature_url = "", drop_full_address = "", order_status = "", package_image_url = "";
    public String pickup_contact_name = "", pickup_contact_number = "", pickup_customer_signature_url = "", pickup_full_address = "", transport_description = "", trip_type = "", user_id = "", vehicle_id = "", vehicle_name = "", payment_required = "";
    public String drop_latitude = "", drop_longitude = "", pickup_latitude = "", pickup_longitude = "", payment_mode = "", order_fare = "", ratings_by_customer = "", ratings_by_driver = "", driver_name = "", extra_expenses_amount = "", delivery_charge = "", vehicle_number = "", approximate_distance = "", pickup_contact_more_info = "", scheduled_date_time = "", driver_mobile = "";
//    public boolean payment_required;
//    public List<DropContact> receiver_details;

    public Order() {
    }


    protected Order(Parcel in) {
        order_id = in.readString();
        created_time_ist = in.readString();
        driver_id = in.readString();
        cancel_option = in.readString();
        created_time1 = in.readString();
        cancelled_by = in.readString();
        created_time = in.readString();
        updated_time = in.readString();
        cancel_time = in.readString();
        crn_number = in.readString();
        driver_comments_on_package = in.readString();
        drop_contact_name = in.readString();
        drop_contact_number = in.readString();
        drop_customer_signature_url = in.readString();
        drop_full_address = in.readString();
        order_status = in.readString();
        package_image_url = in.readString();
        pickup_contact_name = in.readString();
        pickup_contact_number = in.readString();
        pickup_customer_signature_url = in.readString();
        pickup_full_address = in.readString();
        transport_description = in.readString();
        trip_type = in.readString();
        user_id = in.readString();
        vehicle_id = in.readString();
        vehicle_name = in.readString();
        payment_required = in.readString();
        drop_latitude = in.readString();
        drop_longitude = in.readString();
        pickup_latitude = in.readString();
        pickup_longitude = in.readString();
        payment_mode = in.readString();
        order_fare = in.readString();
        ratings_by_customer = in.readString();
        ratings_by_driver = in.readString();
        driver_name = in.readString();
        extra_expenses_amount = in.readString();
        delivery_charge = in.readString();
        vehicle_number = in.readString();
        approximate_distance = in.readString();
        pickup_contact_more_info = in.readString();
        scheduled_date_time = in.readString();
        driver_mobile = in.readString();
    }

    public static final Creator<Order> CREATOR = new Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(order_id);
        parcel.writeString(created_time_ist);
        parcel.writeString(driver_id);
        parcel.writeString(cancel_option);
        parcel.writeString(created_time1);
        parcel.writeString(cancelled_by);
        parcel.writeString(created_time);
        parcel.writeString(updated_time);
        parcel.writeString(cancel_time);
        parcel.writeString(crn_number);
        parcel.writeString(driver_comments_on_package);
        parcel.writeString(drop_contact_name);
        parcel.writeString(drop_contact_number);
        parcel.writeString(drop_customer_signature_url);
        parcel.writeString(drop_full_address);
        parcel.writeString(order_status);
        parcel.writeString(package_image_url);
        parcel.writeString(pickup_contact_name);
        parcel.writeString(pickup_contact_number);
        parcel.writeString(pickup_customer_signature_url);
        parcel.writeString(pickup_full_address);
        parcel.writeString(transport_description);
        parcel.writeString(trip_type);
        parcel.writeString(user_id);
        parcel.writeString(vehicle_id);
        parcel.writeString(vehicle_name);
        parcel.writeString(payment_required);
        parcel.writeString(drop_latitude);
        parcel.writeString(drop_longitude);
        parcel.writeString(pickup_latitude);
        parcel.writeString(pickup_longitude);
        parcel.writeString(payment_mode);
        parcel.writeString(order_fare);
        parcel.writeString(ratings_by_customer);
        parcel.writeString(ratings_by_driver);
        parcel.writeString(driver_name);
        parcel.writeString(extra_expenses_amount);
        parcel.writeString(delivery_charge);
        parcel.writeString(vehicle_number);
        parcel.writeString(approximate_distance);
        parcel.writeString(pickup_contact_more_info);
        parcel.writeString(scheduled_date_time);
        parcel.writeString(driver_mobile);
    }
}
