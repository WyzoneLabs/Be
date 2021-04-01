package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import database.FriendDB;
import database.RecentChatsDB;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Chat;
import utils.Configs;
import utils.DateUtils;
import utils.FriendsFilter;
import xyz.schwaab.avvylib.AvatarView;

import static services.ServiceUtils.isNetworkConnected;
import static utils.Configs.STR_DEFAULT_BASE64;
import static utils.Configs.TIME_TO_OFFLINE;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	public static final int VIEW_NO_DATA = 0;
	public static final int VIEW_RECENT_CHATS = 1;
	public static final int VIEW_CHAT_REQUEST = 2;

	public ArrayList<Chat> mChats, filterList;
	private FriendsFilter filter;
	private Context selfRef;
	private OnChatsItemsInteraction onChatsItemsInteraction;

	private int pageView = VIEW_RECENT_CHATS;

	public ChatsAdapter(Context selfRef, OnChatsItemsInteraction itemsInteraction) {
		this.selfRef = selfRef;
		this.mChats = new ArrayList<>();
		this.filterList = new ArrayList<>();
		this.onChatsItemsInteraction = itemsInteraction;
	}

	public void setPageView(int pageView) {
		this.pageView = pageView;
		notifyDataSetChanged();
	}

	public void setChats(ArrayList<Chat> chats) {
		this.mChats = chats;
		this.filterList = chats;
		notifyDataSetChanged();
	}

	public void setChats() {
		this.mChats = RecentChatsDB.getInstance(selfRef).getListResents(selfRef);
		this.filterList = mChats;
		notifyDataSetChanged();
	}

	public void setFilterList(ArrayList<Chat> filterList) {
		this.filterList = filterList;
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (getItemViewType(position) == VIEW_RECENT_CHATS) {
			((ChatViewHolder)holder).bind(mChats.get(position));
		}else if (getItemViewType(position) == VIEW_CHAT_REQUEST){
			((RequestsViewHolder)holder).bind(mChats.get(position));
		}else if (getItemViewType(position) == VIEW_NO_DATA){
			((NoDataViewHolder)holder).bind();
		}
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch (viewType) {
			case VIEW_NO_DATA:
				View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_data_list, parent, false);
				return new NoDataViewHolder(view1);
			case VIEW_RECENT_CHATS: {
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list, parent, false);
				return new ChatViewHolder(view);
			}
			case VIEW_CHAT_REQUEST: {
				View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_request, parent, false);
				return new RequestsViewHolder(view2);
			}
			default:{
				return null;
			}
		}
	}

	@Override
	public int getItemCount() {
		return mChats != null?(mChats.size() == 0 ? 1 : mChats.size()):1;
	}

	@Override
	public int getItemViewType(int position) {
		return mChats == null || mChats.size() == 0 ? VIEW_NO_DATA : pageView;
	}

	public Filter getFilter() {
		if (filter == null) {
			filter = new FriendsFilter(filterList, this);
		}
		return filter;
	}

	public interface OnChatsItemsInteraction {
		void onChatItemClickListener(Chat chat, int position);
	}

	class WindowTouchListener implements View.OnTouchListener {
		int x, y;
		int touchedX, touchedY;
		private ConstraintLayout _live_cont,_parent;
		private final ConstraintLayout.LayoutParams lParams;
		private final ConstraintLayout.LayoutParams updateLayoutParams;
		private final DatabaseReference mLiveRef;

		WindowTouchListener(ConstraintLayout _live_cont,ConstraintLayout _parent, DatabaseReference liveRef) {
			this._live_cont = _live_cont;
			this._parent = _parent;
			this.mLiveRef = liveRef;
			lParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
			updateLayoutParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();
			touchedX = (int) event.getRawX();
			touchedY = (int) event.getRawY();
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					x = touchedX - updateLayoutParams.leftMargin;
					return true;
				case MotionEvent.ACTION_MOVE:
					boolean motion_left = updateLayoutParams.leftMargin < 0;
					updateLayoutParams.resolveLayoutDirection(LayoutDirection.LTR);
					updateLayoutParams.rightMargin = -(touchedX - x);
					updateLayoutParams.leftMargin = touchedX - x;
					if (!motion_left) {
						_live_cont.setLayoutParams(updateLayoutParams);
						if (touchedX - x >= selfRef.getResources().getConfiguration().screenWidthDp/2) {
//							mLiveRef.child("rejected").setValue(true);
//                            mLiveRef.removeValue();
							_parent.setVisibility(View.GONE);
						}
					}
					return true;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					lParams.leftMargin = 0;
					lParams.rightMargin = 0;
					_live_cont.setLayoutParams(lParams);
					return true;
			}
			return false;
		}

	}

	class RequestsViewHolder extends RecyclerView.ViewHolder {
		ConstraintLayout _body,_parent;
		TextView _username, _message, _timestamp;
		CircleImageView _avata;
		ImageView _status;

		RequestsViewHolder(@NonNull View itemView) {
			super(itemView);
			_parent = itemView.findViewById(R.id.parent);
			_body = itemView.findViewById(R.id.cfl_body);
			_username = itemView.findViewById(R.id.cfl_user_name);
			_message = itemView.findViewById(R.id.cfl_text);
			_timestamp = itemView.findViewById(R.id.cfl_time);
			_avata = itemView.findViewById(R.id.cfl_avata);
			_status = itemView.findViewById(R.id.cl_online_status);
		}

		@SuppressLint("ClickableViewAccessibility")
		void bind(Chat chat){
			int position = getAdapterPosition();

			if (Objects.equals(chat.friend.avata, STR_DEFAULT_BASE64)) {
				Picasso.get()
						.load(R.drawable.user_100px)
						.into(_avata);
			} else {
				byte[] decodedString = Base64.decode(chat
						.friend.avata, Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				_avata.setImageBitmap(bitmap);
			}

			boolean is_online = chat.friend.status != null && (chat.friend.status.is_online &&
					DateUtils.getTimePassed(chat.friend.status.timestamp) < Configs.TIME_TO_OFFLINE);
			_status.setVisibility(is_online ? View.VISIBLE : View.GONE);

			_timestamp.setText(DateUtils.formatDateTime(chat.timestamp));
			_message.setText(chat.message);
			_username.setText(chat.friend.name);

//			_body.setOnTouchListener(new WindowTouchListener(_body, _parent,null));
		}
	}

	class ChatViewHolder extends RecyclerView.ViewHolder {
		View _parent, _sep;
		TextView _username, _message, _timestamp;
		CircleImageView _avata;
		ImageView _status,_emoji;
		View _emoji_box;

		ChatViewHolder(@NonNull View itemView) {
			super(itemView);
			_parent = itemView.findViewById(R.id.cfl_parent);
			_sep = itemView.findViewById(R.id.cfl_sep);
			_username = itemView.findViewById(R.id.cfl_user_name);
			_message = itemView.findViewById(R.id.cfl_text);
			_timestamp = itemView.findViewById(R.id.cfl_time);
			_avata = itemView.findViewById(R.id.cfl_avata);
			_status = itemView.findViewById(R.id.cl_online_status);
			_emoji_box = itemView.findViewById(R.id.fv_emoji_status_img_box);
			_emoji = itemView.findViewById(R.id.fv_emoji_status_img);
		}

		void bind(Chat chat){
//			_avata.setAnimating(false);
//			_avata.setShouldBounceOnClick(true);
//			_avata.setBorderThickness(1);
//			_avata.setHighlightedBorderThickness(4);
//			_avata.setHighlightBorderColor(selfRef.getResources().getColor(R.color.colorAccent, null));
//			_avata.setHighlightBorderColorEnd(selfRef.getResources().getColor(R.color.colorGreen, null));
//			_avata.setNumberOfArches(3);
//			_avata.setBorderColor(Color.TRANSPARENT );
//			_avata.setDistanceToBorder(4);
//			_avata.setHighlighted(true);
//			_avata.setTotalArchesDegreeArea(60);

			int position = getAdapterPosition();

			if (position == 0) {
				_parent.setBackgroundResource(R.drawable.chat_list_top_background);
			} else if (position == mChats.size() - 1) {
				_parent.setBackgroundResource(R.drawable.chat_list_bottom_background);
			} else {
				_parent.setBackgroundResource(R.drawable.chat_list_medium_background);
			}

			if (Objects.equals(chat.friend.avata, STR_DEFAULT_BASE64)) {
				Picasso.get()
						.load(R.drawable.user_100px)
						.into(_avata);
			} else {
				byte[] decodedString = Base64.decode(chat
						.friend.avata, Base64.DEFAULT);
				Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				_avata.setImageBitmap(bitmap);
			}

			boolean is_online = chat.friend.status != null && (chat.friend.status.is_online &&
					DateUtils.getTimePassed(chat.friend.status.timestamp) < Configs.TIME_TO_OFFLINE);
			_status.setVisibility(is_online ? View.VISIBLE : View.GONE);

			if (chat.friend.emoji != null && !chat.friend.emoji.equals(STR_DEFAULT_BASE64)) {
				Picasso.get()
						.load("file:///android_asset/emoji/" + chat.friend.emoji)
						.into(_emoji);
				_emoji_box.setVisibility(View.VISIBLE);
			} else {
				_emoji_box.setVisibility(View.GONE);
			}

			_timestamp.setText(DateUtils.formatDateTime(chat.timestamp));
			_message.setText(chat.message);
			_username.setText(chat.friend.name);
			_sep.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);

			_parent.setOnClickListener(v -> onChatsItemsInteraction.onChatItemClickListener(chat,
					position));
		}
	}

	static class NoDataViewHolder extends RecyclerView.ViewHolder {
		NoDataViewHolder(@NonNull View itemView) {
			super(itemView);
		}

		void bind(){}
	}

	public void updateFriendStatus(Context context, Chat chat) {
		if (isNetworkConnected(context) && chat != null && chat.friend != null && chat.friend.id != null) {
			final String fid = chat.friend.id;
			FirebaseDatabase.getInstance().getReference()
					.child(Configs.DATABASE_USERS_ROOT_NAME)
					.child(fid + "/status").addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
					if (dataSnapshot.getValue() != null) {
						HashMap mapStatus = (HashMap) dataSnapshot.getValue();
						if ((boolean) mapStatus.get("is_online") && DateUtils.getTimePassed((long) mapStatus.get("timestamp")) < TIME_TO_OFFLINE) {
							chat.friend.status.is_online = (boolean) mapStatus.get("is_online");
							chat.friend.status.timestamp = (long) mapStatus.get("timestamp");

							FriendDB.getInstance(context).updateLastSeen(
									chat.friend.id,
									(long) mapStatus.get("timestamp"),
									((boolean) mapStatus.get("is_online")?1:0)
							);
							ChatsAdapter.this.notifyDataSetChanged();
						}
					}
				}

				@Override
				public void onCancelled(@NotNull DatabaseError databaseError) {

				}
			});
		}
	}

}