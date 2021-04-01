package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

import model.Notification;
import utils.DateUtils;

//
// Created by Kevine James on 1/25/2020, Saturday 23:32.
// Copyright (c) 2020 Singular Point. All rights reserved.
//
public class NotificationDB {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_BODY + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_TIMESTAMP + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_TYPE + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    private static NTFDBHelper mDbHelper = null;
    private static NotificationDB instance = null;

	// To prevent someone from accidentally instantiating the contract class,
	// make the constructor private.
	private NotificationDB(Context context) {

		mDbHelper = new NTFDBHelper(context);
		if (getListNTF() != null) {
			for (Notification n : getListNTF()) {
				if (DateUtils.getTimePassed(n.timestamp) > 300) {
					removeNTF(n);
				}
			}
		}
	}

    public static NotificationDB getInstance(Context context) {
        if (instance == null) {
			instance = new NotificationDB(context);
        }
        return instance;
    }

    public long addNTF(Notification notification) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_AVATA, notification.avata);
        values.put(FeedEntry.COLUMN_NAME_NAME, notification.name);
        values.put(FeedEntry.COLUMN_NAME_BODY, notification.body);
        values.put(FeedEntry.COLUMN_NAME_TIMESTAMP, notification.timestamp);
        values.put(FeedEntry.COLUMN_NAME_TYPE, notification.type);
        // Insert the new row, returning the primary key value of the new row
        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }


	public long removeNTF(Notification notification){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(FeedEntry.TABLE_NAME, FeedEntry.COLUMN_NAME_TIMESTAMP + "=?", new String[]{String.valueOf(notification.timestamp)});
    }

    public void addListNTF(ArrayList<Notification> notifications) {
        for (Notification ntf : notifications) {
            addNTF(ntf);
        }
    }

    public ArrayList<Notification> getListNTF() {
        ArrayList<Notification> listNTF = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Notification friend = new Notification();
                friend.body = cursor.getString(1);
                friend.name = cursor.getString(0);
                friend.timestamp = cursor.getLong(2);
                friend.type = cursor.getInt(3);
                friend.avata = cursor.getString(4);
                listNTF.add(friend);
            }
            cursor.close();
        } catch (Exception e) {
            return new ArrayList<Notification>();
        }
        return listNTF;
    }

    public void dropDB() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "notifications";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_BODY = "body";
        static final String COLUMN_NAME_TYPE = "type";
        static final String COLUMN_NAME_AVATA = "avata";
        static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private static class NTFDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "chatrequest.db";

        NTFDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
