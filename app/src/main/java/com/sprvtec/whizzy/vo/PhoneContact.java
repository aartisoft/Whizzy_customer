package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by SPRV on 12/1/2015.
 */
public class PhoneContact implements Comparable, Parcelable {
    public String Name = "", MobileNumber = "";
    public int selected = 0, active = 1;

    public PhoneContact(String Name, String MobileNumber) {
        this.Name = Name;
        this.MobileNumber = MobileNumber;
    }

    protected PhoneContact(Parcel in) {
        Name = in.readString();
        MobileNumber = in.readString();
        selected = in.readInt();
    }

    public static final Creator<PhoneContact> CREATOR = new Creator<PhoneContact>() {
        @Override
        public PhoneContact createFromParcel(Parcel in) {
            return new PhoneContact(in);
        }

        @Override
        public PhoneContact[] newArray(int size) {
            return new PhoneContact[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PhoneContact custom = (PhoneContact) o;

        if (!Name.equals(custom.Name)) {
            return false;
        }
        if (!MobileNumber.equals(custom.MobileNumber)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Name.hashCode();
        result = 31 * result + MobileNumber.hashCode();
        return result;
    }

    @Override
    public int compareTo(Object another) {
//        int compareage=Integer.parseInt(((PhoneContact)another).Name);
        /* For Ascending order*/
        String TickedId2 = ((PhoneContact) another).Name;

        int num1 = this.active;
        int num2 = ((PhoneContact) another).active;

        if (num1 > num2)
            return 1;
        else if (num1 < num2)
            return -1;
        else
            return this.Name.compareToIgnoreCase(((PhoneContact) another).Name);
//        return this.Name.compareTo((((PhoneContact) another).Name));

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Name);
        dest.writeString(MobileNumber);
        dest.writeInt(selected);
    }
}
