package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import model.Friend;

public final class FriendDB {
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ID + TEXT_TYPE + " PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_BIO + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_ID_ROOM + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_STATUS + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_LAST_SEEN + INT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMOJI + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
    private static FriendDBHelper mDbHelper = null;
    private static FriendDB instance = null;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FriendDB() {
    }

    public static FriendDB getInstance(Context context) {
        if (instance == null) {
            instance = new FriendDB();
            mDbHelper = new FriendDBHelper(context);
        }
        return instance;
    }

    public long addFriend(Friend friend) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        if (getFriend(friend.id) != null) {
            updateFriend(friend);
            return -1L;
        }
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_ID, friend.id);
        values.put(FeedEntry.COLUMN_NAME_NAME, friend.name);
        values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.phone);
        values.put(FeedEntry.COLUMN_NAME_BIO, friend.bio);
        values.put(FeedEntry.COLUMN_NAME_ID_ROOM, friend.room_id);
        values.put(FeedEntry.COLUMN_NAME_AVATA, friend.avata);
        values.put(FeedEntry.COLUMN_NAME_LAST_SEEN, friend.status.timestamp);
        values.put(FeedEntry.COLUMN_NAME_STATUS, friend.status.is_online ? 1 : 0);
        values.put(FeedEntry.COLUMN_NAME_EMOJI, friend.emoji);
        // Insert the new row, returning the primary key value of the new row

        Log.d("Brim.FriendDB", "Adding");
        return db.insert(FeedEntry.TABLE_NAME, null, values);
    }

    public boolean updateFriend(Friend friend) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_NAME, friend.name);
            values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.phone);
            values.put(FeedEntry.COLUMN_NAME_BIO, friend.bio);
            values.put(FeedEntry.COLUMN_NAME_ID_ROOM, friend.room_id);
            values.put(FeedEntry.COLUMN_NAME_AVATA, friend.avata);
            values.put(FeedEntry.COLUMN_NAME_LAST_SEEN, friend.status.timestamp);
            values.put(FeedEntry.COLUMN_NAME_STATUS, friend.status.is_online ? 1 : 0);
            values.put(FeedEntry.COLUMN_NAME_EMOJI, friend.emoji);

            final String where = FeedEntry.COLUMN_NAME_ID + " = ? ";
            db.update(FeedEntry.TABLE_NAME, values, where,
                    new String[]{friend.id});

            Log.d("Brim.FriendDB", "Updated");
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }

        return false;
    }

    public boolean updateLastSeen(String friendId, long timestamp, long status) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_LAST_SEEN, timestamp);
            values.put(FeedEntry.COLUMN_NAME_STATUS, status);

            final String where = FeedEntry.COLUMN_NAME_ID + " = ? ";
            db.update(FeedEntry.TABLE_NAME, values, where,
                    new String[]{friendId});
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public boolean updateEmoji(String friendId, String emoji) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FeedEntry.COLUMN_NAME_EMOJI, emoji);

            final String where = FeedEntry.COLUMN_NAME_ID + " = ? ";
            db.update(FeedEntry.TABLE_NAME, values, where,
                    new String[]{friendId});
            return true;
        } catch (SQLException e) {
            Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
        }
        return false;
    }

    public void addListFriend(ArrayList<Friend> listFriend) {
        for (Friend friend : listFriend) {
            addFriend(friend);
        }
    }

    public ArrayList<Friend> getListFriend() {
        ArrayList<Friend> listFriend = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Friend friend = new Friend();

                friend.id = cursor.getString(0);
                friend.name = cursor.getString(1);
                friend.phone = cursor.getString(2);
                friend.bio = cursor.getString(3);
                friend.room_id = cursor.getString(4);
                friend.avata = cursor.getString(5);
                friend.status.timestamp = cursor.getLong(7);
                friend.status.is_online = cursor.getLong(6) != 0;
                friend.emoji = cursor.getString(8);
                listFriend.add(friend);
            }
            cursor.close();
        } catch (Exception e) {
            Log.d("Brim.FriendsDB","Error:"+e.getMessage());
            return new ArrayList<Friend>();

        }
        return listFriend;
    }

    public Friend getFriend(String id) {
        Friend friend = null;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.

        String selection = FeedEntry.COLUMN_NAME_ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {id};

        String[] projection = {
                FeedEntry.COLUMN_NAME_ID,
                FeedEntry.COLUMN_NAME_NAME,
                FeedEntry.COLUMN_NAME_EMAIL,
                FeedEntry.COLUMN_NAME_BIO,
                FeedEntry.COLUMN_NAME_ID_ROOM,
                FeedEntry.COLUMN_NAME_AVATA,
                FeedEntry.COLUMN_NAME_LAST_SEEN,
                FeedEntry.COLUMN_NAME_EMOJI};
        // How you want the results sorted in the resulting Cursor
        String sortOrder = FeedEntry.COLUMN_NAME_NAME + " DESC";

        try {
//            Cursor cursor = db.query(FeedEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
//            cursor.moveToFirst();
            Cursor cursor = db.rawQuery("SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " + FeedEntry.COLUMN_NAME_ID +" = ?" , new String[]{id});
//            while (cursor.moveToNext()) {

//            while (!cursor.isAfterLast()) {

                if (cursor.moveToFirst()) {
                    do {
                        friend = new Friend();
                        friend.id = cursor.getString(0);
                        friend.name = cursor.getString(1);
                        friend.phone = cursor.getString(2);
                        friend.bio = cursor.getString(3);
                        friend.room_id = cursor.getString(4);
                        friend.avata = cursor.getString(5);
                        friend.status.timestamp = cursor.getLong(7);
                        friend.status.is_online = cursor.getLong(6) != 0;
                        friend.emoji = cursor.getString(8);
                    } while (cursor.moveToNext());
                }else{
                    return null;
                }

//            }
            cursor.close();
        } catch (Exception e) {
            return null;
        }
        return friend;
    }

    public void dropDB() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "be_friends";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "phone";
        static final String COLUMN_NAME_BIO = "bio";
        static final String COLUMN_NAME_ID_ROOM = "idRoom";
        static final String COLUMN_NAME_AVATA = "avata";
        static final String COLUMN_NAME_LAST_SEEN = "timestamp";
        static final String COLUMN_NAME_STATUS = "status";
        static final String COLUMN_NAME_EMOJI = "emoji";
    }

    private static class FriendDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "brimbay_be_friend.db";

        FriendDBHelper(Context context) {
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
