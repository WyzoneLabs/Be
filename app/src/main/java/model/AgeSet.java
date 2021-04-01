package model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

//
// Created by Kevine James on 9/24/2020.
// Copyright (c) 2020 Brimbay Software. All rights reserved.
//
public class AgeSet implements Parcelable {
	@SerializedName("age_id")
	public String id;
	@SerializedName("age_set")
	public String set;

	public AgeSet() {
	}

	protected AgeSet(Parcel in) {
		id = in.readString();
		set = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(set);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<AgeSet> CREATOR = new Creator<AgeSet>() {
		@Override
		public AgeSet createFromParcel(Parcel in) {
			return new AgeSet(in);
		}

		@Override
		public AgeSet[] newArray(int size) {
			return new AgeSet[size];
		}
	};
}
