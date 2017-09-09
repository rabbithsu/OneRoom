/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat;

import android.os.ParcelUuid;

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_XMPP_READ = 6;
    public static final int MESSAGE_XMPP_WRITE = 7;
    public static final int MESSAGE_XMPP_GROUPWRITE = 8;
    public static final int MESSAGE_XMPP_GROUPREAD = 9;

    public static final int MESSAGE_ONLINE = 98;
    public static final int MESSAGE_OFFLINE = 99;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final ParcelUuid Service_UUID = ParcelUuid
    //.fromString("");
            .fromString("0000b81d-0000-1000-8000-00805f9b34fb");
    public static final String ChatTableName = "item";
    public static final String GroupTableName = "gitem";

}
