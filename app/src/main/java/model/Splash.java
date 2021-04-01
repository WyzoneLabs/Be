package model;

import android.os.Parcel;
import android.os.Parcelable;

public class Splash implements Parcelable {
    public String title;
    public String info;
    public int image;
    public int background;

    public Splash() {
    }

    protected Splash(Parcel in) {
        title = in.readString();
        info = in.readString();
        image = in.readInt();
        background = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(info);
        dest.writeInt(image);
        dest.writeInt(background);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Splash> CREATOR = new Creator<Splash>() {
        @Override
        public Splash createFromParcel(Parcel in) {
            return new Splash(in);
        }

        @Override
        public Splash[] newArray(int size) {
            return new Splash[size];
        }
    };
}
