package com.sprvtec.whizzy.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;
import com.simpl.android.sdk.Simpl;
import com.sprvtec.whizzy.R;
import com.sprvtec.whizzy.adapter.HomePagerImagesAdapter;
import com.sprvtec.whizzy.util.Constants;
import com.sprvtec.whizzy.util.Utils;
import com.sprvtec.whizzy.util.Webservice;
import com.sprvtec.whizzy.vo.GetHomeImagesResponse;
import com.sprvtec.whizzy.vo.GetNearByVehiclesResponse;
import com.sprvtec.whizzy.vo.HomeImage;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * Created by SPRV on 5/3/2017.
 */
public class HomePagerActivity extends FragmentActivity implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private static final int SIGN_UP = 5, SIGN_UP_SCHEDULE = 6;
    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerList;
    private ImageView profileImage;
    private static final String TAG = "HomePagerActivity";


    private SharedPreferences sp;
    private TextView nameMenu, start;
    private ViewPager viewPager;
    private List<HomeImage> homeImages;
    private FirebaseDatabase mFirebaseInstance;
    Dialog dialog1, cont_dailog;
    Button ok;
    TextView hint;
    String currentVersion = "";
    private int startTime, endTime, currentHourIn24Format, currentMin;
    private int hour, minute;
    private Calendar myCalendar;
    private boolean nextDay;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_home_pager);
        viewPager = findViewById(R.id.view_pager);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        cont_dailog = new Dialog(HomePagerActivity.this);
        cont_dailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cont_dailog.setContentView(R.layout.calllayout);
        cont_dailog.setCanceledOnTouchOutside(true);

        Button call = cont_dailog.findViewById(R.id.call);
        Button dimsiss = cont_dailog.findViewById(R.id.dismiss_call);
        findViewById(R.id.schedule).setOnClickListener(v -> {
            if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                callScheduleFun();
            } else {
                Intent intent = new Intent(HomePagerActivity.this, SignUpActivity.class);
                startActivityForResult(intent, SIGN_UP_SCHEDULE);
            }
        });
        call.setOnClickListener(v -> {
            Intent callWhizzy = new Intent(Intent.ACTION_DIAL);
//            Intent callWhizzy = new Intent(Intent.ACTION_CALL);
            callWhizzy.setData(Uri.parse("tel:" + Constants.KEY_SUPPORT_NUMBER));

//            if (ContextCompat.checkSelfPermission(HomePagerActivity.this,
//                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                cont_dailog.dismiss();
//                requestReadPhoneStatePermission();
//                // TODO: Consider calling
//                //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
//                return;
//            }
            cont_dailog.dismiss();
            startActivity(callWhizzy);
        });

        dimsiss.setOnClickListener(view -> cont_dailog.dismiss());


        dialog1 = new Dialog(HomePagerActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.upgreade_alert);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1.setCanceledOnTouchOutside(false);
        dialog1.setCancelable(false);
        hint = dialog1.findViewById(R.id.hint);
        hint.setGravity(Gravity.CENTER_HORIZONTAL);
        ok = dialog1.findViewById(R.id.ok);
        ok.setOnClickListener(view -> {

            String url = "https://play.google.com/store/apps/details?id=com.sprvtec.whizzy";
            Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

            startActivity(browse);
        });


        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);
        ImageView menu = findViewById(R.id.menu);
        profileImage = findViewById(R.id.image);


        //Menu items
        LinearLayout yourTripMenu = findViewById(R.id.your_trips);
        yourTripMenu.setOnClickListener(this);
        LinearLayout mySavedAddresses = findViewById(R.id.my_saved_addresses);
        mySavedAddresses.setOnClickListener(this);
        LinearLayout referFriendMenu = findViewById(R.id.refer_friend);
        referFriendMenu.setOnClickListener(this);
        LinearLayout legalMenu = findViewById(R.id.legal);
        legalMenu.setOnClickListener(this);
        LinearLayout contactUsMenu = findViewById(R.id.contact_us);
        contactUsMenu.setOnClickListener(this);
        LinearLayout helpMenu = findViewById(R.id.help);
        helpMenu.setOnClickListener(this);
        LinearLayout aboutMenu = findViewById(R.id.about);
        aboutMenu.setOnClickListener(this);
        RelativeLayout editProfile = findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(this);
        nameMenu = findViewById(R.id.name);
        start = findViewById(R.id.start);


        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerLayout.closeDrawers();
        mDrawerLayout.setFocusableInTouchMode(false);
        menu.setOnClickListener(v -> {

            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawers();
            } else {
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals(""))
                    new GetProfileDetailsTask(HomePagerActivity.this).execute();
                nameMenu.setText(sp.getString(Constants.PreferenceKey.KEY_FULL_NAME, "Name"));
                mDrawerLayout.openDrawer(mDrawerList);
            }
        });

        mDrawerList.setOnClickListener(v -> {

        });
        Log.d("Simpl", Simpl.getInstance().toString());
        start.setOnClickListener(v -> {


//            if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                clearOrderPreferences();
                callRequestNowFun();
//            } else {
//                Intent intent = new Intent(HomePagerActivity.this, SignUpActivity.class);
//                startActivityForResult(intent, SIGN_UP);
//            }
        });

        PageIndicatorView pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setSelectedColor(Color.BLUE);
        pageIndicatorView.setUnselectedColor(Color.LTGRAY);
        pageIndicatorView.setAnimationType(AnimationType.SCALE);
        pageIndicatorView.setViewPager(viewPager);
        new GetHomeImagesTask(HomePagerActivity.this).execute();
    }

    private void clearOrderPreferences() {
        sp.edit().putString(Webservice.COUPN_CODE, "").apply();
        sp.edit().putString(Webservice.DISCOUNTED_FARE, "").apply();
        sp.edit().putString(Webservice.DISCOUNT, "").apply();
        sp.edit().putString(Webservice.BUSINESS_NAME, "").apply();
        sp.edit().putString(Webservice.BUSINESS_ID, "").apply();
        sp.edit().putString(Webservice.DROP_CONTACT_DETAILS, "").apply();
        sp.edit().putString(Webservice.ORDER_TYPE_FLOW, "").apply();
        sp.edit().putString(Webservice.BUY_LOCATION, "").apply();

    }

    private void callScheduleFun() {
        if (startTime == 0) {
            mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseScheduleTimingsDb = mFirebaseInstance.getReference("schedule_timings");

            mFirebaseScheduleTimingsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        try {
                            startTime = ((Number) (dataSnapshot.child("start_time").getValue())).intValue();
                            endTime = ((Number) (dataSnapshot.child("end_time").getValue())).intValue();


                            showDatepicker();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else
            showDatepicker();
    }

    private void showDatepicker() {
        Calendar rightNow = Calendar.getInstance();
        currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        currentMin = rightNow.get(Calendar.MINUTE);
        Log.e("Times", startTime + " " + endTime + " " + currentHourIn24Format + " " + currentMin);
//        EditText hours = findViewById(R.id.hours);
//        EditText min = findViewById(R.id.min);
//        currentHourIn24Format = Integer.parseInt(hours.getText().toString());
//        currentMin = Integer.parseInt(min.getText().toString());
        nextDay = false;


        myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            Log.e("date", monthOfYear + " " + dayOfMonth + " " + year);
            if (!(myCalendar.get(Calendar.MONTH) == monthOfYear && myCalendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth && myCalendar.get(Calendar.YEAR) == year))
                nextDay = true;
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                updateLabel();
            callTimePicker();
        };
        DatePickerDialog dateDalog = new DatePickerDialog(HomePagerActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        if (currentHourIn24Format < (endTime - 1)) {
            dateDalog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else if (currentHourIn24Format == (endTime - 1) && currentMin <= 30) {
            dateDalog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            nextDay = true;
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 1);
            dateDalog.getDatePicker().setMinDate(cal.getTimeInMillis());
        }
//        myCalendar.add(Calendar.DAY_OF_YEAR, 5);
//        dateDalog.getDatePicker().setMaxDate(myCalendar.getTimeInMillis());
        dateDalog.show();
    }

    private void callTimePicker() {

        if (nextDay) {

            hour = startTime;
            minute = 0;
        } else {
            if (currentHourIn24Format < startTime) {
                if (currentHourIn24Format == (startTime - 1) && currentMin >= 30) {
                    hour = startTime;
                    minute = currentMin + 30 - 60;
                } else {
                    hour = startTime;
                    minute = 0;
                }
            } else {
                if (currentMin >= 30) {
                    hour = currentHourIn24Format + 1;
                    minute = currentMin + 30 - 60;
                } else {
                    hour = currentHourIn24Format;
                    minute = currentMin + 30;
                }
            }
//            timePickerDialog.setMin(currentHourIn24Format, currentMin);
        }
//        com.wdullaer.materialdatetimepicker.time.TimePickerDialog d = com.wdullaer.materialdatetimepicker.time.TimePickerDialog.newInstance(
//                this,
//                hour,
//                minute,
//                false
//        );
////        d.setMinTime(hour, minute, 0);
////        d.setMaxTime(20, 0, 0);
//        d.setOnTimeSetListener(this);
//
////        RangeTimePickerDialog timePickerDialog = new RangeTimePickerDialog(HomePagerActivity.this, new TimePickerDialog.OnTimeSetListener() {
////            @Override
////            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
////
////            }
////        }, hour, minute, true);
////        timePickerDialog.setMin(hour, minute);
////
////        timePickerDialog.setMax(20, 0);
////        timePickerDialog.show();
//        d.show(getSupportFragmentManager(), "");
        TimePickerDialog timePickerDialog = new TimePickerDialog(HomePagerActivity.this, this, hour, minute, false);
        timePickerDialog.show();

    }


    @Override
    protected void onResume() {
        super.onResume();
        start.setEnabled(true);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        DatabaseReference mFirebaseDatabaseVersion = mFirebaseInstance.getReference("Version_Android");
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("Current version", currentVersion);
        mFirebaseDatabaseVersion.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    String UpdatedVersion = noteDataSnapshot.getValue(String.class);
                    Log.e("Updated version", UpdatedVersion);
                    if (UpdatedVersion.equals(currentVersion)) {
                        if (dialog1.isShowing())
                            dialog1.dismiss();

                    } else {
                        if (!dialog1.isShowing())
                            dialog1.show();
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_profile:
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                    Intent editIntent = new Intent(HomePagerActivity.this, ProfileActivity.class);
                    startActivity(editIntent);
                } else {
                    Intent editIntent = new Intent(HomePagerActivity.this, SignUpActivity.class);
                    startActivity(editIntent);
                }
                break;
            case R.id.your_trips:

                Intent tripsIntent = new Intent(HomePagerActivity.this, AllTripsActivity.class);
                startActivity(tripsIntent);
                break;
            case R.id.my_saved_addresses:
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                    Intent editIntent = new Intent(HomePagerActivity.this, SavedAddressesActivity.class);
                    startActivity(editIntent);
                } else {
                    Intent editIntent = new Intent(HomePagerActivity.this, SignUpActivity.class);
                    startActivity(editIntent);
                }
                break;
            case R.id.legal:
                Intent tcIntent = new Intent(HomePagerActivity.this, TCActivity.class);
                startActivity(tcIntent);
                break;
            case R.id.contact_us:
                cont_dailog.show();
                break;
            case R.id.help:
                Intent helpIntent = new Intent(HomePagerActivity.this, HelpActivity.class);
                startActivity(helpIntent);
                break;

            case R.id.refer_friend:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi, Click on the link to download the Whizzy app to get anything done. https://play.google.com/store/apps/details?id=com.sprvtec.whizzy \n -WHIZZY");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(HomePagerActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

        }
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.e("Onback", "onback presses");
        if (mDrawerLayout.isDrawerOpen(mDrawerList))
            mDrawerLayout.closeDrawers();
        else {
            finish();

        }
    }


    private void callTimeProceed(int hourOfDay, int minutes) {
        java.text.DecimalFormat nft = new
                java.text.DecimalFormat("#00.###");
        nft.setDecimalSeparatorAlwaysShown(false);
        hour = hourOfDay;
        minute = minutes;
        Log.e("time", hourOfDay + " hrs, minutes " + minute);
        String dateTime = nft.format(myCalendar.get(Calendar.DAY_OF_MONTH)) + "/" + nft.format((myCalendar.get(Calendar.MONTH) + 1)) + "/" + myCalendar.get(Calendar.YEAR) + " " + (hour >= 13 ? nft.format((hour - 12)) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " PM" : " AM");
        Log.e("Date is", dateTime);
        clearOrderPreferences();
        Intent intent1 = new Intent(HomePagerActivity.this, OptionsActivity.class);
        intent1.putExtra(Constants.IntentKey.SCHEDULE_ORDER, true);
        intent1.putExtra(Constants.IntentKey.SCHEDULE_TIME, dateTime);
        startActivity(intent1);
    }

    private void callRequestNowFun() {
        if (startTime == 0) {
            mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseScheduleTimingsDb = mFirebaseInstance.getReference("schedule_timings");

            mFirebaseScheduleTimingsDb.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        try {
                            startTime = ((Number) (dataSnapshot.child("start_time").getValue())).intValue();
                            endTime = ((Number) (dataSnapshot.child("end_time").getValue())).intValue();


                            checkCurrentTime();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else
            checkCurrentTime();
    }

    private void checkCurrentTime() {
        Log.e("check", "check");
        Calendar myCalendar = Calendar.getInstance();
        int hourOfDay = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = myCalendar.get(Calendar.MINUTE);
//        int hourOfDay=20;
//        int minutes=0;

        java.text.DecimalFormat nft = new
                java.text.DecimalFormat("#00.###");
        nft.setDecimalSeparatorAlwaysShown(false);
        if (hourOfDay >= startTime && hourOfDay <= endTime) {
            if (hourOfDay == endTime && minutes > 0) {
//                String str = "Our operational hours are from " + (startTime >= 13 ? nft.format(startTime - 12) : nft.format(startTime)) + ":" + nft.format(0) + (startTime >= 12 ? " pm" : " am") + " to " + nft.format((endTime - 12)) + ":00 pm" + "\n\n" + "Sorry, we cannot service your request now. If you wish, you can schedule a request during normal operational hours!";
                String str = "Our operational hours are from " + (startTime >= 13 ? nft.format(startTime - 12) : nft.format(startTime)) + ":" + nft.format(0) + (startTime >= 12 ? " pm" : " am") + " to " + (endTime >= 13 ? nft.format(endTime - 12) : nft.format(endTime)) + ":" + nft.format(0) + (endTime >= 12 ? " pm" : " am") + "\n\n" + "Sorry, we cannot service your request now. If you wish, you can schedule a request during normal operational hours!";

                requestNowDialog(str);
            } else {
                if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                    Intent intent = new Intent(HomePagerActivity.this, OptionsActivity.class);
                    startActivity(intent);
                    start.setEnabled(false);
                } else {
                    Intent intent = new Intent(HomePagerActivity.this, SignUpActivity.class);
                    startActivityForResult(intent, SIGN_UP);
                }

            }

//            else callTimeProceed(hourOfDay, minutes);
        } else {
//            String str = "Our operational hours are from " + (startTime >= 13 ? nft.format(startTime - 12) : nft.format(startTime)) + ":" + nft.format(0) + (startTime >= 12 ? " pm" : " am") + " to " + nft.format((endTime - 12)) + ":00 pm" + "\n\n" + "Sorry, we cannot service your request now. If you wish, you can schedule a request during normal operational hours!";
            String str = "Our operational hours are from " + (startTime >= 13 ? nft.format(startTime - 12) : nft.format(startTime)) + ":" + nft.format(0) + (startTime >= 12 ? " pm" : " am") + " to " + (endTime >= 13 ? nft.format(endTime - 12) : nft.format(endTime)) + ":" + nft.format(0) + (endTime >= 12 ? " pm" : " am") + "\n\n" + "Sorry, we cannot service your request now. If you wish, you can schedule a request during normal operational hours!";

            requestNowDialog(str);

        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
        java.text.DecimalFormat nft = new
                java.text.DecimalFormat("#00.###");
        nft.setDecimalSeparatorAlwaysShown(false);
        if (hourOfDay >= hour && hourOfDay <= endTime) {
            if (hourOfDay == hour) {
                if (minutes >= minute) {
                    callTimeProceed(hourOfDay, minutes);
                } else {
                    String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + (endTime >= 13 ? nft.format(endTime - 12) : nft.format(endTime)) + ":" + nft.format(0) + (endTime >= 12 ? " pm" : " am") + "</b>";

                    timeDialog(str);
                }
            } else if (hourOfDay == endTime && minutes > 0) {
                String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + (endTime >= 13 ? nft.format(endTime - 12) : nft.format(endTime)) + ":" + nft.format(0) + (endTime >= 12 ? " pm" : " am") + "</b>";

                timeDialog(str);
            } else callTimeProceed(hourOfDay, minutes);

//            else callTimeProceed(hourOfDay, minutes);
        } else {
            String str = "For the chosen date, you can schedule only between " + "<b>" + (hour >= 13 ? nft.format(hour - 12) : nft.format(hour)) + ":" + nft.format(minute) + (hour >= 12 ? " pm" : " am") + "</b>" + " to " + "<b>" + (endTime >= 13 ? nft.format(endTime - 12) : nft.format(endTime)) + ":" + nft.format(0) + (endTime >= 12 ? " pm" : " am") + "</b>";

            timeDialog(str);

        }
    }

    private void requestNowDialog(String message) {
        Dialog requestDialog = new Dialog(this);

        requestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        requestDialog.setContentView(R.layout.dialog_delete_all);
        requestDialog.setCanceledOnTouchOutside(false);
        TextView title = requestDialog.findViewById(R.id.title);
        title.setText(message);
        Button yes = requestDialog.findViewById(R.id.yes);
        yes.setText(getResources().getString(R.string.request_later));

        Button no = requestDialog.findViewById(R.id.no);
        no.setText(getResources().getString(R.string.cancel));
        yes.setOnClickListener(v -> {
            requestDialog.dismiss();
            if (!sp.getString(Constants.PreferenceKey.KEY_USER_ID, "").equals("") && sp.getBoolean(Constants.PreferenceKey.KEY_VALID_FLAG, false)) {
                callScheduleFun();
            } else {
                Intent intent = new Intent(HomePagerActivity.this, SignUpActivity.class);
                startActivityForResult(intent, SIGN_UP_SCHEDULE);
            }
        });
        no.setOnClickListener(v -> {
            requestDialog.dismiss();
        });
        requestDialog.show();
    }

    public void timeDialog(String message) {
        Log.e("show", "dialog");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        } else {
            dialog = new Dialog(HomePagerActivity.this);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_interest_settings);
            dialog.setCancelable(false);
            TextView title = dialog.findViewById(R.id.text);

            title.setText(Html.fromHtml(message));
            Button ok = dialog.findViewById(R.id.continuee);
            ok.setText("OK");
            ok.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetHomeImagesTask extends AsyncTask<String, Void, String> {
        private Context context;
        //        String mobile;
//        private SharedPreferences sp;
        String status = "";
        String orderidid = "";
        private GetHomeImagesResponse getHomeImagesResponse;
        String json;


        GetHomeImagesTask(Context context) {
            this.context = context;

//            sp = PreferenceManager.getDefaultSharedPreferences(context);
            Log.i("orderiCOSTRUCCCT", orderidid);

        }


        @Override

        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... params) {

            json = Webservice.callGetService(Webservice.GET_INTRO_IMAGES);

            Log.e(TAG, json);
            try {
                // Convert String to json object
                JSONObject object1 = new JSONObject(json);
                status = object1.getString("status");


            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getHomeImagesResponse = new Gson().fromJson(json, GetHomeImagesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
//                    return context.getResources().getString(R.string.json_exception);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
//            progressDialog.dismiss();
            if (resp == null) {
                if (getHomeImagesResponse != null) {
                    homeImages = getHomeImagesResponse.data.Images;

                    //success response
                    PagerAdapter adapter = new HomePagerImagesAdapter(HomePagerActivity.this, homeImages);
                    viewPager.setAdapter(adapter);


                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            viewPager.post(() -> {
                                System.out.println("imageslength :" + homeImages.size());
//                                            viewPager.setCurrentItem((viewPager.getCurrentItem()+1) % 3);
                                viewPager.setCurrentItem((viewPager.getCurrentItem() + 1) % homeImages.size());


                                System.out.println("scrolee   " + viewPager.getCurrentItem());
                            });
                        }
                    };
                    Timer timer = new Timer();
                    timer.schedule(timerTask, 10000, 10000);

//

                } else
                    showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            } else if (!Utils.isNetConnected(context))
                showDialog1(context, "No internet connection", "Please check your wifi/mobile data signal");
            else {

                showDialog1(context, "Oops!!!", "Something went wrong Please try again after sometime!");
            }
        }

    }


    void showDialog1(final Context context, String title, String message) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            new GetHomeImagesTask(HomePagerActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetProfileDetailsTask extends AsyncTask<String, Void, String> {
        private Context context;
        //used registration response pattern to avoid classes creation
        private GetNearByVehiclesResponse getProfileDetailsResponse;
//        private ProgressDialog progressDialog;

        GetProfileDetailsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            String url = Webservice.GET_PROFILE_DETAILS + "?" + Webservice.USER_ID + "=" + sp.getString(Constants.PreferenceKey.KEY_USER_ID, "") + "&" + Webservice.USER_MOBILE + "=" + sp.getString(Constants.PreferenceKey.KEY_CONSUMER_MOBILE, "");
            String json = Webservice.callGetService(url);
            String status = "";
            Log.e("Response send request", json);
            try {
                // Convert String to json object
                JSONObject jsonObject = new JSONObject(json);
                status = jsonObject.getString("status");

            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }

            if (status.equalsIgnoreCase("success")) {
                try {
                    getProfileDetailsResponse = new Gson().fromJson(json, GetNearByVehiclesResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    // Convert String to json object
                    JSONObject jsonObject = new JSONObject(json);

                    return jsonObject.getJSONObject("data").getString("message");

                } catch (JSONException e) {
                    e.printStackTrace();
                    return e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String resp) {
            super.onPostExecute(resp);
            if (resp == null) {
                if (getProfileDetailsResponse != null) {
                    if (getProfileDetailsResponse.data.user_details.user_image_url != null && !getProfileDetailsResponse.data.user_details.user_image_url.equals(""))
                        Picasso.with(context).load(Webservice.PROFILE_IMAGE_PATH + getProfileDetailsResponse.data.user_details.user_image_url).into(profileImage);
                }
            } else if (!Utils.isNetConnected(context))
                showDialog2(context);

        }
    }

    void showDialog2(final Context context) {

        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle("No internet connection");
        alertDialog.setCancelable(false);

        // Setting Dialog Message
        alertDialog.setMessage("Please check your wifi/mobile data signal");

        // Setting OK Button
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Retry", (dialog, which) -> {

            alertDialog.dismiss();
            new GetProfileDetailsTask(HomePagerActivity.this).execute();
        });

        // Showing Alert Message
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SIGN_UP) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(HomePagerActivity.this, OptionsActivity.class);
                startActivity(intent);
                start.setEnabled(false);
            }
        } else if (requestCode == SIGN_UP_SCHEDULE) {
            if (resultCode == RESULT_OK) {
                callScheduleFun();
            }
        }
    }

}