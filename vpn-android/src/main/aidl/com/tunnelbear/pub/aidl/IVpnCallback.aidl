// IVpnCallback.aidl
package com.tunnelbear.pub.aidl;

// Important information:
// This interface is exposed to the TunnelBear SDK. Modifying this file will likely break the SDK.
// Ensure that any changes are backward-compatible.
interface IVpnCallback {

    oneway void onStatusUpdate(in String status);
    oneway void onError(in String error);
}
