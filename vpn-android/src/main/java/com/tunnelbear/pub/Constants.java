/*
This file is part of vpn-android.

vpn-android is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

vpn-android is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with vpn-android.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.tunnelbear.pub;

/**
 * Constant for intent broadcasting.
 */

public final class Constants {

    public final static String ERROR = "com.tunnelbear.vpn.VPN_ERROR";

    public static final String TYPE_VPN_UPDATE_ERROR = "com.tunnelbear.vpn.VPN_ACTION_UPDATE_ERROR";
    public static final String TYPE_VPN_UPDATE = "com.tunnelbear.vpn.VPN_ACTION_UPDATE_STATUS";
    public static final String ACTION_DISCONNECT = "com.polargrizzly.vpn.ACTION_DISCONNECT";
    public static final String ACTION_CONNECT = "com.polargrizzly.vpn.ACTION_CONNECT";
    public static final String ACTION_UPDATE_LOGGING_ENABLED = "com.polargrizzly.vpn.UPDATE_LOGGING_ENABLED";

    /*
     * Bundle keys - for marshalling params with AIDL
     */
    public static final String BUNDLE_WHITELIST = "WHITELIST",
            BUNDLE_CONFIG_ACTIVITY = "CONFIG_ACTIVITY",
            BUNDLE_CHANNEL_NAME = "CHANNEL_NAME",
            BUNDLE_NOTIF_BAR_ICON = "NOTIF_ICON",
            BUNDLE_NOTIF_STATUS_LIST = "NOTIF_STATUS_LIST",
            BUNDLE_NOTIF_ACTION_LIST = "NOTIF_ACTION_LIST",
            BUNDLE_CUSTOM_NOTIFICATION_ID = "CUSTOM_NOTIFICATION_ID",
            BUNDLE_LOGGING_ENABLED = "LOGGING_ENABLED",
            BUNDLE_ALWAYS_SHOW_DEFAULT_NOTIFICATION = "ALWAYS_SHOW_DEFAULT_NOTIFICATION",
            BUNDLE_MAX_CONNECTION_ATTEMPTS = "MAX_CONNECTION_ATTEMPTS",
            BUNDLE_NETWORK_INACTIVITY_TIMEOUT = "NETWORK_INACTIVITY_TIMEOUT";
    
}
