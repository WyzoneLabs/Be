package com.brimbay.be;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import database.UserDB;
import model.User;
import ui.pinview.PinView;
import utils.Configs;
import utils.PreferenceHelper;
import utils.ViewsUtil;

import static utils.ViewsUtil.hideViews;
import static utils.ViewsUtil.showViews;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PhoneAuthActivity";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    private SignUpActivity selfRef;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    /*Views*/
    private TextView _phone_sms, _resend_code, _code_verification_info, _terms_link, _privacy_link;
    private CountryCodePicker _country_code;
    private EditText _phone_edit;
    private PinView _verify_code_edit;
    private TextView _next_button, _verify_code_btn;
    private View _phone_edit_parent, _phone_verify_parent, _privacy_box;

    /*Member vars*/
    private String mPhoneNumber;
    public static String PHONE_NUMBER ="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        setContentView(R.layout.activity_sign_up);

        createDir();
        //Initialize views
        initViews();

        //Setup country code picker
        _country_code.setNumberAutoFormattingEnabled(true);
        _country_code.registerCarrierNumberEditText(_phone_edit);

        //Views interaction [START]
        _next_button.setOnClickListener(this);
        _verify_code_btn.setOnClickListener(this);
        _resend_code.setOnClickListener(this);
        _terms_link.setOnClickListener(this);
        _privacy_link.setOnClickListener(this);
        //[END]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            checkIsFirstTimeAccess();
        }
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid accepted for verification is made,
                // for instance if the the phone number format is not valid.
                Log.d(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid accepted
                    // [START_EXCLUDE]
                    ViewsUtil.showToast(selfRef,getString(R.string.sign_up_error_invalid_num),0,R.color.colorPink);
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                ViewsUtil.showToast(selfRef, getString(R.string.sign_up_error_verify_failed),0, R.color.colorPink);
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                ViewsUtil.showToast(selfRef, getString(R.string.sign_up_code_sent), 0, R.color.colorAccent);
                _code_verification_info.setText(getString(R.string.enter_verification_code_info, mPhoneNumber));
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                updateUI(STATE_INITIALIZED);
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };
        // [END phone_auth_callbacks]
    }

    private void initViews() {
        _phone_sms = findViewById(R.id.ac_sign_up_phone_sms_info);
        _country_code = findViewById(R.id.ac_sign_up_phone_country_code);
        _phone_edit = findViewById(R.id.ac_sign_up_phone_edit_txt);
        _next_button = findViewById(R.id.ac_sign_up_next_btn);
        _resend_code = findViewById(R.id.ac_sign_up_p_v_request_new_code_link);
        _verify_code_btn = findViewById(R.id.ac_sign_up_verify_next_btn);
        _verify_code_edit = findViewById(R.id.ac_sign_up_code_view);
        _phone_edit_parent = findViewById(R.id.phone_parent);
        _phone_verify_parent = findViewById(R.id.ac_sign_up_code_verify_box);
        _code_verification_info = findViewById(R.id.ac_sign_up_verify_title_info_txt);
        _terms_link = findViewById(R.id.ac_sign_up_term);
        _privacy_link = findViewById(R.id.ac_sign_up_privacy);
        _privacy_box = findViewById(R.id.ac_sign_up_terms_box);

        _phone_edit.setActivated(true);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(_phone_edit.getText().toString());
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser user = task.getResult().getUser();
                        // [START_EXCLUDE]

                        updateUI(STATE_SIGNIN_SUCCESS, user);
                        // [END_EXCLUDE]
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            ViewsUtil.showToast(selfRef, getString(R.string.sign_up_error_invalid_code), 0, R.color.colorOrange);
//                                mVerificationField.setError("Invalid code.");
                            // [END_EXCLUDE]
                        }
                        // [START_EXCLUDE silent]
                        // Update UI
                        ViewsUtil.showToast(selfRef,Objects.requireNonNull(task.getException()).getLocalizedMessage()+ "",0,R.color.colorPink);
                        updateUI(STATE_SIGNIN_FAILED);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END sign_in_with_phone]

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
				showViews(_phone_edit_parent /*, _phone_sms, _next_button,_privacy_box*/);
				hideViews(_phone_verify_parent);

                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
				hideViews(_phone_edit_parent/*, _phone_sms, _next_button, _privacy_box*/);
				showViews(_phone_verify_parent);

                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        _verify_code_edit.setValue(cred.getSmsCode());
                    } else {
                        _verify_code_edit.setValue(getString(R.string.instant_validation));
                    }
                }
                // Signed in
                checkIsFirstTimeAccess();
                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
//                mDetailText.setText(R.string.status_sign_in_failed);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                // Signed in
                checkIsFirstTimeAccess();
                break;
        }
    }

	private boolean validatePhoneNumber() {
        String phoneNumber = _country_code.getFullNumber();
        return !TextUtils.isEmpty(phoneNumber);
    }

    private void checkIsFirstTimeAccess() {
        if(FirebaseAuth.getInstance().getUid() != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child(Configs.DATABASE_USERS_ROOT_NAME)
                    .child(FirebaseAuth.getInstance().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG,"Check:"+ dataSnapshot.toString());
                            if (dataSnapshot.exists() && dataSnapshot.child("name").exists()) {
                                Log.d(TAG,"Check Exists:"+ dataSnapshot.getValue());
                                HashMap hashUser = (HashMap) dataSnapshot.getValue();
                                User userInfo = new User().getSmallUserFromMap(hashUser);
                                UserDB.getInstance(selfRef).addUser(userInfo);

                                PreferenceHelper.getInstance(selfRef).saveFirstTime(false);
                                ViewsUtil.showToast(selfRef,getString(R.string.sign_up_state_success),0,R.color.colorGreen);
                                Intent intent = new Intent(selfRef, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Log.d(TAG,"Check Not Existing:"+ dataSnapshot.toString());
                                Intent intent = new Intent(selfRef, CreateProfileActivity.class);
                                startActivity(intent);
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
        }else{
            Log.d(TAG,"Check Firebase User Null");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ac_sign_up_next_btn) {//                Log.d(TAG, "onClick: full number: " + _country_code.getFullNumber());
//                Log.d(TAG, "onClick: full number with plus: " + _country_code.getFullNumberWithPlus());
//                Log.d(TAG, "onClick: full number formatted: " + _country_code.getFormattedFullNumber());

            if (!validatePhoneNumber()) {
                ViewsUtil.showToast(selfRef, getString(R.string.sign_up_error_invalid_num), 0, R.color.colorPink);
                return;
            } else {

                mPhoneNumber = _country_code.getFullNumberWithPlus();
                PHONE_NUMBER = mPhoneNumber;
            }
//                updateUI(STATE_CODE_SENT);

            startPhoneNumberVerification(mPhoneNumber);
        } else if (id == R.id.ac_sign_up_verify_next_btn) {
            String code = _verify_code_edit.getValue();
            if (TextUtils.isEmpty(code)) {
                ViewsUtil.showToast(selfRef, getString(R.string.sign_up_empty_code_field), 0, R.color.colorOrange);
                //mVerificationField.setError("Cannot be empty.");
                return;
            }

            verifyPhoneNumberWithCode(mVerificationId, code);
        } else if (id == R.id.ac_sign_up_p_v_request_new_code_link) {
            if (mPhoneNumber != null) resendVerificationCode(mPhoneNumber, mResendToken);
        } else if (id == R.id.ac_sign_up_privacy) {//                AccountSet accountSet2 = new AccountSet();
//                accountSet2.icon = R.drawable.privacy_28px_1;
//                accountSet2.title = getString(R.string.privacy);
//                accountSet2.id = "id_4";
//                Intent intent = new Intent(selfRef, SettingActivity.class);
//                intent.setAction(SettingActivity.ACTION_SETTINGS_M_PAGE);
//                intent.putExtra(SettingActivity.INTENT_SETTINGS_M_PAGE, accountSet2);
//                startActivity(intent);
        } else if (id == R.id.ac_sign_up_term) {//                AccountSet accountSet = new AccountSet();
//                accountSet.icon = R.drawable.terms_and_conditions_28px;
//                accountSet.title = getString(R.string.chat_terms_of_service);
//                accountSet.id = "id_3" ;
//                Intent interm = new Intent(selfRef, SettingActivity.class);
//                interm.setAction(SettingActivity.ACTION_SETTINGS_M_PAGE);
//                interm.putExtra(SettingActivity.INTENT_SETTINGS_M_PAGE, accountSet);
//                startActivity(interm);
            //            case R.channel_id.sign_up_back:
//                showViews(_confirm_num_button, _phone_field, _code_container);
//                hideViews(_code_field, _resend_code, _verify_code_btn, _back_button);
//                break;
//            case R.channel_id.signOutButton:
//                signOut();
//                break;
        }
    }

    private void createDir() {


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

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                Log.d("App", "failed to create directory");
            }
        }
    }
}
