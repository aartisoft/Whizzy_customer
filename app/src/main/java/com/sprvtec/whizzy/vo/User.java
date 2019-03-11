package com.sprvtec.whizzy.vo;

/**
 * Created by Sowjanya on 6/5/2017.
 */

public class User {
    public String id = "", name = "", email = "";
    public double latitude, longitude;

    public User(String id, String name, String email, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public User() {
    }
}
