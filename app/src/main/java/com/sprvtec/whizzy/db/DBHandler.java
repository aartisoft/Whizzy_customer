package com.sprvtec.whizzy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sprvtec.whizzy.vo.LocationInf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sowjanya on 5/9/2017.
 */

public class DBHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME =
            "locationsDB";
    //  table name
    private static final String TABLE_LOCATIONS =
            "locations";
    // Shops Table Columns names
    private static final String KEY_ID =
            "id";
    private static final String KEY_LOCATION_NAME =
            "LocationName";
    private static final String KEY_LOCAtION_ADDRESS =
            "LocationAddress";
    private static final String KEY_TYPE =
            "Type";
    private static final String KEY_LATITUDE =
            "latitude";
    private static final String KEY_LONGITUDE =
            "longitude";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LOCATION_NAME + " TEXT,"
                + KEY_LOCAtION_ADDRESS + " TEXT,"
                + KEY_TYPE + " INTEGER,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);
// Creating tables again
        onCreate(db);
    }

    // Adding new Location
    public void addLocation(LocationInf location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION_NAME, location.getLocationName()); // location name
        values.put(KEY_LOCAtION_ADDRESS, location.getLocationAddress()); // location address
        values.put(KEY_TYPE, location.getType()); //location type
        values.put(KEY_LATITUDE, location.getLatitude()); //location latitude
        values.put(KEY_LONGITUDE, location.getLongitude()); //location longitude

// Inserting Row
        db.insert(TABLE_LOCATIONS, null, values);
        db.close(); // Closing database connection
    }

    // Getting one location
    public LocationInf getlocation(String address) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATIONS, new String[]{KEY_ID,
                        KEY_LOCATION_NAME, KEY_LOCAtION_ADDRESS, KEY_TYPE, KEY_LATITUDE, KEY_LONGITUDE}, KEY_LOCAtION_ADDRESS + " =?",
                new String[]{address}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        LocationInf location = null;
        try {
            location = new LocationInf(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), Integer.parseInt(cursor.getString(3)), Double.parseDouble(cursor.getString(4)), Double.parseDouble(cursor.getString(4)));
        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        // return location
        return location;
    }

    // Getting All locations
    private List<LocationInf> getAllLocations() {
        List<LocationInf> shopList = new ArrayList<>();
// Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_LOCATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

// looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocationInf location = new LocationInf();
                location.setId(Integer.parseInt(cursor.getString(0)));
                location.setLocationName(cursor.getString(1));
                location.setLocationAddress(cursor.getString(2));
                location.setType(Integer.parseInt(cursor.getString(3)));
                location.setLatitude(Double.parseDouble(cursor.getString(4)));
                location.setLongitude(Double.parseDouble(cursor.getString(5)));

// Adding contact to list
                shopList.add(location);
            } while (cursor.moveToNext());
        }

// return contact list
        cursor.close();
        return shopList;
    }

    public List<LocationInf> getCustomLocations() {
        List<LocationInf> locations = new ArrayList<>();
        List<LocationInf> allLocations = getAllLocations();
        for (LocationInf loc : allLocations)
            if (loc.type == 1)
                locations.add(loc);
        return locations;
    }

    public List<LocationInf> getRecentLocations() {
        List<LocationInf> locations = new ArrayList<>();
        List<LocationInf> allLocations = getAllLocations();
        for (LocationInf loc : allLocations)
            if (loc.type == 2)
                locations.add(loc);
        return locations;
    }

    public LocationInf getHomeLocation() {
        List<LocationInf> allLocations = getAllLocations();
        for (LocationInf loc : allLocations)
            if (loc.type == 0)
                return loc;

        return null;
    }


//    // Getting location Count
//    public int getLocationsCount() {
//        String countQuery = "SELECT * FROM " + TABLE_LOCATIONS;
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.rawQuery(countQuery, null);
//        cursor.close();
//
//// return count
//        return cursor.getCount();
//    }
//
//    // Updating a location
//    public int updateLocation(LocationInf location) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_LOCATION_NAME, location.getLocationName());
//        values.put(KEY_LOCAtION_ADDRESS, location.getLocationAddress());
//        values.put(KEY_TYPE, String.valueOf(location.getType()));
//        values.put(KEY_TYPE, String.valueOf(location.getLatitude()));
//        values.put(KEY_TYPE, String.valueOf(location.getLongitude()));
//
//// updating row
//        return db.update(TABLE_LOCATIONS, values, KEY_ID + " =?",
//                new String[]{String.valueOf(location.getId())});
//    }
//
//    // Deleting a location
//    public void deleteLocation(LocationInf location) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_LOCATIONS, KEY_ID + " =?",
//                new String[]{String.valueOf(location.getId())});
//        db.close();
//    }
}