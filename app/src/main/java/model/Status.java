package model;


import android.os.Parcel;
import android.os.Parcelable;

public class Status implements Parcelable {
    public boolean is_online;
    public long timestamp;

    public Status() {
        is_online = false;
        timestamp = System.currentTimeMillis();
    }

    protected Status(Parcel in) {
        is_online = in.readByte() != 0;
        timestamp = in.readLong();
    }
    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (is_online ? 1 : 0));
        dest.writeLong(timestamp);
    }
}
