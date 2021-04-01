package model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

//
// Created by Kevine James on 9/24/2020.
// Copyright (c) 2020 Brimbay Software. All rights reserved.
//
public class Locations implements Parcelable {
	@SerializedName("city_id")
	public String id;
	@SerializedName("city_name")
	public String name;
	@SerializedName("city_county")
	public String county;
	@SerializedName("city_country")
	public String country;
	@SerializedName("city_latitude")
	public String latitude;
	@SerializedName("city_longtitude")
	public String longtitude;
	@SerializedName("city_population")
	public String population;

	public Locations() {
	}

	protected Locations(Parcel in) {
		id = in.readString();
		name = in.readString();
		county = in.readString();
		country = in.readString();
		latitude = in.readString();
		longtitude = in.readString();
		population = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(county);
		dest.writeString(country);
		dest.writeString(latitude);
		dest.writeString(longtitude);
		dest.writeString(population);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<Locations> CREATOR = new Creator<Locations>() {
		@Override
		public Locations createFromParcel(Parcel in) {
			return new Locations(in);
		}

		@Override
		public Locations[] newArray(int size) {
			return new Locations[size];
		}
	};
}
