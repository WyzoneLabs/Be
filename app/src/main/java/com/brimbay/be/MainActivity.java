package com.brimbay.be;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brimbay.be.application.AppController;
import com.brimbay.be.application.Config;
import com.brimbay.be.classes.DownloadTask;
import com.brimbay.be.databinding.ActivityMainBinding;
import com.brimbay.be.databinding.FragmentFirstBinding;
import com.brimbay.be.services.FileService;
import com.brimbay.be.sqlite.DbHelper;
import com.google.android.exoplayer2.C;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;
import com.tomash.androidcontacts.contactgetter.entity.ContactData;
import com.tomash.androidcontacts.contactgetter.entity.PhoneNumber;
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import adapter.ChatsAdapter;
import adapter.FriendsAdapter;
import adapter.NotificationsAdapter;
import database.FriendDB;
import database.NotificationDB;
import database.UserDB;
import model.Chat;
import model.ChatRequest;
import model.Friend;
import model.Notification;
import model.SessionState;
import model.Status;
import model.User;
import services.UpdateStatusService;
import ui.dialog.BottomMaterialDialog;
import utils.Configs;
import utils.DateUtils;
import utils.PermissionUtil;
import utils.PreferenceHelper;
import utils.Tools;
import utils.ViewsUtil;

import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.brimbay.be.application.Config.BASE_URL;
import static com.brimbay.be.application.Config.DOWNLOAD_URL;
import static com.brimbay.be.application.Config.S_URL;
import static services.ServiceUtils.isNetworkConnected;
import static utils.Configs.STR_DEFAULT_BASE64;
import static utils.Configs.TIME_TO_OFFLINE;
import static utils.ImageUtils.getCircleBitmap;
import static utils.Tools.isMyServiceRunning;
import static utils.Tools.vibrate;

public class MainActivity extends AppCompatActivity implements ChatsAdapter.OnChatsItemsInteraction, FriendsAdapter.OnFriendsInteraction{
    private MainActivity selfRef;

    private static final String TAG = "Brim.Main";

    public static final String CHANNEL_ID = "com.brimbay.chat.CHAT_REQ_ID11";
    public static final String ACTION_REJECT_CHAT_REQUEST = "com.brimbay.chat.ACTION_REJECT_CHAT_REQUEST11";
    public static final String EXTRA_REJECT_CHAT_REQUEST = "com.brimbay.chat.REJECT_CHAT_REQUEST12";
    public static final String ACTION_CHANGE_EMOJI = "com.brimbay.chat.ACTION_CHANGE_EMOJI161";
    public static final String EXTRA_CHANGE_EMOJI = "com.brimbay.chat_CHANGE_EMOJI144";

    private static final String EMOJI_KEY = "brimbay.be.EMOJI";

    public static final int EMOJI_REQUEST_CODE = 645;
    private static final int REQUEST_CONTACTS = 63;
    private static final String[] PERMISSIONS_CONTACTS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};

    //region Views
    private RecyclerView _recycler_friends,_recycler_alerts,_recycler_chats;
    private ImageView _avata, _search_clear;
    private EditText _search_edit;
    private View _alert_box;
    private SwipeRefreshLayout _swipe_refresh;
    private String mEmojiPath = null;
    //endregion

    //region Vars
    private ChatsAdapter chatsAdapter;
    private FriendsAdapter friendsAdapter;
    private BottomMaterialDialog.Builder bottomMaterialDialog;
    private NotificationsAdapter notificationsAdapter;
    private PreferenceHelper preferenceHelper;
    //endregion

    //region Firebase
    private FirebaseAuth mAuth;
    //endregion

    private ActivityMainBinding binding;


    /*Download Advert Images*/
    DownloadTask downloadTask;
    String NOMEDIA = ".nomedia";
    Context mContext = this;

    String PHONE_NUMBER = "";

    DbHelper DB;


    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        preferenceHelper =  PreferenceHelper.getInstance(selfRef);

        checkPermissions();

        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }

        }).check();


        if (savedInstanceState != null && savedInstanceState.get(EMOJI_KEY) != null){
            mEmojiPath = savedInstanceState.getString(EMOJI_KEY);
        }
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);



        SharedPreferences sharedPreferences = getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        PHONE_NUMBER = sharedPreferences.getString(Config.PHONE_SHARED_PREF,"");//We will start the Profile Activity




        /*Download Images*/

        DB = new DbHelper(mContext);


        /*Another Custom Test*/

        Cursor c =  DB.getData();

        Log.i(TAG, "onCreate: "+ c.getCount());

        syncData();
        getDownloads();

        initUI();


        startService(new Intent(selfRef, FileService.class));


        mAuth = FirebaseAuth.getInstance();
        bottomMaterialDialog = new BottomMaterialDialog.Builder(selfRef);
        friendsAdapter = new FriendsAdapter(selfRef);
        chatsAdapter = new ChatsAdapter(selfRef, this);

        if(UserDB.getInstance(selfRef).getFriend() != null) {
            User user = UserDB.getInstance(selfRef).getFriend();
            if( mEmojiPath == null)mEmojiPath = user.emoji;
            if (user.avata != null)setImageAvatar(user.avata, _avata);
        }

        handleFriendsList();
        handleAlerts();
        handleChatList();

        if (FriendDB.getInstance(selfRef).getListFriend().size() > 0){
            getChatRequests();
            startChatActivity();
            getRejectedRequest();
            updateFriendEmojiStatus();
            chatsAdapter.setChats();
        }else {
            requestGetContacts();
        }

        //Views Interaction

        if(mEmojiPath != null && !mEmojiPath.equals(STR_DEFAULT_BASE64)) {
            Picasso.get()
                    .load("file:///android_asset/emoji/" + mEmojiPath)
                    .into(binding.hfEmojiStatusImg);
            binding.hfEmojiStatusBox.setVisibility(View.VISIBLE);
        }else{
            binding.hfEmojiStatusBox.setVisibility(View.GONE);
        }

        findViewById(R.id.hm_view_rewards).setOnClickListener(view -> {
            ViewsUtil.showToast(selfRef, getString(R.string.redeem_soon), 0, R.color.colorPink);
        });
        findViewById(R.id.hm_view_accounts).setOnClickListener(v -> {
            Intent intent = new Intent(selfRef,FriendsActivity.class);
            intent.setAction(FriendsActivity.ACTION_BE_CONTACTS);
            intent.putExtra(FriendsActivity.INTENT_BE_CONTACTS_PAGE, FriendsActivity.CONTACTS_FOR_GENERAL);
            startActivity(intent);
        });
        findViewById(R.id.fr_send_chat_request).setOnClickListener(v -> {
            Intent intent = new Intent(selfRef, FriendsActivity.class);
            intent.setAction(FriendsActivity.ACTION_BE_CONTACTS);
            intent.putExtra(FriendsActivity.INTENT_BE_CONTACTS_PAGE, FriendsActivity.CONTACTS_FOR_SEND_CHAT_REQUEST);
            startActivity(intent);
        });
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        binding.hfSearchBack.setOnClickListener(v -> {
            binding.hmSearchBox.setVisibility(View.GONE);
            binding.hmTopBox.setVisibility(View.VISIBLE);
            inputMethodManager.hideSoftInputFromWindow(_search_edit.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        });
        binding.hfSearch.setOnClickListener(v -> {
            binding.hmSearchBox.setVisibility(View.VISIBLE);
            binding.hmTopBox.setVisibility(View.GONE);
            _search_edit.requestFocus();
            inputMethodManager.showSoftInput(_search_edit, InputMethodManager.SHOW_IMPLICIT);
        });
        binding.hfAvataBox.setOnClickListener(v -> {
            Intent intent = new Intent(selfRef,StatusActivity.class);
            intent.setAction(ACTION_CHANGE_EMOJI);
            intent.putExtra(EXTRA_CHANGE_EMOJI, mEmojiPath);
            startActivityForResult(intent,EMOJI_REQUEST_CODE);
        });
        binding.hfSettings.setOnClickListener(view -> startActivity(new Intent(selfRef, ProfileActivity.class)));

        _swipe_refresh.setOnRefreshListener(this::requestGetContacts);
        handleSearch();
    }

    //region Member Methods
    private void initUI(){
        _avata = binding.hfAvata;
        _recycler_friends = findViewById(R.id.hm_recent_recycler) ;
        _recycler_alerts = findViewById(R.id.mn_alerts_recycler);
        _recycler_chats = findViewById(R.id.hm_chats_recycler);
        _alert_box = findViewById(R.id.mn_notifications_box);
        _swipe_refresh = findViewById(R.id.hm_swipe_layout);
        _search_clear = findViewById(R.id.sb_search_delete);
        _search_edit = findViewById(R.id.sb_search_edit);
    }

    private void handleSearch(){
        _search_clear.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(_search_edit.getText().toString())){
                _search_edit.setText(null);
                friendsAdapter.getFilter().filter(null);
            }
        });
        _search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                _search_clear.setVisibility(count > 0?View.VISIBLE:View.GONE);
                friendsAdapter.getFilter().filter(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _search_clear.setVisibility(count > 0?View.VISIBLE:View.GONE);
                friendsAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                _search_clear.setVisibility(s.toString().trim().length() > 0?View.VISIBLE:View.GONE);
                friendsAdapter.getFilter().filter(s);
            }
        });
    }

    private void handleAlerts(){
        _recycler_alerts.setHasFixedSize(true);
        _recycler_alerts.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.HORIZONTAL, false));
        notificationsAdapter = new NotificationsAdapter(selfRef);
        _recycler_alerts.setAdapter(notificationsAdapter);

        notificationsAdapter.setOnNotificationInteraction(friend -> {
            Intent intent = new Intent(selfRef, ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_FRIEND, friend);
            intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_REQUEST_ACCEPT, "START_CHAT");
            intent.setAction(ChatActivity.ACTION_PRIVATE_CHAT_DATA);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            selfRef.startActivity(intent);
        });
//        _alert_box.setVisibility(notificationsAdapter.getItemCount() > 0 ? View.GONE:View.VISIBLE);
    }

    private void setImageAvatar(String imgBase64, ImageView imageView) {
        try {
            if (Objects.equals(imgBase64, STR_DEFAULT_BASE64)) {
                Picasso.get()
                        .load(R.drawable.user_100px)
                        .into(imageView);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFriendsList() {
        _recycler_friends.setHasFixedSize(true);
        _recycler_friends.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.HORIZONTAL, false));
        _recycler_friends.setAdapter(friendsAdapter);
        friendsAdapter.setOnFriendsInteraction(this);
    }

    private void handleChatList() {
        _recycler_chats.setHasFixedSize(true);
        _recycler_chats.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false));
        _recycler_chats.setAdapter(chatsAdapter);
    }

    private void requestContactPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(selfRef, Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(selfRef,
                Manifest.permission.WRITE_CONTACTS)) {
            // Display a SnackBar with an explanation and a button to trigger the accepted.
            Snackbar.make(_recycler_friends, getString(R.string.allow_app_permissions_snack_bar, "contacts"),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.dialog_ok),
                            view -> ActivityCompat.requestPermissions(selfRef, PERMISSIONS_CONTACTS, REQUEST_CONTACTS))
                    .show();
        } else {
            ActivityCompat.requestPermissions(selfRef, PERMISSIONS_CONTACTS, REQUEST_CONTACTS);
        }
    }

    private void requestGetContacts() {
        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(selfRef, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(selfRef, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permissions have not been granted.
//            if(_swipe_refresh.isRefreshing())_swipe_refresh.setRefreshing(false);
            requestContactPermissions();
        } else {
            getContactsList();
        }
    }

    private void getContactsList() {
        ArrayList<ContactData> contacts = (ArrayList<ContactData>) new ContactsGetterBuilder(selfRef)
                .onlyWithPhones()
                .buildList();

        for (int i = 0; i < contacts.size(); i++) {

            ArrayList<PhoneNumber> phoneNumbers = (ArrayList<PhoneNumber>) contacts.get(i).getPhoneList();
            String name = contacts.get(i).getCompositeName();

            ArrayList<String> book = new ArrayList<>();

            for (int j = 0; j < phoneNumbers.size(); j++) {
                PhoneNumber n = phoneNumbers.get(j);
                String number = n.getMainData();
                if (number.contains(" ")) {
                    number = number.replace(" ", "");
                }

                if (number.length() > 9) {
                    number = number.substring(number.length() - 8);

                    if (!book.contains(number)) book.add(number);
                }
            }

            for (String s : book) {
                findPhones(s, name);
            }
        }
    }

    private void findPhones(String email, String name) {
        FirebaseDatabase.getInstance().getReference().child(Configs.DATABASE_USERS_ROOT_NAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(_swipe_refresh.isRefreshing())_swipe_refresh.setRefreshing(false);
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                User user = d.getValue(User.class);
                                String id = d.getKey();

                                if (user != null && user.phone != null && !Objects.equals(id, mAuth.getUid())) {
                                    if (user.phone.length() < 9) return;
                                    if (user.phone.substring(user.phone.length() - 8).equals(email)) {
                                        Log.d(TAG,"Phone:"+email + "\tName:"+ name +"\tf"+user.phone.substring(user.phone.length() - 8));
                                        Friend friend = new Friend();
                                        friend.name = name;//(String) userMap.get("name");
                                        friend.phone = user.phone;//(String) userMap.get("phone");
                                        friend.avata = user.avata;//(String) userMap.get("avata");
                                        friend.id = id;
                                        friend.status = user.status;
                                        friend.room_id = (id != null ? id.compareTo(mAuth.getUid()) : 0) > 0 ?
                                                (mAuth.getUid() + id).hashCode() + "" : "" + (id + mAuth.getUid()).hashCode();

                                        FriendDB.getInstance(selfRef).addFriend(friend);
                                        updateFriendEmojiStatus();
//											_no_friends.setVisibility(View.GONE);
                                    }
                                }
                            }
                            if (friendsAdapter != null)friendsAdapter.refresh();
                        } else {
//							_no_friends.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        waitingDialog.dismiss();
                        if(_swipe_refresh.isRefreshing())_swipe_refresh.setRefreshing(false);
                    }
                });
    }

    private void shareToAll() {
        new Thread(() -> {
            String msg = "\nHey check out this awesome app\nhttps://play.google.com/store/apps/details?id=" +
                    BuildConfig.APPLICATION_ID + "\n\n";
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType( "text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, msg);
            try {
                selfRef.startActivity(Intent.createChooser(intent, selfRef.getString(R.string.invite_with)));
            } catch (Exception e) {
                ViewsUtil.showToast(selfRef,"Sorry, content could not be shared",0,R.color.colorPink);
            }
        }).start();
    }

    private void checkIsFirstTimeAccess() {
        if(FirebaseAuth.getInstance().getUid() != null) {
            FirebaseDatabase.getInstance().getReference().child(Configs.DATABASE_USERS_ROOT_NAME).child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG,"Check:"+ dataSnapshot.toString());
                            if (dataSnapshot.exists()) {
                                try {
                                    HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                    User userInfo = new User().getSmallUserFromMap(hashUser);
                                    UserDB.getInstance(selfRef).addUser(userInfo);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }else{
                                Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
                                UserDB.getInstance(selfRef).dropDB();
                                FriendDB.getInstance(selfRef).dropDB();
                                FirebaseAuth.getInstance().signOut();
                                FirebaseAuth.getInstance().getCurrentUser().delete();
                                startActivity(new Intent(selfRef, StartActivity.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "Connection Err:", databaseError.toException());
                        }

                    });
        }else{
            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).delete();
            UserDB.getInstance(selfRef).dropDB();
            FriendDB.getInstance(selfRef).dropDB();
            startActivity(new Intent(selfRef, StartActivity.class));
            finish();
        }
    }

    //[START] Chat Requests
    private void setUpChatRequests(Friend friend){
        if (mAuth == null || mAuth.getUid() == null) return;
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_CHAT_REQUEST_ROOT_NAME)
                .child(friend.id)
                .child("request");

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.accepted = true;
        chatRequest.caller_id = mAuth.getUid();
        chatRequest.guest_id = friend.id;
        chatRequest.room_id = friend.room_id;
        chatRequest.timestamp = System.currentTimeMillis();

        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    ChatRequest request = dataSnapshot.getValue(ChatRequest.class);
                    if (request == null || request.caller_id == null) {
                        requestRef.setValue(chatRequest);
                        ViewsUtil.showToast(selfRef,getString(R.string.chat_request_send_success),0,R.color.colorGreen);
                    } else {
                        if (DateUtils.getTimePassed(request.timestamp) > 60) {
                            dataSnapshot.getRef().removeValue();
                            requestRef.setValue(chatRequest);
                            ViewsUtil.showToast(selfRef,getString(R.string.chat_request_send_success),0,R.color.colorGreen);
                        } else {
                            ViewsUtil.showToast(selfRef,getString(R.string.chat_request_already_sent),0,R.color.colorAccent);
                        }
                    }
                } else {
                    requestRef.setValue(chatRequest);
                    ViewsUtil.showToast(selfRef,getString(R.string.chat_request_send_success),0,R.color.colorGreen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("bein.MainAct","Error: " + databaseError.getMessage());
            }
        });
    }

    public void updateFriendEmojiStatus(){
        if (isNetworkConnected(selfRef)){
            FirebaseDatabase.getInstance().getReference()
                    .child(Configs.DATABASE_USERS_ROOT_NAME)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            onFriendChange(snapshot);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable  String previousChildName) {
                            onFriendChange(snapshot);
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable @org.jetbrains.annotations.Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void onFriendChange(DataSnapshot snapshot){
        if (snapshot.getValue() != null) {
            try {
                String key = snapshot.getKey();
                boolean is_online = (snapshot.child("status").child("is_online").exists() &&
                        (boolean) snapshot.child("status").child("is_online").getValue());
                long timestamp = (snapshot.child("status").child("timestamp").exists()?
                        (long) snapshot.child("status").child("timestamp").getValue():System.currentTimeMillis());
                String emoji = (snapshot.child("emoji").exists()?(String) snapshot.child("emoji").getValue():STR_DEFAULT_BASE64);

                boolean online = (is_online && DateUtils.getTimePassed(timestamp) < TIME_TO_OFFLINE);
                if (is_online && DateUtils.getTimePassed(timestamp) > TIME_TO_OFFLINE) {
                    FirebaseDatabase.getInstance().getReference()
                            .child(Configs.DATABASE_USERS_ROOT_NAME)
                            .child(key + "/status/is_online").setValue(false);
                    FriendDB.getInstance(selfRef).updateLastSeen(key, timestamp, 0);
                }
                FriendDB.getInstance(selfRef).updateLastSeen(key, timestamp, online ? 1 : 0);
                FriendDB.getInstance(selfRef).updateEmoji(key, emoji);
                if (friendsAdapter != null)friendsAdapter.refresh();
                if (chatsAdapter != null)chatsAdapter.setChats();
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.d(TAG,"Error:"+e.getLocalizedMessage());
            }
        }
    }

    public void getChatRequests() {
        if (mAuth == null || mAuth.getUid() == null) return;

        FirebaseDatabase.getInstance().getReference()
                .child(Configs.DATABASE_CHAT_REQUEST_ROOT_NAME)
                .child(mAuth.getUid()).addChildEventListener(new ChildEventListener() {
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

                            if (chatRequest.guest_id == null) return;
                            if (chatRequest.accepted && DateUtils.getTimePassed(chatRequest.timestamp) < 60) {
                                createNotification(chatRequest);
                            }

                            dataSnapshot.getRef().removeValue();
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

    private void startChatActivity() {
        for (Friend friend : FriendDB.getInstance(selfRef).getListFriend()) {
            FirebaseDatabase.getInstance().getReference()
                    .child("Chat_Session")
                    .child(friend.room_id).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getValue() != null) {
                        if ((long) dataSnapshot.getValue() == SessionState.STARTED) {
                            dataSnapshot.getRef().setValue(SessionState.CONNECTED);
                            Intent intent = new Intent(selfRef, ChatActivity.class);
                            intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_FRIEND, friend);
                            intent.setAction(ChatActivity.ACTION_PRIVATE_CHAT_DATA);
                            selfRef.startActivity(intent);
                            finishAffinity();
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
        }
    }

    private void getRejectedRequest() {
        for (Friend friend : FriendDB.getInstance(selfRef).getListFriend()) {
            FirebaseDatabase.getInstance().getReference()
                    .child("rejected_request")
                    .child(friend.id)
                    .child(friend.room_id).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.getValue() != null) {
                        new BottomMaterialDialog.Builder(selfRef)
                                .setTitle(getString(R.string.chat_request_status))
                                .setMessage("Sorry, " + friend.name + " rejected your request!")
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.dialog_ok), (dialogInterface, which) -> {
                                    dataSnapshot.getRef().removeValue();
                                    dialogInterface.dismiss();
                                })
                                .build().show();

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
    }

    private void createNotification(ChatRequest chatRequest) {

        boolean unmuted = preferenceHelper.getNotificationsRingtone();
        String ringtone = "content://settings/system/notification_sound";

        Friend friend = null;

        ArrayList<Friend> friends = FriendDB.getInstance(selfRef).getListFriend();

        for (Friend f : friends) {
            if (f.id.trim().equals(chatRequest.caller_id.trim())) {
                friend = new Friend();
                Status status = new Status();
                status.is_online = false;
                status.timestamp = System.currentTimeMillis();
                friend.id = chatRequest.caller_id;
                friend.name = f.name;
                friend.avata = f.avata;
                friend.status = f.status;
                friend.room_id = f.room_id;
                friend.phone = f.phone;
            }
        }

        if (friend == null) {
            friend = new Friend();
            Status status = new Status();
            status.is_online = false;
            status.timestamp = System.currentTimeMillis();
            friend.id = chatRequest.caller_id;
            friend.name = "Unknown";
            friend.avata = "default";
            friend.status = status;
            friend.room_id = chatRequest.room_id;
            friend.phone = "unknown";
        }

        Notification ntf = new Notification();
        ntf.timestamp = chatRequest.timestamp;
        ntf.type = Notification.NTF_RECEIVED_CHAT_REQUEST;
        ntf.name = friend.name;
        ntf.avata = friend.avata;
        ntf.friend = friend;
        ntf.body = getString(R.string.new_chat_request);
        _alert_box.setVisibility(View.VISIBLE);

		NotificationDB.getInstance(selfRef).addNTF(ntf);
		notificationsAdapter.refresh();

//        _alert_box.setVisibility(notificationsAdapter.getItemCount() > 0 ? View.GONE:View.VISIBLE);
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(selfRef, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_FRIEND, friend);
        intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_REQUEST_ACCEPT, "START_CHAT");
        intent.setAction(ChatActivity.ACTION_PRIVATE_CHAT_DATA);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent rejectIntent = new Intent(selfRef, MainActivity.class);
        rejectIntent.setAction(ACTION_REJECT_CHAT_REQUEST);
        rejectIntent.putExtra(EXTRA_REJECT_CHAT_REQUEST, friend.room_id);
        PendingIntent rejectPendingIntent = PendingIntent.getActivity(selfRef, 0, rejectIntent, PendingIntent.FLAG_ONE_SHOT);

        PendingIntent pendingIntent = PendingIntent.getActivity(selfRef, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap icon;
        if (friend.avata.equals("default")) {
            icon = BitmapFactory.decodeResource(selfRef.getResources(), R.drawable.user_100px);
        } else {
            byte[] decodedString = Base64.decode(friend.avata, Base64.DEFAULT);
            icon = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        // the check ensures that the channel will only be made
        // if the device is running Android 8+
        try {

            NotificationCompat.Builder notification =
                    new NotificationCompat.Builder(selfRef, CHANNEL_ID);
            // the second parameter is the channel id.
            // it should be the same as passed to the makeNotificationChannel() method

            notification
                    .setContentTitle(friend.name)
                    .setContentText(selfRef.getString(R.string.new_chat_request))
                    .setAutoCancel(true)
                    .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(R.drawable.tbc_notification)
                    .setLargeIcon(getCircleBitmap(icon))
                    .setColor(selfRef.getResources().getColor(R.color.colorPrimary,null))
                    .setVisibility(VISIBILITY_PUBLIC)
                    .setTimeoutAfter(60000)
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.delete_20px, selfRef.getString(R.string.reject),
                            rejectPendingIntent)
                    .setNumber(1); // this shows a number in the notification dots

            if (unmuted) {
                notification.setSound(Uri.parse(ringtone));
            }

            if (preferenceHelper.getNotificationsVibrate()) {
                notification.setVibrate(vibrate(2));
            }

            NotificationManager notificationManager =
                    (NotificationManager) selfRef.getSystemService(NOTIFICATION_SERVICE);

            assert notificationManager != null;
            final int nId = Integer.parseInt(friend.room_id.substring(4));
            notificationManager.notify(nId, notification.build());
            // it is better to not use 0 as notification id, so used 1.

            new Handler(getMainLooper()).postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
                    for (StatusBarNotification s : barNotifications) {
                        if (s.getId() == nId) {
                            notificationManager.notify(nId, notification.build());
                        }
                    }
                } else {
                    notificationManager.notify(nId, notification.build());
                }
            }, 30000);

            new Handler(getMainLooper()).postDelayed(() -> notificationManager.cancel(nId), 60000);
        } catch (java.lang.IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                CharSequence name = "CHAT_REQ";
                String description = selfRef.getString(R.string.chat_request_title,
                        selfRef.getString(R.string.app_name));//"TBC chat requests";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                channel.enableVibration(true);
                channel.setShowBadge(true);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = selfRef.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            } catch (java.lang.IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
    //[END] Chat requests

    //endregion

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEmojiPath != null)outState.putString(EMOJI_KEY, mEmojiPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EMOJI_REQUEST_CODE){
            if (data != null && data.getAction().equals(ACTION_CHANGE_EMOJI) &&
                    data.getStringExtra(EXTRA_CHANGE_EMOJI) != null){
                mEmojiPath = data.getStringExtra(EXTRA_CHANGE_EMOJI);
                if(mEmojiPath != null) {
                    if(!mEmojiPath.equals(STR_DEFAULT_BASE64)) {
                        Picasso.get()
                                .load("file:///android_asset/emoji/" + mEmojiPath)
                                .into(binding.hfEmojiStatusImg);
                        binding.hfEmojiStatusBox.setVisibility(View.VISIBLE);
                    }else{
                        binding.hfEmojiStatusBox.setVisibility(View.GONE);
                    }
                    UserDB.getInstance(selfRef).updateEmoji(mEmojiPath);
                    FirebaseDatabase.getInstance().getReference()
                            .child(Configs.DATABASE_USERS_ROOT_NAME)
                            .child(mAuth.getUid() + "/emoji").setValue(mEmojiPath);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        User user = UserDB.getInstance(selfRef).getFriend();

        if(user == null || user.avata == null || user.phone.equals("")) {
            checkIsFirstTimeAccess();
        }

        try {
            if (!isMyServiceRunning(selfRef, UpdateStatusService.class)){
                startService(new Intent(selfRef, UpdateStatusService.class));
            }
        }catch (java.lang.IllegalStateException e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isMyServiceRunning(selfRef,UpdateStatusService.class) ) {
            stopService(new Intent(selfRef, UpdateStatusService.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                if (Tools.isDeviceOnline(selfRef) &&  mAuth != null && mAuth.getUid() != null )getContactsList();
            } else {
                // Display a SnackBar with an explanation and a button to trigger the accepted.
                Snackbar.make(_recycler_friends, selfRef.getString(R.string.allow_app_permissions_snack_bar,
                        "contacts"),
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(selfRef.getString(R.string.dialog_ok),
                                view -> ActivityCompat.requestPermissions(selfRef, PERMISSIONS_CONTACTS, REQUEST_CONTACTS))
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onChatItemClickListener(Chat chat, int position) {
        BottomDialogFragment bottomSheetDialog = BottomDialogFragment.newInstance(chat.friend);
        bottomSheetDialog.setOnBottomDialogInteraction(friend1 -> {
//            setUpChatRequests(chat.friend);
            Intent intent = new Intent(selfRef, ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_EXTRA_CHAT_FRIEND, friend1);
            intent.setAction(ChatActivity.ACTION_PRIVATE_CHAT_DATA);
            startActivity(intent);
        });
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
    }

    @Override
    public void onAddFriendClickListener() {
        shareToAll();
    }

    @Override
    public void onFriendClickListener(Friend friend, int i) {
        BottomDialogFragment bottomSheetDialog = BottomDialogFragment.newInstance(friend);
        bottomSheetDialog.setOnBottomDialogInteraction(this::setUpChatRequests);
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
    }


    private void checkIfPermissionSet() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE

                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*final Intent i = new Intent();
                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + selfRef.getPackageName()));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            selfRef.startActivity(i);*/
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private void syncData() {
        Cursor c =  DB.getData();
        Log.i(TAG, "onCreate: "+ c.getCount());

        if(c != null){

            c.moveToFirst();
            while (!c.isAfterLast()) {
                int advert_id = c.getInt(0);
                int advert_clicks = c.getInt(6);
                int advert_views = c.getInt(7);


                Log.i(TAG, "syncData: "+advert_id);

                if((advert_clicks != 0) || (advert_views != 0)){

                    Toast.makeText(selfRef, " VIEWS = "+ advert_views, Toast.LENGTH_SHORT).show();

                    syncValues(advert_id,advert_clicks,advert_views);
                }

                c.moveToNext();
            }


        }




    }

    private void syncValues(int advert_id, int advert_clicks, int advert_views) {




        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                S_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i(TAG, "onResponse: " + response + " Response");

                        if(response.startsWith("u_")){

                            String string = response;
                            String[] parts = string.split("_");
                            String id = parts[1];

                            DB = new DbHelper(mContext);

                            DB.updateViewandClicks(id);

                            Toast.makeText(selfRef, id, Toast.LENGTH_SHORT).show();


                        }else if(response.startsWith("d_")){

                            /* Delete Advert and File */

                            String string = response;
                            String[] parts = string.split("_");
                            String id = parts[1];


                            DB = new DbHelper(mContext);




                            Cursor cursor =  DB.getSingleAdvert(id);

                            Log.i(TAG, "onResponse: "+cursor.getCount() + " hello Delete");

                            Toast.makeText(selfRef, id, Toast.LENGTH_SHORT).show();

                            if(cursor.getCount()>0){


                                if(cursor != null)
                                {

                                    cursor.moveToFirst();

                                    if(!cursor.isAfterLast()) {

                                        String ImagePath = cursor.getString(3);

                                        Boolean deleted = deleteImage(ImagePath);

                                        if (deleted) {

                                            DB.removeAdvert(id);

                                        } else {
                                            Log.i(TAG, "onResponse: File not deleted");
                                        }

                                    }



                                }else{

                                    Log.i(TAG, "onResponse: Data not found");

                                }


                            }


                        }else{



                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                        //Toast.makeText(OrdersActivity.this,
                        //error.toString(),
                        // Toast.LENGTH_LONG).show();
                        //waitingDialog.dismiss();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request

                Log.i(TAG, "getParams: "+advert_id);
                params.put("a",String.valueOf(advert_id));
                params.put("v",String.valueOf(advert_views));
                params.put("c",String.valueOf(advert_clicks));
                params.put("p",PHONE_NUMBER);


                //returning parameter
                return params;
            }

        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private boolean deleteImage(String imagePath) {



        File fdelete =  new File(Environment.getExternalStorageDirectory() + "/wyzone/"+ imagePath);

        if (fdelete.exists()) {

            if (fdelete.delete()) {

                return  true;

            } else {

                return false;
            }
        }

        return  false;
    }

    public  void getDownloads(){

        createDir();

        StringRequest billionaireReq = new StringRequest(DOWNLOAD_URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.i("Application Data", response);

                        try {


                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray data = jsonObject.getJSONArray("result");

                            Log.i(TAG, "onResponse: "+data.length());


                            for (int j = 0; j < data.length(); j++) {

                                //do a for loop to download multiple images

                                JSONObject obj = data.getJSONObject(j);

                                String advert_image_path =  BASE_URL + obj.getString("advert_image_path");

                                Log.e(TAG, "onResponse: " + advert_image_path);


                                String string = obj.getString("advert_image_path");
                                String[] parts = string.split("/");


                                boolean stored = DB.addAdvert(obj.getString("advert_id"),
                                        obj.getString("advert_name"),
                                        obj.getString(""),
                                        obj.getString(""),
                                        parts[1],
                                        obj.getString("company_name"));


                                // Store new ads on Sqlite
                                if(stored){

                                    downloadTask = new DownloadTask(MainActivity.this);
                                    downloadTask.execute(advert_image_path);


                                }



                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse (VolleyError error){
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        })

                /*what might make it not work*/
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request
                params.put("p", PHONE_NUMBER);


                //returning parameter
                return params;
            }

        }/*what might make it not work*/;

        AppController.getInstance().addToRequestQueue(billionaireReq, "");
    }


    private void createDir() {


        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            //finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "wyzone");
        createNomedia();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                Log.d("App", "failed to create directory");
            }else{

                createNomedia();
            }
        }else{
            createNomedia();
        }

    }

    private void createNomedia() {
        Log.d("App", "failed to create directory");
        File nomediaFile = new File(Environment.getExternalStorageDirectory() + "/wyzone/"+ NOMEDIA);
        if(!nomediaFile.exists()){
            try {
                nomediaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Checks the dynamically-controlled permissions and requests missing permissions from end user.
     */
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS, grantResults);
        }
    }


}