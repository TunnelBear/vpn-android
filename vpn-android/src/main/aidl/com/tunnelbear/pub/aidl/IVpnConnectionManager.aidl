// IVpnConnectionManager.aidl
package com.tunnelbear.pub.aidl;

import com.tunnelbear.pub.aidl.VpnServerItem;
import com.tunnelbear.pub.aidl.IVpnCallback;
import android.os.Bundle;

// Important information:
// This interface is exposed to the TunnelBear SDK. Modifying this file will likely break the SDK.
// Ensure that any changes are backward-compatible.
interface IVpnConnectionManager {

    // Start a vpn connection with the following parameters:
    // List<VpnServerItem> of servers to connect to,
    // vpnToken,
    // Bundle of other optionally-configured params
    //  - fully-qualified name of an Activity to set up VpnBuilder configIntent and other PendingIntent
    //  (typically the main VPN activity screen of an app),
    //  - "channel name," describing the category of notifications from this service,
    //  - integer corresponding to an icon resource for the Notification bar (optional),
    //  - list of Strings corresponding to status messages for the Notification bar (optional),
    //  - list of Strings (of length 2) corresponding to custom notification bar action text (Disconnect/OK),
    // and Callback to report status or errors back to calling process.
    oneway void startVpn(in List<VpnServerItem> vpnServerItem,
     in String vpnToken,
     in Bundle optArgs,
     IVpnCallback callback);

    oneway void stopVpn();

    oneway void updateLoggingEnabled(in boolean loggingEnabled);
}
