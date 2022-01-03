package com.omni.navisdk.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

public class NameCircleViewMaker {

    public static final int BIG_CIRCLE_VIEW = 0;
    public static final int SMALL_CIRCLE_VIEW = 1;

    private static NameCircleViewMaker sNameCircleViewMaker;

    public static NameCircleViewMaker getInstance() {
        if (sNameCircleViewMaker == null) {
            sNameCircleViewMaker = new NameCircleViewMaker();
        }
        return sNameCircleViewMaker;
    }

    public Bitmap getBitmapFromView(View view) {

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);

        return bitmap;
    }
}
