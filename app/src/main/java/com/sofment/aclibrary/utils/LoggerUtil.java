package com.sofment.aclibrary.utils;

import android.util.Log;

/**
 * project ACLibrary
 * package com.sofment.aclibrary.utils
 *
 * Created by maxim on 5/21/18.
 * Copyright © 2018 Sofment Group. All rights reserved.
 */

public class LoggerUtil {
    public static boolean isDebug = false;

    private static final String TAG = "ACLibrary";

    public static void i(String message){
        if(isDebug) {
            Log.i(TAG, message);
        }
    }
}
