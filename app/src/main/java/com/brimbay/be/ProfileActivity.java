package com.brimbay.be;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brimbay.be.databinding.ActivityMainBinding;
import com.brimbay.be.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import database.FriendDB;
import database.UserDB;
import model.User;
import utils.Configs;
import utils.ImageUtils;
import utils.PreferenceHelper;
import utils.Tools;
import utils.ViewsUtil;

import static utils.Configs.STR_DEFAULT_BASE64;

public class ProfileActivity extends AppCompatActivity  implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ProfileActivity selfRef;

    private static final int CONTEXT_MENU_DELETE = 1;
    private static final int CONTEXT_MENU_EDIT = 2;
    private static final int CONTEXT_MENU_VIEW = 3;
    private static final int PICK_AVATA = 367;

    private static final String TAG = "ProfileAct";

    //region Views
    private Toolbar _toolbar;
    private ActionBar _actionbar;
    private TextView _name, _location, _contact, _version, _signout;
    private ImageView _avata;
    private ConstraintLayout _ringtone_box, _vibrate_box, _inchat_sounds_box;
    private SwitchCompat _switch_ringtone, _switch_vibrate, _switch_inchat;
    private ConstraintLayout _enter_box, _background_box;
    private SwitchCompat _switch_enter, _switch_background;
    //endregion

    private User mUser;
    private PreferenceHelper preferenceHelper;
    private DatabaseReference mUserDB;
    private FirebaseAuth mAuth;

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initUI();

        mAuth = FirebaseAuth.getInstance();
        mUser = UserDB.getInstance(selfRef).getFriend();
        preferenceHelper = PreferenceHelper.getInstance(selfRef);
        mUserDB = FirebaseDatabase.getInstance().getReference().child(Configs.DATABASE_USERS_ROOT_NAME).child(mAuth.getUid());

        if(mUser != null) {
            if(mUser.avata != null)setImageAvatar(mUser.avata, _avata);
            if(mUser.bio != null)_name.setText(mUser.bio);
//			if(mUser.bio != null)_location.setText(mUser.bio);
            if(mUser.phone != null)_contact.setText(mUser.phone);
        }

        _switch_ringtone.setChecked(preferenceHelper.getNotificationsRingtone());
        _switch_vibrate.setChecked(preferenceHelper.getNotificationsVibrate());
        _switch_inchat.setChecked(preferenceHelper.getNotificationsInChatSound());
        _switch_background.setChecked(preferenceHelper.getChatSettingVideoBackground());
        _switch_enter.setChecked(preferenceHelper.getChatSettingEnterSend());

        //Views Interaction
        _inchat_sounds_box.setOnClickListener(this);
        _ringtone_box.setOnClickListener(this);
        _vibrate_box.setOnClickListener(this);
        _switch_ringtone.setOnCheckedChangeListener(this);
        _switch_vibrate.setOnCheckedChangeListener(this);
        _switch_inchat.setOnCheckedChangeListener(this);
        _background_box.setOnClickListener(this);
        _enter_box.setOnClickListener(this);
        _switch_background.setOnCheckedChangeListener(this);
        _switch_enter.setOnCheckedChangeListener(this);

        _avata.setOnClickListener(v -> v.getParent().showContextMenuForChild(v));
        _avata.getRootView().setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.setHeaderTitle(getString(R.string.manage_avata));
            menu.add(Menu.NONE, CONTEXT_MENU_VIEW, Menu.NONE, R.string.view_profile_photo);
            menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, R.string.change_profile_photo);
            menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, R.string.delete_profile_photo);
        });
    }

    //region Member Methods
    private void initUI() {
        _toolbar = binding.toolbar;
        setSupportActionBar(_toolbar);

        _actionbar = getSupportActionBar();
        if (_actionbar != null) {
            _actionbar.setDisplayHomeAsUpEnabled(true);
        }
//        _actionbar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.left_26px, null));
        _avata = findViewById(R.id.pr_user_avata);
        _contact = findViewById(R.id.pr_user_contact);
        _name = findViewById(R.id.pr_user_name);
        _location = findViewById(R.id.pr_user_location);

        _inchat_sounds_box = findViewById(R.id.ntf_in_app_sound_box);
        _ringtone_box = findViewById(R.id.ntf_sound_box);
        _vibrate_box = findViewById(R.id.ntf_vibrate_box);
        _switch_inchat = findViewById(R.id.ntf_in_chat_snd_switch);
        _switch_ringtone = findViewById(R.id.ntf_snd_switch);
        _switch_vibrate = findViewById(R.id.ntf_vibrate__switch);

        _background_box = findViewById(R.id.cset_background_box);
        _enter_box = findViewById(R.id.cset_enter_key_box);
        _switch_background = findViewById(R.id.cset_background_switch);
        _switch_enter = findViewById(R.id.cset_enter_key_switch);

        _signout = findViewById(R.id.ntf_logout);
        _version = findViewById(R.id.pr_version);

        _version.setText(getString(R.string.version,BuildConfig.VERSION_NAME));

        _signout.setOnClickListener(v -> {
            new AlertDialog.Builder(selfRef)
                    .setTitle(getString(R.string.sign_out))
                    .setMessage(getString(R.string.confirm_logout))
                    .setCancelable(true)
                    .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel())
                    .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                        logout();
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        });
    }

    private void logout(){
        UserDB.getInstance(selfRef).dropDB();
        FriendDB.getInstance(selfRef).dropDB();
        mAuth.signOut();
        // Navigate back to MainActivity
        Intent intent =  new Intent(selfRef, StartActivity.class);
        startActivity(intent);
        finishAffinity();
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

    private void getUserData(){
        if(FirebaseAuth.getInstance().getUid() != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child(Configs.DATABASE_USERS_PLUS_ROOT_NAME)
                    .child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG,"Check:"+ dataSnapshot.toString());
                            if (dataSnapshot.exists()) {
                                Log.d(TAG,"Check Exists:"+ dataSnapshot.getValue());
                                HashMap map = (HashMap) dataSnapshot.getValue();
                                if(map != null && map.get("location") != null){
                                    String location = (String) map.get("location");
                                    if(location != null )_location.setText(location);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ntf_in_app_sound_box) {
            _switch_inchat.toggle();
        } else if (id == R.id.ntf_vibrate_box) {
            _switch_vibrate.toggle();
        } else if (id == R.id.ntf_sound_box) {
            _switch_ringtone.toggle();
        } else if (id == R.id.cset_enter_key_box) {
            _switch_enter.toggle();
        } else if (id == R.id.cset_background_box) {
            _switch_background.toggle();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int id = compoundButton.getId();
        if (id == R.id.ntf_in_chat_snd_switch) {
            preferenceHelper.saveNotificationsInChatSound(b);
        } else if (id == R.id.ntf_snd_switch) {
            preferenceHelper.saveNotificationsRingtone(b);
        } else if (id == R.id.ntf_vibrate__switch) {
            preferenceHelper.saveNotificationsVibrate(b);
        } else if (id == R.id.cset_background_switch) {
            preferenceHelper.saveChatSettingVideoBackground(b);
        } else if (id == R.id.cset_enter_key_switch) {
            preferenceHelper.saveChatSettingEnterSend(b);
        }
    }

    //endregion

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
//            onBackPressed();
            startActivity(new Intent(selfRef, MainActivity.class));
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                if (Tools.isDeviceOnline(selfRef) && !mUser.avata.equals("default")) {
                    mUser.avata = "default";
                    UserDB.getInstance(selfRef).updateAvata("default");

                    LovelyInfoDialog waitingDialog = new LovelyInfoDialog(selfRef);

                    waitingDialog.setCancelable(false)
                            .setTitle("Avatar updating....")
                            .setTopColorRes(R.color.colorPrimary)
                            .show();

                    setImageAvatar("default", _avata);

                    mUserDB.child("avata").setValue("default")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    waitingDialog.dismiss();
//									preferenceHelper.saveUserInfo(mUser);

                                    new LovelyInfoDialog(selfRef)
                                            .setIcon(R.drawable.user_100px)
                                            .setTopColorRes(R.color.colorPrimary)
                                            .setTitle("Success")
                                            .setMessage("Update avatar successfully!")
                                            .show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                waitingDialog.dismiss();
                                Log.d("Update Avatar", "failed");
                                new LovelyInfoDialog(selfRef)
                                        .setIcon(R.drawable.user_100px)
                                        .setTopColorRes(R.color.colorAccent)
                                        .setTitle("Failed")
                                        .setMessage("Could not update avatar")
                                        .show();
                            });
                } else {
                    ViewsUtil.showToast(selfRef, "Sorry you cannot delete your avata at the moment.Try again later!", 0, R.color.colorPink);
                }
                break;
            case CONTEXT_MENU_EDIT:
                if (Tools.isDeviceOnline(selfRef)) {
                    new AlertDialog.Builder(selfRef)
                            .setTitle("Avatar")
                            .setMessage("Are you sure want to change avatar profile?")
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_PICK);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_AVATA);
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show();
                } else {
                    ViewsUtil.showToast(selfRef, "Please enable network connection to change your avata!", 0, R.color.colorPink);
                }
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATA && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                ViewsUtil.showToast(selfRef, "No image data selected", 0, R.color.colorOrange);
                return;
            }
            try {
                InputStream inputStream = selfRef.getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                if (imageBase64 != null) {
                    mUser.avata = imageBase64;
                    UserDB.getInstance(selfRef).updateAvata(imageBase64);


                    LovelyInfoDialog waitingDialog = new LovelyInfoDialog(selfRef);

                    waitingDialog.setCancelable(false)
                            .setTitle("Avatar updating....")
                            .setTopColorRes(R.color.colorPrimary)
                            .show();

                    setImageAvatar(imageBase64, _avata);
                    Log.d("VATAEUYY", imageBase64);

                    mUserDB.child("avata").setValue(imageBase64)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    waitingDialog.dismiss();
//									preferenceHelper.saveUserInfo(mUser);

                                    new LovelyInfoDialog(selfRef)
                                            .setIcon(R.drawable.user_100px)
                                            .setTopColorRes(R.color.colorAccent)
                                            .setTitle("Success")
                                            .setMessage("Update avatar successfully!")
                                            .show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                waitingDialog.dismiss();
                                Log.d("Update Avatar", "failed");
                                new LovelyInfoDialog(selfRef)
                                        .setIcon(R.drawable.user_100px)
                                        .setTopColorRes(R.color.colorAccent)
                                        .setTitle("Failed")
                                        .setMessage("Could not update avatar")
                                        .show();
                            });
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }

        }
    }
}