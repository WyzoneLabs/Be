package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Kevine James
 * @date 5/5/2019
 */
public class ChatRequest implements Parcelable {
    public static final Creator<ChatRequest> CREATOR = new Creator<ChatRequest>() {
        @Override
        public ChatRequest createFromParcel(Parcel in) {
            return new ChatRequest(in);
        }

        @Override
        public ChatRequest[] newArray(int size) {
            return new ChatRequest[size];
        }
    };
    public String caller_id;
    public String guest_id;
    public boolean accepted;
    public String room_id;
    public long timestamp;


    public ChatRequest() {
    }

    protected ChatRequest(Parcel in) {
        caller_id = in.readString();
        guest_id = in.readString();
        accepted = in.readByte() != 0;
        room_id = in.readString();
        timestamp = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caller_id);
        dest.writeString(guest_id);
        dest.writeByte((byte) (accepted ? 1 : 0));
        dest.writeString(room_id);
        dest.writeLong(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
