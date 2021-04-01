package ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;

import com.brimbay.be.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import ui.dialog.model.DialogButton;

public class BottomMaterialDialog extends AbstractDialog {

	protected BottomMaterialDialog(@NonNull final Activity mActivity, @NonNull String title,
								   @NonNull String message, @Nullable String number,
	                               int image_res, @NonNull Bitmap image_bitmap, boolean mCancelable,
	                               @NonNull DialogButton mPositiveButton, @NonNull DialogButton mNegativeButton,
	                               @RawRes int mAnimationResId, @NonNull String mAnimationFile) {
		super(mActivity, title, message,number,image_res, image_bitmap, mCancelable, mPositiveButton, mNegativeButton, mAnimationResId, mAnimationFile);

		// Init Dialog, Create Bottom Sheet Dialog
		mDialog = new BottomSheetDialog(mActivity);

		LayoutInflater inflater = mActivity.getLayoutInflater();

		View dialogView = createView(inflater, null);
		mDialog.setContentView(dialogView);

		// Set Cancelable property
		mDialog.setCancelable(mCancelable);

		// Clip AnimationView to round Corners
		dialogView.setOutlineProvider(new ViewOutlineProvider() {
			@Override
			public void getOutline(View view, Outline outline) {
				float radius = mActivity.getResources().getDimension(R.dimen.radiusTop);
				outline.setRoundRect(0, 0, view.getWidth(), view.getHeight() + (int) radius, radius);
			}
		});
		dialogView.setClipToOutline(true);

		// Expand Bottom Sheet after showing.
		mDialog.setOnShowListener(dialog -> {
			BottomSheetDialog d = (BottomSheetDialog) dialog;

			FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

			if (bottomSheet != null) {
				BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
			}
		});
	}

	@Override
	protected View createView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return super.createView(inflater, container);
	}

	/**
	 * Builder for {@link BottomMaterialDialog}.
	 */
	public static class Builder {
		private Activity activity;
		private String title;
		private String message;
		private String number;
		protected int image_res;
		protected Bitmap image_bitmap;
		private boolean isCancelable;
		private DialogButton positiveButton;
		private DialogButton negativeButton;
		private int animationResId = NO_ANIMATION;
		private String animationFile;

		/**
		 * @param activity where BottomSheet Material Dialog is to be built.
		 */
		public Builder(@NonNull Activity activity) {
			this.activity = activity;
		}

		/**
		 * @param title Sets the Title of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setTitle(@NonNull String title) {
			this.title = title;
			return this;
		}

		/**
		 * @param message Sets the Message of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setMessage(@NonNull String message) {
			this.message = message;
			return this;
		}

		/**
		 * @param number Sets the Number of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setNumber(@Nullable String number) {
			this.number = number;
			return this;
		}

		/**
		 * @param image_res Sets the Image Resource of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setImage(@DrawableRes int image_res) {
			this.image_res = image_res;
			return this;
		}

		/**
		 * @param image_bitmap Sets the Image Bitmap of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setImage(@NonNull Bitmap image_bitmap) {
			this.image_bitmap = image_bitmap;
			return this;
		}

		/**
		 * @param isCancelable Sets cancelable property of BottomSheet Material Dialog.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setCancelable(boolean isCancelable) {
			this.isCancelable = isCancelable;
			return this;
		}

		/**
		 * Sets the Positive Button to BottomSheet Material Dialog without icon
		 *
		 * @param name            sets the name/label of button.
		 * @param onClickListener interface for callback event on click of button.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setPositiveButton(@NonNull String name, @NonNull OnClickListener onClickListener) {
			return setPositiveButton(name, NO_ICON, onClickListener);
		}

		/**
		 * Sets the Positive Button to BottomSheet Material Dialog with icon
		 *
		 * @param name            sets the name/label of button.
		 * @param icon            sets the resource icon for button.
		 * @param onClickListener interface for callback event on click of button.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setPositiveButton(@NonNull String name, int icon, @NonNull OnClickListener onClickListener) {
			positiveButton = new DialogButton(name, icon, onClickListener);
			return this;
		}

		/**
		 * Sets the Negative Button to BottomSheet Material Dialog without icon.
		 *
		 * @param name            sets the name/label of button.
		 * @param onClickListener interface for callback event on click of button.
		 * @see this, for chaining.
		 */
		@NonNull
		public Builder setNegativeButton(@NonNull String name, @NonNull OnClickListener onClickListener) {
			return setNegativeButton(name, NO_ICON, onClickListener);
		}

		/**
		 * Sets the Negative Button to BottomSheet Material Dialog with icon
		 *
		 * @param name            sets the name/label of button.
		 * @param icon            sets the resource icon for button.
		 * @param onClickListener interface for callback event on click of button.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setNegativeButton(@NonNull String name, int icon, @NonNull OnClickListener onClickListener) {
			negativeButton = new DialogButton(name, icon, onClickListener);
			return this;
		}

		/**
		 * It sets the resource json to the {@link com.airbnb.lottie.LottieAnimationView}.
		 *
		 * @param animationResId sets the resource to {@link com.airbnb.lottie.LottieAnimationView}.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setAnimation(@RawRes int animationResId) {
			this.animationResId = animationResId;
			return this;
		}

		/**
		 * It sets the json file to the {@link com.airbnb.lottie.LottieAnimationView} from assets.
		 *
		 * @param fileName sets the file from assets to {@link com.airbnb.lottie.LottieAnimationView}.
		 * @return this, for chaining.
		 */
		@NonNull
		public Builder setAnimation(@NonNull String fileName) {
			this.animationFile = fileName;
			return this;
		}

		/**
		 * Build the {@link BottomMaterialDialog}.
		 */
		@NonNull
		public BottomMaterialDialog build() {
			return new BottomMaterialDialog(activity, title, message,number,image_res, image_bitmap, isCancelable, positiveButton,
					negativeButton, animationResId, animationFile);
		}
	}

	static class BottomSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialog {
		BottomSheetDialog(@NonNull Context context) {
			super(context, R.style.Theme_Be_BottomSheetDialogTheme);
		}
	}

}
