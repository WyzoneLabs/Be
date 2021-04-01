package utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.IOException;


public class Tools {

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(activity.getCurrentFocus() != null)inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static long[] vibrate(int length) {

        switch (length) {
            case 0:
                return new long[]{
                        500
                };
            case 1:
                return new long[]{
                        1000
                };
            case 2:
                return new long[]{
                        100, 1000
                };
            default:
                return new long[]{
                };
        }
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    public static boolean isDeviceOnline(Context context) {
        if (context != null) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } else {
            return false;
        }
    }

    public static boolean isServiceRunning(Context context, Class<?> service) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    public static Inbox messageToInbox(GMessage message){
//        Inbox inbox = new Inbox();
//        inbox.message_id = message.message_id ;
//        inbox.sender_id = message.sender_id ;
//        inbox.room_id = message.room_id ;
//        inbox.is_group = "y";
//
//        return inbox;
//    }

    public static void selectImage(Activity activity, int REQUEST_IMAGE_OPEN) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_IMAGE_OPEN);
        }
    }

    public static void selectVideo(Activity activity, int REQUEST_VIDEO_OPEN) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_VIDEO_OPEN);
        }
    }

    public static void selectAudio(Activity activity, int REQUEST_AUDIO_OPEN) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("audio/*");
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_AUDIO_OPEN);
        } else {
            Toast.makeText(activity, "Sorry you don't a suitable app for selecting such kind of file", Toast.LENGTH_SHORT).show();
        }
    }

    public static void selectDoc(Activity activity, int REQUEST_DOC_OPEN) {

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_PICK);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_DOC_OPEN);

//        Intent intent = new Intent(Intent.ACTION_PICK);
//        intent.setType("*/*");
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
//            activity.startActivityForResult(intent, REQUEST_DOC_OPEN);
        } else {
            Toast.makeText(activity, "Sorry you don't a suitable app for selecting such kind of file", Toast.LENGTH_SHORT).show();
        }
    }

    public static void selectSpecificContact(Activity activity, int REQUEST_SELECT_PHONE_NUMBER) {
        // Start an activity for the user to pick a phone number from contacts
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_SELECT_PHONE_NUMBER);
        }
    }

    public static void selectContact(Activity activity, int REQUEST_PHONE_OPEN) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(activity.getApplicationContext().getPackageManager()) != null) {
            activity.startActivityForResult(intent, REQUEST_PHONE_OPEN);
        }
    }

    public static void playMedia(Context context, int resId) {
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resId);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.reset();
            mediaPlayer.setVolume(100f, 100f);
            mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            assetFileDescriptor.close();
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }finally {
            mediaPlayer.release();
        }
    }
}
