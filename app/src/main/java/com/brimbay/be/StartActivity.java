package com.brimbay.be;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import utils.PreferenceHelper;

public class StartActivity extends AppCompatActivity {
	private StartActivity selfRef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		setContentView(R.layout.activity_start);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();

		PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(selfRef);

		Dexter.withContext(this)
				.withPermissions(
						Manifest.permission.MANAGE_EXTERNAL_STORAGE,
						Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE,
						Manifest.permission.CALL_PHONE
				).withListener(new MultiplePermissionsListener() {
			@Override
			public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
				new Handler(Looper.getMainLooper()).postDelayed(() -> {

					if (mAuth.getCurrentUser() != null) {


                         //check if user profile is complete - redirect to signup
						startActivity(new Intent(selfRef, MainActivity.class));
						
					}else {
						if (preferenceHelper.getFirstTimeAccess()) {
							preferenceHelper.initPreference();
						}

						startActivity(new Intent(selfRef, SignUpActivity.class));
					}
					finish();
				}, 1000);
			}

			@Override
			public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

			}

		}).check();

		/*new Handler(Looper.getMainLooper()).postDelayed(() -> {
			if (mAuth.getCurrentUser() != null) {
				startActivity(new Intent(selfRef, MainActivity.class));
			}else {
				if (preferenceHelper.getFirstTimeAccess()) {
					preferenceHelper.initPreference();
				}
//				startActivity(new Intent(selfRef, SignUpActivity.class));
				startActivity(new Intent(selfRef, AccountActivity.class));
			}
			finish();
		}, 1000);*/
	}


}













