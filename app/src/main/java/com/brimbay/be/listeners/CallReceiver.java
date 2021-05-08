package com.brimbay.be.listeners;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.brimbay.be.MyCustomDialog;
import com.brimbay.be.services.FileService;

import java.util.Date;


public class CallReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    private WindowManager wm;
    private static LinearLayout ly1;
    private WindowManager.LayoutParams params1;

    Context pcontext;


    public static int CALL_STATUS = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.w("intent " , intent.getAction().toString());

        pcontext = context;


        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");

        }
        else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;


            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;

                Thread pageTimer = new Thread(){
                    public void run(){
                        try{
                            sleep(1000);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        } finally {
                            /*Intent i = new Intent();
                            i.setClass(pcontext, MyCustomDialog.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                            i.addCategory(Intent.CATEGORY_LAUNCHER);
                            pcontext.startActivity(i);*/
                        }
                    }
                };
                pageTimer.start();

            }

            onCallStateChanged(context, state, number);
        }


    }


    @SuppressLint("WrongConstant")
    public void onCallStateChanged(Context context, int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;

                CALL_STATUS = 1;

                /*WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.RIGHT | Gravity.TOP;*/

                Intent i = new Intent();
                i.setClass(pcontext, MyCustomDialog.class);
                //i.setClass(pcontext, FloatingViewService.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
               context.startActivity(i);
               //context.startService(i);

                context.startService(new Intent(context, FileService.class)); //start service which is MyService.java

               Toast.makeText(context, "Incoming Call Ringing" + number , Toast.LENGTH_SHORT).show();


                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    CALL_STATUS = 2;
                    isIncoming = false;
                    callStartTime = new Date();
                    // Toast.makeText(context, "Outgoing Call Started" , Toast.LENGTH_SHORT).show();
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:

                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    CALL_STATUS = 2;
                    //Ring but no pickup-  a miss
                     Toast.makeText(context, "Ringing but no pickup" + savedNumber + " Call time " + callStartTime +" Date " + new Date() , Toast.LENGTH_SHORT).show();
                }
                else if(isIncoming){
                    CALL_STATUS = 1;

                    Toast.makeText(context, "Incoming " + savedNumber + " Call time " + callStartTime  , Toast.LENGTH_SHORT).show();
                }
                else{

                     Toast.makeText(context, "outgoing " + savedNumber + " Call time " + callStartTime +" Date " + new Date() , Toast.LENGTH_SHORT).show();

                }

                break;

            default:
                break;
        }
        lastState = state;
    }
}