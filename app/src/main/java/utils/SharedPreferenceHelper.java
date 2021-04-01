package utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.brimbay.be.R;

import model.User;

import static utils.Configs.STR_DEFAULT_BASE64;


public class SharedPreferenceHelper {
    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static Context context;
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_BIO = "bio";
    private static String SHARE_KEY_EMAIL = "phone";
    private static String SHARE_KEY_AVATA = "avata";
    private static String SHARE_KEY_UID = "uid";


    private SharedPreferenceHelper() {
    }

    public static SharedPreferenceHelper getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = ctx.getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void saveUserInfo(User user) {
        editor.putString(SHARE_KEY_NAME, user.name);
        editor.putString(SHARE_KEY_EMAIL, user.phone);
        editor.putString(SHARE_KEY_BIO, user.bio);
        editor.putString(SHARE_KEY_AVATA, user.avata);
        editor.putString(SHARE_KEY_UID, "");
        editor.apply();
    }

    public User getUserInfo() {

        String userName = preferences.getString(SHARE_KEY_NAME, context.getString(R.string.default_username, context.getString(R.string.app_name)));
        String email = preferences.getString(SHARE_KEY_EMAIL, "");
        String bio = preferences.getString(SHARE_KEY_BIO, context.getString(R.string.default_bio, context.getString(R.string.app_name)));
        String avatar = preferences.getString(SHARE_KEY_AVATA, STR_DEFAULT_BASE64);

        User user = new User();
        user.name = userName;
        user.phone = email;
        user.bio = bio;
        user.avata = avatar;

        return user;
    }

    public String getUID() {
        return preferences.getString(SHARE_KEY_UID, "");
    }

}