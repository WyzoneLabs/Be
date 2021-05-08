package com.brimbay.be.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;


/**
 * Created by startappz on 27/03/2017.
 */

public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;

    public DownloadTask(Context context) {
        this.context = context;
    }
    public static ProgressDialog mProgressDialog;

    String TAG = "DownLoadTask";


    @Override
    protected String doInBackground(String... sUrl) {

        Log.i(TAG, "doInBackground: " + Arrays.toString(sUrl));



        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            String fileName = url.toString().substring(url.toString().lastIndexOf('/') + 1);

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory()+"/wyzone/"+ fileName);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
       /* mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Opening File");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);*/
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
       // mProgressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
       // mProgressDialog.setIndeterminate(false);
      //  mProgressDialog.setMax(100);
       // mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
       // mProgressDialog.dismiss();
        if (result != null) {
            //Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            Log.i("result",result);
        }
        else {
            //Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
          /*  File file = new File(Environment.getExternalStorageDirectory() + "/M-hub/" + file_name);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            // File file = new File(path);


            intent.setDataAndType(Uri.fromFile(file), file_type);

            context.startActivity(intent);*/
        }
    }

}