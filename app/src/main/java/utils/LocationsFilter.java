package utils;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import adapter.LocationAdapter;
import model.Locations;

//
// Created by Kevine James on 9/24/2020.
// Copyright (c) 2020 Brimbay Software. All rights reserved.
//
public class LocationsFilter extends Filter {
	private final LocationAdapter adapter;
	private final List<Locations> filterList;

	public LocationsFilter(List<Locations> filterList, LocationAdapter adapter) {
		this.adapter = adapter;
		this.filterList = filterList;
	}

	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		FilterResults results = new FilterResults();

		if (constraint != null) {
			constraint = constraint.toString().toUpperCase();
			List<Locations> filteredPlayers = new ArrayList<>();

			for (int i = 0; i < filterList.size(); i++) {
				if (filterList.get(i).name.toUpperCase().contains(constraint) /*||
						filterList.get(i).county.toUpperCase().contains(constraint)||
						filterList.get(i).country.toUpperCase().contains(constraint)*/) {
					filteredPlayers.add(filterList.get(i));
				}
			}
			results.count = filteredPlayers.size();
			results.values = filteredPlayers;
		} else {
			results.count = filterList.size();
			results.values = filterList;
		}

		return results;
	}

	@Override
	protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
		if (adapter != null) {
			adapter.locationsList = (List<Locations>) filterResults.values;
			adapter.notifyDataSetChanged();
		}
	}
}
