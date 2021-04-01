package services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import database.FriendDB;
import model.Friend;
import utils.Configs;
import utils.DateUtils;

import static utils.Configs.TIME_TO_OFFLINE;


public class ServiceUtils {

    public static void updateUserStatus(Context context) {
        if (isNetworkConnected(context)) {
            String uid = FirebaseAuth.getInstance().getUid();//SharedPreferenceHelper.getInstance(context).getUID();
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference()
                        .child(Configs.DATABASE_USERS_ROOT_NAME)
                        .child(uid + "/status/is_online").setValue(true);
                FirebaseDatabase.getInstance().getReference()
                        .child(Configs.DATABASE_USERS_ROOT_NAME)
                        .child(uid + "/status/timestamp").setValue(System.currentTimeMillis());
            }

            ArrayList<Friend> friends = FriendDB.getInstance(context).getListFriend();

            for (Friend friend : friends) {
                if (friend != null && friend.status.is_online
                        && DateUtils.getTimePassed(friend.status.timestamp) > TIME_TO_OFFLINE) {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Configs.DATABASE_USERS_ROOT_NAME)
                            .child(friend.id + "/status/is_online").setValue(false);
                }
            }
        }
    }

    public static void updateFriendStatus(Context context, List<Friend> listFriend) {

    }


    public static void updateFriendStatus(Context context, Friend friend) {
        if (isNetworkConnected(context)) {
            final String fid = friend.id;
            FirebaseDatabase.getInstance().getReference()
                    .child(Configs.DATABASE_USERS_ROOT_NAME)
                    .child(fid + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapStatus = (HashMap) dataSnapshot.getValue();
                        Log.d("TEXTSX","IS_ONLINE:"+ mapStatus.get("is_online") +
                                " TIME:"+ DateUtils.getTimePassed((long) mapStatus.get("timestamp")));
                        if ((boolean) mapStatus.get("is_online") && DateUtils.getTimePassed((long) mapStatus.get("timestamp")) > TIME_TO_OFFLINE) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Configs.DATABASE_USERS_ROOT_NAME).child(fid + "/status/is_online").setValue(false);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static boolean isNetworkConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        } catch (Exception e) {
            return true;
        }
    }
}
