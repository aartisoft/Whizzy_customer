package com.sprvtec.whizzy.util;

/*
  Created by SPRV on 5/3/2017
 */

import android.app.Application;
import android.content.Context;

import com.simpl.android.sdk.Simpl;
import com.sprvtec.whizzy.vo.PhoneContact;

import java.util.ArrayList;
import java.util.List;

import androidx.multidex.MultiDex;

public class GlobalApplication extends Application {
    private static GlobalApplication singleton;
    public static String fcmToken;
    public List<PhoneContact> phoneContacts = new ArrayList<>();


    public static GlobalApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        Simpl.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}