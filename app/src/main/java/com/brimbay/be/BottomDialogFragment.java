package com.brimbay.be;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Friend;
import utils.Configs;
import utils.DateUtils;
import utils.ViewsUtil;

import static utils.Configs.STR_DEFAULT_BASE64;

public class BottomDialogFragment extends BottomSheetDialogFragment {
    private Friend friend;
    private OnBottomDialogInteraction onBottomDialogInteraction;

    public interface OnBottomDialogInteraction{
        void OnSendChatRequest(Friend friend);
    }

    public BottomDialogFragment(Friend friend) {
        this.friend = friend;
    }

    public OnBottomDialogInteraction getOnBottomDialogInteraction() {
        return onBottomDialogInteraction;
    }

    public void setOnBottomDialogInteraction(OnBottomDialogInteraction onBottomDialogInteraction) {
        this.onBottomDialogInteraction = onBottomDialogInteraction;
    }

    public static BottomDialogFragment newInstance(Friend friend) {
        return new BottomDialogFragment(friend);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setStyle(BottomSheetDialogFragment.STYLE_NO_FRAME, R.style.SheetDialog);
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottomsheet, container, false);
        CircleImageView _avata = view.findViewById(R.id.fb_avata);
        ImageView _emoji = view.findViewById(R.id.fb_emoji_status_img);
        View _emoji_box = view.findViewById(R.id.fb_emoji_status_img_box);
        TextView _name = view.findViewById(R.id.fb_name);
        TextView _phone = view.findViewById(R.id.fb_phone);
        ImageView _online = view.findViewById(R.id.fb_online_status);
        AppCompatButton _send = view.findViewById(R.id.fb_confirm);
        AppCompatButton _cancel = view.findViewById(R.id.fb_cancel);

        if (friend != null) {
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
            boolean is_online = friend.status != null && (friend.status.is_online &&
                    DateUtils.getTimePassed(friend.status.timestamp) <= Configs.TIME_TO_OFFLINE);
            _online.setVisibility(is_online?View.VISIBLE:View.GONE);
            _cancel.setOnClickListener(v -> dismiss());
            _send.setOnClickListener(v -> {
                if(onBottomDialogInteraction != null){
                    onBottomDialogInteraction.OnSendChatRequest(friend);
                    dismiss();
                }
            });
        }else {
            ViewsUtil.showToast(requireActivity(),"Sorry, something went wrong please try again",0,R.color.colorPink);
            this.dismiss();
        }

        return view;
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
}
