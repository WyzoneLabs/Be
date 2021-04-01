package com.brimbay.be;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.databinding.ActivityChatBinding;
import com.brimbay.be.databinding.ActivityMainBinding;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.one.EmojiOneProvider;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.LiveChatAdapter;
import database.RecentChatsDB;
import database.UserDB;
import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import model.Chat;
import model.ChatRequest;
import model.Friend;
import model.LiveMessage;
import model.SessionState;
import model.User;
import ui.BeinSwitch;
import utils.Configs;
import utils.LiveChatTools;
import utils.PermissionUtil;
import utils.PreferenceHelper;
import utils.Tools;
import utils.ViewsUtil;
import xyz.schwaab.avvylib.AvatarView;

import static utils.Configs.STR_DEFAULT_BASE64;
import static utils.Configs.sEmojiRegex;
import static utils.Tools.playMedia;

public class ChatActivity extends AppCompatActivity  implements BeinSwitch.CheckedChangeListener {
	private ChatActivity selfRef;
	private static final String TAG = "Brim.ChatActivity";

	//Static vars
	public static final String ACTION_PRIVATE_CHAT_DATA = "com.brimbay.be.ACTION_CHAT_FRIEND";
	public static final String INTENT_EXTRA_CHAT_FRIEND = "com.brimbay.be.CHAT_FRIEND";
	public static final String INTENT_EXTRA_CHAT_REQUEST_ACCEPT = "com.brimbay.be.CHAT_REQUEST_ACCEPT";
	private static final String EXTRA_FRIEND = "com.brimbay.be.EXTRA_FRIEND";
	private static final int PERMISSION_REQ_ID = 22;
	// permission WRITE_EXTERNAL_STORAGE is not mandatory for Agora RTC SDK, just in case if you wanna save logs to external sdcard
	private static final String[] REQUESTED_PERMISSIONS = {
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	//region Views
	private RelativeLayout _particles;
	private RecyclerView _recycler_chats, _recycler_vid_chats;
	private View _send_btn,_typing_box, _vid_chat_box;
	private EmojiEditText _message_field;
	private BeinSwitch _beinswitch;
	private ImageView _emoji;
	private View _parent_view,_live_btn_accept,_emoji_box, _live_cont/*,_user_info_box*/;
	private CircleImageView _friend_avata;
	private AvatarView _typing_avata,_vid_request_avata;
	private TextView _title_username/*,_username,*/,_live_text_info;
	//endregion

	//Firebase
	private FirebaseAuth mAuth;
	private DatabaseReference mMessageDbRef = null;

	//region Member Vars
	private ActivityChatBinding binding;
	private Friend mFriend;
	private LiveChatTools mLiveChatTools;
	private User mUser;
	private EmojiPopup mEmojiPopup;
	private PreferenceHelper preferenceHelper;
	private ArrayList<LiveMessage> mMessages;
	private LinearLayoutManager mLinearLayoutManager, mLinearLayoutManager2;
	private LiveChatAdapter mAdapter;

	private RtcEngine mRtcEngine;
	private final long mUptime = 45000;
	private CountDownTimer mCountDownTimer;
	private boolean isAcceptingCall = false;
	private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
		@Override
		public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
			runOnUiThread(() -> setupRemoteVideo(uid));
		}

		@Override
		public void onUserOffline(int uid, int reason) {
			runOnUiThread(() -> onRemoteUserLeft());
		}

		@Override
		public void onUserMuteVideo(final int uid, final boolean muted) {
			runOnUiThread(() -> onRemoteUserVideoMuted(uid, muted));
		}
	};
	//endregion

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selfRef = this;
		// This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.
		EmojiManager.install(new EmojiOneProvider());
		binding = ActivityChatBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		initViews();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Activity data
		if (getIntent() != null && Objects.equals(getIntent().getAction(), ACTION_PRIVATE_CHAT_DATA)) {
			mFriend = getIntent().getParcelableExtra(INTENT_EXTRA_CHAT_FRIEND);
			if (getIntent().getStringExtra(INTENT_EXTRA_CHAT_REQUEST_ACCEPT) != null) {
				initChatSession(((Friend) Objects.requireNonNull(getIntent().getParcelableExtra(INTENT_EXTRA_CHAT_FRIEND))).room_id);
			}
		} else {
			if (savedInstanceState != null && savedInstanceState.getParcelable(EXTRA_FRIEND) != null) {
				mFriend = savedInstanceState.getParcelable(EXTRA_FRIEND);
			}
		}

		//Firebase
		mAuth = FirebaseAuth.getInstance();
		mMessageDbRef = FirebaseDatabase.getInstance().getReference()
				.child(Configs.DATABASE_CHATS_ROOT_NAME)
				.child(Configs.DATABASE_LIVE_CHATS_ROOT_NAME).child(mFriend.id);

		mLiveChatTools = new LiveChatTools(selfRef);
		mUser = UserDB.getInstance(selfRef).getFriend();
		preferenceHelper = PreferenceHelper.getInstance(selfRef);

		if (mFriend != null && mFriend.room_id != null){
			listenToChatSession( mFriend.room_id);
			setAvata(_friend_avata);
			setAvata(_typing_avata);
			setAvata(_vid_request_avata);
			_title_username.setText(mFriend.name);
//			_username.setText(mFriend.name);

			getLiveChatRequests(mFriend, _live_cont, _live_text_info, _live_btn_accept);

			Log.d(TAG, "Chat:" + isAcceptingCall);

			ProgressBar progressBar = findViewById(R.id.ft_progress);
			Sprite doubleBounce = new Wave();
			doubleBounce.setColor(getResources().getColor(R.color.colorAccent,null));
			progressBar.setIndeterminateDrawable(doubleBounce);

			setTypingIndicator(_message_field, mFriend.id, mFriend.room_id);
			getTypingIndicator(mFriend.room_id);

			if(mFriend.emoji != null && !mFriend.emoji.equals(STR_DEFAULT_BASE64)) {
				Picasso.get()
						.load("file:///android_asset/emoji/" + mFriend.emoji)
						.into(_emoji);
				_emoji_box.setVisibility(View.VISIBLE);
			}else{
				_emoji_box.setVisibility(View.GONE);
			}
		}

		//UI interaction [START]
		_message_field.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				_send_btn.setEnabled(s.toString().trim().length() > 0);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				_send_btn.setEnabled(s.toString().trim().length() > 0);
			}

			@Override
			public void afterTextChanged(Editable s) {
				_send_btn.setEnabled(s.toString().trim().length() > 0);
			}
		});
		_send_btn.setOnClickListener(this::CallClickInit);
		_message_field.setOnClickListener(v -> {
			final InputMethodManager inputMethodManager = (InputMethodManager) selfRef.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
		});
		_beinswitch.setCheckedChangeListener(this);
		//[END]

		handleMessagesList();

		mCountDownTimer = new CountDownTimer(mUptime, 2000) {
			@Override
			public void onTick(long millisUntilFinished) {
				if (millisUntilFinished <= 10000) {
                    ViewsUtil.showToast(selfRef,"Please send a text, else live chat is closing in " + millisUntilFinished / 1000 + "sec",0,R.color.colorOrange);
				}
			}
			@Override
			public void onFinish() {
                ViewsUtil.showToast(selfRef,"Closing live vid chat, inactivity for 45sec",0,R.color.colorAccent);
				leaveChannel();
			}
		};
	}

	//region Member Methods
	private void initViews() {
		_recycler_chats = findViewById(R.id.recyclerChat);
		_live_btn_accept = findViewById(R.id.live_accept_btn);
		_live_text_info = findViewById(R.id.live_not_text);
		_live_cont = findViewById(R.id.c_live_box);
		_emoji = findViewById(R.id.vm_emoji_status_img);
		_emoji_box = findViewById(R.id.vm_emoji_status_box);
//		_username = findViewById(R.id.vm_say_hi_user_name);
		_vid_chat_box = findViewById(R.id.vid_cat_box);
		_recycler_vid_chats =  findViewById(R.id.recyclerChatVideo);

		_typing_avata = findViewById(R.id.ft_image);
		_typing_box = findViewById(R.id.typing_cont);
//		_typing_initial = findViewById(R.id.ft_initial);
		_parent_view = findViewById(R.id.vm_parent);
//        _bottom_box = findViewById(R.id.vm_bottom_box);
		_beinswitch = findViewById(R.id.c_start_vid_cam);
//        _btn_emoji_add = findViewById(R.id.message_camera);
		_message_field = findViewById(R.id.vc_edit_msg);
		_send_btn = findViewById(R.id.vc_send_btn);
		mEmojiPopup = EmojiPopup.Builder.fromRootView(_parent_view).build(_message_field);
		_friend_avata = findViewById(R.id.vm_guest_avata);
		_title_username = findViewById(R.id.vm_username);
//		_user_info_box =  findViewById(R.id.vm_friend_box);
		_vid_request_avata = findViewById(R.id.vm_vid_req_avata);

		setAvataView(_vid_request_avata);
		setAvataView(_typing_avata);
	}
	
	private void setAvataView(AvatarView avataView){
		avataView.setAnimating(true);
		avataView.setShouldBounceOnClick(true);
		avataView.setBorderThickness(1);
		avataView.setHighlightedBorderThickness(4);
		avataView.setHighlightBorderColor(selfRef.getResources().getColor(R.color.colorAccent,null));
		avataView.setHighlightBorderColorEnd(selfRef.getResources().getColor(R.color.purple_500,null));
		avataView.setNumberOfArches(3);
		avataView.setBorderColor(Color.TRANSPARENT );
		avataView.setDistanceToBorder(4);
		avataView.setHighlighted(true);
		avataView.setTotalArchesDegreeArea(60);
	}

	private void initChatSession(String room_id) {
		FirebaseDatabase.getInstance().getReference()
				.child("Chat_Session")
				.child(room_id).child("room").setValue(SessionState.STARTED);

		FirebaseDatabase.getInstance().getReference()
				.child(Configs.DATABASE_CHAT_REQUEST_ROOT_NAME)
				.child(mFriend.id)
				.child("request").removeValue();
	}

	private void setAvata(CircleImageView _avata) {

		if (Objects.equals(mFriend.avata, STR_DEFAULT_BASE64)) {
			Picasso.get()
					.load(R.drawable.user_100px)
					.into(_avata);
		} else {
			byte[] decodedString = Base64.decode(mFriend.avata, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			_avata.setImageBitmap(bitmap);
		}
	}

	private void setAvata(AvatarView _avata) {

		if (Objects.equals(mFriend.avata, STR_DEFAULT_BASE64)) {
			Picasso.get()
					.load(R.drawable.user_100px)
					.into(_avata);
		} else {
			byte[] decodedString = Base64.decode(mFriend.avata, Base64.DEFAULT);
			Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
			_avata.setImageBitmap(bitmap);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@SuppressLint("StaticFieldLeak")
	public void CallClickInit(View v) {
		int id = v.getId();
		if (id == R.id.vc_send_btn) {
			final String content = _message_field.getText().toString().trim();
			if (!TextUtils.isEmpty(content)) {
//					if (_user_info_box.getVisibility() == View.VISIBLE)_user_info_box.setVisibility(View.GONE);

				new AsyncTask<String, Void, Void>() {
					@Override
					protected Void doInBackground(String... strings) {
						sendSimpleMessage(strings[0]);
						return null;
					}

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						_message_field.setText("");
					}

					@Override
					protected void onPostExecute(Void aVoid) {
						super.onPostExecute(aVoid);
						emojiFinder(content);
						mAdapter.notifyDataSetChanged();
						mLinearLayoutManager.scrollToPosition(mMessages.size() - 1);
						if (mAdapter != null && mAdapter.getChatType() != 0) {
							mLinearLayoutManager2.scrollToPosition(mMessages.size() - 1);
						}
					}
				}.execute(content);
			}
		} else if (id == R.id.vc_keyboard) {
			mEmojiPopup.toggle(); // Toggles visibility of the Popup.
			if (mEmojiPopup.isShowing()) {
				((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.keyboard_26px, null));
			} else {
				((ImageView) v).setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.happy_26px, null));
			}
		} else if (id == R.id.btn_end) {
			onBackPressed();
			//            case R.id.fab:
//
//                break;
//            case R.id.message_camera:
////                mFileType = FILE_TYPE.IMAGE;
////                new Handler(Looper.getMainLooper()).post(this::requestToSelectFile);
//
//                if (_ej_box.getVisibility() == View.GONE) {
//                    _ej_box.setVisibility(View.VISIBLE);
//                    Picasso.get()
//                            .load(R.drawable.heart__filled_24px)
//                            .into((ImageView) findViewById(R.id.message_camera));
//                } else {
//                    _ej_box.setVisibility(View.GONE);
//                    Picasso.get()
//                            .load(R.drawable.heart_outline_24px)
//                            .into((ImageView) findViewById(R.id.message_camera));
//                }
//                break;
//
//            case R.id.ej_kiss:
//                _message_field.append("\uD83D\uDE18");
//                _ej_box.setVisibility(View.GONE);
//                Picasso.get()
//                        .load(R.drawable.heart_outline_24px)
//                        .into((ImageView) findViewById(R.id.message_camera));
//                break;
//            case R.id.ej_heart:
//                _message_field.append("♥");
//                _ej_box.setVisibility(View.GONE);
//                Picasso.get()
//                        .load(R.drawable.heart_outline_24px)
//                        .into((ImageView) findViewById(R.id.message_camera));
//                break;
//            case R.id.ej_two_heart:
//                _message_field.append("\uD83D\uDC95");
//                _ej_box.setVisibility(View.GONE);
//                Picasso.get()
//                        .load(R.drawable.heart_outline_24px)
//                        .into((ImageView) findViewById(R.id.message_camera));
//                break;
//            case R.id.ej_arrow_heart:
//                _message_field.append("\uD83D\uDC98");
//                _ej_box.setVisibility(View.GONE);
//                Picasso.get()
//                        .load(R.drawable.heart_outline_24px)
//                        .into((ImageView) findViewById(R.id.message_camera));
//                break;
//            case R.id.ej_eye_heart:
//                _message_field.append("\uD83D\uDE0D");
//                _ej_box.setVisibility(View.GONE);
//                Picasso.get()
//                        .load(R.drawable.heart_outline_24px)
//                        .into((ImageView) findViewById(R.id.message_camera));
//                break;
		}
	}

	private void endChatSession(String room_id) {
		DatabaseReference dRef = FirebaseDatabase.getInstance().getReference()
				.child("Chat_Session")
				.child(room_id).child("room");
		dRef.setValue(SessionState.ENDED);

		new Handler(getMainLooper()).postDelayed(dRef::removeValue, 1000);
		_beinswitch.setChecked(BeinSwitch.Checked.LEFT);
	}

	private void listenToChatSession(String room_id) {
		FirebaseDatabase.getInstance().getReference()
				.child("Chat_Session")
				.child(room_id).child("room").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.getValue() != null) {

					if ((long) dataSnapshot.getValue() == SessionState.ENDED) {
						ViewsUtil.showToast(selfRef,"Chat session ended",0,R.color.colorGreen);
//                        MessagesDB.getInstance(selfRef).deleteRoomMessage(mFriend.room_id);
						startActivity(new Intent(selfRef, MainActivity.class));
//						onBackPressed();
						finish();
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private void handleMessagesList() {
		mMessages = new ArrayList<>();

		mLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
		_recycler_chats.setLayoutManager(mLinearLayoutManager);
		mAdapter = new LiveChatAdapter(selfRef, mMessages, mAuth.getUid(), mFriend.id);
		_recycler_chats.setAdapter(mAdapter);

		mLinearLayoutManager2 = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
		_recycler_vid_chats.setLayoutManager(mLinearLayoutManager2);
		_recycler_vid_chats.setAdapter(mAdapter);

		setAdminMessage();

		databaseMessageQuery();
	}

	private void changeView(boolean is_vid_chat,boolean video_connected){
		_vid_chat_box.setVisibility(is_vid_chat && video_connected?View.VISIBLE:View.GONE);
		_recycler_chats.setVisibility(is_vid_chat && video_connected?View.GONE:View.VISIBLE);
		mAdapter.setChatType(is_vid_chat && video_connected?1:0);
		mAdapter.notifyDataSetChanged();
	}

	private void databaseMessageQuery() {
//        if (mLiveChannel == null || mLiveChannel.channel_id == null) return;
		FirebaseDatabase.getInstance().getReference()
				.child(Configs.DATABASE_CHATS_ROOT_NAME)
				.child(Configs.DATABASE_LIVE_CHATS_ROOT_NAME).child(mAuth.getUid()).addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				if (dataSnapshot.getValue() != null) {

					if (dataSnapshot.getValue(LiveMessage.class) != null &&
							!Objects.equals(Objects.requireNonNull(dataSnapshot.getValue(LiveMessage.class)).sender_id, mAuth.getUid())) {
						LiveMessage liveMessage = dataSnapshot.getValue(LiveMessage.class);
						if (liveMessage == null || liveMessage.message_id == null) return;

						if (preferenceHelper.getNotificationsRingtone() &&
								preferenceHelper.getNotificationsInChatSound()) {
							playMedia(selfRef, R.raw.in_ringtone);
						}
						mMessages.add(liveMessage);
						mAdapter.notifyDataSetChanged();
						mLinearLayoutManager.scrollToPosition(mMessages.size() - 1);
						if (mAdapter != null && mAdapter.getChatType() != 0) {
							mLinearLayoutManager2.scrollToPosition(mMessages.size() - 1);
						}

						emojiFinder(liveMessage.text);
						Chat chat = new Chat();
						chat.friend = mFriend;
						chat.message = liveMessage.text;
						chat.timestamp = liveMessage.timestamp;


						long k = RecentChatsDB.getInstance(selfRef).addResentChat(selfRef, chat);

//						if (_user_info_box.getVisibility() == View.VISIBLE)_user_info_box.setVisibility(View.GONE);


						//Remove read messages
						dataSnapshot.getRef().removeValue();
					}
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void getLiveChatRequests(Friend friend, View v_parent, TextView v_info, View v_accept) {
//        if (mAuth == null || mAuth.getUid() == null) return;
		ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams) v_parent.getLayoutParams();
		lParams.rightMargin = 0;
		lParams.leftMargin = 0;
		FirebaseDatabase.getInstance().getReference()
				.child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
				.child(/*"FKNLZC54Wea9ie54V6v2zyZBf9J3"*/mAuth.getUid()).addChildEventListener(new ChildEventListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				if (dataSnapshot.getValue() != null) {
					ChatRequest chatRequest = new ChatRequest();
					chatRequest.accepted = (boolean) dataSnapshot.child("accepted").getValue();
					chatRequest.guest_id = (String) dataSnapshot.child("guest_id").getValue();
					chatRequest.timestamp = (long) dataSnapshot.child("timestamp").getValue();
					chatRequest.room_id = (String) dataSnapshot.child("room_id").getValue();
					chatRequest.caller_id = (String) dataSnapshot.child("caller_id").getValue();

					if (chatRequest.guest_id == null ||
							!chatRequest.room_id.substring(1).equals(friend.room_id.substring(1)))
						return;

					if (preferenceHelper.getNotificationsRingtone() && preferenceHelper.getNotificationsInChatSound()) {
						Tools.playMedia(selfRef, R.raw.chat_request);
					}

					try {
						v_parent.setLayoutParams(lParams);
						v_parent.setVisibility(View.VISIBLE);
						v_info.setText(getString(R.string.live_chat_request, friend.name));
						v_parent.setOnTouchListener(new WindowTouchListener(v_parent, dataSnapshot.getRef()));
						v_accept.setOnClickListener(v -> {
							dataSnapshot.getRef().child("accepted").setValue(true);
							//TODO:enable video call here
							isAcceptingCall = true;
							_beinswitch.setChecked(BeinSwitch.Checked.RIGHT);
//                            Intent intent = new Intent(mContext, VideoCallActivity.class);
//                            intent.setAction(VideoCallActivity.ACTION_PRIVATE_LIVE_CHAT_DATA);
//                            intent.putExtra(VideoCallActivity.INTENT_EXTRA_LIVE_CHAT_FRIEND, friend);
//                            mContext.startActivity(intent);
							v_parent.setVisibility(View.GONE);
							v_parent.setOnTouchListener(null);
						});

					} catch (WindowManager.BadTokenException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		FirebaseDatabase.getInstance().getReference()
				.child(Configs.DATABASE_LIVE_CHANNELS_ROOT_NAME)
				.child(friend.id).addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				if (dataSnapshot.getValue() != null) {

					ChatRequest chatRequest = dataSnapshot.getValue(ChatRequest.class);
					if (chatRequest == null) return;
					if (chatRequest.accepted && chatRequest.caller_id.equals(/*"FKNLZC54Wea9ie54V6v2zyZBf9J3"*/mAuth.getUid()) &&
							chatRequest.room_id.substring(3).equals(friend.room_id.substring(3))) {

						dataSnapshot.getRef().removeValue();
						Log.d("Vid_Chat_Request", "Friend accepted chat accepted" );
					} else {
						_beinswitch.setChecked(BeinSwitch.Checked.LEFT);
						dataSnapshot.getRef().removeValue();
						ViewsUtil.showToast(selfRef,String.format("Could not connect to %s!", friend.name),0,R.color.colorPink);
					}
				}
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                Toast.makeText(selfRef, String.format("Could not connect to %s!", friend.name), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

	}

	private static final class WindowTouchListener implements View.OnTouchListener {

		int x, y;
		int touchedX, touchedY;
		private final View _live_cont;
		private final ConstraintLayout.LayoutParams lParams;
		private final ConstraintLayout.LayoutParams updateLayoutParams;
		private final DatabaseReference mLiveRef;

		WindowTouchListener(View _live_cont, DatabaseReference liveRef) {
			this._live_cont = _live_cont;
			this.mLiveRef = liveRef;
			lParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
			updateLayoutParams = (ConstraintLayout.LayoutParams) _live_cont.getLayoutParams();
		}


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();
			touchedX = (int) event.getRawX();
			touchedY = (int) event.getRawY();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					x = touchedX - updateLayoutParams.leftMargin;
//                    y = touchedY - updateLayoutParams.y;
					return true;
				case MotionEvent.ACTION_MOVE:
					updateLayoutParams.resolveLayoutDirection(LayoutDirection.LTR);
					updateLayoutParams.rightMargin = -(touchedX - x);
					updateLayoutParams.leftMargin = touchedX - x;
//                    updateLayoutParams.y = touchedY - y;
					if (touchedX - x >= 0) {
						_live_cont.setLayoutParams(updateLayoutParams);

						if (touchedX - x >= 200) {
							mLiveRef.child("accepted").setValue(true);
//                            mLiveRef.removeValue();
							_live_cont.setVisibility(View.GONE);
							lParams.leftMargin = 0;
							lParams.rightMargin = 0;
							_live_cont.setLayoutParams(lParams);
							_live_cont.setOnTouchListener(null);
						}
					}
					return true;
			}
			return false;
		}
	}

	private void emojiFinder(String txt) {
//        String sEmojiTest = "😀😃😄😁😆😅😂🤣☺️😊😇🙂🙃😉😌😍😘😗😙😚😋😜😝😛🤑🤗🤓😎🤡🤠😏😒😞😔😟😕🙁☹️😣😖😫😩😤😠😡😶😐😑😯😦😧😮😲😵😳😱😨😰😢😥🤤😭😓😪😴🙄🤔🤥😬🤐🤢🤧😷🤒🤕😈👿👹👺💩👻💀☠️👽👾🤖🎃😺😸😹😻😼😽🙀😿😾👐🙌👏🙏🤝👍👎👊✊🤛🤜🤞✌️🤘👌👈👉👆👇☝️✋🤚🖐🖖👋🤙💪🖕✍️🤳💅💍💄💋👄👅👂👃👣👁👀🗣👤👥👶👦👧👨👩👱‍♀👱👴👵👲👳‍♀👳👮‍♀👮👷‍♀👷💂‍♀💂🕵️‍♀️🕵👩‍⚕👨‍⚕👩‍🌾👨‍🌾👩‍🍳👨‍🍳👩‍🎓👨‍🎓👩‍🎤👨‍🎤👩‍🏫👨‍🏫👩‍🏭👨‍🏭👩‍💻👨‍💻👩‍💼👨‍💼👩‍🔧👨‍🔧👩‍🔬👨‍🔬👩‍🎨👨‍🎨👩‍🚒👨‍🚒👩‍✈👨‍✈👩‍🚀👨‍🚀👩‍⚖👨‍⚖🤶🎅👸🤴👰🤵👼🤰🙇‍♀🙇💁💁‍♂🙅🙅‍♂🙆🙆‍♂🙋🙋‍♂🤦‍♀🤦‍♂🤷‍♀🤷‍♂🙎🙎‍♂🙍🙍‍♂💇💇‍♂💆💆‍♂🕴💃🕺👯👯‍♂🚶‍♀🚶🏃‍♀🏃👫👭👬💑👩‍❤️‍👩👨‍❤️‍👨";

		final Pattern pattern = Pattern.compile(sEmojiRegex);
		final Matcher matcher = pattern.matcher(txt);
		int foundEmojiCount = 0;
		while (matcher.find()) {
			Log.d("JSHJHSJ", "Full match: " + matcher.group(0));

//			if (matcher.group(0).equals("\uD83D\uDE18")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.kiss_48px, 10000);
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 260)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			} else if (matcher.group(0).equals("\uD83D\uDE0D")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.smiling_face_with_heart_48px, 10000);
//
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 260)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			} else if (matcher.group(0).equals("♥") || matcher.group(0).equals("❤")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.heart_48px, 10000);
//
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 260)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			} else if (matcher.group(0).equals("\uD83D\uDC95")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.two_hearts_48px, 10000);
//
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 260)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			} else if (matcher.group(0).equals("\uD83D\uDC98")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.heart_with_arrow_48px, 10000);
//
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 260)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			} else if (matcher.group(0).equals("\uD83D\uDC9D")) {
//				ParticleSystem particleSystem = new ParticleSystem(this, 150, R.drawable.heart_balloon_48px, 10000);
//
//				particleSystem.setSpeedModuleAndAngleRange(0f, 0.3f, 200, 200)
//						.setRotationSpeed(144)
//						.setAcceleration(0.00005f, 260)
//						.emitWithGravity(findViewById(R.id.particles), Gravity.END | Gravity.BOTTOM, 15, 10000);
//				break;
//			}
//			foundEmojiCount++;
		}
		Log.d("JSHJHSJ", "Captured Emoji count = " + foundEmojiCount);
	}

	private void setTypingIndicator(EmojiEditText editText, String fId, String roomId) {
		if (roomId == null || fId == null || editText == null) return;
		DatabaseReference typingRef = FirebaseDatabase.getInstance().getReference()
				.child("live_conversation")
				.child(fId)
				.child(roomId)
				.child("typing");

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				new Handler(getMainLooper()).postDelayed(() -> {
					typingRef.setValue(false);
				}, 3000);
				if (s.toString().trim().length() > 0) {
//                    _btn_add_file.setVisibility(View.GONE);
//                    _send_btn_cont.setEnabled(true)
					mCountDownTimer.cancel();
					mCountDownTimer.start();
				}
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString().trim().length() > 0) {
//                    _btn_add_file.setVisibility(View.GONE);
//                    _send_btn_cont.setEnabled(true);
					mCountDownTimer.cancel();
					mCountDownTimer.start();
					typingRef.setValue(true);
				} else {
//                    _btn_add_file.setVisibility(View.VISIBLE);
//                    _send_btn_cont.setEnabled(false);
					typingRef.setValue(false);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().trim().length() > 0) {
//                    _btn_add_file.setVisibility(View.GONE);
//                    _send_btn_cont.setEnabled(true);
					mCountDownTimer.cancel();
					mCountDownTimer.start();
					typingRef.setValue(true);
				} else {
//                    _btn_add_file.setVisibility(View.VISIBLE);
//                    _send_btn_cont.setEnabled(false);
					typingRef.setValue(false);
				}
			}
		});
	}

	private void getTypingIndicator(String roomId) {
		if (mAuth.getUid() == null || roomId == null) return;
		DatabaseReference typingRef = FirebaseDatabase.getInstance().getReference()
				.child("live_conversation")
				.child(mAuth.getUid())
				.child(roomId)
				.child("typing");
		typingRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (!dataSnapshot.exists()) return;

				if (dataSnapshot.getValue() != null) {

					if ((boolean) dataSnapshot.getValue()) {
						mCountDownTimer.cancel();
						mCountDownTimer.start();
//						_friend_avata.setAnimating(true);
                        _typing_box.setVisibility(View.VISIBLE);
					} else {
                        _typing_box.setVisibility(View.GONE);

//						_avata.setAnimating(false);
					}

                    dataSnapshot.getRef().removeValue();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			}
		});
	}

	private void initAgoraEngineAndJoinChannel() {
		if (mFriend == null ) return;
		if (!isAcceptingCall)mLiveChatTools.sendLiveChatRequest(mFriend);
		mCountDownTimer.start();

		initializeAgoraEngine();
		setupVideoProfile();
		setupLocalVideo();
		joinChannel();
		mRtcEngine.disableAudio();
		mRtcEngine.enableDualStreamMode(false);
		VideoEncoderConfiguration configuration = new VideoEncoderConfiguration();
		configuration.bitrate = VideoEncoderConfiguration.DEFAULT_MIN_BITRATE;
		configuration.frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_10;
		mRtcEngine.setVideoEncoderConfiguration(configuration);
		mRtcEngine.setVideoQualityParameters(true);
		mRtcEngine.setLocalRenderMode(2);
	}

	private void requestPermissions() {
		// BEGIN_INCLUDE(contacts_permission_request)
		if (ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.RECORD_AUDIO) ||
				ActivityCompat.shouldShowRequestPermissionRationale(this,
						Manifest.permission.CAMERA)
				|| ActivityCompat.shouldShowRequestPermissionRationale(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

			// Display a SnackBar with an explanation and a button to trigger the accepted.
			Snackbar.make(_parent_view, getString(R.string.allow_app_permissions_snack_bar, "storage"),
					Snackbar.LENGTH_INDEFINITE)
					.setAction(getString(R.string.dialog_ok), new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							ActivityCompat.requestPermissions(selfRef, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
						}
					})
					.show();
		} else {
			// Storage permissions have not been granted yet. Request them directly.
			ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
		}
		// END_INCLUDE(storage_permission_request)
	}

	public void startEngine() {
		// Verify that all required storage permissions have been granted.
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			// Storage permissions have not been granted.
			requestPermissions();
		} else {
			//Permissions granted

//            ViewsUtil.hideViews(_top_box, _bottom_box);
//            ViewsUtil.showViews(_mid_box);
			initAgoraEngineAndJoinChannel();
		}
	}

	private void setAdminMessage(){
		if (mUser != null && mFriend != null) {
			LiveMessage message = new LiveMessage();
			message.message_id = "key@001x";
			message.sender_id = "0";
			message.receiver_id = "0";
			message.timestamp = System.currentTimeMillis();
			message.text = "n/a";
			message.avata = mUser.avata;
			message.name = mFriend.name;
			message.room_id = mFriend.room_id;

			mMessages.add(message);
			mAdapter.notifyDataSetChanged();
		}
	}

	private void sendSimpleMessage(String content) {
		if (content.length() > 0 && mMessageDbRef != null && mUser != null && mFriend != null) {

			final LiveMessage message = new LiveMessage();
			message.message_id = mMessageDbRef.push().getKey();
			message.sender_id = mAuth.getUid();
			message.receiver_id = mFriend.id;
			message.timestamp = System.currentTimeMillis();
			message.text = content;
			message.avata = mUser.avata;
			message.name = mUser.name;
			message.room_id = mFriend.room_id;

			mMessageDbRef.push().setValue(message);

			mMessages.add(message);
			Chat chat = new Chat();
			chat.friend = mFriend;
			chat.message = message.text;
			chat.timestamp = message.timestamp;

			long k = RecentChatsDB.getInstance(selfRef).addResentChat(selfRef, chat);
		}
	}

	public void onLocalVideoMuteClicked(View view) {
		ImageView iv = (ImageView) view;
		if (iv.isSelected()) {
			iv.setSelected(false);
			iv.clearColorFilter();
		} else {
			iv.setSelected(true);
			iv.setColorFilter(getResources().getColor(R.color.colorPrimary,null), PorterDuff.Mode.MULTIPLY);
		}

		mRtcEngine.muteLocalVideoStream(iv.isSelected());

		FrameLayout container = findViewById(R.id.vm_local_video_box);
		SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);
		surfaceView.setZOrderMediaOverlay(!iv.isSelected());
		surfaceView.setVisibility(iv.isSelected() ? View.GONE : View.VISIBLE);
	}

	public void onLocalAudioMuteClicked(View view) {
		ImageView iv = (ImageView) view;
		if (iv.isSelected()) {
			iv.setSelected(false);
			iv.clearColorFilter();
		} else {
			iv.setSelected(true);
			iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
		}

		mRtcEngine.muteLocalAudioStream(iv.isSelected());
	}

	public void onSwitchCameraClicked(View view) {
		mRtcEngine.switchCamera();
	}

	private void leaveChannel() {
		try {
			isAcceptingCall = false;
			mRtcEngine.leaveChannel();
			onRemoteUserLeft();
//            finish();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void initializeAgoraEngine() {
		try {
			mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
		} catch (Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));

			throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
		}
	}

	private void setupVideoProfile() {
		mRtcEngine.enableVideo();

//      mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false); // Earlier than 2.3.0
		mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
				VideoEncoderConfiguration.STANDARD_BITRATE,
				VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
	}

	private void setupLocalVideo() {
		FrameLayout container = findViewById(R.id.vm_local_video_box);
		SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
		surfaceView.setZOrderMediaOverlay(true);
		container.addView(surfaceView);
		mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
		ViewsUtil.showViews(container, findViewById(R.id.btn_switch_cam), findViewById(R.id.btn_vid_mute));
	}

	private void joinChannel() {
		mRtcEngine.joinChannel(null, mFriend.room_id, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
	}

	private void setupRemoteVideo(int uid) {
		FrameLayout container = findViewById(R.id.vm_guest_video_box);

		if (container.getChildCount() >= 1) {
			return;
		}

		SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
		container.addView(surfaceView);
		mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));

		surfaceView.setTag(uid); // for mark purpose
		changeView(true,true);
		//TODO: Hide views
//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.GONE);
//        ViewsUtil.hideViews(_mid_box);
//        ViewsUtil.showViews(_top_box, _bottom_box);
	}

	private void onRemoteUserLeft() {
		mCountDownTimer.cancel();
		_beinswitch.setChecked(BeinSwitch.Checked.LEFT);
		FrameLayout container1 = findViewById(R.id.vm_guest_video_box);
		FrameLayout container = findViewById(R.id.vm_local_video_box);
		container1.removeAllViews();
		container.removeAllViews();
		ViewsUtil.hideViews(container, findViewById(R.id.btn_switch_cam), findViewById(R.id.btn_vid_mute));
	}

	private void onRemoteUserVideoMuted(int uid, boolean muted) {
		FrameLayout container = findViewById(R.id.vm_guest_video_box);

		SurfaceView surfaceView = (SurfaceView) container.getChildAt(0);

		Object tag = surfaceView.getTag();
		if (tag != null && (Integer) tag == uid) {
			surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
		}
	}

	//endregion

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions, @NonNull int[] grantResults) {
		Log.i(TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

		if (requestCode == PERMISSION_REQ_ID) {
			if (PermissionUtil.verifyPermissions(grantResults)) {
				startEngine();
			} else {
				ViewsUtil.showToast(selfRef,"Need permissions " + Manifest.permission.RECORD_AUDIO + "/" +
						Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE,0,R.color.colorPink);
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		if (mFriend != null) outState.putParcelable(EXTRA_FRIEND, mFriend);
		super.onSaveInstanceState(outState);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onBackPressed() {
		if (mEmojiPopup.isShowing()) {
			mEmojiPopup.dismiss();
		}

		final InputMethodManager inputMethodManager = (InputMethodManager) selfRef.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager.isActive() || inputMethodManager.isAcceptingText()) {
			Tools.hideSoftKeyboard(selfRef);
		}
		if (_beinswitch.getChecked() == BeinSwitch.Checked.RIGHT){
			_beinswitch.setChecked(BeinSwitch.Checked.LEFT);
		}
		endChatSession(mFriend.room_id);
		startActivity(new Intent(selfRef,MainActivity.class));
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//        if (mAuth != null && mAuth.getUid() != null) {
//            MessagingTools.setUserState(mAuth.getUid(), UserState.ONLINE_IN_APP);
//        }

		mCountDownTimer.cancel();
		leaveChannel();
		RtcEngine.destroy();
		try {
			onBackPressed();
		}catch (RuntimeException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onCheckChanged(BeinSwitch.Checked current) {
		changeView(current == BeinSwitch.Checked.RIGHT, false);
		if (current == BeinSwitch.Checked.RIGHT){
			startEngine();
		}else {
			leaveChannel();
		}
	}

}
