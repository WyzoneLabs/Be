package model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

//
// Created by Kevine James on 9/24/2020.
// Copyright (c) 2020 Brimbay Software. All rights reserved.
//
public class Gender implements Parcelable {
	@SerializedName("id")
	public String id;
	@SerializedName("gender_name")
	public String name;

	public Gender() {
	}


	protected Gender(Parcel in) {
		id = in.readString();
		name = in.readString();
	}

	public static final Creator<Gender> CREATOR = new Creator<Gender>() {
		@Override
		public Gender createFromParcel(Parcel in) {
			return new Gender(in);
		}

		@Override
		public Gender[] newArray(int size) {
			return new Gender[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(id);
		parcel.writeString(name);
	}
}
