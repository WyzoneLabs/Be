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

import model.Chat;
import model.Friend;

public class RecentChatsDB {
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
					FeedEntry.COLUMN_FRIEND_ID + TEXT_TYPE + COMMA_SEP +
					FeedEntry.COLUMN_ROOM_ID + TEXT_TYPE + COMMA_SEP +
					FeedEntry.COLUMN_FRIEND_NAME + TEXT_TYPE + COMMA_SEP +
					FeedEntry.COLUMN_FRIEND_AVATA + TEXT_TYPE + COMMA_SEP +
					FeedEntry.COLUMN_LAST_MSG + TEXT_TYPE + COMMA_SEP +
					FeedEntry.COLUMN_TIMESTAMP + INT_TYPE + " )";
	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;
	private static RecentsDBHelper mDbHelper = null;
	private static RecentChatsDB instance = null;

	// To prevent someone from accidentally instantiating the contract class,
	// make the constructor private.
	private RecentChatsDB() {

	}

	public static RecentChatsDB getInstance(Context context) {
		if (instance == null) {
			instance = new RecentChatsDB();
			mDbHelper = new RecentsDBHelper(context);
		}
		return instance;
	}

	public long addResentChat(Context context, Chat chat) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		// Create a new map of values, where column names are the keys
		if (getChat(context, chat.friend.id) == null) {
			ContentValues values = new ContentValues();
			values.put(FeedEntry.COLUMN_FRIEND_AVATA, chat.friend.avata);
			values.put(FeedEntry.COLUMN_FRIEND_ID, chat.friend.id);
			values.put(FeedEntry.COLUMN_FRIEND_NAME, chat.friend.name);
			values.put(FeedEntry.COLUMN_TIMESTAMP, chat.timestamp);
			values.put(FeedEntry.COLUMN_ROOM_ID, chat.friend.room_id);
			values.put(FeedEntry.COLUMN_LAST_MSG, chat.message);
			// Insert the new row, returning the primary key value of the new row

			Log.d("Recent", "added:" + chat.message + " > " + chat.friend.id);
			return db.insert(FeedEntry.TABLE_NAME, null, values);
		} else {
			return updateResent(chat.friend.id, chat.message, chat.timestamp) ? 1 : 0;
		}

	}

	public boolean updateResent(String friendId, String msg, long timestamp) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		try {
			ContentValues values = new ContentValues();
			values.put(FeedEntry.COLUMN_LAST_MSG, msg);
			values.put(FeedEntry.COLUMN_TIMESTAMP, timestamp);

			final String where = FeedEntry.COLUMN_FRIEND_ID + " = ? ";
			db.update(FeedEntry.TABLE_NAME, values, where,
					new String[]{friendId});
			return true;
		} catch (SQLException e) {
			Log.e("UPDATE_DELIVERY_STATUS", e.getLocalizedMessage() + " in line:\n" + Arrays.toString(e.getStackTrace()));
		}
		return false;
	}

	public long removeChat(Chat chat) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		return db.delete(FeedEntry.TABLE_NAME, FeedEntry.COLUMN_FRIEND_ID + "=?",
				new String[]{String.valueOf(chat.friend.id)});
	}

	public void addListNTF(Context context, ArrayList<Chat> chats) {
		for (Chat chat : chats) {
			addResentChat(context, chat);
		}
	}

	public Chat getChat(Context context, String id) {
		Chat chat = null;
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.

		String selection = FeedEntry.COLUMN_FRIEND_ID + " = ?";
		// Specify arguments in placeholder order.
		String[] selectionArgs = {id};

		String[] projection = {
				FeedEntry.COLUMN_FRIEND_ID,
				FeedEntry.COLUMN_ROOM_ID,
				FeedEntry.COLUMN_FRIEND_NAME,
				FeedEntry.COLUMN_FRIEND_AVATA,
				FeedEntry.COLUMN_LAST_MSG,
				FeedEntry.COLUMN_TIMESTAMP};
		// How you want the results sorted in the resulting Cursor
		String sortOrder = FeedEntry.COLUMN_FRIEND_ID + " DESC";

		try {
			Cursor cursor =
					db.rawQuery("SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " +
							FeedEntry.COLUMN_FRIEND_ID + " = ?", new String[]{id});
			if (cursor.moveToFirst()) {
				do {
					chat = new Chat();
					Friend friend = FriendDB.getInstance(context).getFriend(cursor.getString(0));

					if (friend == null || friend.name == null) {
						friend = new Friend();
						friend.id = cursor.getString(0);
						friend.room_id = cursor.getString(1);
						friend.name = cursor.getString(2);
						friend.avata = cursor.getString(3);
					}


					chat.friend = friend;
					chat.message = cursor.getString(4);
					chat.timestamp = cursor.getLong(5);
				} while (cursor.moveToNext());
			} else {
				return null;
			}
			cursor.close();
		} catch (Exception e) {
			return null;
		}
		return chat;
	}

	public ArrayList<Chat> getListResents(Context context) {
		ArrayList<Chat> listNTF = new ArrayList<>();
		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		// Define a projection that specifies which columns from the database
		// you will actually use after this query.
		try {
			Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
			while (cursor.moveToNext()) {
				Chat chat = new Chat();
				Friend friend = FriendDB.getInstance(context).getFriend(cursor.getString(0));

				if (friend == null || friend.name == null) {
					friend = new Friend();
					friend.id = cursor.getString(0);
					friend.room_id = cursor.getString(1);
					friend.name = cursor.getString(2);
					friend.avata = cursor.getString(3);
				}


				chat.friend = friend;
				chat.message = cursor.getString(4);
				chat.timestamp = cursor.getLong(5);
				listNTF.add(chat);
			}
			cursor.close();
		} catch (Exception e) {
			Log.d("Recents", "error:" + e.getLocalizedMessage());
			return new ArrayList<Chat>();
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
		static final String TABLE_NAME = "chat_resents";
		static final String COLUMN_FRIEND_ID = "friend_id";
		static final String COLUMN_ROOM_ID = "room_id";
		static final String COLUMN_FRIEND_NAME = "name";
		static final String COLUMN_LAST_MSG = "message";
		static final String COLUMN_FRIEND_AVATA = "avata";
		static final String COLUMN_TIMESTAMP = "timestamp";
	}

	private static class RecentsDBHelper extends SQLiteOpenHelper {
		// If you change the database schema, you must increment the database version.
		static final int DATABASE_VERSION = 1;
		static final String DATABASE_NAME = "chatresents.db";

		RecentsDBHelper(Context context) {
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
