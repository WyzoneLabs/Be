package com.brimbay.be.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by startappz on 22/07/2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    /*SELECT `advert_id`, `advert_name`, `advert_detail`,
    `advert_image_path`, `advert_age_group`, 
    `advert_create_date`, `advert_end_date`,
     `advert_created_by` FROM `advert` WHERE 1*/

    public static final String DATABASE_NAME = "advertdb.db";

    public static final String ADVERT_ID = "advert_id";
    public static final String ADVERT_NAME = "advert_name";
    public static final String ADVERT_DETAIL = "advert_detail";
    public static final String ADVERT_IMAGE = "advert_image_path";
    public static final String ADVERT_END_DATE = "advert_end_date";
    public static final String ADVERT_CLICKS = "advert_clicks";
    public static final String ADVERT_VIEWS = "advert_views";
    public static final String ADVERT_LIMIT = "advert_limit";
    public static final String ADVERT_TIME_TO_DISPLAY = "advert_display_time";


    public static final String ADVERT_COMPANY_NAME = "advert_company_name";




    public static  final String TABLE_NAME="advert";

    public static  final   int  DB_VERSION = 1;


    //Constructor
    public DbHelper(Context context) {
        super(context, DATABASE_NAME , null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + "("
                + ADVERT_ID + " INTEGER PRIMARY KEY,"
                + ADVERT_NAME + " VARCHAR, "
                + ADVERT_DETAIL + " VARCHAR, "
                + ADVERT_IMAGE + " TEXT, "
                + ADVERT_END_DATE + " VARCHAR, "
                + ADVERT_COMPANY_NAME + " TEXT, "
                + ADVERT_CLICKS + " INTEGER,"
                + ADVERT_VIEWS + " INTEGER,"
                + ADVERT_LIMIT + " INTEGER,"
                + ADVERT_TIME_TO_DISPLAY + " TEXT"
                + ");";

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
                              String IMAGE_,
                              String COMPANY_){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(ADVERT_ID, ID_);
        contentValues.put(ADVERT_NAME, NAME_);
        contentValues.put(ADVERT_DETAIL, DETAIL_);
        contentValues.put(ADVERT_END_DATE, END_DATE_);
        contentValues.put(ADVERT_IMAGE, IMAGE_);
        contentValues.put(ADVERT_COMPANY_NAME, COMPANY_);

        db.insert(TABLE_NAME, null, contentValues);
        db.close();

        return  true;
    }

    private boolean checkIfExist(String id) {

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME +  " WHERE " + ADVERT_ID + " = "+id+ ";";
        Cursor c = db.rawQuery(sql, null);

        if(c == null) {
            return true;
        }else { c.close();

            return false;
        }

    }

    public boolean removeAdvert(String Id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_NAME, ADVERT_ID+ "=" + Id, null) > 0;
    }


    public Cursor getSingleAdvert(String Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ADVERT_ID + " = "+ Id + "  ORDER BY " + ADVERT_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ADVERT_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


    public Cursor getRandomData() {

        /* SELECT * FROM table ORDER BY RANDOM() LIMIT 1; */

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY RANDOM() LIMIT 1 ";
        Cursor c = db.rawQuery(sql, null);
        return c;

    }


    public boolean removeExpiredAds() {

        SQLiteDatabase db = this.getReadableDatabase();
        return db.delete(TABLE_NAME, ADVERT_END_DATE +  "< date('now')", null) > 0;
    }

    /*Update Clicks */

    public Cursor getAdvertClicks(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE "+ ADVERT_ID + " = " + id;
        Log.i(TAG, "getAdvertClicks: "+ sql);
        Cursor c = db.rawQuery(sql, null);
        return c;
    }


    public boolean updateClicks(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        Cursor c = getAdvertClicks(id);

        int advert_clicks_ = 0;

        /*if (c.moveToFirst()){
            do advert_clicks_ = Integer.parseInt(c.getString(0)); while(c.moveToNext());
        }*/

        if(c != null){
            c.moveToFirst();
            if(!c.isAfterLast()){

                advert_clicks_ =  c.getInt(6);

            }

        }



        cv.put(ADVERT_CLICKS, String.valueOf(advert_clicks_ + 1));

        db.update(TABLE_NAME, cv, ADVERT_ID + "=" + id, null);

        return  true;
    }

    public boolean updateNumberOfViews(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        Cursor c = getAdvertClicks(id);

        int advert_views_ = 0;

        if(c != null){
            c.moveToFirst();
            if(!c.isAfterLast()){

                advert_views_ =  c.getInt(7);


            }

        }

        Log.i(TAG, "updateNumberOfViews: "+advert_views_+1);
        Log.i(TAG, "advert_id: "+id);


        cv.put(ADVERT_VIEWS, String.valueOf(advert_views_ + 1));

        db.update(TABLE_NAME, cv, ADVERT_ID + "=" + id, null);

        return  true;
    }



    public boolean updateViewandClicks(String id){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ADVERT_CLICKS, 0);
        cv.put(ADVERT_VIEWS, 0);

        Log.i(TAG, "updateViewandClicks: " + id);


        db.update(TABLE_NAME, cv, ADVERT_ID + "=" + id, null);

        return true;
    }
}