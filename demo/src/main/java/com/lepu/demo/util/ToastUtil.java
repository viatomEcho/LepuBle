package com.lepu.demo.util;

import android.content.Context;
import android.widget.Toast;



public class ToastUtil {

    public static void showToast(Context context, String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

}
