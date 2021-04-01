package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Kevine James
 * @date 4/19/2019
 */
public class LiveMessage implements Parcelable {
    public static final Creator<LiveMessage> CREATOR = new Creator<LiveMessage>() {
        @Override
        public LiveMessage createFromParcel(Parcel in) {
            return new LiveMessage(in);
        }

        @Override
        public LiveMessage[] newArray(int size) {
            return new LiveMessage[size];
        }
    };
    public String message_id;
    public String sender_id;
    public String receiver_id;
    public String room_id;
    public long timestamp;
    public String text;
    public String name;
    public String avata;


    public LiveMessage() {
    }

    private LiveMessage(Parcel in) {
        message_id = in.readString();
        sender_id = in.readString();
        receiver_id = in.readString();
        room_id = in.readString();
        timestamp = in.readLong();
        text = in.readString();
        name = in.readString();
        avata = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message_id);
        dest.writeString(sender_id);
        dest.writeString(receiver_id);
        dest.writeString(room_id);
        dest.writeLong(timestamp);
        dest.writeString(text);
        dest.writeString(name);
        dest.writeString(avata);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
