package com.phonereplay.tasklogger.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.View;

public class BitmapUtils {

    private static Bitmap captureAndResizeView(Bitmap originalBitmap, int originalWidth, int originalHeight, int newHeight) {
        //if (this.orientation) {
        //  return resizeBitmap(rotateBitmap(originalBitmap, 90), mainWidth, mainHeight);
        //}
        float aspectRatio = (float) originalWidth / originalHeight;
        int newWidth = Math.round(newHeight * aspectRatio);

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }

    public static Bitmap convertViewToDrawable(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(canvas);
        return captureAndResizeView(bitmap, view.getMeasuredWidth(), view.getMeasuredHeight(), view.getMeasuredHeight());
    }

    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

    public Bitmap resizeBitmap(Bitmap original, int newWidth, int newHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(original, 0, 0, width, height, matrix, false);
    }
}
