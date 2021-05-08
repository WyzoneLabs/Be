package utils;

import com.brimbay.be.R;

public class Configs {
    public static final String DATABASE_NAME = "dialogs";
    public static final String BASE_URL = "https://wyzone.co.ke/app/";
    public static final String FILE_URL = "https://wyzone.co.ke/cdn/";

    public static final String STR_DEFAULT_BASE64 = "default";
    public static final String STR_DEFAULT_ICON = "default_icon";
    public static final long TIME_TO_REFRESH = (long) (5 * 1000);
    public static final long TIME_TO_OFFLINE = 10;

    public static final String DATABASE_USERS_ROOT_NAME = "users";
    public static final String DATABASE_USERS_PLUS_ROOT_NAME = "users_plus";
    public static final String DATABASE_GROUPS_ROOT_NAME = "groups";
    public static final String DATABASE_FRIENDS_ROOT_NAME = "friends";
    public static final String DATABASE_USER_DEVICE_TOKEN = "device_token";

    //Typing
    public static final String DATABASE_TYPING_ROOT_NAME = "typing";

    //Read status
    public static final String DATABASE_READ_STATUS_ROOT_NAME = "read_status";

    //Live broadcast
    public static final String DATABASE_LIVE_CHANNELS_ROOT_NAME = "live_channels";

    public static final String DATABASE_CHAT_REQUEST_ROOT_NAME = "chat_request";

    //Chats
    public static final String DATABASE_CHATS_ROOT_NAME = "chats";
    public static final String DATABASE_PRIVATE_CHATS_ROOT_NAME = "private_chats";
    public static final String DATABASE_GROUP_CHATS_ROOT_NAME = "group_chats";
    public static final String DATABASE_LIVE_CHATS_ROOT_NAME = "live_chats";

    //Chat files storage
    public static final String STORAGE_CHATS_ROOT_NAME = "chats";
    public static final String STORAGE_PRIVATE_CHATS_ROOT_NAME = "private_chats";
    public static final String STORAGE_GROUP_CHATS_ROOT_NAME = "group_chats";
    public static final String STORAGE_IMAGES_ROOT_NAME = "images";
    public static final String STORAGE_VIDEOS_ROOT_NAME = "videos";
    public static final String STORAGE_PDF_ROOT_NAME = "pdf";
    public static final String STORAGE_TXT_ROOT_NAME = "txt";
    public static final String STORAGE_GIF_ROOT_NAME = "gif";
    public static final String STORAGE_OTHER_ROOT_NAME = "other_files";

    public static final Integer[] COLORS = new Integer[]{
            R.color.colorAccent, R.color.colorPrimaryDark, R.color.colorGreen,
            R.color.colorOrange, R.color.colorPink, R.color.purple_700,
            R.color.purple_500, R.color.colorDark
    };

    public static final String sEmojiRegex = "(?:[\\u2700-\\u27bf]|" +
            "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
            "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?" +
            "(?:\\u200d(?:[^\\ud800-\\udfff]|" +
            "(?:[\\ud83c\\udde6-\\ud83c\\uddff]){2}|" +
            "[\\ud800\\udc00-\\uDBFF\\uDFFF]|[\\u2600-\\u26FF])[\\ufe0e\\ufe0f]?(?:[\\u0300-\\u036f\\ufe20-\\ufe23\\u20d0-\\u20f0]|[\\ud83c\\udffb-\\ud83c\\udfff])?)*|" +
            "[\\u0023-\\u0039]\\ufe0f?\\u20e3|\\u3299|\\u3297|\\u303d|\\u3030|\\u24c2|[\\ud83c\\udd70-\\ud83c\\udd71]|[\\ud83c\\udd7e-\\ud83c\\udd7f]|\\ud83c\\udd8e|[\\ud83c\\udd91-\\ud83c\\udd9a]|[\\ud83c\\udde6-\\ud83c\\uddff]|[\\ud83c\\ude01-\\ud83c\\ude02]|\\ud83c\\ude1a|\\ud83c\\ude2f|[\\ud83c\\ude32-\\ud83c\\ude3a]|[\\ud83c\\ude50-\\ud83c\\ude51]|\\u203c|\\u2049|[\\u25aa-\\u25ab]|\\u25b6|\\u25c0|[\\u25fb-\\u25fe]|\\u00a9|\\u00ae|\\u2122|\\u2139|\\ud83c\\udc04|[\\u2600-\\u26FF]|\\u2b05|\\u2b06|\\u2b07|\\u2b1b|\\u2b1c|\\u2b50|\\u2b55|\\u231a|\\u231b|\\u2328|\\u23cf|[\\u23e9-\\u23f3]|[\\u23f8-\\u23fa]|\\ud83c\\udccf|\\u2934|\\u2935|[\\u2190-\\u21ff]";


    public static class Typing {
        public boolean typing;

        public Typing() {
            typing = false;
        }
    }

}