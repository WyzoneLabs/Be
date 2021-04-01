package adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brimbay.be.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import static utils.Configs.STR_DEFAULT_BASE64;

public class EmojisAdapter extends RecyclerView.Adapter<EmojisAdapter.ViewHolder> {
    private List<String> mEmojis;
    private OnClickListener onClickListener;

    public interface OnClickListener{
        void OnEmojiClickListener(String emoji, int i);
    }

    public EmojisAdapter() {
    }

    public void setEmojis(List<String> emojis) {
        this.mEmojis = emojis;
        notifyDataSetChanged();
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.emojis_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String emoji = mEmojis.get(position);
        if (position == 0 && emoji.equals(STR_DEFAULT_BASE64)){
            Picasso.get()
                    .load(R.drawable.unavailable_100px)
                    .into(holder._emoji);
        }else {
            Picasso.get()
                    .load("file:///android_asset/emoji/" + emoji)
                    .into(holder._emoji);
        }
        holder._emoji.setOnClickListener(v -> {
            if (onClickListener != null)onClickListener.OnEmojiClickListener(emoji, position);
        });
    }

    @Override
    public int getItemCount() {
        return mEmojis.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView _emoji;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            _emoji = itemView.findViewById(R.id.ev_emoji);
        }
    }
}
