package adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import database.FriendDB;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Friend;
import model.LiveMessage;

import static utils.Configs.STR_DEFAULT_BASE64;

/**
 * @author Kevine James
 * @date 4/8/2019
 */
public class LiveChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Integer[] colors = new Integer[]{
            R.color.colorAccent, R.color.colorPrimaryDark, R.color.colorGreen,
            R.color.purple_700, R.color.colorPrimaryDark, R.color.colorPrimary
    };

    private int CHAT_TYPE = 0;

    private final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private final int VIEW_TYPE_ADMIN_MESSAGE = 0;
    private final int VIEW_TYPE_USER_MESSAGE = 2;
    int randColor = colors[new Random().nextInt(colors.length)];
    private final Context context;
    private final ArrayList<LiveMessage> mMessages;
    private final String userId;
    private final String frndId;

    public LiveChatAdapter(Context ctx, ArrayList<LiveMessage> mMessages, String userId, String frnId) {
        this.context = ctx;
        this.mMessages = mMessages;
        this.userId = userId;
        this.frndId = frnId;
    }

    public void setChatType(int CHAT_TYPE) {
        this.CHAT_TYPE = CHAT_TYPE;
        notifyDataSetChanged();
    }

    public int getChatType() {
        return CHAT_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FRIEND_MESSAGE:
                View viewFndMsg = null;
                if (CHAT_TYPE == 0) {
                    viewFndMsg = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend_default, parent, false);
                } else if (CHAT_TYPE == 2) {
                    viewFndMsg = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend_l, parent, false);
                } else {
                    viewFndMsg = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend_v_l, parent, false);
                }
                return new ItemMessageFriendHolder(viewFndMsg);
            case VIEW_TYPE_ADMIN_MESSAGE:
                View viewAdmin = LayoutInflater.from(context).inflate(R.layout.rc_item_message_admin_l, parent, false);
                return new ItemMessageAdminHolder(viewAdmin);
            case VIEW_TYPE_USER_MESSAGE:
                View viewUser = null;
                if (CHAT_TYPE == 0) {
                    viewUser = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user_default, parent, false);
                } else if (CHAT_TYPE == 2) {
                    viewUser = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user_l, parent, false);
                } else {
                    viewUser = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user_v_l, parent, false);
                }
                return new ItemMessageUserHolder(viewUser);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LiveMessage message = mMessages.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_FRIEND_MESSAGE:
                ((ItemMessageFriendHolder) holder).bind(context, message, CHAT_TYPE);
                break;
            case VIEW_TYPE_ADMIN_MESSAGE:
                ((ItemMessageAdminHolder) holder).bind(message);
                break;
            case VIEW_TYPE_USER_MESSAGE:
                ((ItemMessageUserHolder) holder).bind(message, CHAT_TYPE);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        LiveMessage message = mMessages.get(position);
        if (message != null) {
            if (Objects.equals(message.sender_id, userId)) {
                return VIEW_TYPE_USER_MESSAGE;
            } else {
                if (Objects.equals(message.sender_id, frndId)) {
                    return VIEW_TYPE_FRIEND_MESSAGE;
                } else {
                    return VIEW_TYPE_ADMIN_MESSAGE;
                }
            }
        }
        return -2;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    private void setImageAvatar(String imgBase64, CircleImageView imageView) {
        try {
            if (Objects.equals(imgBase64, STR_DEFAULT_BASE64)) {
                Picasso.get()
                        .load(R.drawable.user_100px)
                        .into(imageView);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFontFriend(EmojiTextView textView) {
        Typeface typeface = ResourcesCompat.getFont(context, R.font.comfortaa_bold);

        textView.setTypeface(typeface);
        textView.setTextSize(17);
        textView.setEmojiSize(17 + 5);
        textView.setTextColor(ColorStateList.valueOf(0xFF0288D1));
    }

    private void setFontUser(EmojiTextView textView) {
        Typeface typeface = ResourcesCompat.getFont(context, R.font.comfortaa_bold);

        textView.setTypeface(typeface);
        textView.setTextSize(17);
        textView.setEmojiSize(17 + 5);
        textView.setTextColor(ColorStateList.valueOf(0xFFFFFFFF));
    }

    class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
        EmojiTextView txtContent;
        CircleImageView avata;
        TextView _name;

        ItemMessageFriendHolder(View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.textContentFriend);
            avata = itemView.findViewById(R.id.friend_image);
            _name = itemView.findViewById(R.id.friend_name);
        }

        void bind(final Context context, final LiveMessage message, int chat_type) {
            if (message != null) {
//                setFontFriend(txtContent);
                if(chat_type != 0 && _name != null) {
                    _name.setText(message.name);
                }
                txtContent.setText(message.text);
                setImageAvatar(message.avata, avata);
            }

        }
    }

    class ItemMessageUserHolder extends RecyclerView.ViewHolder {
        EmojiTextView txtContent;
        TextView _name;
        CircleImageView avata;

        ItemMessageUserHolder(View itemView) {
            super(itemView);
            txtContent = itemView.findViewById(R.id.textContentFriend);
            _name = itemView.findViewById(R.id.user_name);
            avata = itemView.findViewById(R.id.user_image);
        }

        void bind(LiveMessage message, int chat_type) {
            if (message != null) {
//                setFontUser(txtContent);
                if(chat_type != 0 && _name != null && avata != null) {
                    _name.setText(R.string.you);
                    setImageAvatar(message.avata, avata);
                }
                txtContent.setText(message.text);
            }
        }
    }

    class ItemMessageAdminHolder extends RecyclerView.ViewHolder {
        CircleImageView _avata;
        ImageView _emoji;
        View _emoji_box,_parent;
        TextView _name, _phone;

        ItemMessageAdminHolder(@NonNull View itemView) {
            super(itemView);
            _parent = itemView.findViewById(R.id.fb_parent);
            _avata = itemView.findViewById(R.id.fb_avata);
            _emoji = itemView.findViewById(R.id.fb_emoji_status_img);
            _emoji_box = itemView.findViewById(R.id.fb_emoji_status_img_box);
            _name = itemView.findViewById(R.id.fb_name);
            _phone = itemView.findViewById(R.id.fb_phone);
        }

        void bind(final LiveMessage message) {
            Friend friend = FriendDB.getInstance(context).getFriend(frndId);
            if (friend != null && friend.name != null && message != null && CHAT_TYPE == 0) {
                setImageAvatar(friend.avata, _avata);
                if(friend.emoji != null && !friend.emoji.equals(STR_DEFAULT_BASE64)) {
                    Picasso.get()
                            .load("file:///android_asset/emoji/" + friend.emoji)
                            .into(_emoji);
                    _emoji_box.setVisibility(View.VISIBLE);
                }else{
                    _emoji_box.setVisibility(View.GONE);
                }
                _name.setText(friend.name);
                _phone.setText(friend.phone);
                _parent.setVisibility(View.VISIBLE);
            }else {
                _parent.setVisibility(View.GONE);
            }
        }
    }
}
