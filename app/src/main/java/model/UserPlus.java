package model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Kevine James
 * @date 6/25/2019
 */
public class UserPlus implements Parcelable {
    public static final Creator<UserPlus> CREATOR = new Creator<UserPlus>() {
        @Override
        public UserPlus createFromParcel(Parcel in) {
            return new UserPlus(in);
        }

        @Override
        public UserPlus[] newArray(int size) {
            return new UserPlus[size];
        }
    };
    public String gender;
    public String age_set;
    public String location;

    public UserPlus() {
    }

    protected UserPlus(Parcel in) {
        gender = in.readString();
        age_set = in.readString();
        location = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(gender);
        dest.writeString(age_set);
        dest.writeString(location);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
