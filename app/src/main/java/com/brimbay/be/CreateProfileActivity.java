package com.brimbay.be;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import database.UserDB;
import de.hdodenhof.circleimageview.CircleImageView;
import model.AgeSet;
import model.Gender;
import model.Locations;
import model.User;
import model.UserPlus;
import network.APIClient;
import network.APIInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ui.CustomListAlertDialog;
import utils.Configs;
import utils.ImageUtils;
import utils.PermissionUtil;
import utils.Tools;
import utils.ViewsUtil;

public class CreateProfileActivity extends AppCompatActivity implements View.OnClickListener {
	/**
	 * Id to identify a permission accepted.
	 */
	private static final int REQUEST_STORAGE = 2;
	private static final int PICK_AVATA = 287;
	private static final int PICK_LOCATION = 1342;
	private static final String TAG = "PROFILE_CREATE";
	/**
	 * Permissions required to read and write contacts..
	 */
	private static final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
	private CreateProfileActivity selfRef;
	/*Views*/
	private TextView _link_skip;
	private Button _first_next_btn, _second_next_btn;
	private CircleImageView _title_image;
	private TextView _gender_spinner, _age_spinner, _location_picker;
	private View _first_page_box, _second_page_box;
	/*Member Vars*/
	private FirebaseAuth mAuth;
	private FirebaseUser mUser;
	private boolean firstTimeAccess;
	private String mUserAvata = "default";

	private APIInterface mApiInterface;
	private List<Gender> mGendersList = new ArrayList<>();
	private List<AgeSet> mAgeList = new ArrayList<>();
	private List<Locations> mLocationsList = new ArrayList<>();
	private CustomListAlertDialog mAlertDialog;

	private static final int LOCATION_REQUEST = 200;
	String connectivity = "Mobile";
    TelephonyManager tm;
    String cid, lac, device_name,
            IMEINumber,subscriberID,
            SIMSerialNumber,networkCountryISO,
            SIMCountryISO,softwareVersion,
            voiceMailNumber,simOperator,age,location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        setContentView(R.layout.activity_create_profile);

        initViews();

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mApiInterface = APIClient.getClient(selfRef).create(APIInterface.class);
	    mAlertDialog = new CustomListAlertDialog(selfRef);

        _first_page_box.setVisibility(View.VISIBLE);
        _second_page_box.setVisibility(View.GONE);
		_link_skip.setVisibility(View.GONE);

		mAuth = FirebaseAuth.getInstance();
		mUser = mAuth.getCurrentUser();

		//Views interaction
		_first_next_btn.setOnClickListener(this);
		_second_next_btn.setOnClickListener(this);
		_link_skip.setOnClickListener(this);
		_location_picker.setOnClickListener(this);
		_gender_spinner.setOnClickListener(this);
		_age_spinner.setOnClickListener(this);

	    getGender();
	    getAgeSet();
	    getLocations();
    }

    private void initViews() {
        _link_skip = findViewById(R.id.ac_profile_skip_link);
        _gender_spinner = findViewById(R.id.ac_profile_gender);
        _age_spinner = findViewById(R.id.ac_profile_ageset);
        _location_picker = findViewById(R.id.ac_profile_location);
        _first_next_btn = findViewById(R.id.ac_profile_next_btn);
        _second_next_btn = findViewById(R.id.ac_profile_pt_btn);
        _title_image = findViewById(R.id.ac_c_profile_welcome_img);
        _first_page_box = findViewById(R.id.ac_profile_first_page_cont);
        _second_page_box = findViewById(R.id.ac_profile_second_page_cont);
    }

	private void setUpAgeSet(List<AgeSet> data) {
		mAlertDialog.alertDialogCustomAge(selfRef.getString(R.string.select_ageset), data);
		mAlertDialog.setOnCustomViewAlertDialogInteraction((s, i) -> {
			_age_spinner.setText(((AgeSet) s).set);
		});
	}

	private void setUpGender(List<Gender> data) {
		mAlertDialog.alertDialogCustomGender(selfRef.getString(R.string.select_gender), data);
		mAlertDialog.setOnCustomViewAlertDialogInteraction((s, i) -> {
			_gender_spinner.setText(((Gender) s).name);
		});
	}

	public void getGender() {
		Call<List<Gender>> genderCall = mApiInterface.getGender();
		genderCall.enqueue(new Callback<List<Gender>>() {
			@Override
			public void onResponse(@NotNull Call<List<Gender>> call, @NotNull Response<List<Gender>> response) {
				if (response.code() == 200) {
					if (response.body() != null && response.body().size() > 0){
						mGendersList = response.body();
						Log.d(TAG,"Gender:"+mGendersList.get(0).name);
					}else {
						Log.d(TAG,"Null List");
					}
				}
			}

			@Override
			public void onFailure(@NotNull Call<List<Gender>> call, @NotNull Throwable t) {
				Log.d(TAG,"Error:"+t.getLocalizedMessage());
			}
		});
	}

	public void getAgeSet() {
		Call<List<AgeSet>> genderCall = mApiInterface.getAgeSet();
		genderCall.enqueue(new Callback<List<AgeSet>>() {
			@Override
			public void onResponse(@NotNull Call<List<AgeSet>> call, @NotNull Response<List<AgeSet>> response) {
				if (response.code() == 200) {
					if (response.body() != null && response.body().size() > 0){
						mAgeList = response.body();
						Log.d(TAG,"Age:"+mAgeList.get(0).set);
					}else {
						Log.d(TAG,"Null age List");
					}
				}
			}

			@Override
			public void onFailure(@NotNull Call<List<AgeSet>> call, @NotNull Throwable t) {
				Log.d(TAG,"Error:"+t.getLocalizedMessage());
			}
		});
	}

	public void getLocations() {
		Call<List<Locations>> genderCall = mApiInterface.getLocations();
		genderCall.enqueue(new Callback<List<Locations>>() {
			@Override
			public void onResponse(@NotNull Call<List<Locations>> call, @NotNull Response<List<Locations>> response) {
				if (response.code() == 200) {
					if (response.body() != null && response.body().size() > 0){
						mLocationsList = response.body();
						Log.d(TAG,"Location:"+mLocationsList.get(0).name);
					}else {
						Log.d(TAG,"Null Locations List");
					}

				}
			}

			@Override
			public void onFailure(@NotNull Call<List<Locations>> call, @NotNull Throwable t) {
				Log.d(TAG,"Error:"+t.getLocalizedMessage());
			}
		});
	}

	private void setSpinneraData(Spinner spinner, String... list) {
		SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(selfRef, android.R.layout.simple_spinner_dropdown_item
				, list);
		spinner.setAdapter(spinnerAdapter);
	}

	private boolean verifyUserName(String name) {
		return name.length() >= 3 && name.length() <= 10;
	}

    /**
     * Requests the Storage permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestStoragePermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the accepted has been denied previously.
            Log.i(TAG, "Displaying storage permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the accepted.
            Snackbar.make(_first_next_btn, getString(R.string.allow_app_permissions_snack_bar, "storage"),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.dialog_ok), view -> ActivityCompat.requestPermissions(selfRef, PERMISSIONS_STORAGE, REQUEST_STORAGE))
                    .show();
        } else {
            // Storage permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
        // END_INCLUDE(storage_permission_request)
    }

    public void continueToUpload() {
        Log.i(TAG, "Checking storage permissions.");

        // Verify that all required contact permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permissions have not been granted.
            Log.i(TAG, "Storage permissions has NOT been granted. Requesting permissions.");
            requestStoragePermissions();

        } else {

            // Storage permissions have been granted. Show the contacts fragment.
            Log.i(TAG, "Storage permissions have already been granted. Displaying contact details.");

            pickAvata();
        }
    }

    private void pickAvata() {
        if (Tools.isDeviceOnline(selfRef)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_AVATA);
        } else {
			ViewsUtil.showToast(selfRef, "Please enable network connection to change your avata!", 0, R.color.colorOrange);
        }
    }

    /**
     * Callback received when a permissions accepted has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_STORAGE) {
            Log.i(TAG, "Received response for storage permissions accepted.");

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.

                Snackbar.make(_first_next_btn, R.string.permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                pickAvata();

            } else {
                Log.i(TAG, "Storage permissions were NOT granted.");
                Snackbar.make(_first_next_btn, R.string.permissions_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PICK_AVATA) {
				if (data == null) {
					ViewsUtil.showToast(selfRef, "No image data selected", 0, R.color.colorAccent);
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
						User user = new User();
						user.phone = mUser.getPhoneNumber();
						user.avata = mUserAvata;
						user.name = "Be User";
						user.bio = "TBC is awesome";

						UserDB.getInstance(selfRef).addUser(user);

						Intent intent = new Intent(selfRef, UploadUserDataActivity.class);
						intent.setAction(UploadUserDataActivity.ACTION_INTENT_DATA_USERNAME);
						intent.putExtra(UploadUserDataActivity.INTENT_DATA_USER, user);
						startActivity(intent);
						finish();
					}

					Log.d("VATAEUYY", imageBase64);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == PICK_LOCATION) {
				if (data == null) {
					Snackbar.make(_location_picker, "You did not select location", Snackbar.LENGTH_LONG)
							.show();
				} else if (data.getAction() != null && data.getAction().equals("LOCATION")) {
					Locations location = data.getParcelableExtra("data");

					if (location != null) {
						_location_picker.setText(location.name);
					}
				}
			}
        }
    }

    private void handleUserPlusData() {
		UserPlus userPlus = new UserPlus();
		userPlus.age_set = _age_spinner.getText().toString();
		userPlus.gender = _gender_spinner.getText().toString();
		userPlus.location = _location_picker.getText().toString();

		FirebaseDatabase.getInstance().getReference().child(Configs.DATABASE_USERS_PLUS_ROOT_NAME)
				.child(Objects.requireNonNull(mAuth.getUid()))
				.setValue(userPlus);
	}

    private boolean verifyLocation(){
        return _location_picker.getText().toString().equals(getString(R.string.select_location));
    }

    private boolean verifySpinners(Spinner spinner) {
        return spinner.getSelectedItemPosition() == 0;
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ac_profile_next_btn) {
			if (TextUtils.isEmpty(_age_spinner.getText()) || TextUtils.isEmpty(_gender_spinner.getText()) || verifyLocation()) {
				ViewsUtil.showToast(selfRef, "Please check all your details before you continue!", 0, R.color.colorAccent);
			} else {
				handleUserPlusData();
				_first_page_box.setVisibility(View.GONE);
				_first_next_btn.setVisibility(View.INVISIBLE);
				_second_page_box.setVisibility(View.VISIBLE);
				_link_skip.setVisibility(View.VISIBLE);
			}
		} else if (id == R.id.ac_profile_pt_btn) {
			continueToUpload();
		} else if (id == R.id.ac_profile_skip_link) {
			User user = new User();
			user.phone = mUser.getPhoneNumber();
			user.avata = "default";
			user.name = "Be User";
			user.bio = "TBC is awesome";

			UserDB.getInstance(selfRef).addUser(user);

			Intent intent = new Intent(selfRef, UploadUserDataActivity.class);
			intent.setAction(UploadUserDataActivity.ACTION_INTENT_DATA_USERNAME);
			intent.putExtra(UploadUserDataActivity.INTENT_DATA_USER, user);
			startActivity(intent);
			finish();
		} else if (id == R.id.ac_profile_location) {
			startActivityForResult(new Intent(selfRef, SearchLocationActivity.class), PICK_LOCATION);
		} else if (id == R.id.ac_profile_ageset) {
			if (mAgeList != null && mAgeList.size() > 0) {
				setUpAgeSet(mAgeList);
			}
		} else if (id == R.id.ac_profile_gender) {
			if (mGendersList != null && mGendersList.size() > 0) {
				setUpGender(mGendersList);
			}
		}
    }
}
