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

/**
 * Use for reporting errors from {@link Thread} objects.
 * Use the {@link Thread#setDefaultUncaughtExceptionHandler(Thread.UncaughtExceptionHandler)} and
 * supply a {@link VpnThreadExceptionHandler} that reports errors back to a listener.
 * This is useful because the VpnManagementThreads need to report back to their original Service
 * any exceptions they encounter.
 */
public class VpnThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
    private IVpnThreadListener listener;

    public VpnThreadExceptionHandler(IVpnThreadListener listener) {
        this.listener = listener;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        listener.reportError(e);
    }
}
