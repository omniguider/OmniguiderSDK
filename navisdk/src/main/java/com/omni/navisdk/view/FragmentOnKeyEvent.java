package com.omni.navisdk.view;

import android.view.KeyEvent;

public interface FragmentOnKeyEvent {
    String TOUCH_POINT = "TOUCH_POINT";
    boolean onKeyUp(int keyCode, KeyEvent event);
}

