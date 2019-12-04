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
package com.tunnelbear.vpn.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

/**
 * Created by raed on 2018-04-23.
 */

public final class DeviceHelper {

    /**
     * Determines whether we have the ability to exclude application traffic from going through the
     * VPN connection. The API we use in VpnService to exclude traffic on a per-app basis is
     * only available for {@code Build.VERSION_CODES.LOLLIPOP} and higher.
     */
    public static boolean doesSupportVpnBypass() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Checks if the application is installed on external storage by testing if the
     * ApplicationInfo.FLAG_EXTERNAL_STORAGE flag is set.
     */
    public static boolean isInstalledOnExternalMemory(Context c) {
        return (c.getApplicationInfo().flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
    }
}
