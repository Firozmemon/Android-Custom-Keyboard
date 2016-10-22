package com.firozmemon.customkeyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by firoz
 */
public class Util {
    private Util() {

    }

    public static boolean isLangSupported(Context context, String text) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        int w = 200, h = 80;
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        Bitmap orig = bitmap.copy(conf, false);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setTextSize((int) (14 * scale));

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;

        canvas.drawText(text, x, y, paint);
        boolean res = false;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            res = !(orig == bitmap);
        } else {
            res = !orig.sameAs(bitmap);
        }
        orig.recycle();
        bitmap.recycle();
        return res;
    }

    public static void displayAlert(Activity activity, String titleText, String messageText) {
        AlertDialog.Builder alertDialogBuilder =
                new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(titleText)
                .setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

}
