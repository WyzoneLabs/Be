package model;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountSet implements Parcelable {
    public String id;
    public String title;
    public String sub_title;
    public int icon;
    public int color;

    public AccountSet() {
    }

    protected AccountSet(Parcel in) {
        id = in.readString();
        title = in.readString();
        sub_title = in.readString();
        icon = in.readInt();
        color = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(sub_title);
        dest.writeInt(icon);
        dest.writeInt(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AccountSet> CREATOR = new Creator<AccountSet>() {
        @Override
        public AccountSet createFromParcel(Parcel in) {
            return new AccountSet(in);
        }

        @Override
        public AccountSet[] newArray(int size) {
            return new AccountSet[size];
        }
    };
}
