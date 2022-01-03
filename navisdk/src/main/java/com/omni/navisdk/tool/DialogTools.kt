package com.omni.navisdk.tool

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import androidx.annotation.StringRes
import com.omni.navisdk.R
import java.lang.ref.WeakReference

class DialogTools {
    private var mProgressDialog: ProgressDialog? = null
    private var mNoNetworkDialog: AlertDialog? = null
    private fun createAlertDialog(
        context: Context?, title: String, message: String, iconRes: Int,
        positiveBtnText: String, positiveBtnClickListener: DialogInterface.OnClickListener?,
        negativeBtnText: String?, negativeBtnClickListener: DialogInterface.OnClickListener?
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
        if (iconRes != -1) {
            builder.setIcon(iconRes)
        }
        if (!TextUtils.isEmpty(positiveBtnText) && positiveBtnClickListener != null) {
            builder.setPositiveButton(positiveBtnText, positiveBtnClickListener)
        }
        if (!TextUtils.isEmpty(negativeBtnText) && negativeBtnClickListener != null) {
            builder.setNegativeButton(negativeBtnText, negativeBtnClickListener)
        }
        return builder.create()
    }

    fun showProgress(activity: Activity?) {
        activity!!.runOnUiThread {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog(activity)
                mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                mProgressDialog!!.setCanceledOnTouchOutside(false)
                mProgressDialog!!.setCancelable(false)
                mProgressDialog!!.setMessage(null)
            }
            val weakContext = WeakReference(activity)
            if (activity != null && !activity.isFinishing && !activity.isDestroyed
                && weakContext.get() != null && !mProgressDialog!!.isShowing
            ) {
                try {
                    mProgressDialog!!.show()
                } catch (e: IllegalArgumentException) {
                    // Handle or log or ignore
                } catch (e: Exception) {
                    // Handle or log or ignore
                }
            }
        }
    }

    fun dismissProgress(activity: Activity?) {
        activity!!.runOnUiThread {
            val weakContext = WeakReference(activity)
            if (activity != null && !activity.isFinishing && weakContext.get() != null && mProgressDialog != null && mProgressDialog!!.isShowing) {
                try {
                    mProgressDialog!!.dismiss()
                } catch (e: IllegalArgumentException) {
                    // Handle or log or ignore
                } catch (e: Exception) {
                    // Handle or log or ignore
                }
                mProgressDialog = null
            }
        }
    }

    fun showNoNetworkMessage(context: Context?) {
        if (mNoNetworkDialog == null) {
            mNoNetworkDialog = createAlertDialog(
                context,
                context!!.getString(R.string.error_dialog_title_text_no_network),
                context.getString(R.string.error_dialog_message_text_no_network),
                -1,
                context.resources.getString(R.string.dialog_button_ok_text),
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() },
                null,
                null
            )
        }
        val weakContext = WeakReference(context)
        if (context != null && !(context as Activity).isFinishing && weakContext.get() != null && !mNoNetworkDialog!!.isShowing) {
            mNoNetworkDialog!!.show()
        }
    }

    fun createErrorMessageDialog(
        context: Context?,
        @StringRes titleRes: Int,
        message: String
    ): Dialog? {
        return if (context != null && !(context as Activity).isFinishing) {
            createAlertDialog(
                context,
                context.getResources().getString(titleRes),
                message,
                -1,
                context.getResources().getString(R.string.dialog_button_ok_text),
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() },
                null,
                null
            )
        } else null
    }

    fun showErrorMessage(context: Context, @StringRes titleRes: Int, @StringRes messageRes: Int) {
        showErrorMessage(
            context,
            context.resources.getString(titleRes),
            context.resources.getString(messageRes),
            null
        )
    }

    fun showErrorMessage(context: Context, @StringRes titleRes: Int, message: String) {
        showErrorMessage(context, context.resources.getString(titleRes), message, null)
    }

    fun showErrorMessage(
        context: Context,
        @StringRes titleRes: Int,
        @StringRes message: Int,
        dismissListener: DialogInterface.OnDismissListener?
    ) {
        showErrorMessage(
            context,
            context.resources.getString(titleRes),
            context.resources.getString(message),
            -1,
            dismissListener
        )
    }

    fun showErrorMessage(
        context: Context,
        @StringRes titleRes: Int,
        message: String,
        dismissListener: DialogInterface.OnDismissListener?
    ) {
        showErrorMessage(
            context,
            context.resources.getString(titleRes),
            message,
            -1,
            dismissListener
        )
    }

    @JvmOverloads
    fun showErrorMessage(
        context: Context?,
        title: String,
        message: String,
        dismissListener: DialogInterface.OnDismissListener? = null
    ) {
        showErrorMessage(context, title, message, -1, dismissListener)
    }

    fun showErrorMessage(
        context: Context?,
        title: String,
        message: String,
        iconRes: Int,
        dismissListener: DialogInterface.OnDismissListener?
    ) {
        val weakContext = WeakReference(context)
        if (context != null && !(context as Activity).isFinishing && weakContext.get() != null) {
            context.runOnUiThread {
                val dialog: Dialog = createAlertDialog(
                    context,
                    title,
                    message,
                    iconRes,
                    context.getResources().getString(R.string.dialog_button_ok_text),
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() },
                    null,
                    null
                )
                if (dismissListener != null) {
                    dialog.setOnDismissListener(dismissListener)
                }
                dialog.show()
            }
        }
    }

    fun showHintDialog(activity: Activity?, @StringRes contentRes: Int) {
        showErrorMessage(
            activity, activity!!.resources.getString(R.string.dialog_title_hint),
            activity!!.resources.getString(contentRes), -1, null
        )
    }

    fun showHintDialog(activity: Activity?, title: String?, content: String?) {
        showErrorMessage(
            activity, title!!, content!!, -1, null
        )
    }

    companion object {
        private var mDialogTools: DialogTools? = null
        val instance: DialogTools
            get() {
                if (mDialogTools == null) {
                    mDialogTools = DialogTools()
                }
                return mDialogTools!!
            }
    }
}