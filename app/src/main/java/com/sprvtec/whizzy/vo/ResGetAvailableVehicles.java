package com.sprvtec.whizzy.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sowjanya on 5/23/2017.
 */
public class ResGetAvailableVehicles {
    public String message = "";
    public List<Vehicle> Vehicles = new ArrayList<>();
    public List<CancelOption> Options = new ArrayList<>();
    public UserDetails user_details;
    public List<Order> Orders = new ArrayList<>();
}
