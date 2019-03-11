package com.sprvtec.whizzy.vo;

import android.os.Parcel;
import android.os.Parcelable;

public class GridCategory implements Parcelable {
    public String name;
    public String mainCategory;
    public String subCategory;
    public String googleCategory;
    public int image;

//    public GridCategory(String name1, int image1) {
//        name = name1;
//        image = image1;
//    }

    public GridCategory(String name1, int image1, String mainCategory1, String subCategory1, String googleCategory1) {
        name = name1;
        image = image1;
        mainCategory=mainCategory1;
        subCategory=subCategory1;
        googleCategory=googleCategory1;
    }


    private GridCategory(Parcel in) {
        name = in.readString();
        mainCategory = in.readString();
        subCategory = in.readString();
        googleCategory = in.readString();
        image = in.readInt();
    }

    public static final Creator<GridCategory> CREATOR = new Creator<GridCategory>() {
        @Override
        public GridCategory createFromParcel(Parcel in) {
            return new GridCategory(in);
        }

        @Override
        public GridCategory[] newArray(int size) {
            return new GridCategory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mainCategory);
        dest.writeString(subCategory);
        dest.writeString(googleCategory);
        dest.writeInt(image);
    }
}
