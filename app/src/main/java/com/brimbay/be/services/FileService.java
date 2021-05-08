package com.brimbay.be.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.brimbay.be.MainActivity;
import com.brimbay.be.application.AppController;
import com.brimbay.be.classes.DownloadTask;
import com.brimbay.be.sqlite.DbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.brimbay.be.application.Config.DOWNLOAD_URL;
import static utils.Configs.FILE_URL;


public class FileService extends Service {

    final Context context = this;

    DownloadTask downloadTask;

    private static final String TAG = MainActivity.class.getSimpleName();
    private String tag_string_req = "string_req";

    DbHelper DB;

    public static final int notify = 10000;  //interval between two services(Here Service run every 5 Minute)
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        //Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast
                   /// Toast.makeText(getBaseContext(), "Service is running", Toast.LENGTH_SHORT).show();


                    DB = new DbHelper(context);

        Log.i(TAG, "onReceive: Network Detected");

        if(isNetworkAvailable())

            createDir();

           // Toast.makeText(context, "Connection Available", Toast.LENGTH_LONG).show();
       // else
            //Toast.makeText(context, "Connection Not Available", Toast.LENGTH_LONG).show();

                }
            });
        }


        private void createDir() {

            Log.d("App", "failed to create directory");

            File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "wyzone");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {

                    Log.d("App", "failed to create directory");
                }
            }else {

            /*File nomedia = new File(Environment.getExternalStorageDirectory(), "wyzone/"+".nomedia");
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
                downloadTask = new DownloadTask(context);
                getData();

            }
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        public  void  getData(){

            StringRequest billionaireReq = new StringRequest(DOWNLOAD_URL,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {

                            Log.i("Application Data", response);

                            try {


                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray data = jsonObject.getJSONArray("result");


                                for (int j = 0; j < data.length(); j++) {


                                    JSONObject obj = data.getJSONObject(j);

                                    String advert_image_path =  FILE_URL + obj.getString("advert_image_path");

                                    Log.e(TAG, "onResponse: " + advert_image_path);


                                    /*String string = obj.getString("advert_image_path");

                                    String[] parts = string.split("/");

                                    Log.e(TAG, "onResponse: " + advert_image_path);*/

                                    DB = new DbHelper(context);

                                    if(DB.addAdvert(obj.getString("advert_id"),
                                            obj.getString("advert_name"),
                                            obj.getString("advert_detail"),
                                            obj.getString("advert_end_date"),
                                            obj.getString("advert_image_path"),
                                            obj.getString("company_name"))){
                                        // Toast.makeText(mContext, "inserted", Toast.LENGTH_SHORT).show();

                                       new DownloadTask(context).execute(advert_image_path);

                                    }  else {
                                        //      Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show();
                                    }



                                }


                                //Log.i("Application Data", result.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener()

            {
                @Override
                public void onErrorResponse (VolleyError error){
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                }
            });

            AppController.getInstance().addToRequestQueue(billionaireReq, tag_string_req);
        }



    }
}
