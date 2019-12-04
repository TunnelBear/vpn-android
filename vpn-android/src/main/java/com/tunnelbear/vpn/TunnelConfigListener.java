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

import android.os.ParcelFileDescriptor;

import com.tunnelbear.pub.aidl.VpnConnectionStatus;
import com.tunnelbear.vpn.models.VpnConnectionConfig;

/**
 * Created by raed on 2018-05-14.
 */

public interface TunnelConfigListener {

    ParcelFileDescriptor onOpenTun(VpnConnectionConfig currentConfig);

    boolean onProtectFileDescriptor(int fd);

    void onNotifyConnectionStatus(VpnConnectionStatus status);

    void onNotifySpeed(long speed);

    void onNotifyData(long data);
}
