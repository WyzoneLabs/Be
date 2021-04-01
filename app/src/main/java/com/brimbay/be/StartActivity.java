package com.brimbay.be;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;

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

		new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
		}, 1000);
	}


}













