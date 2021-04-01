package utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * @author Kevine James
 * @date 9/5/2019
 */
public class LocationTools {
    private Context context;
    private OnLocationProcessing onLocationProcessing;

    public LocationTools(Context context, OnLocationProcessing onLocationProcessing) {
        this.context = context;
        this.onLocationProcessing = onLocationProcessing;
    }

    public static String readFromAssetFiles(Context context, String fileName ) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets().open(fileName, Context.MODE_PRIVATE);//MODE_WORLD_READABLE
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line;
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }

    public void initRegions(){
        new ProcessLocation().execute("region.json");
    }

    public interface OnLocationProcessing{
        void onBeforeProcess();
        void onPostProcess(ArrayList<String> region);
    }

    private class ProcessLocation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return readFromAssetFiles(context, strings[0]);
        }

        @Override
        protected void onPreExecute() {
            onLocationProcessing.onBeforeProcess();
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayList<String> location = new ArrayList<>();
            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    JSONArray jsonArray = jsonObject.getJSONArray("in");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject j1 = jsonArray.getJSONObject(i);
                        String objectId =  j1.getString("TOWN_NAME");
                        location.add(objectId);
                    }
                    onLocationProcessing.onPostProcess(location);

                } catch (JSONException e) {
                    Log.e("VOLLEY_ERR", e.getMessage());
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }
}
