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
package com.tunnelbear.vpn.models;

import android.util.Log;

import com.tunnelbear.vpn.CidrBlock;
import com.tunnelbear.vpn.management.IpAddress;
import com.tunnelbear.vpn.management.NetworkSpace;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by raed on 2018-05-14.
 */

public class VpnConnectionConfig {

    public static final String VPNSERVICE_TUN = "vpnservice-tun";
    private static String TAG = "VpnConnectionConfig";
    private final NetworkSpace mRoutes = new NetworkSpace();
    private final NetworkSpace mRoutesv6 = new NetworkSpace();
    private List<String> mDnslist = new ArrayList<>();
    private CidrBlock mLocalIP = null;
    private String mLocalIPv6 = null;
    private int mMTU;
    private String mRemoteGW;

    public void addDNS(String dns) {
        mDnslist.add(dns);
    }

    public List<String> getDnslist() {
        return mDnslist;
    }

    public void addRoute(CidrBlock route, boolean include) {
        mRoutes.addIPv4(route, include);
    }

    public void addRoute(String dest, String mask, String gateway, String device) {
        CidrBlock route = CidrBlock.fromMask(dest, mask);
        boolean include = isAndroidTunDevice(device);

        IpAddress gatewayIP = new IpAddress(new CidrBlock(gateway, 32), false);

        if (mLocalIP == null) {
            Log.e(TAG, "Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses. Opening tun device is most likely going to fail.");
            return;
        }
        IpAddress localNet = new IpAddress(mLocalIP, true);
        if (localNet.containsNet(gatewayIP)) {
            include = true;
        }

        if (gateway != null && (gateway.equals("255.255.255.255") || gateway.equals(mRemoteGW))) {
            include = true;
        }

        if (route.getLength() == 32 && !mask.equals("255.255.255.255")) {
            Log.w(TAG, "route_not_cidr" +  dest + " : " + mask);
        }

        if (route.normalise())
            Log.w(TAG, "route_not_netip" + " : " +  dest + " : " + route.getLength() + " : " + route.getIp());

        mRoutes.addIPv4(route, include);
    }

    public NetworkSpace getRoutes() {
        return mRoutes;
    }

    private boolean isAndroidTunDevice(String device) {
        return device != null &&
                (device.startsWith("tun") || "(null)".equals(device) || VPNSERVICE_TUN.equals(device));
    }

    public void addRoutev6(String network, String device) {
        // Tun is opened after ROUTE6, no device name may be present
        boolean included = isAndroidTunDevice(device);
        addRoutev6(network, included);
    }

    private void addRoutev6(String network, boolean included) {
        String[] v6parts = network.split("/");

        try {
            Inet6Address ip = (Inet6Address) InetAddress.getAllByName(v6parts[0])[0];
            int mask = Integer.parseInt(v6parts[1]);
            mRoutesv6.addIPv6(ip, mask, included);

        } catch (UnknownHostException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public NetworkSpace getRoutesv6() {
        return mRoutesv6;
    }

    public CidrBlock getLocalIp() {
        return mLocalIP;
    }

    public void setLocalIP(CidrBlock cdrip) {
        mLocalIP = cdrip;
    }

    public int getMtu() {
        return mMTU;
    }

    public void setMtu(int mMTU) {
        this.mMTU = mMTU;
    }

    public String getLocalIPv6() {
        return mLocalIPv6;
    }

    public void setLocalIPv6(String mLocalIPv6) {
        this.mLocalIPv6 = mLocalIPv6;
    }

    public void reset() {
        mDnslist.clear();
        mRoutes.clear();
        mRoutesv6.clear();

        mLocalIP = null;
        mLocalIPv6 = null;
    }
}
