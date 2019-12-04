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
package com.tunnelbear.pub.aidl;

/**
 * Holds the different possible states of the VPN connection.
 */
public enum VpnConnectionStatus {

    /**
     *
     * The order of these enums is important! The different statuses are used to
     * display custom messages to the user in a Notification when the VPN is on.
     *
     * A String[] of custom messages is passed to the VpnService, and the messages
     * are ordered to correspond to the ordinal values of these enums.
     *
     * So, if you change the values of these states (boo) or add more states (okay), make sure to:
     *
     *  -  Update {@code VpnConnectionSpec#getNotificationStatuses()}, and, if needed, add a corresponding
     *     status in {@code VpnConnectionSpec#NotificationStatus} that matches the new enum here,
     *  -  Update the code in {@code PolarVpnService#getStatusMessage(VpnConnectionStatus status)}
     *     that matches the String message in statusMessages to the enum value.
     *  -  Test your status messages to make sure that they are mapped correctly, i.e. your
     *     "connecting" message shows when the VPN is in a connecting state, "disconnected" when it's
     *     in a disconnected state, etc.
     *
     * Why is it this way?
     * We could have passed a Map and converted it to a  {@code Map<String, String>} or{@code Map<Status, String>},
     * but then it would need to be bundled into the intent extras anyway to send to the {@code PolarVpnService},
     * and it's not convenient to send a map that way.
     *
     */

    // These should stay in this order with these values, since the values are used to index an array of messages
    INITIALIZING(0),
    CONNECTING(1),
    RECONNECTING(2),
    CONNECTED(3),
    ERROR(4),

    // It's okay to add additional states if need be with different (higher) values
    DISCONNECTED(5),
    NEEDS_VPN_PERMISSION(6),
    PRE_CONNECTING(7),
    PERMISSION_REVOKED(8),
    AUTHENTICATION_FAILURE(9),
    DEVICE_RESTART_REQUIRED(10);

    private int value;

    VpnConnectionStatus(int value) {
        this.value = value;
    }

    public int get() {
        return value;
    }
}
