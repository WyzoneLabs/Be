package model;


import android.os.Parcel;
import android.os.Parcelable;

public class Friend extends User implements Parcelable {
    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel in) {
            return new Friend(in);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
    public String id;
    public String room_id;

    public Friend() {
    }

    protected Friend(Parcel in) {
        super(in);
        id = in.readString();
        room_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeString(room_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
