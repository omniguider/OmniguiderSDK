package com.omni.omninavi.tool;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by wiliiamwang on 21/07/2017.
 */

public class Tools {

    private static Tools mTools;

    public static Tools getInstance() {
        if (mTools == null) {
            mTools = new Tools();
        }
        return mTools;
    }

//    public void showAppHashKey(Activity activity) {
//        PackageInfo info;
//        try {
//            info = activity.getPackageManager().getPackageInfo("com.example.omniguidermac.taipeiknowledgestation", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String KeyResult = new String(Base64.encode(md.digest(), 0));//String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", KeyResult);
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }
//    }

    public static final float beaconTrigger05 = 0.7f;
    public static final float beaconTrigger2 = 2f;

    private static float beaconTrigger = beaconTrigger2;
    public float getBeaconTrigger() {
        return beaconTrigger;
    }
    public void setBeaconTrigger(float f) {
        beaconTrigger = f;
    }

    private static int beaconCD = 60;
    public int getBeaconCD() {
        return beaconCD;
    }
    public void setBeaconCD(int i) {
        beaconCD = i;
    }

    private static boolean beaconFarAwayHide = false;
    public boolean getBeaconFarAwayHide() {
        return beaconFarAwayHide;
    }
    public void setBeaconFarAwayHide(boolean b) {
        beaconFarAwayHide = b;
    }

    public static boolean shouldReloadMapWhenReconnected = true;

    public int getTabBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        } else {
            return dpToIntPx(context, 55);
        }
    }

    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public float getDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public float dpToFloatPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public int dpToIntPx(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public int convertSpToPixels(Context context, float sp) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
        return px;
    }

    public double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    public void changeDrawableBGWithColor(Context context, View view, @ColorRes int strokeColorRes, @ColorRes int fillColorRes) {
        GradientDrawable evacuationRouteShape = (GradientDrawable) view.getBackground();
        evacuationRouteShape.setColor(ContextCompat.getColor(context, fillColorRes));
        evacuationRouteShape.setStroke(dpToIntPx(context, 1), getColor(context, strokeColorRes));
    }

    public int getColor(Context context, int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    public Drawable getDrawable(Context context, int drawableId) {
        return ContextCompat.getDrawable(context, drawableId);
    }
}
