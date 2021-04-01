package com.brimbay.be;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.brimbay.be.databinding.ActivityAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import database.UserDB;
import model.User;
import ui.ios_dialog.iOSDialog;
import ui.ios_dialog.iOSDialogBuilder;
import ui.loading.BrimbayProgress;
import ui.pinview.PinView;
import utils.Configs;
import utils.PreferenceHelper;
import utils.ViewsUtil;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {
    private AccountActivity selfRef;

    private static final String TAG = "brimbay.be.Account";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private CountDownTimer countDownTimer;
    private String mPhone;

    private ActivityAccountBinding mBinding;

    //region Views
    private BrimbayProgress brimbayProgress;
    private CountryCodePicker _country_code;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        mBinding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        _country_code = findViewById(R.id.ac_sign_up_phone_country_code);
        _country_code.setNumberAutoFormattingEnabled(true);
        _country_code.registerCarrierNumberEditText(mBinding.acSignUpPhoneEditTxt);
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        mAuth = FirebaseAuth.getInstance();
        brimbayProgress = BrimbayProgress.create(selfRef)
                .setStyle(BrimbayProgress.Style.SPIN_INDETERMINATE)
                .setSize(108, 108)
                .setCancellable(false)
                .setBackgroundColor(selfRef.getResources().getColor(R.color.colorDialogBackground,null))
                .setDimAmount(0.6f);

        //Views Interaction
        countDownTimer = new CountDownTimer(60*1000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                long secs = (millisUntilFinished/1000);
                mBinding.rcRemaining.setText(getString(R.string.remaining_time,secs));
            }

            @Override
            public void onFinish() {
                mBinding.countdownTimerBox.setVisibility(View.INVISIBLE);
                mBinding.acSignUpPVRequestNewCodeLink.setVisibility(View.VISIBLE);
            }
        };
        mBinding.acSignUpPhoneEditTxt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mBinding.acSignUpPhoneEditTxt, InputMethodManager.SHOW_IMPLICIT);
        mBinding.acSignUpPhoneEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence phone, int start, int before, int count) {
                mBinding.acSignUpNextBtn.setEnabled((!TextUtils.isEmpty(phone) &&
                        (phone.length() >= 4 && phone.length() <= 12)));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.acSignUpCodeView.setPinViewEventListener((PinView, fromUser) ->
                mBinding.acSignUpVerifyNextBtn.setEnabled((PinView.getValue().length() == 6)));

        mBinding.acSignUpNextBtn.setOnClickListener(this);
        mBinding.acSignUpVerifyNextBtn.setOnClickListener(this);
        mBinding.acSignUpPVRequestNewCodeLink.setOnClickListener(this);

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
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
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mBinding.acSignUpPhoneEditTxt.setError("Invalid phone number.");
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
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    //region Member Methods
    private void showDialog(String title, String subtitle) {
        new iOSDialogBuilder(selfRef)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setCancelable(true)
                .setBoldPositiveLabel(true)
                .setPositiveListener(getString(R.string.ok), iOSDialog::dismiss)
                .build().show();
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .setForceResendingToken(token)     // ForceResendingToken from callbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (brimbayProgress.isShowing())brimbayProgress.dismiss();
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
                            ViewsUtil.showToast(selfRef, "Invalid code", 0, 0);
                            // [END_EXCLUDE]
                        }
                        // [START_EXCLUDE silent]
                        // Update UI
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
                mBinding.acSignUpNextBtn.setEnabled(false);
                mBinding.phoneParent.setVisibility(View.VISIBLE);
                mBinding.acSignUpCodeVerifyBox.setVisibility(View.GONE);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                countDownTimer.start();
                if (brimbayProgress.isShowing())brimbayProgress.dismiss();
                mBinding.phoneParent.setVisibility(View.GONE);
                mBinding.acSignUpVerifyTitleInfoTxt.setText(
                        getString(R.string.enter_verification_code_info,mPhone));
                mBinding.acSignUpVerifyNextBtn.setEnabled(true);
                mBinding.acSignUpCodeVerifyBox.setVisibility(View.VISIBLE);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                if (brimbayProgress.isShowing())brimbayProgress.dismiss();
                mBinding.acSignUpVerifyErr.setVisibility(View.VISIBLE);
                mBinding.acSignUpVerifyErr.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
//                mBinding.acSignUpVerifyNextBtn.setEnabled(false);
                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        mBinding.acSignUpCodeView.setValue(cred.getSmsCode());
                        if (!brimbayProgress.isShowing())brimbayProgress.show();
                    } else {
//                        mBinding.acSignUpCodeView.setValue(R.string.instant_validation);
                    }

                    checkIsFirstTimeAccess();
                }
                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                updateUI(STATE_INITIALIZED);
                ViewsUtil.showToast(selfRef, getString(R.string.status_sign_in_failed),0,0 );
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                checkIsFirstTimeAccess();
                break;
        }

        if (user == null) {
            // Signed out
        } else {
            // Signed in
            mBinding.acSignUpPhoneEditTxt.setText(null);
        }
    }

    private boolean validatePhoneNumber() {
        String phoneNumber = mBinding.acSignUpPhoneEditTxt.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mBinding.acSignUpPhoneEditTxt.setError("Invalid phone number.");
            return false;
        }
        return true;
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
    //endregion

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == mBinding.acSignUpNextBtn.getId()) {
            if (!validatePhoneNumber()) {
                return;
            }
            mPhone = _country_code.getFullNumberWithPlus();
            brimbayProgress.show();
            startPhoneNumberVerification(mPhone);
        } else if (id == mBinding.acSignUpVerifyNextBtn.getId()) {
            String code = mBinding.acSignUpCodeView.getValue();
            if (TextUtils.isEmpty(code)) {
                mBinding.acSignUpVerifyErr.setError("Cannot be empty.");
                return;
            }
            if(!brimbayProgress.isShowing())brimbayProgress.show();
            verifyPhoneNumberWithCode(mVerificationId, code);
        } else if (id == mBinding.acSignUpPVRequestNewCodeLink.getId()) {
            if(!brimbayProgress.isShowing())brimbayProgress.show();
            resendVerificationCode(mPhone, mResendToken);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            checkIsFirstTimeAccess();
        }
        updateUI(currentUser);

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(mBinding.acSignUpPhoneEditTxt.getText().toString());
        }
        // [END_EXCLUDE]
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

}