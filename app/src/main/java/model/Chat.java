package model;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {
	public static final Creator<Chat> CREATOR = new Creator<Chat>() {
		@Override
		public Chat createFromParcel(Parcel in) {
			return new Chat(in);
		}

		@Override
		public Chat[] newArray(int size) {
			return new Chat[size];
		}
	};
	public Friend friend;
	public String message;
	public long timestamp;


	public Chat() {
	}

	protected Chat(Parcel in) {
		friend = in.readParcelable(Friend.class.getClassLoader());
		message = in.readString();
		timestamp = in.readLong();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(friend, flags);
		dest.writeString(message);
		dest.writeLong(timestamp);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
