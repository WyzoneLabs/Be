package ui.loading;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.brimbay.be.R;

public class BrimbayBackgroundLayout extends LinearLayout {

    private float mCornerRadius;
    private int mBackgroundColor;

    public BrimbayBackgroundLayout(Context context) {
        super(context);
        init();
    }

    public BrimbayBackgroundLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BrimbayBackgroundLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int color = getContext().getResources().getColor(R.color.brimbay_progress_default_color, null);
        initBackground(color, mCornerRadius);
    }

    private void initBackground(int color, float cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        setBackground(drawable);
    }

    public void setCornerRadius(float radius) {
        mCornerRadius = Helper.dpToPixel(radius, getContext());
        initBackground(mBackgroundColor, mCornerRadius);
    }

    public void setBaseColor(int color) {
        mBackgroundColor = color;
        initBackground(mBackgroundColor, mCornerRadius);
    }
}