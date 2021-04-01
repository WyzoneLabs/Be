package services;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import utils.Tools;

import static utils.Configs.TIME_TO_REFRESH;

public class UpdateStatusService extends Service {
    private CountDownTimer detectFriendOnline;

    public UpdateStatusService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance(Objects.requireNonNull(FirebaseApp.initializeApp(this)));
        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                if (mAuth.getUid() == null) return;
                ServiceUtils.updateUserStatus(UpdateStatusService.this);
            }

            @Override
            public void onFinish() {

            }
        };
        if (Tools.isDeviceOnline(UpdateStatusService.this)) {
            detectFriendOnline.start();
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        if (detectFriendOnline != null)detectFriendOnline.cancel();
        super.onDestroy();
    }
}
