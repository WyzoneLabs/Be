package model;

import android.os.Parcel;
import android.os.Parcelable;

//
// Created by Kevine James on 1/25/2020, Saturday 22:34.
// Copyright (c) 2020 Singular Point. All rights reserved.
//
public class Notification implements Parcelable {
    public static final int NTF_SENT_CHAT_REQUEST = 0;
    public static final int NTF_RECEIVED_CHAT_REQUEST = 1;

    public String body;
    public String name;
    public long timestamp;
    public int type;
    public String avata;
    public Friend friend;

    public Notification() {
        friend = new Friend();
    }

    protected Notification(Parcel in) {
        body = in.readString();
        name = in.readString();
        timestamp = in.readLong();
        type = in.readInt();
        avata = in.readString();
        friend = in.readParcelable(Friend.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeString(name);
        dest.writeLong(timestamp);
        dest.writeInt(type);
        dest.writeString(avata);
        dest.writeParcelable(friend, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };
}
