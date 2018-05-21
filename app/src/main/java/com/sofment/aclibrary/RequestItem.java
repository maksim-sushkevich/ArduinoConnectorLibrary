package com.sofment.aclibrary;

/**
 * project ACLibrary
 * package com.sofment.aclibrary
 *
 * Created by maxim on 5/21/18.
 * Copyright © 2018 Sofment Group. All rights reserved.
 */

public class RequestItem {
    private String mCommand;
    private ACManager.OnResponseListener mOnResponseListener;

    public RequestItem(String command, ACManager.OnResponseListener onResponseListener) {
        mCommand = command;
        mOnResponseListener = onResponseListener;
    }

    public String getCommand() {
        return mCommand;
    }

    public ACManager.OnResponseListener getOnResponseListener() {
        return mOnResponseListener;
    }
}
