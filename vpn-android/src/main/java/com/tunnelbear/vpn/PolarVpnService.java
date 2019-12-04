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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tunnelbear.pub.Constants;
import com.tunnelbear.pub.aidl.VpnConnectionStatus;
import com.tunnelbear.pub.aidl.VpnServerItem;
import com.tunnelbear.vpn.management.IpAddress;
import com.tunnelbear.vpn.management.ManagementHandler;
import com.tunnelbear.vpn.models.VpnConfig;
import com.tunnelbear.vpn.models.VpnConnectionConfig;
import com.tunnelbear.vpn.utils.DeviceHelper;

import java.util.ArrayList;
import java.util.Collection;

import static com.tunnelbear.pub.Constants.ACTION_DISCONNECT;
import static com.tunnelbear.pub.Constants.ACTION_UPDATE_LOGGING_ENABLED;
import static com.tunnelbear.pub.Constants.TYPE_VPN_UPDATE;
import static com.tunnelbear.pub.Constants.TYPE_VPN_UPDATE_ERROR;

/**
 * {@link VpnService} implementation.
 */
public class PolarVpnService extends VpnService implements IVpnThreadListener, TunnelConfigListener {

    /*
     * Configurable extras for setting up VPN intent and ongoing notification.
     */
    public static final String EXTRA_OPTIONAL_ARGUMENTS = "EXTRA_OPTIONAL_ARGUMENTS",
            EXTRA_VPN_SERVERS = "EXTRA_VPN_SERVERS",
            EXTRA_VPN_TOKEN = "EXTRA_VPN_TOKEN",
            EXTRA_CLASSNAME = "EXTRA_CLASS_LAUNCHER",
            EXTRA_APPS_WHITELIST = "EXTRA_APPS_WHITELIST",
            EXTRA_ICON_VPN_ID = "EXTRA_ICON_VPN_ID",
            EXTRA_STATUS_LIST = "EXTRA_STATUS_LIST", // "Initializing, Connecting, Reconnecting, Connected, Disconnected" (or custom equivalents) -- in order!
            EXTRA_ICON_DISCONNECT_ID = "EXTRA_ICON_DISCONNECT_ID",
            EXTRA_NOTIF_ACTION_LIST = "EXTRA_NOTIF_ACTION_LIST", // "Disconnect", "OK", etc
            EXTRA_NOTIF_CHANNEL_DISPLAY_NAME = "EXTRA_CHANNEL_NAME",
            EXTRA_CUSTOM_NOTIFICATION_ID = "EXTRA_CUSTOM_NOTIFICATION_ID",
            EXTRA_LOGGING_ENABLED = "EXTRA_LOGGING_ENABLED",
            EXTRA_ALWAYS_SHOW_DEFAULT_NOTIFICATION = "EXTRA_ALWAYS_SHOW_DEFAULT_NOTIFICATION";
    /*
     * Intent extras used to broadcast VPN status back to {@link VpnRemoteService}
     */
    public static final String EXTRA_BROADCAST_VPN_STATUS = "EXTRA_BROADCAST_VPN_STATUS",
            EXTRA_BROADCAST_SPEED = "EXTRA_SPEED",
            EXTRA_BROADCAST_DATA = "EXTRA_DATA_USE";

    private static final String TAG = "PolarVpnService";
    private static final String CHANNEL_ID = "vpn_notif_channel";
    private static final CharSequence DEFAULT_CHANNEL_NAME = "VPN Status";
    private static final int POLARGRIZZLY_DEFAULT_NOTIFICATION_ID = 1;
    private static String sCurrentConnectionStatus = VpnConnectionStatus.DISCONNECTED.toString();
    private static Notification currentCustomNotification;
    private static int currentCustomNotificationId = POLARGRIZZLY_DEFAULT_NOTIFICATION_ID;
    // Used to build VPN Notification and configure connection
    private NotificationCompat.Builder mNotificationBuilder;
    private Bundle optionalArguments;
    private ArrayList<VpnServerItem> vpnServers;
    private String vpnToken;
    private PendingIntent mConfigureIntent;
    private int notifId = R.drawable.ic_vpn_default;
    private int disconnectId = R.drawable.ic_disconnect_default; // for the "cancel/stop vpn" part of notification
    private String channelName;
    private String[] whitelistPackageNames, statusMessages, actionMessages; // whitelisted apps, vpn status custom text, notif bar custom text
    private int customNotificationId;
    private boolean loggingEnabled;
    private boolean alwaysShowDefaultNotification;

    /*
     * Management component for the VPN Service
     */
    private ManagementHandler management;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_DISCONNECT.equals(intent.getAction())) {
                disconnect(VpnConnectionStatus.DISCONNECTED);
                return START_NOT_STICKY;
            } else if (ACTION_UPDATE_LOGGING_ENABLED.equals(intent.getAction())) {
                updateLoggingEnabled(intent.getBooleanExtra(EXTRA_LOGGING_ENABLED, loggingEnabled));
                return START_NOT_STICKY;
            } else if (Constants.ACTION_CONNECT.equals(intent.getAction())) {
                connect(intent);
            }
            return START_STICKY;
        }
        // We aren't expecting this
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (management != null) {
            management.disconnect();
        }
    }

    /**
     * Get the extras from the intent to configure a connection:
     * ID integer representing the notification icons (ongoing, disconnection) we will display.
     * String representing the qualified classname we need for PendingIntent
     * (for notification and system settings menu configuration).
     * Configuration extras: whitelisted app package names, whether or not logging is enabled, etc.
     * Notification channel name (required for API >= 26).
     */
    private void prepareConnection(Intent intent) {
        int iconId = intent.getIntExtra(EXTRA_ICON_VPN_ID, 0);

        if (iconId > 0) notifId = iconId;
        if (intent.getIntExtra(EXTRA_ICON_DISCONNECT_ID, 0) > 0) disconnectId = iconId;

        optionalArguments = intent.getBundleExtra(EXTRA_OPTIONAL_ARGUMENTS);
        vpnServers = intent.getParcelableArrayListExtra(EXTRA_VPN_SERVERS);
        vpnToken = intent.getStringExtra(EXTRA_VPN_TOKEN);

        String className = intent.getStringExtra(EXTRA_CLASSNAME);
        channelName = intent.getStringExtra(EXTRA_NOTIF_CHANNEL_DISPLAY_NAME);
        whitelistPackageNames = intent.getStringArrayExtra(EXTRA_APPS_WHITELIST);
        statusMessages = intent.getStringArrayExtra(EXTRA_STATUS_LIST);
        actionMessages = intent.getStringArrayExtra(EXTRA_NOTIF_ACTION_LIST);
        customNotificationId = intent.getIntExtra(EXTRA_CUSTOM_NOTIFICATION_ID, -1);
        loggingEnabled = intent.getBooleanExtra(EXTRA_LOGGING_ENABLED, true);
        alwaysShowDefaultNotification = intent.getBooleanExtra(
                EXTRA_ALWAYS_SHOW_DEFAULT_NOTIFICATION, false);

        /*
         * Since we're starting a new connection, set up the PendingIntent that will be used
         * in the notification bar and as the ConfigureIntent.
         */
        if (mConfigureIntent == null) {
            try {
                mConfigureIntent = buildConfigureIntent(className);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Could not find class name "
                        + intent.getStringExtra(EXTRA_CLASSNAME)
                        + " so could not finish building PendingIntent. Please specify a"
                        + " valid class name.");
            }
        }

        promoteToForeground();
    }

    /**
     * In the event that the device is on Oreo+, promote the {@link VpnService} to the foreground.
     * Without doing so within a few second, the service would by killed by the operating system.
     */
    private void promoteToForeground() {
        Notification notification = null;
        int notificationId = POLARGRIZZLY_DEFAULT_NOTIFICATION_ID;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (customNotificationId != -1) {
                StatusBarNotification customNotification = getCustomNotification(true);
                if (customNotification != null) {
                    notification = customNotification.getNotification();
                    notificationId = customNotification.getId();

                    currentCustomNotification = notification;
                    currentCustomNotificationId = notificationId;
                }

                if (notification == null) {
                    notification = currentCustomNotification;
                    notificationId = currentCustomNotificationId;
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || alwaysShowDefaultNotification) {
            if (notification == null) {
                // Keep a reference to this NotificationBuilder so that the content text and other
                // notification details can be updated.
                mNotificationBuilder = getNotificationBuilder()
                        .setContentIntent(mConfigureIntent)
                        .setSmallIcon(notifId)
                        .setAutoCancel(false)
                        .setOnlyAlertOnce(true)
                        .setContentText(getStatusMessage(VpnConnectionStatus.INITIALIZING, false));

                if (actionMessages == null || actionMessages.length > 0) {
                    mNotificationBuilder = mNotificationBuilder.addAction(
                            R.drawable.ic_disconnect_default,
                            getActionTitle(VpnConnectionStatus.INITIALIZING),
                            (PendingIntent.getService(this, 0,
                                    new Intent(this, PolarVpnService.class)
                                            .setAction(ACTION_DISCONNECT), 0)));
                }

                notification = mNotificationBuilder.build();
            }

            // Don't delete these flags, the service would be killed without them.
            notification.flags = notification.flags | Notification.FLAG_NO_CLEAR;
            startForeground(notificationId, notification);
        }
    }

    /**
     * Connect the VPN Service. This is a private method and should only be called from within the
     * service; in order for an external component to connect to the VPN
     * service, it should call {@link android.content.Context#startService(Intent)} with an explicit
     * Intent for this service, passing {@link com.tunnelbear.pub.Constants#ACTION_CONNECT} as the
     * intent's action and letting the service handle the request.
     */
    private void connect(final Intent intent) {
        // Start OpenVPN in a new thread
        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "entering connect...");
                prepareConnection(intent);

                // Set up VPN profile and save it to a file in the cache dir.
                VpnConfig.initConfig(getApplicationContext(), vpnServers, vpnToken, optionalArguments);

                if (management != null) {
                    Log.i(TAG, "management != null");
                    management.disconnect();
                }

                management = new ManagementHandler(PolarVpnService.this,
                        getApplicationContext(), loggingEnabled);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        management.connect_new();
                    }
                }).start();
            }
        }, "connectThread");

        connectThread.setUncaughtExceptionHandler(new VpnThreadExceptionHandler(this));
        connectThread.start();
    }

    /**
     * Disconnect the VPN Service. This is a private method and should only be called from within the
     * service; in order for an external component to disconnect (or connect, or operate on) the VPN
     * service, it should call {@link android.content.Context#startService(Intent)} with an explicit
     * Intent for this service, passing {@link com.tunnelbear.pub.Constants#ACTION_DISCONNECT} as the
     * intent's action and letting the service handle the request.
     * <p>
     * As per the {@link VpnService#onRevoke()} documentation, disconnect should shut down file descriptors
     * and stop the process.
     */
    private void disconnect(final VpnConnectionStatus connectionStatus) {
        Thread disconnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (management != null) management.disconnect();
                // Update cached notification on disconnect
                StatusBarNotification customNotification = getCustomNotification(false);
                if (customNotification != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    currentCustomNotification = customNotification.getNotification();
                }

                if (!sCurrentConnectionStatus.equals(connectionStatus.toString()) ||
                        VpnConnectionStatus.ERROR == connectionStatus) {
                    sCurrentConnectionStatus = connectionStatus.toString();

                    Intent statusIntent = new Intent(PolarVpnService.this, VpnRemoteService.class)
                            .setAction(VpnRemoteService.VPN_STATUS_UPDATES)
                            .setType(TYPE_VPN_UPDATE)
                            .setPackage(getPackageName())
                            .putExtra(EXTRA_BROADCAST_VPN_STATUS, connectionStatus.toString());
                    startService(statusIntent);
                }

                stopSelf();
            }
        }, "disconnectThread");

        disconnectThread.setUncaughtExceptionHandler(new VpnThreadExceptionHandler(this));
        disconnectThread.start();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRevoke() {
        disconnect(VpnConnectionStatus.PERMISSION_REVOKED);
    }

    /**
     * Implement {@link IVpnThreadListener} interface to send error info back to calling service.
     * This error is triggered if the thread responsible for setting up OpenVpn encounters an error.
     * Errors setting up OpenVPN are fatal and the service should no longer proceed.
     *
     * @param e Error encountered in VPN thread.
     */
    @Override
    public void reportError(Throwable e) {
        Intent errorIntent = new Intent(this, VpnRemoteService.class)
                .setAction(VpnRemoteService.VPN_STATUS_UPDATES)
                .setType(TYPE_VPN_UPDATE_ERROR)
                .setPackage(getPackageName())
                .putExtra(EXTRA_BROADCAST_VPN_STATUS, e.getClass().getName());
        startService(errorIntent);
        stopSelf();
    }

    /*
     * TunnelConfigListener callbacks: the OpenVPN setup informs the VPN Service when it has
     * finished different aspects of setup and requires the service to take action.
     */

    /**
     * When OpenVPN has finished configuring connection requirements, it provides its
     * {@link TunnelConfigListener} with an {@link VpnConnectionConfig} object, containing
     * {@link CidrBlock} and DNS information, and requests from that listener a connection. In turn
     * the listener establishes a connection and returns the file descriptor of the active
     * VPN interface. (Note that the system can only have one active VPN interface at a time).
     *
     * @param currentConfig
     * @return
     * @see Builder#establish()
     */
    @Override
    public ParcelFileDescriptor onOpenTun(VpnConnectionConfig currentConfig) {
        return getVpnBuilder(currentConfig).establish();
    }

    /**
     * Called by the {@link ManagementHandler} when it requires that a File Descriptor be protected
     * from VPN connections (bypassed).
     *
     * @param fd
     * @see VpnService#protect(int)
     */
    @Override
    public boolean onProtectFileDescriptor(int fd) {
        return protect(fd);
    }

    /**
     * Update the persistent notification and send status info back to RemoteService.
     *
     * @param status
     */
    @Override
    public void onNotifyConnectionStatus(VpnConnectionStatus status) {
        if (!sCurrentConnectionStatus.equals(status.toString()) ||
                VpnConnectionStatus.ERROR == status) {
            sCurrentConnectionStatus = status.toString();

            Intent statusIntent = new Intent(this, VpnRemoteService.class)
                    .setAction(VpnRemoteService.VPN_STATUS_UPDATES)
                    .setType(TYPE_VPN_UPDATE)
                    .setPackage(getPackageName())
                    .putExtra(EXTRA_BROADCAST_VPN_STATUS, status.toString());
            startService(statusIntent);

            if (currentCustomNotification == null && (alwaysShowDefaultNotification ||
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                // Update notification content
                mNotificationBuilder.setContentText(getStatusMessage(status, false))
                        .setAutoCancel(true)
                        .mActions.clear();

                // Only show disconnect action option for non-error notif
                if (status != VpnConnectionStatus.ERROR) {
                    mNotificationBuilder.setAutoCancel(false);

                    if (actionMessages == null || actionMessages.length > 0) {
                        mNotificationBuilder.addAction(disconnectId, getActionTitle(status),
                                (PendingIntent.getService(this, 0,
                                        new Intent(this, PolarVpnService.class)
                                                .setAction(ACTION_DISCONNECT), 0)));
                    }
                } else { // Just go away - get rid of the notification, stop the service.
                    mNotificationBuilder.setContentIntent(
                            PendingIntent.getService(this, 0,
                                    new Intent(this, PolarVpnService.class)
                                            .setAction(ACTION_DISCONNECT), 0));
                }

                // Update notification with new status
                NotificationManagerCompat.from(this)
                        .notify(POLARGRIZZLY_DEFAULT_NOTIFICATION_ID, mNotificationBuilder
                                .build());
            }
        }
    }

    /**
     * Return the message to display in the {@link Notification} bar. If the user has supplied a set
     * of custom messages corresponding to {@link com.tunnelbear.pub.aidl.VpnConnectionStatus} states,
     * use those, otherwise present the
     *
     * @param status     current VpnConnectionStatus
     * @param isFailOver if improperly-formatted array, default to simple message.
     * @return
     */
    private String getStatusMessage(VpnConnectionStatus status, boolean isFailOver) {
        String message;
        if (statusMessages == null || isFailOver) {
            message = status.toString();

            // Title case
            return message.substring(0, 1).toUpperCase() + message.substring(1, message.length()).toLowerCase();
        } else {
            /*
             * This is why the order is important.
             */
            try {
                return statusMessages[status.ordinal()];
            } catch (IndexOutOfBoundsException ex) {
                if (BuildConfig.DEBUG) Log.e(TAG, "Error accessing statusMessage at index "
                        + status.ordinal()
                        + ", defaulting to simple message");
                return getStatusMessage(status, true);
            }
        }
    }

    /**
     * Return the text to display in the Notification Action bar (currently only supports
     * one action, Disconnect).
     *
     * @param status
     * @return
     */
    private String getActionTitle(VpnConnectionStatus status) {
        return (actionMessages == null
                ? getString(R.string.action_disconnect)
                : actionMessages[0]);
    }

    @Override
    public void onNotifySpeed(long speed) {
        Intent statusIntent = new Intent(this, VpnRemoteService.class)
                .setAction(VpnRemoteService.VPN_STATUS_UPDATES)
                .setType(TYPE_VPN_UPDATE)
                .setPackage(getPackageName())
                .putExtra(EXTRA_BROADCAST_SPEED, speed);
        startService(statusIntent);
    }

    @Override
    public void onNotifyData(long data) {
        Intent statusIntent = new Intent(this, VpnRemoteService.class)
                .setAction(VpnRemoteService.VPN_STATUS_UPDATES)
                .setType(TYPE_VPN_UPDATE)
                .setPackage(getPackageName())
                .putExtra(EXTRA_BROADCAST_DATA, data);
        startService(statusIntent);
    }

    private StatusBarNotification getCustomNotification(boolean cancel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (customNotificationId != -1) {
                try {
                    NotificationManager notificationManager = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

                    for (StatusBarNotification current : activeNotifications) {
                        if (current.getId() == customNotificationId) {
                            if (cancel) {
                                // Cancel the notification so that we avoid duplicates when we start the
                                // foreground service with this new one.
                                notificationManager.cancel(current.getTag(), current.getId());
                            }

                            return current;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception getCustomNotification: " + e.getClass() + " :: " + e.getMessage());
                }
            }
        }

        return null;
    }

    /**
     * Return Notification.Builder, creating new channel for notifications for API >= {@link Build.VERSION_CODES#O}
     *
     * @return Builder for persistent service notification
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID,
                    (channelName != null) ? channelName : DEFAULT_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) {
                mgr.createNotificationChannel(mChannel);
            }
        }
        return new NotificationCompat.Builder(PolarVpnService.this, CHANNEL_ID);
    }


    private PendingIntent buildConfigureIntent(String qualifiedClassName) throws ClassNotFoundException {
        PendingIntent pendingIntent;
        Class qualifiedClass = Class.forName(qualifiedClassName);

        Intent activityIntent = new Intent(this, qualifiedClass)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }


    /**
     * Set up the {@link VpnService.Builder} with the correct configuration based on both the
     * {@link VpnConnectionConfig} and the extra specifications provided via the connection intent
     * that launches this Service.
     *
     * @param currentConfig
     * @return
     * @see VpnService.Builder
     */
    private Builder getVpnBuilder(VpnConnectionConfig currentConfig) {
        Builder builder = new Builder();

        CidrBlock localIp = currentConfig.getLocalIp();
        String localIpv6 = currentConfig.getLocalIPv6();

        if (localIp == null && localIpv6 == null) {
            Log.e(TAG, "No ip address");
            return null;
        }

        if (localIp != null) {
            builder.addAddress(localIp.getIp(), localIp.getLength());
        }

        if (localIpv6 != null) {
            String[] ipv6parts = localIpv6.split("/");
            builder.addAddress(ipv6parts[0], Integer.parseInt(ipv6parts[1]));
        }

        for (String dns : currentConfig.getDnslist()) {
            builder.addDnsServer(dns);
        }

        builder.setMtu(currentConfig.getMtu());

        Collection<IpAddress> positiveIPv4Routes = currentConfig.getRoutes().getPositiveIPList();
        Collection<IpAddress> positiveIPv6Routes = currentConfig.getRoutesv6().getPositiveIPList();


        for (IpAddress route : positiveIPv4Routes) {
            try {
                builder.addRoute(route.getIPv4Address(), route.getNetworkMask());
            } catch (IllegalArgumentException ia) {
                Log.e(TAG, "Route rejected" + route + " " + ia.getLocalizedMessage());
            }
        }

        for (IpAddress route6 : positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.getNetworkMask());
            } catch (IllegalArgumentException ia) {
                Log.e(TAG, "Route rejected" + route6 + " " + ia.getLocalizedMessage());
            }
        }

        // Reset information
        currentConfig.reset();

        // To allow users to manage the VPN from the phone's system settings menu
        builder.setConfigureIntent(mConfigureIntent);

        // Session name is marked as an optional field when establishing a VPN. It seems that may
        // not be the case for all Android devices/versions.
        int applicationNameId = getApplicationInfo().labelRes;
        String session = applicationNameId == 0 ? getApplicationInfo().nonLocalizedLabel.toString() :
                getString(applicationNameId);
        builder.setSession(session);

        /*
         * Application whitelisting: If we have the ability to exclude traffic on a per-app basis,
         * exclude on our own from being tunneled, as well as the whitelisted app names provided
         * by the client.
         */
        if (DeviceHelper.doesSupportVpnBypass()) {
            try {
                // TODO add this back in once we have captive portal support in the SDK
//                builder.addDisallowedApplication(getPackageName());

                if (whitelistPackageNames != null && whitelistPackageNames.length > 0) {
                    for (String canonicalPackageName : whitelistPackageNames) {
                        if (BuildConfig.DEBUG) Log.d(TAG, "Whitelisting " + canonicalPackageName);
                        builder.addDisallowedApplication(canonicalPackageName);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return builder;
    }

    private void updateLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
        if (management != null) management.updateLoggingEnabled(enabled);
    }
}
