package com.vhn.doan.utils;

import android.content.Context;
import android.content.Intent;

import com.vhn.doan.presentation.debug.FCMTokenActivity;

/**
 * Helper class để dễ dàng mở FCMTokenActivity từ bất kỳ đâu
 *
 * Cách sử dụng:
 * FCMTokenLauncher.launch(context);
 */
public class FCMTokenLauncher {

    /**
     * Mở FCMTokenActivity
     * @param context Context hiện tại
     */
    public static void launch(Context context) {
        Intent intent = new Intent(context, FCMTokenActivity.class);
        context.startActivity(intent);
    }
}

