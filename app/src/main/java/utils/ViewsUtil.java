package utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.brimbay.be.R;
import com.squareup.picasso.Picasso;

public class ViewsUtil {

    public ViewsUtil() {
    }

    public static void showToast(Activity activity, @NonNull String title, @DrawableRes int icon, @ColorRes int theme_color){
        View layout = activity.getLayoutInflater().inflate(R.layout.toast_view, null /*(ViewGroup) activity.findViewById(R.id.toast_container)*/);
        TextView text = layout.findViewById(R.id.toast_text);
        ImageView image = layout.findViewById(R.id.toast_icon);
        View sep = layout.findViewById(R.id.toast_sep);

        text.setText(title);
        if (icon != 0) {
            Picasso.get()
                    .load(icon)
                    .error(R.drawable.toast_notice_64px)
                    .into(image);
        }
        if (theme_color != 0){
            sep.getBackground().setTint(activity.getResources().getColor(theme_color, null));
        }

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 32);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public static void hideViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    public static void showViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static Bitmap getBitmapFromView(Context context, View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                context.getResources().getDisplayMetrics().widthPixels/*view.getWidth()*/,
                context.getResources().getDisplayMetrics().heightPixels /*view.getHeight()*/, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);
        return bitmap;
    }

    public static class RSBlurProcessor {

        private static final int MAX_RADIUS = 25;
        private final boolean IS_BLUR_SUPPORTED = true;
        private RenderScript rs;

        public RSBlurProcessor(RenderScript rs) {
            this.rs = rs;
        }

        @Nullable
        public Bitmap blur(@NonNull Bitmap bitmap, float radius, int repeat) {

            if (!IS_BLUR_SUPPORTED) {
                return null;
            }

            if (radius > MAX_RADIUS) {
                radius = MAX_RADIUS;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Create allocation type
            Type bitmapType = new Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(width)
                    .setY(height)
                    .setMipmaps(false) // We are using MipmapControl.MIPMAP_NONE
                    .create();

            // Create allocation
            Allocation allocation = Allocation.createTyped(rs, bitmapType);

            // Create blur script
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            blurScript.setRadius(radius);

            // Copy data to allocation
            allocation.copyFrom(bitmap);

            // set blur script input
            blurScript.setInput(allocation);

            // invoke the script to blur
            blurScript.forEach(allocation);

            // Repeat the blur for extra effect
            for (int i = 0; i < repeat; i++) {
                blurScript.forEach(allocation);
            }

            // copy data back to the bitmap
            allocation.copyTo(bitmap);

            // release memory
            allocation.destroy();
            blurScript.destroy();
            allocation = null;
            blurScript = null;

            return bitmap;
        }
    }
}
