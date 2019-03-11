package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Sowjanya on 7/11/2017.
 */

public class DriverCancelOption implements Parcelable {
    public String reason = "";
    public int flag;

    public DriverCancelOption() {
    }

    protected DriverCancelOption(Parcel in) {
        reason = in.readString();
        flag = in.readInt();
    }

    public static final Creator<DriverCancelOption> CREATOR = new Creator<DriverCancelOption>() {
        @Override
        public DriverCancelOption createFromParcel(Parcel in) {
            return new DriverCancelOption(in);
        }

        @Override
        public DriverCancelOption[] newArray(int size) {
            return new DriverCancelOption[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(reason);
        dest.writeInt(flag);
    }
}
