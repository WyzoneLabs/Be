package com.brimbay.be.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by startappz on 28/07/2017.
 */

public class DbProfile extends SQLiteOpenHelper {

    /*SELECT `advert_id`, `advert_name`, `advert_detail`,
    `advert_image_path`, `advert_age_group`,
    `advert_create_date`, `advert_end_date`,
     `advert_created_by` FROM `advert` WHERE 1*/

    public static final String DATABASE_NAME = "profile.db";

    public static final String USER_NAME = "user_name";
    public static final String USER_PHONE = "user_phone";
    public static final String USER_COVER_IMAGE = "user_cover_image";
    public static final String USER_PROFILE_IMAGE = "user_profile_image";
    public static final String ADVERT_POINTS = "user_points";

    public static  final String TABLE_NAME="advert";

    public static  final   int  DB_VERSION = 1;


    //Constructor
    public  DbProfile (Context context) {
        super(context, DATABASE_NAME , null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "("
                + USER_PHONE + " INTEGER PRIMARY KEY,"
                + USER_NAME + " VARCHAR, "
                + USER_COVER_IMAGE + " VARCHAR, "
                + USER_PROFILE_IMAGE + " VARCHAR, "
                + ADVERT_POINTS + " INTEGER);";

        Log.d("Sql",sql);

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public  boolean addAdvert(String ID_,
                              String NAME_,
                              String DETAIL_,
                              String END_DATE_,
                              String IMAGE_){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(USER_PHONE, ID_);
        contentValues.put(USER_NAME, NAME_);
        contentValues.put(USER_COVER_IMAGE, DETAIL_);
        contentValues.put(USER_PROFILE_IMAGE, IMAGE_);

        db.insert(TABLE_NAME, null, contentValues);
        db.close();

        /*if(checkIfExist(ID_)) {
            db.insert(TABLE_NAME, null, contentValues);
            db.close();
        } else {
            return  false;
        }*/
        return  true;
    }

    private boolean checkIfExist(String id) {

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME +  " WHERE " + USER_PHONE + " = "+id+ ";";
        Cursor c = db.rawQuery(sql, null);

        if(c == null) {
            return true;
        }else { c.close();

            return false;
        }

    }

    public boolean removeUser(String Id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_NAME, USER_PHONE+ "=" + Id, null) > 0;
    }


    public boolean updateUser(String Id, String cover, String profile)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        return true;
        //return db.delete(TABLE_NAME, USER_PHONE+ "=" + Id, null) > 0;
    }



    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + USER_PHONE + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }

    public boolean updateClicks(String id){
        return  true;
    }
}

