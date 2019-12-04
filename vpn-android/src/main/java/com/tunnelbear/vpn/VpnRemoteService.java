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
package com.tunnelbear.vpn;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.tunnelbear.pub.Constants;
import com.tunnelbear.pub.aidl.IVpnCallback;
import com.tunnelbear.pub.aidl.IVpnConnectionManager;
import com.tunnelbear.pub.aidl.VpnConnectionStatus;
import com.tunnelbear.pub.aidl.VpnServerItem;
import com.tunnelbear.pub.error.VpnSetupError;
import com.tunnelbear.vpn.models.VpnConfig;
import com.tunnelbear.vpn.utils.DeviceHelper;
import com.tunnelbear.vpn.utils.IOHelper;
import com.tunnelbear.vpn.utils.VpnHelper;

import java.util.ArrayList;
import java.util.List;

import static com.tunnelbear.pub.Constants.BUNDLE_ALWAYS_SHOW_DEFAULT_NOTIFICATION;
import static com.tunnelbear.pub.Constants.BUNDLE_CHANNEL_NAME;
import static com.tunnelbear.pub.Constants.BUNDLE_CONFIG_ACTIVITY;
import static com.tunnelbear.pub.Constants.BUNDLE_CUSTOM_NOTIFICATION_ID;
import static com.tunnelbear.pub.Constants.BUNDLE_LOGGING_ENABLED;
import static com.tunnelbear.pub.Constants.BUNDLE_NOTIF_ACTION_LIST;
import static com.tunnelbear.pub.Constants.BUNDLE_NOTIF_BAR_ICON;
import static com.tunnelbear.pub.Constants.BUNDLE_NOTIF_STATUS_LIST;
import static com.tunnelbear.pub.Constants.BUNDLE_WHITELIST;
import static com.tunnelbear.pub.Constants.TYPE_VPN_UPDATE;
import static com.tunnelbear.pub.Constants.TYPE_VPN_UPDATE_ERROR;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_ALWAYS_SHOW_DEFAULT_NOTIFICATION;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_APPS_WHITELIST;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_BROADCAST_VPN_STATUS;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_CLASSNAME;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_CUSTOM_NOTIFICATION_ID;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_ICON_VPN_ID;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_LOGGING_ENABLED;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_NOTIF_ACTION_LIST;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_NOTIF_CHANNEL_DISPLAY_NAME;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_OPTIONAL_ARGUMENTS;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_STATUS_LIST;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_VPN_SERVERS;
import static com.tunnelbear.vpn.PolarVpnService.EXTRA_VPN_TOKEN;

/**
 * VpnRemoteService class.
 * Binds via aidl interface to client and exposes start(), stop(), and VPN connection options.
 * <p>
 * Note this is not the VPN Service.
 */
public class VpnRemoteService extends Service {

    public static final String VPN_STATUS_UPDATES = "com.tunnelbear.vpn.VPN_STATUS_UPDATES";

    private static final String TAG = "VpnRemoteService";
    private IVpnCallback callback;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && VPN_STATUS_UPDATES.equals(intent.getAction())) {
            String vpnStatus = intent.getStringExtra(EXTRA_BROADCAST_VPN_STATUS);
            if (TYPE_VPN_UPDATE_ERROR.equals(intent.getType())) {
                notifyCallbackError(vpnStatus);
            } else if (TYPE_VPN_UPDATE.equals(intent.getType())) {
                if (intent.hasExtra(EXTRA_BROADCAST_VPN_STATUS)) {
                    notifyCallbackSuccess(vpnStatus);
                }
            }

            return START_NOT_STICKY;
        } else {
            return START_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IVpnConnectionManager.Stub mBinder = new IVpnConnectionManager.Stub() {

        @Override
        public void startVpn(List<VpnServerItem> vpnServers,
                             String vpnToken,
                             Bundle optArgs,
                             IVpnCallback callback) throws RemoteException {
            VpnRemoteService.this.callback = callback;

            Intent canStartVpnIntent = VpnService.prepare(VpnRemoteService.this);
            if (canStartVpnIntent != null) {

                // Something needs fixing, either another service is started or we don't have permissions.
                // Note: we report error back to client instead of launching an activity because a) we are in a service,
                // we may not be foregrounded, and b) launching an activity disrupts the task flow.
                callback.onStatusUpdate(VpnConnectionStatus.NEEDS_VPN_PERMISSION.toString());
            } else {
                try {

                    // Make sure vpn binaries are installed in the right place
                    checkOrMoveBinaries();

                    // Get additional arguments from Bundle
                    List<String> whitelistedApps = optArgs.getStringArrayList(BUNDLE_WHITELIST);
                    String configActivityName = optArgs.getString(BUNDLE_CONFIG_ACTIVITY);
                    String channelName = optArgs.getString(BUNDLE_CHANNEL_NAME);
                    int notificationIcon = optArgs.getInt(BUNDLE_NOTIF_BAR_ICON);
                    List<String> statusNames = optArgs.getStringArrayList(BUNDLE_NOTIF_STATUS_LIST);
                    List<String> actionNames = optArgs.getStringArrayList(BUNDLE_NOTIF_ACTION_LIST);
                    int customNotificationId = optArgs.getInt(BUNDLE_CUSTOM_NOTIFICATION_ID, -1);
                    boolean loggingEnabled = optArgs.getBoolean(BUNDLE_LOGGING_ENABLED, true);
                    boolean alwaysShowDefaultNotification = optArgs.getBoolean(
                            BUNDLE_ALWAYS_SHOW_DEFAULT_NOTIFICATION, false);

                    /*
                     * With config and binaries set up, build connection intent for our VpnService.
                     *
                     * To make sure the VPN Service isn't killed, it needs to be a foreground service.
                     * As a foreground service it needs a persistent notification showing it's running.
                     * In API >= 26, these notifications require a 'Channel' name, which is visible to the user.
                     * Therefore client-implementing apps can pass a localized or custom String here to
                     * customize that aspect of the user experience, otherwise the channel name will be
                     * {@link VpnConstants.DEFAULT_CHANNEL_NAME}, which currently is "VPN".
                     */
                    Intent startVpnIntent = new Intent(VpnRemoteService.this, PolarVpnService.class)
                            .setAction(Constants.ACTION_CONNECT) // Use intent action to trigger connect/disconnect
                            .putExtra(EXTRA_OPTIONAL_ARGUMENTS, optArgs)
                            .putParcelableArrayListExtra(EXTRA_VPN_SERVERS, new ArrayList<>(vpnServers))
                            .putExtra(EXTRA_VPN_TOKEN, vpnToken)
                            .putExtra(EXTRA_NOTIF_CHANNEL_DISPLAY_NAME, channelName) // enforced non-null via VpnConnectionSpec
                            .putExtra(EXTRA_CLASSNAME, configActivityName)
                            .putExtra(EXTRA_ICON_VPN_ID, notificationIcon)
                            .putExtra(EXTRA_CUSTOM_NOTIFICATION_ID, customNotificationId)
                            .putExtra(EXTRA_LOGGING_ENABLED, loggingEnabled)
                            .putExtra(EXTRA_ALWAYS_SHOW_DEFAULT_NOTIFICATION,
                                    alwaysShowDefaultNotification);

                    if (whitelistedApps != null) startVpnIntent.putExtra(EXTRA_APPS_WHITELIST,
                            whitelistedApps.toArray(new String[whitelistedApps.size()]));

                    if (statusNames != null) startVpnIntent.putExtra(EXTRA_STATUS_LIST,
                            statusNames.toArray(new String[statusNames.size()]));

                    if (actionNames != null) startVpnIntent.putExtra(EXTRA_NOTIF_ACTION_LIST,
                            actionNames.size() > 0 ? actionNames.toArray(
                                    new String[actionNames.size()]) : new String[0]);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(startVpnIntent); // Gives 5 seconds to promote to foreground or OS will kill this service
                    } else {
                        startService(startVpnIntent);
                    }
                } catch (VpnSetupError err) {
                    notifyCallbackError(err.getMessage());
                }

            }
        }

        @Override
        public void stopVpn() {
            Log.i(TAG, "stopVpn called");
            Intent stopVpnIntent = new Intent(VpnRemoteService.this, PolarVpnService.class)
                    .setAction(Constants.ACTION_DISCONNECT);
            VpnRemoteService.this.startService(stopVpnIntent);
        }

        @Override
        public void updateLoggingEnabled(boolean loggingEnabled) {
            Intent updateLoggingIntent = new Intent(VpnRemoteService.this, PolarVpnService.class)
                    .setAction(Constants.ACTION_UPDATE_LOGGING_ENABLED)
                    .putExtra(EXTRA_LOGGING_ENABLED, loggingEnabled);

            VpnRemoteService.this.startService(updateLoggingIntent);
        }
    };

    /*
     * Called when the PolarVpnService broadcasts an error - we report it to our client through aidl callback.
     */
    private void notifyCallbackError(String vpnStatus) {
        if (callback != null) {
            try {
                callback.onError(vpnStatus);
            } catch (RemoteException ex) {
                // we can't report back - just log it :(
                Log.e(TAG, "Failed to report VPN exception to caller");
            }
        }
    }

    /*
     * Called when the PolarVpnService broadcasts non-error update - we report it to our client.
     */
    private void notifyCallbackSuccess(String vpnStatus) {
        if (callback != null) {
            try {
                callback.onStatusUpdate(vpnStatus);
            } catch (RemoteException ex) {
                Log.e(TAG, "Failed to report VPN exception to caller");
            }
        }
    }

    private void checkOrMoveBinaries() throws VpnSetupError {
        if (DeviceHelper.isInstalledOnExternalMemory(this)) {
            try {
                if (!IOHelper.copyBinaryToCacheDir(this,
                        VpnHelper.getBinaryName(getApplicationContext()))) {
                    throw new VpnSetupError("Error copying VPN executable from native lib directory to " +
                            "cache directory");
                }
                if (!VpnConfig.moveAssetsToCacheDir(this)) {
                    throw new VpnSetupError("Error copying assets to cache directory");
                }
            } catch (Exception e) { // hm @todo
                throw new VpnSetupError(e.getLocalizedMessage());
            }
        }
    }
}
