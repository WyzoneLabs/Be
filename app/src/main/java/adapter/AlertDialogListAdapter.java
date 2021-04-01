package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;

import java.util.ArrayList;
import java.util.List;

import model.AgeSet;
import model.Gender;
import model.Locations;

//
// Created by Kevine James on 12/5/2019, Thursday 11:43.
// Copyright (c) 2019 Ubein Apps. All rights reserved.
//
public class AlertDialogListAdapter extends RecyclerView.Adapter<AlertDialogListAdapter.ViewHolder> {
    private ArrayList<String> lists = new ArrayList<>();
    private List<Gender> genderLists = new ArrayList<>();
    private List<AgeSet> ageLists = new ArrayList<>();
    private List<Locations> locationLists = new ArrayList<>();

    public static final int TYPE_STRING = 0;
    public static final int TYPE_GENDER = 1;
    public static final int TYPE_AGE = 2;
    public static final int TYPE_LOCATION = 3;


    private int type = TYPE_STRING;
    private OnAlertDialogListInteraction onAlertDialogListInteraction;

    public AlertDialogListAdapter() {
    }

    public void setOnAlertDialogListInteraction(OnAlertDialogListInteraction onAlertDialogListInteraction) {
        this.onAlertDialogListInteraction = onAlertDialogListInteraction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alert_dialog_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (type == TYPE_AGE) {
            AgeSet ageSet = ageLists.get(position);
            holder._text.setText(ageSet.set);
            holder._text.setOnClickListener(v -> {
                if (onAlertDialogListInteraction != null)
                    onAlertDialogListInteraction.onAlertDialogListItemClick(ageSet, position);
            });
        }else if (type == TYPE_GENDER) {
            Gender d = genderLists.get(position);
            holder._text.setText(d.name);
            holder._text.setOnClickListener(v -> {
                if (onAlertDialogListInteraction != null)
                    onAlertDialogListInteraction.onAlertDialogListItemClick(d, position);
            });
        }else if (type == TYPE_LOCATION) {
            Locations location = locationLists.get(position);
            holder._text.setText(location.name);
            holder._text.setOnClickListener(v -> {
                if (onAlertDialogListInteraction != null)
                    onAlertDialogListInteraction.onAlertDialogListItemClick(location, position);
            });
        }else {
            String d = lists.get(position);
            holder._text.setText(d);
            holder._text.setOnClickListener(v -> {
                if (onAlertDialogListInteraction != null)
                    onAlertDialogListInteraction.onAlertDialogListItemClick(d, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (type == TYPE_AGE){
            return ageLists.size();
        }else if(type == TYPE_GENDER){
            return genderLists.size();
        }else if(type == TYPE_LOCATION){
            return locationLists.size();
        }else {
            return lists.size();
        }

    }

    public void setLists(ArrayList<String> lists) {
        this.lists = lists;
        this.type = TYPE_STRING;
        notifyDataSetChanged();
    }

    public void setGenderLists(List<Gender> genderLists) {
        this.genderLists = genderLists;
        this.type = TYPE_GENDER;
        notifyDataSetChanged();
    }

    public void setAgeLists(List<AgeSet> ageLists) {
        this.ageLists = ageLists;
        this.type = TYPE_AGE;
        notifyDataSetChanged();
    }

    public void setLocationLists(List<Locations> locationLists) {
        this.locationLists = locationLists;
        this.type = TYPE_LOCATION;
        notifyDataSetChanged();
    }


    public interface OnAlertDialogListInteraction {
        void onAlertDialogListItemClick(Object s, int i);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView _text;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            _text = itemView.findViewById(R.id.adl_text);
        }
    }
}
