package com.brimbay.be;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.databinding.ActivityStatusBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import adapter.EmojisAdapter;
import database.UserDB;
import de.hdodenhof.circleimageview.CircleImageView;
import xyz.schwaab.avvylib.AvatarView;

import static utils.Configs.STR_DEFAULT_BASE64;

public class StatusActivity extends Activity implements EmojisAdapter.OnClickListener {
    private StatusActivity selfRef;
    private static final String TAG = "brimbay.be.Status";

    private static final String EMOJI_KEY = "brimbay.be.EMOJI";

    //region Views
    private CircleImageView _avata;
    private TextView _done_btn;
    private ImageView _emoji;
    private RecyclerView _recycler_emoji;
    //endregion
    private ActivityStatusBinding binding;
    private EmojisAdapter mAdapter;
    private String mEmojiPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfRef = this;
        binding = ActivityStatusBinding.inflate(getLayoutInflater());
        if (getIntent() != null && getIntent().getAction().equals(MainActivity.ACTION_CHANGE_EMOJI) &&
                getIntent().getStringExtra(MainActivity.EXTRA_CHANGE_EMOJI) != null){
            mEmojiPath = getIntent().getStringExtra(MainActivity.EXTRA_CHANGE_EMOJI);
        }else if (savedInstanceState != null && savedInstanceState.get(EMOJI_KEY) != null){
            mEmojiPath = savedInstanceState.getString(EMOJI_KEY);
        }
        setContentView(binding.getRoot());
        initUI();

        if(UserDB.getInstance(selfRef).getFriend().avata != null) {
            setImageAvatar(UserDB.getInstance(selfRef).getFriend().avata, _avata);
        }

        handleEmojis();

        _done_btn.setOnClickListener(v -> {
            Intent intent = new Intent(selfRef,MainActivity.class);
            intent.setAction(MainActivity.ACTION_CHANGE_EMOJI);
            intent.putExtra(MainActivity.EXTRA_CHANGE_EMOJI, mEmojiPath);
            setResult(RESULT_OK, intent);
            finish();
        });
        binding.stBackBtn.setOnClickListener(v -> onBackPressed());
    }

    //region Member Methods
    private void initUI(){
        _avata = binding.stAvata;
        _done_btn = binding.stDoneBtn;
        _emoji = binding.stEmojiStatusImg;
        _recycler_emoji = findViewById(R.id.st_recycler);

        if(mEmojiPath != null) {
            if(!mEmojiPath.equals(STR_DEFAULT_BASE64)) {
                Picasso.get()
                        .load("file:///android_asset/emoji/" + mEmojiPath)
                        .into(binding.stEmojiStatusImg);
            }else{
                Picasso.get()
                        .load(R.drawable.unavailable_accent_100px)
                        .into(binding.stEmojiStatusImg);
            }
        }
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

    private void handleEmojis(){
        _recycler_emoji.setHasFixedSize(true);
        _recycler_emoji.setLayoutManager(new GridLayoutManager(selfRef, 4, RecyclerView.VERTICAL, false));
        mAdapter = new EmojisAdapter();
        _recycler_emoji.setAdapter(mAdapter);
        mAdapter.setOnClickListener(this);
        addEmojis();
    }

    private void addEmojis(){
        List<String> files = new ArrayList<>();
        files.add(STR_DEFAULT_BASE64);
        try {
            files.addAll(Arrays.asList(getAssets().list("emoji")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAdapter.setEmojis(files);
    }

    @Override
    public void OnEmojiClickListener(String emoji, int i) {
        if (i == 0 && emoji.equals(STR_DEFAULT_BASE64)){
            Picasso.get().load(R.drawable.unavailable_accent_100px).into(_emoji);
        }else {
            Picasso.get().load("file:///android_asset/emoji/"+emoji).into(_emoji);
        }
        mEmojiPath = emoji;
    }

    //endregion


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEmojiPath != null)outState.putString(EMOJI_KEY, mEmojiPath);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}