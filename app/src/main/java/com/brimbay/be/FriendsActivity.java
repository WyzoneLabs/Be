package com.brimbay.be;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.brimbay.be.databinding.ActivityFriendsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import adapter.FriendsAdapter;
import model.ChatRequest;
import model.Friend;
import ui.viewholder.HomeViewModel;
import utils.Configs;
import utils.DateUtils;
import utils.PermissionUtil;
import utils.ViewsUtil;

public class FriendsActivity extends AppCompatActivity implements FriendsAdapter.OnContactsInteraction {
    private FriendsActivity selfRef;

    public static final String ACTION_BE_CONTACTS = "action.brimbay.android.be.friends";
    public static final String INTENT_BE_CONTACTS_PAGE = "intent.page.brimbay.android.be.friends";
    public static final String BE_CONTACTS_KEY = "contact.key.brimbay.android.be.friends";

    public static final int CONTACTS_FOR_SEND_CHAT_REQUEST = 112;
    public static final int CONTACTS_FOR_GENERAL = 113;
    public static final int CONTACTS_FOR_RESULT = 114;

    private static final int REQUEST_CONTACTS = 32;
    /**
     * Permissions required to read and write contacts..
     */
    private static final String[] PERMISSIONS_CONTACTS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};

    //region Views
    private EditText _search_edit;
    private TextView _title, _sub_title;
    private SwipeRefreshLayout _swipe_refresh;
    private ImageView _search_clear,_add_contact;
    private RecyclerView _recycler_friend;
    //endregion

    //region Member vars
    private HomeViewModel mViewModel;
    private FriendsAdapter friendsAdapter;
    //endregion

    private FirebaseAuth mAuth;
    private ActivityFriendsBinding binding;
    private int current_page = CONTACTS_FOR_GENERAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        if (savedInstanceState != null){
            current_page = savedInstanceState.getInt(BE_CONTACTS_KEY,CONTACTS_FOR_GENERAL);
        }else{
            handleIntents();
        }

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();
        handleFriends();
        updateTitle();

        _search_clear.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(_search_edit.getText().toString())){
                _search_edit.setText(null);
                friendsAdapter.getFilter().filter(null);
            }
        });
        _search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0) {
                    _search_clear.setVisibility(View.VISIBLE);
                    friendsAdapter.getFilter().filter(s);
                } else {
                    _search_clear.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    _search_clear.setVisibility(View.VISIBLE);
                    friendsAdapter.getFilter().filter(s);
                } else {
                    _search_clear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    _search_clear.setVisibility(View.VISIBLE);
                    friendsAdapter.getFilter().filter(s);
                } else {
                    _search_clear.setVisibility(View.GONE);
                }
            }
        });

        _swipe_refresh.setOnRefreshListener(this::requestGetContacts);
    }

    //region Member Methods
    private void initUI() {
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        _recycler_friend = findViewById(R.id.fr_recycler);
        _search_clear = findViewById(R.id.sb_search_delete);
        _search_edit = findViewById(R.id.sb_search_edit);
        _add_contact = findViewById(R.id.fr_add_contact);
        _title = findViewById(R.id.fr_title);
        _sub_title = findViewById(R.id.fr_sub_title);
        _swipe_refresh = findViewById(R.id.fr_swipe_layout);
    }

    private void handleIntents(){
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equals(ACTION_BE_CONTACTS)){
            current_page = intent.getIntExtra(INTENT_BE_CONTACTS_PAGE,CONTACTS_FOR_GENERAL);
        }
    }

    private void updateTitle(){
        if (current_page == CONTACTS_FOR_SEND_CHAT_REQUEST){
            _sub_title.setText(getString(R.string.send_chat_request));
            _sub_title.setVisibility(View.VISIBLE);

        }else {
            _sub_title.setVisibility(View.GONE);
        }
        if (friendsAdapter != null)friendsAdapter.disableDetailsMenu(true);
    }

    private void handleFriends(){
        _recycler_friend.setHasFixedSize(true);
        friendsAdapter = new FriendsAdapter(selfRef);
        _recycler_friend.setLayoutManager(new LinearLayoutManager(selfRef, RecyclerView.VERTICAL, false));
        _recycler_friend.setAdapter(friendsAdapter);
        friendsAdapter.setOnContactsInteraction(this);
        friendsAdapter.setType(FriendsAdapter.DETAILED_LIST_FRIENDS);

        requestGetContacts();

        mViewModel.getQuickContacts().observe(this, contacts -> {
            friendsAdapter.setFriends(contacts);
            if(_swipe_refresh.isRefreshing())_swipe_refresh.setRefreshing(false);
        });
    }

    private void requestGetContacts() {
        // Verify that all required storage permissions have been granted.
        if (ActivityCompat.checkSelfPermission(selfRef, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(selfRef, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permissions have not been granted.
            requestContactPermissions();
        } else {
//            if (mViewModel != null) mViewModel.initQuickContacts(selfRef, true);
            if (mViewModel != null) mViewModel.initFriends(selfRef);
        }
    }

    /**
     * Requests the Contacts permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestContactPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(selfRef,Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(selfRef,Manifest.permission.WRITE_CONTACTS)) {
            Snackbar.make(_search_edit, getString(R.string.allow_app_permissions_snack_bar, "contacts"),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.dialog_ok), view -> ActivityCompat.requestPermissions(selfRef, PERMISSIONS_CONTACTS, REQUEST_CONTACTS))
                    .show();
        } else {
            ActivityCompat.requestPermissions(selfRef, PERMISSIONS_CONTACTS, REQUEST_CONTACTS);
        }
    }

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
        finish();
    }

    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        if (requestCode == REQUEST_CONTACTS) {
            // We have requested multiple permissions for contacts, so all of them need to be checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                Snackbar.make(_search_edit, getString(R.string.permission_granted, "Contacts"),
                        Snackbar.LENGTH_SHORT)
                        .show();
//                if (mViewModel != null) mViewModel.initQuickContacts(selfRef,true);
                if (mViewModel != null) mViewModel.initFriends(selfRef);
            } else {
                Snackbar.make(_search_edit, getString(R.string.permissions_denied, "Contacts"),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        outState.putInt(BE_CONTACTS_KEY, current_page);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onContactClickListener(Friend friend, int i) {
        BottomDialogFragment bottomSheetDialog = BottomDialogFragment.newInstance(friend);
        bottomSheetDialog.setOnBottomDialogInteraction(this::setUpChatRequests);
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

    }

}