package com.brimbay.be;

/**
 * Created by startappz on 16/05/2017.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

import static com.brimbay.be.listeners.CallReceiver.CALL_STATUS;


public class MyCustomDialog extends Activity
{
    TextView tv_client;
    String phone_no;
    Button dialog_ok;
    ImageView imageView;
    Context context = this;

    Bitmap b = null;


    LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        setWindowParams();


        try
        {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.setFinishOnTouchOutside(true);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog);
            initializeContent();




            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                   // if(checkcallstate)

                   // Toast.makeText(context, "Status" + CALL_STATUS, Toast.LENGTH_SHORT).show();
                    System.out.print(CALL_STATUS);

                    if(CALL_STATUS==1){

                        addNotification();
                    }else{
                        Toast.makeText(context, "Take me to the call to action", Toast.LENGTH_SHORT).show();
                    }



                    MyCustomDialog.this.finish();
                    System.exit(0);
                }
            });

            /*dialog_ok.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    MyCustomDialog.this.finish();
//                    this.setFinishOnTouchOutside(false);
                    System.exit(0);
                }
            });*/
        }
        catch (Exception e)
        {
            Log.d("Exception", e.toString());
            e.printStackTrace();
        }
    }


    public void setWindowParams() {
        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.dimAmount = 0;
        wlp.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                ;
        getWindow().setAttributes(wlp);
    }

    private void addNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "tutorialspoint_01";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription("Advert Name");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        NotificationCompat.Builder builder = notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(b)/*
                .setStyle(new Notification.BigPictureStyle()
                        .bigPicture(b))*/
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(b))
                .setTicker("tt")
                //.setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("WyLabs")
                .setContentText("Redeem your points")
                .setContentInfo("Information");
        notificationManager.notify(1, notificationBuilder.build());
    }


    private void initializeContent()
    {
        // tv_client   = (TextView) findViewById(R.id.tv_client);
        // dialog_ok   = (Button) findViewById(R.id.dialog_ok);

        imageView = findViewById(R.id.image_preview);

        linearLayout = findViewById(R.id.layout);

        getFilePath();
    }

    private void getFilePath() {

        String path = Environment.getExternalStorageDirectory().toString()+"/wyzone";
        Log.d("Files", "Path: " + path);


        File directory = new File(path);
        File[] files = directory.listFiles();


        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {

            Log.d("Files", "FileName:" + files[i].getName());

            // fileUpload(files[i].getPath());

        }

        Random r = new Random();
        int i1 = r.nextInt((files.length+1));

        String img = files[i1].getName();

        Log.d("File", "FileName--->:" + img);

        Log.d("File", "FileName--->:" + files[i1].getName());

        if(img.endsWith("gif")){

            File f=new File(path, img);

/*
            GifImageView gifImageView = (GifImageView) findViewById(R.id.GifImageView);
            gifImageView.setGifImageUri(Uri.parse("file://"+f.toString()));*/

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.profile_img)
                    .error(R.drawable.profile_img)
                    .priority(Priority.HIGH);

            RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                    .load("https://media.giphy.com/media/3oriNY7jFpuXvzBBTO/giphy.gif");


            Glide.with(context)
                    .load(Uri.parse("https://media.giphy.com/media/3oriNY7jFpuXvzBBTO/giphy.gif"))
                     .apply(options)
                    // .animate((ViewPropertyAnimation.Animator) Uri.parse(f.toString()))
                    //.crossFade()
                    // .fitCenter()
                    .into(imageView);
            Log.d("Files", "Path: " + "file://"+f.toString());
        } else {
            try {
                File f=new File(path, img);
                b = BitmapFactory.decodeStream(new FileInputStream(f));
                imageView.setImageBitmap(b);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }

    }
}
