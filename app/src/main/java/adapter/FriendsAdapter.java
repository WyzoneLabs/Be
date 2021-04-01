package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import database.FriendDB;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Friend;
import utils.Configs;
import utils.DateUtils;
import utils.FriendsFilter;
import xyz.schwaab.avvylib.AvatarView;

import static utils.Configs.COLORS;
import static utils.Configs.STR_DEFAULT_BASE64;
import static utils.Configs.STR_DEFAULT_ICON;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ADVANCED_LIST_FRIENDS = 3;
    public static final int DETAILED_LIST_FRIENDS = 4;
    public static final int ADD_FRIENDS = 1;
    public static final int LIST_FRIENDS = 2;
    private final Context context;
    private boolean details_disabled = false;
    private OnContactsInteraction onContactsInteraction;
    public List<Friend> mFriends, filterList;
    private OnFriendsInteraction onFriendsInteraction;
    private FriendsFilter filter;
    private int type = 0;

    public FriendsAdapter(Context context) {
        this.context = context;
        mFriends = FriendDB.getInstance(context).getListFriend();
        filterList = mFriends;
//		mFriends.add(new Friend());
    }

    public interface OnContactsInteraction{
        void onContactClickListener(Friend friend, int i);
    }

    public void setOnContactsInteraction(OnContactsInteraction onContactsInteraction) {
        this.onContactsInteraction = onContactsInteraction;
    }

    public void disableDetailsMenu(boolean disabled) {
        this.details_disabled = disabled;
        notifyDataSetChanged();
    }

    public void setType(int type) {
        this.type = type;
        notifyDataSetChanged();
    }

    public List<Friend> getFriends() {
        return mFriends;
    }

    public void setFriends(List<Friend> mFriends) {
        this.mFriends = mFriends;
        this.filterList = mFriends;
        notifyDataSetChanged();
    }

    public void refresh() {
        this.mFriends = FriendDB.getInstance(context).getListFriend();
        this.filterList = mFriends;
        notifyDataSetChanged();
    }

    public void setOnFriendsInteraction(OnFriendsInteraction onFriendsInteraction) {
        this.onFriendsInteraction = onFriendsInteraction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case LIST_FRIENDS:
                View friendView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_view, parent, false);
                return new ListFriendsViewHolder(friendView);
            case ADVANCED_LIST_FRIENDS:
                View viewAdvanced = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_view_advanced, parent, false);
                return new AdvancedViewHolder(viewAdvanced);
            case DETAILED_LIST_FRIENDS:
                View viewDetailed = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_view_detailed, parent, false);
                return new DetailedViewHolder(viewDetailed);
            case ADD_FRIENDS:
                View addView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friends_view, parent, false);
                return new AddFriendsViewHolder(addView);
            default:
                return null;
        }
    }

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case LIST_FRIENDS:
                if (position > 0) {
                    Friend friend = mFriends.get(position - 1);
                    ((ListFriendsViewHolder) holder).bind(context, friend);
                }
                break;
            case ADVANCED_LIST_FRIENDS:
                Friend friend = mFriends.get(position);
                ((AdvancedViewHolder) holder).bind(friend);
                break;
            case DETAILED_LIST_FRIENDS:
                ((DetailedViewHolder) holder).bind(context,mFriends);
                break;
            case ADD_FRIENDS:
                ((AddFriendsViewHolder) holder).bind();
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (type == 0) {
            return mFriends != null ? mFriends.size() + 1 : 1;
        } else {
            return mFriends.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (type == 0) {
            return position == 0 ? ADD_FRIENDS : LIST_FRIENDS;
        } else {
            return type;
        }
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new FriendsFilter(filterList, this);
        }
        return filter;
    }

    public interface OnFriendsInteraction {
        void onAddFriendClickListener();
        void onFriendClickListener(Friend friend, int i);
    }

    class ListFriendsViewHolder extends RecyclerView.ViewHolder {
        AvatarView _avata;
        ImageView _status,_emoji;
        TextView _name,_initial;
        View _emoji_box;

        public ListFriendsViewHolder(@NonNull View friendView) {
            super(friendView);
            _avata = friendView.findViewById(R.id.fv_avata);
            _initial = friendView.findViewById(R.id.fv_initial);
            _status = friendView.findViewById(R.id.fv_online_status);
            _emoji_box = friendView.findViewById(R.id.fv_emoji_status_img_box);
            _emoji = friendView.findViewById(R.id.fv_emoji_status_img);
            _name = friendView.findViewById(R.id.fv_user_name);
        }

        void bind(Context context, Friend friend) {
            boolean is_online = friend.status != null && (friend.status.is_online &&
                    DateUtils.getTimePassed(friend.status.timestamp) <= Configs.TIME_TO_OFFLINE);
//            _avata.setAnimating(is_online);
            _status.setVisibility(is_online?View.VISIBLE:View.GONE);
            _avata.setShouldBounceOnClick(true);
            _avata.setBorderThickness(1);
            _avata.setHighlightedBorderThickness(4);
            _avata.setHighlightBorderColor(context.getResources().getColor(R.color.colorAccent, null));
            _avata.setHighlightBorderColorEnd(context.getResources().getColor(R.color.colorGreen, null));
            _avata.setNumberOfArches(3);
            _avata.setBorderColor(Color.TRANSPARENT);
            _avata.setDistanceToBorder(4);
            _avata.setHighlighted(true);
            _avata.setTotalArchesDegreeArea(60);

            int randColor = COLORS[new Random().nextInt(COLORS.length)];
            int sec = friend.name.indexOf(" ") + 1;
            String s = (friend.name.charAt(0) + (sec  > 0 && sec < friend.name.length()? friend.name.charAt(sec) + "" : "")).toUpperCase();

            if (Objects.equals(friend.avata, STR_DEFAULT_BASE64) || friend.avata == null) {
                _initial.getBackground()
                        .setColorFilter(context.getResources().getColor(randColor,null), PorterDuff.Mode.SRC_IN);
                _initial.setText(s);
                _initial.setVisibility(View.VISIBLE);
                _avata.setVisibility(View.INVISIBLE);
            } else {
                byte[] decodedString = Base64.decode(friend.avata, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                _avata.setImageBitmap(bitmap);

                _initial.setVisibility(View.GONE);
                _avata.setVisibility(View.VISIBLE);
            }

            if(friend.emoji != null && !friend.emoji.equals(STR_DEFAULT_BASE64)) {
                Picasso.get()
                        .load("file:///android_asset/emoji/" + friend.emoji)
                        .into(_emoji);
                _emoji_box.setVisibility(View.VISIBLE);
            }else{
                _emoji_box.setVisibility(View.GONE);
            }

            _name.setText(friend.name);
            _initial.setOnClickListener(v -> {
                if (onFriendsInteraction != null)
                    onFriendsInteraction.onFriendClickListener(friend, getAdapterPosition());
            });
            _avata.setOnClickListener(v -> {
                if (onFriendsInteraction != null)
                    onFriendsInteraction.onFriendClickListener(friend, getAdapterPosition());
            });
        }
    }

    class AdvancedViewHolder extends RecyclerView.ViewHolder {
        TextView _friend_name, _friend_group_init;
        EmojiTextView _friend_online_status;
        CircleImageView _friend_avata;
        View _seperator, _container, _padding;

        public AdvancedViewHolder(@NonNull View itemView) {
            super(itemView);
            _friend_group_init = itemView.findViewById(R.id.rc_group_txt);
            _padding = itemView.findViewById(R.id.rc_start_padding);
            _friend_avata = itemView.findViewById(R.id.friend_avata);
            _friend_name = itemView.findViewById(R.id.friend_name);
            _friend_online_status = itemView.findViewById(R.id.friend_online_status);
            _seperator = itemView.findViewById(R.id.rc_sep);
            _container = itemView.findViewById(R.id.friends_cont);
        }

        void bind(Friend friend) {
            _friend_name.setText(friend.name);
            _friend_online_status.setText(friend.phone);

            if (getAdapterPosition() == 0) {
                _seperator.setVisibility(View.INVISIBLE);
                _padding.setVisibility(View.VISIBLE);
            } else {
                _padding.setVisibility(View.GONE);
            }

            int sec = friend.name.indexOf(" ") + 1;
            String s = (friend.name.charAt(0) + (sec > 0 && sec < friend.name.length() ? friend.name.charAt(sec) + "" : "")).toUpperCase();
            _friend_group_init.setText(String.valueOf(s.charAt(0)));
            if (getAdapterPosition() == 0) {
                _friend_group_init.setVisibility(View.VISIBLE);
            } else {
                if (Objects.equals(String.valueOf(mFriends.get(getAdapterPosition() - 1).name.charAt(0)).toUpperCase(),
                        String.valueOf(mFriends.get(getAdapterPosition()).name.charAt(0)).toUpperCase())) {
                    _friend_group_init.setVisibility(View.INVISIBLE);
                } else {
                    _friend_group_init.setVisibility(View.VISIBLE);
                }
            }

            if (Objects.equals(friend.avata, STR_DEFAULT_BASE64) || friend.avata == null) {
                Picasso.get()
                        .load(R.drawable.user_100px)
                        .into(_friend_avata);
            } else {
                byte[] decodedString = Base64.decode(friend.avata, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                _friend_avata.setImageBitmap(bitmap);
            }

            _container.setOnClickListener(v -> {
                if (onFriendsInteraction != null)
                    onFriendsInteraction.onFriendClickListener(friend, getAdapterPosition());
            });
        }
    }

    class DetailedViewHolder extends RecyclerView.ViewHolder {
        TextView _friend_initial, _friend_name, _friend_group_init;
        TextView _friend_online_status;
        CircleImageView _friend_avata;
        View _seperator, _container, _padding;
        ImageView _more_details,_emoji;
        View _emoji_box;

        public DetailedViewHolder(@NonNull View itemView) {
            super(itemView);
            _friend_group_init = itemView.findViewById(R.id.rc_group_txt);
            _padding = itemView.findViewById(R.id.rc_start_padding);
            _friend_avata = itemView.findViewById(R.id.friend_avata);
            _friend_initial = itemView.findViewById(R.id.friend_initial);
            _friend_name = itemView.findViewById(R.id.friend_name);
            _friend_online_status = itemView.findViewById(R.id.friend_online_status);
            _seperator = itemView.findViewById(R.id.rc_sep);
            _container = itemView.findViewById(R.id.friends_box);
            _more_details = itemView.findViewById(R.id.more_details);
            _emoji_box = itemView.findViewById(R.id.fv_emoji_status_img_box);
            _emoji = itemView.findViewById(R.id.fv_emoji_status_img);
        }

        void bind(Context context, List<Friend> friends){
            Friend friend = friends.get(getAdapterPosition());
            _friend_name.setText(friend.name);
            _friend_online_status.setText(friend.phone);

            if (getAdapterPosition() == 0) {
                _seperator.setVisibility(View.INVISIBLE);
                _padding.setVisibility(View.GONE);
            }else {
                _padding.setVisibility(View.GONE);
            }
            _more_details.setVisibility(details_disabled?View.GONE:View.VISIBLE);

            int randColor = COLORS[new Random().nextInt(COLORS.length)];
            int sec = friend.name.indexOf(" ") + 1;
            String s = (friend.name.charAt(0) + (sec  > 0 && sec < friend.name.length()? friend.name.charAt(sec) + "" : "")).toUpperCase();
            _friend_group_init.setText(String.valueOf(s.charAt(0)));
            if (getAdapterPosition() == 0){
                _friend_group_init.setVisibility(View.VISIBLE);
            }else {
                if (Objects.equals(String.valueOf(friends.get(getAdapterPosition() - 1).name.charAt(0)).toUpperCase() ,
                        String.valueOf(friends.get(getAdapterPosition()).name.charAt(0)).toUpperCase())){
                    _friend_group_init.setVisibility(View.INVISIBLE);
                }else {
                    _friend_group_init.setVisibility(View.VISIBLE);
                }
            }

            if (Objects.equals(friend.avata, STR_DEFAULT_BASE64)) {
                _friend_initial.getBackground()
                        .setColorFilter(context.getResources().getColor(randColor, null), PorterDuff.Mode.SRC_IN);
                _friend_initial.setText(s);
                _friend_initial.setVisibility(View.VISIBLE);
                _friend_avata.setVisibility(View.GONE);
            } else {
                byte[] decodedString = Base64.decode(friend.avata, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                _friend_avata.setImageBitmap(bitmap);

                _friend_initial.setVisibility(View.INVISIBLE);
                _friend_avata.setVisibility(View.VISIBLE);
            }

            if(friend.emoji != null && !friend.emoji.equals(STR_DEFAULT_BASE64)) {
                Picasso.get()
                        .load("file:///android_asset/emoji/" + friend.emoji)
                        .into(_emoji);
                _emoji_box.setVisibility(View.VISIBLE);
            }else{
                _emoji_box.setVisibility(View.GONE);
            }
            _container.setOnClickListener(v -> {
                if (onContactsInteraction != null)onContactsInteraction.onContactClickListener(friend, getAdapterPosition());
            });
        }
    }

    class AddFriendsViewHolder extends RecyclerView.ViewHolder {
        AvatarView _avata;

        public AddFriendsViewHolder(@NonNull View addView) {
            super(addView);
            _avata = addView.findViewById(R.id.af_avata);
        }

        void bind() {
            _avata.setAnimating(false);
            _avata.setShouldBounceOnClick(true);

            _avata.setOnClickListener(view -> {
                if (onFriendsInteraction != null) onFriendsInteraction.onAddFriendClickListener();
            });
        }
    }

}
