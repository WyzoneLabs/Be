package utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.brimbay.be.R;


public class PreferenceHelper {
    private static PreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static Context context;

    public static final boolean PREF_DEFAULT_NTF_RINGTONE = true;
    public static final boolean PREF_DEFAULT_NTF_IN_CHAT_SOUNDS= true;
    public static final boolean PREF_DEFAULT_NTF_VIBRATE = true;

    public static final boolean PREF_DEFAULT_CHAT_SET_ENTER_SEND = false;
    public static final boolean PREF_DEFAULT_CHAT_SET_BACKGROUND = true;

    public static final String PREF_KEY_NTF_RINGTONE = "pref_ntf_ringtone";
    public static final String PREF_KEY_NTF_IN_CHAT_SOUNDS= "pref_ntf_in_chat_sound";
    public static final String PREF_KEY_NTF_VIBRATE = "pref_ntf_vibrate";

    public static final String PREF_KEY_CHAT_SET_ENTER_SEND = "pref_chat_set_enter_send";
    public static final String PREF_KEY_CHAT_SET_BACKGROUND = "pref_chat_set_background";

    public static final boolean PREF_DEFAULT_FIRST_TIME_ACCESS = true;
    public static final String PREF_KEY_FIRST_TIME_ACCESS= "pref_first_time_access";

    private PreferenceHelper() {
    }

    public static PreferenceHelper getInstance(Context ctx) {
        context = ctx;
        if (instance == null) {
            instance = new PreferenceHelper();
            preferences = ctx.getSharedPreferences(ctx.getString(R.string.pref_main_preference), Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void initPreference(){
        saveFirstTime(getFirstTimeAccess());
        saveChatSettingEnterSend(getChatSettingEnterSend());
        saveChatSettingVideoBackground(getChatSettingVideoBackground());
        saveNotificationsInChatSound(getNotificationsInChatSound());
        saveNotificationsRingtone(getNotificationsRingtone());
        saveNotificationsVibrate(getNotificationsVibrate());
    }

    public void saveFirstTime(boolean first_time){
        editor.putBoolean(PREF_KEY_FIRST_TIME_ACCESS, first_time);
    }

    public boolean getFirstTimeAccess(){
        return  preferences.getBoolean(PREF_KEY_FIRST_TIME_ACCESS, PREF_DEFAULT_FIRST_TIME_ACCESS);
    }

    public void saveNotificationsRingtone(boolean play_ringtone){
        editor.putBoolean(PREF_KEY_NTF_RINGTONE, play_ringtone).apply();
    }

    public void saveNotificationsInChatSound(boolean chat_sounds){
        editor.putBoolean(PREF_KEY_NTF_IN_CHAT_SOUNDS, chat_sounds).apply();
    }

    public void saveNotificationsVibrate(boolean vibrate){
        editor.putBoolean(PREF_KEY_NTF_VIBRATE, vibrate).apply();
    }

    public void saveChatSettingEnterSend(boolean enter_send){
        editor.putBoolean(PREF_KEY_CHAT_SET_ENTER_SEND, enter_send).apply();
    }

    public void saveChatSettingVideoBackground(boolean background){
        editor.putBoolean(PREF_KEY_CHAT_SET_BACKGROUND, background).apply();
    }

    public boolean getNotificationsRingtone(){
        return preferences.getBoolean(PREF_KEY_NTF_RINGTONE, PREF_DEFAULT_NTF_RINGTONE);
    }

    public boolean getNotificationsInChatSound(){
        return preferences.getBoolean(PREF_KEY_NTF_IN_CHAT_SOUNDS, PREF_DEFAULT_NTF_IN_CHAT_SOUNDS);
    }

    public boolean getNotificationsVibrate(){
        return preferences.getBoolean(PREF_KEY_NTF_VIBRATE, PREF_DEFAULT_NTF_VIBRATE);
    }

    public boolean getChatSettingEnterSend(){
        return preferences.getBoolean(PREF_KEY_CHAT_SET_ENTER_SEND, PREF_DEFAULT_CHAT_SET_ENTER_SEND);
    }

    public boolean getChatSettingVideoBackground(){
        return preferences.getBoolean(PREF_KEY_CHAT_SET_BACKGROUND, PREF_DEFAULT_CHAT_SET_BACKGROUND);
    }

}
