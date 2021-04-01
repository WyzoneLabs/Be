package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;


public class ImageUtils {

    public static final int AVATAR_WIDTH = 240;
    public static final int AVATAR_HEIGHT = 240;

    /**
     * Bo tròn ảnh avatar
     *
     * @param context
     * @param src     ảnh dạng bitmap
     * @return RoundedBitmapDrawable là đầu vào cho hàm setImageDrawable()
     */
    public static RoundedBitmapDrawable roundedImage(Context context, Bitmap src) {
        /*Bo tròn avatar*/
        Resources res = context.getResources();
        RoundedBitmapDrawable dr =
                RoundedBitmapDrawableFactory.create(res, src);
        dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);

        return dr;
    }

    /**
     * Đối với ảnh hình chữ nhật thì cần cắt ảnh theo hình vuông và lấy phần tâm
     * ảnh để khi đặt làm avatar sẽ không bị méo
     *
     * @param srcBmp
     * @return
     */
    public static Bitmap cropToSquare(Bitmap srcBmp) {
        Bitmap dstBmp = null;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        return dstBmp;
    }

    /**
     * Convert ảnh dạng bitmap ra String base64
     *
     * @param imgBitmap
     * @return
     */
    public static String encodeBase64(Bitmap imgBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imgBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Làm giảm số điểm ảnh xuống để tránh lỗi Firebase Database OutOfMemory
     *
     * @param is        anh dau vao
     * @param reqWidth  kích thước chiều rộng sau khi giảm
     * @param reqHeight kích thước chiều cao sau khi giảm
     * @return
     */
    public static Bitmap makeImageLite(InputStream is, int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        // Calculate inSampleSize
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    public static InputStream convertBitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        return bs;
    }

//    public static Drawable bitmapToDrawable( Bitmap bitmap){
//        return new FastBitmapDrawable(bitmap);
//    }

    public static Drawable getBitmapFrmView(Context context, View view) {

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);
        return new BitmapDrawable(context.getResources(), returnedBitmap);
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

//    public static ArrayList<ImageDataModel> getAllImages(Activity activity) {
//
//        //Remove older images to avoid copying same image twice
//
//        ArrayList<ImageDataModel> allImages = new ArrayList<>();
//        allImages.clear();
//        Uri uri;
//        Cursor cursor;
//        int column_index_data, column_index_size, column_index_type, column_index_height, column_index_width,  column_index_folder_name;
//
//        String absolutePathOfImage = null, imageName;
//
//        //get all images from external storage
//
//        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//
//        String[] projection = {
//                MediaStore.Images.ImageColumns.SIZE,
//                MediaStore.Images.ImageColumns.WIDTH,
//                MediaStore.Images.ImageColumns.HEIGHT,
//                MediaStore.Images.ImageColumns.MIME_TYPE,
//                MediaStore.Images.ImageColumns.DATA,
//                MediaStore.Images.ImageColumns.DISPLAY_NAME };
//
//        cursor = activity.getContentResolver().query(uri, projection, null, null, null);
//
//        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
//        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
//        column_index_size = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE);
//        column_index_type = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE);
//        column_index_height = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT);
//        column_index_width = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH);
//
//        while (cursor.moveToNext()) {
//            ImageDataModel imgData = new ImageDataModel();
//            imgData.height = cursor.getInt(column_index_height);
//            imgData.width = cursor.getInt(column_index_width);
//            imgData.path = cursor.getString(column_index_data);
//            imgData.size = cursor.getInt(column_index_size);
//            imgData.type = cursor.getString(column_index_type);
//            imgData.name = cursor.getString(column_index_folder_name);
//
//            allImages.add(imgData);
//        }
//
//        // Get all Internal storage images
//
//        uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
//
//        cursor = activity.getContentResolver().query(uri, projection, null,
//                null, null);
//
//        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
//        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
//        column_index_size = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.SIZE);
//        column_index_type = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE);
//        column_index_height = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.HEIGHT);
//        column_index_width = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.WIDTH);
//
//        while (cursor.moveToNext()) {
//            ImageDataModel imgData = new ImageDataModel();
//            imgData.height = cursor.getInt(column_index_height);
//            imgData.width = cursor.getInt(column_index_width);
//            imgData.path = cursor.getString(column_index_data);
//            imgData.size = cursor.getInt(column_index_size);
//            imgData.type = cursor.getString(column_index_type);
//            imgData.name = cursor.getString(column_index_folder_name);
//
//            allImages.add(imgData);
//        }
//
//        return allImages;
//    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

//        bitmap.recycle();

        return output;
    }

    public ArrayList<String> getFilePaths(Activity context) {


        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<>();
        ArrayList<String> resultIAV = new ArrayList<>();

        String[] directories = null;
        if (u != null) {
            c = context.getContentResolver().query(u, projection, null, null, null);
        }

        if ((c != null) && (c.moveToFirst())) {
            do {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try {
                    dirList.add(tempDir);
                } catch (Exception e) {

                }
            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);

        }

        for (int i = 0; i < dirList.size(); i++) {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if (imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {

                    if (imagePath.isDirectory()) {
                        imageList = imagePath.listFiles();

                    }
                    if (imagePath.getName().contains(".jpg") || imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg") || imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    ) {


                        String path = imagePath.getAbsolutePath();
                        resultIAV.add(path);

                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return resultIAV;


    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch(Activity activity) {

        final int READ_REQUEST_CODE = 42;
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        activity.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    // Here are some examples of how you might call this method.
    // The first parameter is the MIME type, and the second parameter is the name
    // of the file you are creating:
    //
    // createFile("text/plain", "foobar.txt");
    // createFile("image/png", "mypicture.png");

    // Unique accepted code.

    public void dumpImageMetaData(Activity activity, Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = activity.getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("Lodm", "Display Name: " + displayName);

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("Lodm", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
    }

    private void createFile(Activity activity, String mimeType, String fileName) {
        final int WRITE_REQUEST_CODE = 43;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Filter to only show results that can be "opened", such as
        // a file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Create a file with the requested MIME type.
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        activity.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

}
