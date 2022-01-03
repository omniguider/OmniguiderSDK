package com.omni.navisdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

public class OmniTextInputEditText extends TextInputEditText {

    public interface OnOmniEditTextActionListener {
        void onSoftKeyboardDismiss();
        void onTouch(MotionEvent event);
    }

    private Context mContext;
    private OnOmniEditTextActionListener mListener;
    private boolean mIsShowKeyboard = true;

    public OmniTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        this.setFocusable(false);

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsShowKeyboard) {
                    OmniTextInputEditText.this.setFocusable(true);
                    OmniTextInputEditText.this.setFocusableInTouchMode(true);
                    mListener.onTouch(event);
                    return false;
                } else {
                    return true;
                }
            }
        });

        this.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e("@W@", "actionId : " + actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    OmniTextInputEditText.this.setFocusable(false);

                    mListener.onSoftKeyboardDismiss();
                } else if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    OmniTextInputEditText.this.setFocusable(false);

                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);

                    mListener.onSoftKeyboardDismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);

            this.setFocusable(false);

            mListener.onSoftKeyboardDismiss();

            return true;
        }
        Log.e("@W@", "keyCode : " + keyCode);
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnOmniEditTextActionListener(OnOmniEditTextActionListener listener) {
        mListener = listener;
    }

    public void setKeyboardShow(boolean shouldShow) {
        mIsShowKeyboard = shouldShow;
    }

}
