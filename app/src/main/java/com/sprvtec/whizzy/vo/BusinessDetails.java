package com.sprvtec.whizzy.vo;

public class BusinessDetails {

    public String name = "", vicinity = "",id="";
    public LocationGeo geometry;


    public BusinessDetails(String name,String vicinity,String id) {
        super();
        this.name = name;
        this.vicinity = vicinity;
        this.id = id;
    }
    public BusinessDetails() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
