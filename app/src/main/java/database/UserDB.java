package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Arrays;

import model.User;

public class UserDB {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_BIO + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMOJI + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    private static UserDBHelper mDbHelper = null;
    private static UserDB instance = null;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private UserDB() {
    }

    public static UserDB getInstance(Context context) {
        if (instance == null) {
            instance = new UserDB();
            mDbHelper = new UserDBHelper(context);
        }
        return instance;
    }

    public void addUser(User friend) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_NAME, friend.name);
        values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.phone);
        values.put(FeedEntry.COLUMN_NAME_BIO, friend.bio);
        values.put(FeedEntry.COLUMN_NAME_AVATA, friend.avata);
        values.put(FeedEntry.COLUMN_NAME_EMOJI, friend.emoji);
        // Insert the new row, returning the primary key value of the new row
        db.insert(FeedEntry.TABLE_NAME, null, values);

    }

    public User getFriend() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
        User user = new User();
        while (cursor.moveToNext()) {
            user.name = cursor.getString(0);
            user.phone = cursor.getString(1);
            user.bio = cursor.getString(2);
            user.avata = cursor.getString(3);
            user.emoji = cursor.getString(4);
        }
        cursor.close();
        return user;
    }

    public boolean updateName(String name) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_NAME, name);

            db.update(FeedEntry.TABLE_NAME, values, null, null);
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public boolean updateBio(String bio) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_BIO, bio);

            db.update(FeedEntry.TABLE_NAME, values, null, null);
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public boolean updateAvata(String avata) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_AVATA, avata);

            db.update(FeedEntry.TABLE_NAME, values, null, null);
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public boolean updateEmoji(String emoji) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_EMOJI, emoji);
            db.update(FeedEntry.TABLE_NAME, values, null, null);
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public void dropDB() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "be_user";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_BIO = "bio";
        static final String COLUMN_NAME_AVATA = "avata";
        static final String COLUMN_NAME_EMOJI = "emoji";
    }

    private static class UserDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "brimbay_be_user.db";

        UserDBHelper(Context context) {
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
