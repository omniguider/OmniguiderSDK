package com.omni.omninavi.omninavi.tool;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.omni.omninavi.R;

/**
 * Created by wiliiamwang on 31/10/2017.
 */

public class DialogTools {

    private boolean isTest = false;

    private static DialogTools mDialogTools;
    private ProgressDialog mProgressDialog;
    private AlertDialog mNoNetworkDialog;

    public static DialogTools getInstance() {
        if (mDialogTools == null) {
            mDialogTools = new DialogTools();
        }
        return mDialogTools;
    }

    private AlertDialog createAlertDialog(Context context, String title, String message, int iconRes,
                                          String positiveBtnText, DialogInterface.OnClickListener positiveBtnClickListener,
                                          String negativeBtnText, DialogInterface.OnClickListener negativeBtnClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);

        if (iconRes != -1) {
            builder.setIcon(iconRes);
        }

        if (!TextUtils.isEmpty(positiveBtnText) && positiveBtnClickListener != null) {
            builder.setPositiveButton(positiveBtnText, positiveBtnClickListener);
        }

        if (!TextUtils.isEmpty(negativeBtnText) && negativeBtnClickListener != null) {
            builder.setNegativeButton(negativeBtnText, negativeBtnClickListener);
        }

        return builder.create();
    }

    public void showProgress(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(null);
        }
        if (context != null && !((Activity) context).isFinishing() && !mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void dismissProgress(Context context) {
        if (context != null && !((Activity) context).isFinishing() && mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void showNoNetworkMessage(Context context) {
        if (mNoNetworkDialog == null) {
            mNoNetworkDialog = createAlertDialog(context, "No network connection", "Checking internet connection", -1,
                    context.getResources().getString(R.string.dialog_button_ok_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    },
                    null, null);
        }
        if (context != null && !((Activity) context).isFinishing() && !mNoNetworkDialog.isShowing()) {
            mNoNetworkDialog.show();
        }
    }

    public void showTestMessage(Context context, @StringRes int titleRes, String message) {
        if (isTest) {
            showErrorMessage(context, titleRes, message);
        }
    }

    public Dialog createErrorMessageDialog(Context context, @StringRes int titleRes, String message) {
        if (context != null && !((Activity) context).isFinishing()) {
            return createAlertDialog(context, context.getResources().getString(titleRes), message, -1,
                    context.getResources().getString(R.string.dialog_button_ok_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    },
                    null, null);
        }
        return null;
    }

    public void showErrorMessage(Context context, @StringRes int titleRes, String message) {
        showErrorMessage(context, context.getResources().getString(titleRes), message, null);
    }

    public void showErrorMessage(Context context, String title, String message) {
        showErrorMessage(context, title, message, null);
    }

    public void showErrorMessage(Context context, @StringRes int titleRes, String message, @Nullable DialogInterface.OnDismissListener dismissListener) {
        showErrorMessage(context, context.getResources().getString(titleRes), message, -1, dismissListener);
    }

    public void showErrorMessage(Context context, String title, String message, @Nullable DialogInterface.OnDismissListener dismissListener) {
        showErrorMessage(context, title, message, -1, dismissListener);
    }

    public void showErrorMessage(Context context, String title, String message, int iconRes, @Nullable DialogInterface.OnDismissListener dismissListener) {
        if (context != null && !((Activity) context).isFinishing()) {
            Dialog dialog = createAlertDialog(context, title, message, iconRes,
                    context.getResources().getString(R.string.dialog_button_ok_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    },
                    null, null);

            if (dismissListener != null) {
                dialog.setOnDismissListener(dismissListener);
            }

            dialog.show();
        }
    }
}
