package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;

import java.util.ArrayList;
import java.util.Objects;

import database.NotificationDB;
import model.Friend;
import model.Notification;
import utils.DateUtils;
import xyz.schwaab.avvylib.AvatarView;

//
// Created by Kevine James on 1/25/2020, Saturday 22:32.
// Copyright (c) 2020 Singular Point Soft. All rights reserved.
//
public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notification> notifications;
    private OnNotificationInteraction onNotificationInteraction;

    public interface OnNotificationInteraction{
        void onClickNotification(Friend friend);
    }

    public NotificationsAdapter(Context context) {
        this.context = context;
        this.notifications = NotificationDB.getInstance(context).getListNTF();
    }

    public void refresh() {
        this.notifications = NotificationDB.getInstance(context).getListNTF();
        notifyDataSetChanged();
    }

    public void setOnNotificationInteraction(OnNotificationInteraction onNotificationInteraction) {
        this.onNotificationInteraction = onNotificationInteraction;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_view, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder._name.setText(notification.name);
        holder._body.setText(notification.body);
        holder._time.setText(DateUtils.getPassedTime(notification.timestamp));
        holder.bind(context);


        if (!Objects.equals(notification.avata, "default")) {
            byte[] decodedString = Base64.decode(notification.avata, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder._avata.setImageBitmap(bitmap);
        }
        holder.itemView.setOnClickListener(view -> {
            if(onNotificationInteraction != null ){
                onNotificationInteraction.onClickNotification(notification.friend);
            }
        });

        new CountDownTimer(DateUtils.getTimePassed(notification.timestamp)*1000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                long secs = (millisUntilFinished/1000);
                holder._time.setText(secs+" s");
            }

            @Override
            public void onFinish() {
                NotificationDB.getInstance(context).removeNTF(notification);
            }
        }.start();

//        if (notification.type == Notification.NTF_SENT_CHAT_REQUEST){
//            holder._sep.setBackgroundColor(context.getResources().getColor(R.color.colorGreen));
//        }else {
//            holder._sep.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
//        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView _name, _time, _body;
        AvatarView _avata;
        ImageView _accept;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            _time = itemView.findViewById(R.id.ntf_time);
            _name = itemView.findViewById(R.id.ntf_name);
            _body = itemView.findViewById(R.id.ntf_body);
            _avata = itemView.findViewById(R.id.ntf_avata);
            _accept = itemView.findViewById(R.id.ntf_accept);
        }

        void bind(Context context){
            _avata.setAnimating(true);
            _avata.setShouldBounceOnClick(true);
            _avata.setBorderThickness(1);
            _avata.setHighlightedBorderThickness(4);
            _avata.setHighlightBorderColor(context.getResources().getColor(R.color.colorAccent, null));
            _avata.setHighlightBorderColorEnd(context.getResources().getColor(R.color.colorGreen,null));
            _avata.setNumberOfArches(3);
            _avata.setBorderColor(Color.TRANSPARENT );
            _avata.setDistanceToBorder(4);
            _avata.setHighlighted(true);
            _avata.setTotalArchesDegreeArea(60);
        }
    }
}
