package com.brimbay.be.application;

/**
 * Created by StartappzKE on 17-Oct-16.
 */

public class Config {


    //URL to our login.php file
    public static final String BASE_URL = "https://www.wyzone.co.ke/";

    public static final String IMAGE_URL = "https://www.wyzone.co.ke/app/";

    //URL to our login.php file
    public static final String LOGIN_URL = "https://www.wyzone.co.ke/app/loginuser.php";

    //URL to our register.php file
    public static final String REGISTER_URL = "https://www.wyzone.co.ke/app/register.php";


    public static final String UPLOAD_URL = "https://www.wyzone.co.ke/app/upload.php";

    public static final String DOWNLOAD_URL = "https://wyzone.co.ke/app/download.php";




    public static final String LOCATION_URL = "https://wyzone.co.ke/app/getlocation.php";
    public static final String AGE_SET_URL = "https://wyzone.co.ke/app/getageset.php";


    public static final String USER_URL = "https://wyzone.co.ke/app/getusers.php";

    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE= "phone";
    public static final String KEY_NAMES= "names";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_COUNTY = "county";
    public static final String KEY_AGE= "age";


    public static final String KEY_IMAGE= "image";
    public static final String KEY_TYPE= "type";
    public static final String KEY_USER= "user";


    public static final String LOGIN_SUCCESS = "success";

    public static final String EXISTS = "exists";


    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "mylogindetails";

    //This would be used to store the email of current logged in user
    public static final String EMAIL_SHARED_PREF = "email";

    public static final String PHONE_SHARED_PREF = "phone";

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";
    public static final String GET_USERS = "https://www.wyzone.co.ke/app/getusers.php" ;


    public static int REQUEST_CODE_REGISTER = 2000;
    public static String STR_EXTRA_ACTION_LOGIN = "login";
    public static String STR_EXTRA_ACTION_RESET = "resetpass";
    public static String STR_EXTRA_ACTION = "action";
    public static String STR_EXTRA_USERNAME = "username";
    public static String STR_EXTRA_PASSWORD = "password";
    public static String STR_DEFAULT_BASE64 = "default";
    public static String UID = "";
    //TODO only use this UID for debug mode
//    public static String UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22";
    public static String INTENT_KEY_CHAT_FRIEND = "friendname";
    public static String INTENT_KEY_CHAT_AVATA = "friendavata";
    public static String INTENT_KEY_CHAT_ID = "friendid";
    public static String INTENT_KEY_CHAT_ROOM_ID = "roomid";
    public static long TIME_TO_REFRESH = 10 * 1000;
    public static long TIME_TO_OFFLINE = 2 * 60 * 1000;


    public static final String S_URL = "https://wyzone.co.ke/app/sync.php";




}