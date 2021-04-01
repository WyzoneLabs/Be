package com.brimbay.be;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import database.UserDB;
import de.hdodenhof.circleimageview.CircleImageView;
import model.User;
import utils.Configs;
import utils.ImageUtils;
import utils.PreferenceHelper;
import utils.Tools;
import utils.ViewsUtil;

public class UploadUserDataActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ACTION_INTENT_DATA_USERNAME = "com.software.forbidden.ACTION_INTENT_USERNAME";
    public static final String INTENT_DATA_USER = "com.software.forbidden.INTENT_USERNAME";
    private final int PICK_AVATA = 982;
    private UploadUserDataActivity selfRef;
    /*Views*/
    private Button _complete_btn;
    private TextView _change_avata_link, _avata_title;
    private CircleImageView _chosen_avata;
    private LovelyProgressDialog waitingDialog;
    private User mUser;
    private String mUsername = "TBC_User";
    private String mUserAvata = "default";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        setContentView(R.layout.activity_upload_user_data);

        initViews();
        waitingDialog = new LovelyProgressDialog(selfRef);

        mAuth = FirebaseAuth.getInstance();
        mUser = new User();

        Intent intent = getIntent();

        if (intent != null && Objects.equals(intent.getAction(), ACTION_INTENT_DATA_USERNAME)) {
            mUser = intent.getParcelableExtra(INTENT_DATA_USER);
            if (mUser.avata == null) return;
            mUsername = mUser.name;
            mUserAvata = mUser.avata;

            if (!mUserAvata.equals("default")) {
                byte[] decodedString = Base64.decode(mUserAvata, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                _chosen_avata.setImageBitmap(bitmap);
                _avata_title.setText(getString(R.string.avata_added));
            } else {
                _avata_title.setText(getString(R.string.default_avata));
            }

        }

        //Views interaction
        _change_avata_link.setOnClickListener(this);
        _complete_btn.setOnClickListener(this);

    }

    private void initViews() {
        _complete_btn = findViewById(R.id.u_complete_btn);
        _change_avata_link = findViewById(R.id.u_change_avata_link);
        _chosen_avata = findViewById(R.id.u_user_avata);
        _avata_title = findViewById(R.id.u_title);
    }

    private void initNewUserInfo() {
        waitingDialog.setTitle("Registering....")
                .setIcon(R.drawable.user_100px)
                .setTopColorRes(R.color.colorAccent)
                .show();
        FirebaseDatabase.getInstance().getReference().child(Configs.DATABASE_USERS_ROOT_NAME)
                .child(Objects.requireNonNull(mAuth.getUid()))
                .setValue(User.getSmallMapFromUser(mUser)).addOnSuccessListener(aVoid -> {
            waitingDialog.dismiss();
            PreferenceHelper.getInstance(selfRef).saveFirstTime(false);
            startActivity(new Intent(selfRef, MainActivity.class));
            finish();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INTENT_DATA_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mUser = savedInstanceState.getParcelable(INTENT_DATA_USER);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATA && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                ViewsUtil.showToast(selfRef, "No image data selected", 0, R.color.colorPink);
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);
                if (imageBase64 != null) {
                    mUserAvata = imageBase64;
                    mUser.avata = imageBase64;

                    UserDB.getInstance(selfRef).updateAvata(imageBase64);

                    byte[] decodedString = Base64.decode(mUserAvata, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    _chosen_avata.setImageBitmap(bitmap);
                    _avata_title.setText(getString(R.string.avata_added));
                }

                Log.d("VATAEUYY", imageBase64);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.u_change_avata_link) {
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
        } else if (id == R.id.u_complete_btn) {
            if (mUser.phone != null && mUser.avata != null) initNewUserInfo();
        }
    }
}
