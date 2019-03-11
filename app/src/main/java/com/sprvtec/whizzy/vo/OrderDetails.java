package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 8/9/2017.
 */

public class OrderDetails implements Parcelable {
    public String payment_status = "", ratings_status = "", order_id = "", driver_id = "", created_time = "", cancel_time = "", crn_number = "", driver_comments_on_package = "", drop_contact_name = "", drop_contact_number = "", drop_customer_signature_url = "", drop_full_address = "", order_status = "", package_image_url = "";
    public String pickup_contact_name = "", pickup_contact_number = "", pickup_customer_signature_url = "", pickup_full_address = "", transport_description = "", trip_type = "", user_id = "", vehicle_id = "", vehicle_name = "", payment_required = "";
    public String drop_latitude = "", drop_longitude = "", pickup_latitude = "", pickup_longitude = "", processing = "", order_fare = "", payment_mode = "", order_type_flow = "",pickup_contact_more_info="",pickup_landmark="",drop_landmark="";
    public long updated_time;
    public DriverDetails driver_details;
    public DriverCancelOption cancel_option;
//    public boolean payment_required;

    public OrderDetails() {
    }


    protected OrderDetails(Parcel in) {
        payment_status = in.readString();
        ratings_status = in.readString();
        order_id = in.readString();
        driver_id = in.readString();
        created_time = in.readString();
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
        processing = in.readString();
        order_fare = in.readString();
        payment_mode = in.readString();
        order_type_flow = in.readString();
        pickup_contact_more_info = in.readString();
        updated_time = in.readLong();
        driver_details = in.readParcelable(DriverDetails.class.getClassLoader());
        cancel_option = in.readParcelable(DriverCancelOption.class.getClassLoader());
    }

    public static final Creator<OrderDetails> CREATOR = new Creator<OrderDetails>() {
        @Override
        public OrderDetails createFromParcel(Parcel in) {
            return new OrderDetails(in);
        }

        @Override
        public OrderDetails[] newArray(int size) {
            return new OrderDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(payment_status);
        dest.writeString(ratings_status);
        dest.writeString(order_id);
        dest.writeString(driver_id);
        dest.writeString(created_time);
        dest.writeString(cancel_time);
        dest.writeString(crn_number);
        dest.writeString(driver_comments_on_package);
        dest.writeString(drop_contact_name);
        dest.writeString(drop_contact_number);
        dest.writeString(drop_customer_signature_url);
        dest.writeString(drop_full_address);
        dest.writeString(order_status);
        dest.writeString(package_image_url);
        dest.writeString(pickup_contact_name);
        dest.writeString(pickup_contact_number);
        dest.writeString(pickup_customer_signature_url);
        dest.writeString(pickup_full_address);
        dest.writeString(transport_description);
        dest.writeString(trip_type);
        dest.writeString(user_id);
        dest.writeString(vehicle_id);
        dest.writeString(vehicle_name);
        dest.writeString(payment_required);
        dest.writeString(drop_latitude);
        dest.writeString(drop_longitude);
        dest.writeString(pickup_latitude);
        dest.writeString(pickup_longitude);
        dest.writeString(processing);
        dest.writeString(order_fare);
        dest.writeString(payment_mode);
        dest.writeString(order_type_flow);
        dest.writeString(pickup_contact_more_info);
        dest.writeLong(updated_time);
        dest.writeParcelable(driver_details, flags);
        dest.writeParcelable(cancel_option, flags);
    }
}
