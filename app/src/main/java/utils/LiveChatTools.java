package utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.brimbay.be.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import model.ChatRequest;
import model.Friend;
import ui.BeinSwitch;

/**
 * @author Kevine James
 * @date 5/29/2019
 */
public class LiveChatTools {
    private Activity mContext;
    private PreferenceHelper preferenceHelper;

    /*Firebase*/
    private FirebaseAuth mAuth = null;

    public LiveChatTools(Activity activity) {
        this.mContext = activity;
        preferenceHelper = PreferenceHelper.getInstance(activity.getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
    }

    public void sendLiveChatRequest(Friend friend) {
//        if (mAuth == null || friend == null || mAuth.getUid() == null) return;

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.accepted = false;
        chatRequest.caller_id =  mAuth.getUid();
        chatRequest.guest_id = friend.id;
        chatRequest.room_id = friend.room_id;
        chatRequest.timestamp = System.currentTimeMillis();

        FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
                .child(friend.id)
                .child("request").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    long timestamp = (long) dataSnapshot.child("timestamp").getValue();
                    boolean accepted = (boolean) dataSnapshot.child("accepted").getValue();
                    String caller_id = (String) dataSnapshot.child("caller_id").getValue();

                    if (accepted) {
                        //Friend is another call
                        ViewsUtil.showToast(mContext, friend.phone + " is on another call, try again later!", 0, R.color.colorOrange);
                    } else {
                        long timepassed = DateUtils.getTimePassed(timestamp);
                        if (timepassed >= 45) {
                            dataSnapshot.getRef().removeValue();
                            dataSnapshot.getRef().setValue(chatRequest);
                            ViewsUtil.showToast(mContext, "Live Chat successfully sent", 0, R.color.colorGreen);
                        } else {
                            ViewsUtil.showToast(mContext, "Sorry, line busy. Please try again later!", 0, R.color.colorOrange);
                        }
                    }
                } else {
                    dataSnapshot.getRef().setValue(chatRequest);
                    ViewsUtil.showToast(mContext, "Live Chat successfully sent", 0, R.color.colorGreen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ViewsUtil.showToast(mContext, "Error: " + databaseError.getMessage(), 0, R.color.colorPink);
            }
        });
    }

    public void liveChatRequests() {
        if (mAuth == null || mAuth.getUid() == null) return;

        FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
                .child(mAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {
                    ChatRequest chatRequest = new ChatRequest();
                    chatRequest.accepted = (boolean) dataSnapshot.child("accepted").getValue();
                    chatRequest.guest_id = (String) dataSnapshot.child("guest_id").getValue();
                    chatRequest.timestamp = (long) dataSnapshot.child("timestamp").getValue();
                    chatRequest.room_id = (String) dataSnapshot.child("room_id").getValue();
                    chatRequest.caller_id = (String) dataSnapshot.child("caller_id").getValue();

//                    liveChatNotification(chatRequest);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getLiveChatRequests(Friend friend, BeinSwitch beinSwitch, View v_parent, TextView v_info, Button v_accept) {
//        if (mAuth == null || mAuth.getUid() == null) return;
        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) v_parent.getLayoutParams();
        lParams.rightMargin = 0;
        lParams.leftMargin = 0;
        FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
                .child(/*"FKNLZC54Wea9ie54V6v2zyZBf9J3"*/mAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {
                    ChatRequest chatRequest = new ChatRequest();
                    chatRequest.accepted = (boolean) dataSnapshot.child("accepted").getValue();
                    chatRequest.guest_id = (String) dataSnapshot.child("guest_id").getValue();
                    chatRequest.timestamp = (long) dataSnapshot.child("timestamp").getValue();
                    chatRequest.room_id = (String) dataSnapshot.child("room_id").getValue();
                    chatRequest.caller_id = (String) dataSnapshot.child("caller_id").getValue();

                    if (chatRequest.guest_id == null ||
                            !chatRequest.room_id.substring(1).equals(friend.room_id.substring(1)))
                        return;

                    if (preferenceHelper.getNotificationsRingtone() && preferenceHelper.getNotificationsInChatSound()) {
                        Tools.playMedia(mContext, R.raw.chat_request);
                    }

                    try {
                        v_parent.setLayoutParams(lParams);
                        v_parent.setVisibility(View.VISIBLE);
                        v_info.setText(mContext.getString(R.string.live_chat_request, friend.name));
                        v_parent.setOnTouchListener(new WindowTouchListener(v_parent, dataSnapshot.getRef()));
                        v_accept.setOnClickListener(v -> {
                            dataSnapshot.getRef().child("accepted").setValue(true);
                            //TODO:enable video call here

                            beinSwitch.setChecked(BeinSwitch.Checked.RIGHT);
//                            Intent intent = new Intent(mContext, VideoCallActivity.class);
//                            intent.setAction(VideoCallActivity.ACTION_PRIVATE_LIVE_CHAT_DATA);
//                            intent.putExtra(VideoCallActivity.INTENT_EXTRA_LIVE_CHAT_FRIEND, friend);
//                            mContext.startActivity(intent);
                            v_parent.setVisibility(View.GONE);
                            v_parent.setOnTouchListener(null);
                        });

                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
                .child(friend.id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getValue() != null) {

                    ChatRequest chatRequest = dataSnapshot.getValue(ChatRequest.class);
                    if (chatRequest == null) return;
                    if (chatRequest.accepted && chatRequest.caller_id.equals(/*"FKNLZC54Wea9ie54V6v2zyZBf9J3"*/mAuth.getUid()) &&
                            chatRequest.room_id.substring(3).equals(friend.room_id.substring(3))) {
                        //TODO:Video call request accepted

//                        Intent intent = new Intent(mContext, VideoCallActivity.class);
//                        intent.setAction(VideoCallActivity.ACTION_PRIVATE_LIVE_CHAT_DATA);
//                        intent.putExtra(VideoCallActivity.INTENT_EXTRA_LIVE_CHAT_FRIEND, friend);
//                        mContext.startActivity(intent);

                        dataSnapshot.getRef().removeValue();
                        Log.d("Vid_Chat_Request", "Friend accepted chat accepted" );
                    } else {
                        dataSnapshot.getRef().removeValue();
                        ViewsUtil.showToast(mContext, String.format("Could not connect to %s!", friend.name), 0, R.color.colorPink);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                Toast.makeText(mContext, String.format("Could not connect to %s!", friend.name), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static final class WindowTouchListener implements View.OnTouchListener {

        int x, y;
        int touchedX, touchedY;
        private View _live_cont;
        private ConstraintLayout.LayoutParams lParams;
        private ConstraintLayout.LayoutParams updateLayoutParams;
        private DatabaseReference mLiveRef;

        WindowTouchListener(View _live_cont, DatabaseReference liveRef) {
            this._live_cont = _live_cont;
            this.mLiveRef = liveRef;
            lParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
            updateLayoutParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
        }


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            v.performClick();
            touchedX = (int) event.getRawX();
            touchedY = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    x = touchedX - updateLayoutParams.leftMargin;
//                    y = touchedY - updateLayoutParams.y;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    updateLayoutParams.resolveLayoutDirection(LayoutDirection.LTR);
                    updateLayoutParams.rightMargin = -(touchedX - x);
                    updateLayoutParams.leftMargin = touchedX - x;
//                    updateLayoutParams.y = touchedY - y;
                    if (touchedX - x >= 0) {
                        _live_cont.setLayoutParams(updateLayoutParams);

                        if (touchedX - x >= 200) {
                            mLiveRef.removeValue();
                            _live_cont.setVisibility(View.GONE);
                            lParams.leftMargin = 0;
                            lParams.rightMargin = 0;
                            _live_cont.setLayoutParams(lParams);
                            _live_cont.setOnTouchListener(null);
                        }
                    }
                    return true;
            }
            return false;
        }
    }

}
















