package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 5/9/2017.
 */

public class LocationInf implements Parcelable {
    public double latitude, longitude;
    public int id, type;
    public String locationName = "", locationAddress = "", contactName = "", contactNumber = "",landmark="";

    public LocationInf(int id, String locationName, String locationAddress, int type, double longitude, double latitude) {
        this.id = id;
        this.type = type;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.longitude = longitude;
        this.latitude = latitude;
//        this.contactName = contactName;
//        this.contactNumber = contactNumber;
    }

    public LocationInf() {

    }

    protected LocationInf(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        id = in.readInt();
        type = in.readInt();
        locationName = in.readString();
        locationAddress = in.readString();
        contactName= in.readString();
        contactNumber=in.readString();
        landmark=in.readString();
    }

    public static final Creator<LocationInf> CREATOR = new Creator<LocationInf>() {
        @Override
        public LocationInf createFromParcel(Parcel in) {
            return new LocationInf(in);
        }

        @Override
        public LocationInf[] newArray(int size) {
            return new LocationInf[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(id);
        dest.writeInt(type);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(landmark);
    }
}
