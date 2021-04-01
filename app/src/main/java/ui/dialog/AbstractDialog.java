package ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.brimbay.be.R;
import com.squareup.picasso.Picasso;

import ui.dialog.model.DialogButton;

public class AbstractDialog implements DialogInterface {

    //Constants
    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEGATIVE = -1;
    public static final int NO_ICON = -111;
    public static final int NO_ANIMATION = -111;

    protected Dialog mDialog;
    protected Activity mActivity;
    protected String title;
    protected String message;
    protected String number;
    protected int image_res;
    protected Bitmap image_bitmap;
    protected boolean mCancelable;
    protected DialogButton mPositiveButton;
    protected DialogButton mNegativeButton;
    protected int mAnimationResId;
    protected String mAnimationFile;
    protected LottieAnimationView mAnimationView;

    protected OnDismissListener mOnDismissListener;
    protected OnCancelListener mOnCancelListener;
    protected OnShowListener mOnShowListener;

    protected AbstractDialog(@NonNull Activity mActivity, @NonNull String title, @NonNull String message, @Nullable String number,
                             int image_res, @NonNull Bitmap image_bitmap, boolean mCancelable, @NonNull DialogButton mPositiveButton,
                             @NonNull DialogButton mNegativeButton, @RawRes int mAnimationResId, @NonNull String mAnimationFile) {
        this.mActivity = mActivity;
        this.title = title;
        this.message = message;
        this.number = number;
        this.image_res = image_res;
        this.image_bitmap = image_bitmap;
        this.mCancelable = mCancelable;
        this.mPositiveButton = mPositiveButton;
        this.mNegativeButton = mNegativeButton;
        this.mAnimationResId = mAnimationResId;
        this.mAnimationFile = mAnimationFile;
    }

    protected AbstractDialog(@NonNull Activity mActivity,
                             @NonNull String title,
                             @NonNull String message,
                             boolean mCancelable,
                             @NonNull DialogButton mPositiveButton,
                             @NonNull DialogButton mNegativeButton,
                             @RawRes int mAnimationResId,
                             @NonNull String mAnimationFile) {
        this.mActivity = mActivity;
        this.title = title;
        this.message = message;
        this.mCancelable = mCancelable;
        this.mPositiveButton = mPositiveButton;
        this.mNegativeButton = mNegativeButton;
        this.mAnimationResId = mAnimationResId;
        this.mAnimationFile = mAnimationFile;
    }

    protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View dialogView = inflater.inflate(R.layout.layout_alert_dialog, container, false);

        // Initialize Views
        TextView mTitleView = dialogView.findViewById(R.id.textView_title);
        TextView mMessageView = dialogView.findViewById(R.id.textView_message);
        Button mPositiveButtonView = dialogView.findViewById(R.id.button_positive);
        Button mNegativeButtonView = dialogView.findViewById(R.id.button_negative);
        mAnimationView = dialogView.findViewById(R.id.animation_view);
        ImageView image = dialogView.findViewById(R.id.image_view);
        View number_box = dialogView.findViewById(R.id.number_box);
        TextView phone_number = dialogView.findViewById(R.id.phone_number);

        // Set Title
        if (title != null) {
            mTitleView.setVisibility(View.VISIBLE);
            mTitleView.setText(title);
        } else {
            mTitleView.setVisibility(View.GONE);
        }

        // Set Message
        if (message != null) {
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(message);
        } else {
            mMessageView.setVisibility(View.GONE);
        }

        // Set Number
        if (number != null) {
            number_box.setVisibility(View.VISIBLE);
            phone_number.setText(number);
        } else {
            number_box.setVisibility(View.GONE);
        }

        //Set Image
        if (image_res != 0){
            Picasso.get()
                    .load(image_res)
                    .into(image);
            image.setVisibility(View.VISIBLE);
        }else if(image_bitmap != null){
            image.setImageBitmap(image_bitmap);
            image.setVisibility(View.VISIBLE);
        }else {
            image.setVisibility(View.GONE);
        }

        // Set Positive Button
        if (mPositiveButton != null) {
            mPositiveButtonView.setVisibility(View.VISIBLE);
            mPositiveButtonView.setText(mPositiveButton.getTitle());
            if (mPositiveButton.getIcon() != NO_ICON) {
//                mPositiveButtonView.setIcon(mActivity.getDrawable(mPositiveButton.getIcon()));
                mPositiveButtonView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(mActivity.getApplicationContext(), mNegativeButton.getIcon()),null,null,null);
            }

            mPositiveButtonView.setOnClickListener(view -> mPositiveButton.getOnClickListener().onClick(AbstractDialog.this, BUTTON_POSITIVE));
        } else {
            mPositiveButtonView.setVisibility(View.INVISIBLE);
        }

        // Set Negative Button
        if (mNegativeButton != null) {
            mNegativeButtonView.setVisibility(View.VISIBLE);
            mNegativeButtonView.setText(mNegativeButton.getTitle());
            if (mNegativeButton.getIcon() != NO_ICON) {
                mNegativeButtonView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(mActivity.getApplicationContext(),mNegativeButton.getIcon()),null,null,null);
            }

            mNegativeButtonView.setOnClickListener(view -> mNegativeButton.getOnClickListener().onClick(AbstractDialog.this, BUTTON_NEGATIVE));
        } else {
            mNegativeButtonView.setVisibility(View.INVISIBLE);
        }

        // If Orientation is Horizontal, Hide AnimationView
        int orientation = mActivity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mAnimationView.setVisibility(View.GONE);
        } else {
            // Set Animation from Resource
            if (mAnimationResId != NO_ANIMATION) {
                mAnimationView.setVisibility(View.VISIBLE);
                mAnimationView.setAnimation(mAnimationResId);
                mAnimationView.playAnimation();

                // Set Animation from Assets File
            } else if (mAnimationFile != null) {
                mAnimationView.setVisibility(View.VISIBLE);
                mAnimationView.setAnimation(mAnimationFile);
                mAnimationView.playAnimation();

            } else {
                mAnimationView.setVisibility(View.GONE);
            }
        }

        // Apply Styles
        TypedArray a = mActivity.getTheme().obtainStyledAttributes(R.styleable.MaterialDialog);

        try {
            // Set Dialog Background
            dialogView.setBackgroundColor(
                    a.getColor(R.styleable.MaterialDialog_material_dialog_background,
                            mActivity.getResources().getColor(R.color.material_dialog_background, null)));

            // Set Title Text Color
            mTitleView.setTextColor(
                    a.getColor(R.styleable.MaterialDialog_material_dialog_title_text_color,
                            mActivity.getResources().getColor(R.color.material_dialog_title_text_color)));

            // Set Message Text Color
            mMessageView.setTextColor(
                    a.getColor(R.styleable.MaterialDialog_material_dialog_message_text_color,
                            mActivity.getResources().getColor((R.color.material_dialog_message_text_color))));

            // Set Positive Button Icon Tint
            ColorStateList mPositiveButtonTint = a.getColorStateList(
                    R.styleable.MaterialDialog_material_dialog_positive_button_text_color);

            if (mPositiveButtonTint == null) {
                mPositiveButtonTint = ContextCompat.getColorStateList(
                        mActivity.getApplicationContext(),
                        R.color.material_dialog_positive_button_text_color);
            }
            mPositiveButtonView.setTextColor(mPositiveButtonTint);
//            mPositiveButtonView.setIconTint(mPositiveButtonTint);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPositiveButtonView.setCompoundDrawableTintList(mPositiveButtonTint);
            }

            // Set Negative Button Icon & Text Tint
            ColorStateList mNegativeButtonTint = a.getColorStateList(
                    R.styleable.MaterialDialog_material_dialog_negative_button_text_color);

            if (mNegativeButtonTint == null) {
                mNegativeButtonTint = ContextCompat.getColorStateList(
                        mActivity.getApplicationContext(),
                        R.color.material_dialog_negative_button_text_color);
            }
//            mNegativeButtonView.setIconTint(mNegativeButtonTint);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mNegativeButtonView.setCompoundDrawableTintList(mNegativeButtonTint);
            }
            mNegativeButtonView.setTextColor(mNegativeButtonTint);

            // Set Positive Button Background Tint
            ColorStateList mBackgroundTint = a.getColorStateList(
                    R.styleable.MaterialDialog_material_dialog_positive_button_color);

            if (mBackgroundTint == null) {
                mBackgroundTint = ContextCompat.getColorStateList(
                        mActivity.getApplicationContext(),
                        R.color.material_dialog_positive_button_color);
            }
            mPositiveButtonView.setBackgroundTintList(mBackgroundTint);
            if (mBackgroundTint != null) {
//                mNegativeButtonView.setRippleColor(mBackgroundTint.withAlpha(75));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            a.recycle();
        }

        return dialogView;
    }

    /**
     * Displays the Dialog
     */
    public void show() {
        if (mDialog != null) {
            mDialog.show();
        } else {
            throwNullDialog();
        }
    }

    /**
     * Cancels the Dialog
     */
    @Override
    public void cancel() {
        if (mDialog != null) {
            mDialog.cancel();
        } else {
            throwNullDialog();
        }
    }

    /**
     * Dismisses the Dialog
     */
    @Override
    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        } else {
            throwNullDialog();
        }
    }

    /**
     * @param onShowListener interface for callback events when dialog is showed.
     */
    public void setOnShowListener(@NonNull final OnShowListener onShowListener) {
        this.mOnShowListener = onShowListener;

        mDialog.setOnShowListener(dialogInterface -> showCallback());
    }

    /**
     * @param onCancelListener interface for callback events when dialog is cancelled.
     */
    public void setOnCancelListener(@NonNull final OnCancelListener onCancelListener) {
        this.mOnCancelListener = onCancelListener;

        mDialog.setOnCancelListener(dialogInterface -> cancelCallback());
    }

    /**
     * @param onDismissListener interface for callback events when dialog is dismissed;
     */
    public void setOnDismissListener(@NonNull final OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;

        mDialog.setOnDismissListener(dialogInterface -> dismissCallback());
    }

    /**
     * @return {@link LottieAnimationView} from the Dialog.
     */
    public LottieAnimationView getAnimationView() {
        return mAnimationView;
    }

    private void showCallback() {
        if (mOnShowListener != null) {
            mOnShowListener.onShow(this);
        }
    }

    private void dismissCallback() {
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(this);
        }
    }

    private void cancelCallback() {
        if (mOnCancelListener != null) {
            mOnCancelListener.onCancel(this);
        }
    }

    private void throwNullDialog() {
        throw new NullPointerException("Called method on null Dialog. Create dialog using `Builder` before calling on Dialog");
    }

    public interface OnClickListener {
        void onClick(DialogInterface dialogInterface, int which);
    }
}