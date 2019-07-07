package com.example.boreme;

/**
 * Created by Jenkin on 7/16/2017.
 */

import androidx.browser.customtabs.CustomTabsClient;

/**
 * Callback for events when connecting and disconnecting from Custom Tabs Service.
 */
public interface ServiceConnectionCallback {
    /**
     * Called when the service is connected.
     * @param client a CustomTabsClient
     */

    /**
     * Called when the service is disconnected.
     */
    void onServiceDisconnected();

    void onServiceConnected(CustomTabsClient client);
}