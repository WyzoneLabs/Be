package model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;

import java.util.HashMap;

import static utils.Configs.STR_DEFAULT_BASE64;

public class User implements Parcelable {
    public static final String NAME = "name";
    public static final String PHONE = "phone";
    public static final String AVATA = "avata";
    public static final String BIO = "bio";
    public static final String EMOJI = "emoji";
    public static final String STATUS =  "status";
    public static final String STATUS_IS_ONLINE = "is_online";
    public static final String STATUS_TIMESTAMP =  "timestamp";

    public String name;
    public String phone;
    public String avata;
    public String emoji;
    public String bio;
    public Status status;

    public User() {
        status = new Status();
        status.is_online = false;
        status.timestamp = System.currentTimeMillis();
        emoji = STR_DEFAULT_BASE64;
    }

    public User getSmallUserFromMap(HashMap<String, String> map){
        if (map == null || map.get(NAME) == null) return null;
        this.phone = map.get(PHONE);
        this.avata = map.get(AVATA);
        this.bio = map.get(BIO);
        this.emoji = map.get(EMOJI);
        this.name  = map.get(NAME);
        return this;
    }

    public static HashMap<String,String> getSmallMapFromUser(User user){
        if (user == null || user.name == null) return null;
        HashMap<String, String> hashMap =  new HashMap<>();
        hashMap.put(PHONE, user.phone);
        hashMap.put(AVATA, user.avata);
        hashMap.put(BIO, user.bio);
        hashMap.put(EMOJI, user.emoji);
        hashMap.put(NAME, user.name);
        return hashMap;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    protected User(Parcel in) {
        name = in.readString();
        phone = in.readString();
        avata = in.readString();
        bio = in.readString();
        emoji = in.readString();
        status = in.readParcelable(Status.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(avata);
        dest.writeString(bio);
        dest.writeString(emoji);
        dest.writeParcelable(status, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
