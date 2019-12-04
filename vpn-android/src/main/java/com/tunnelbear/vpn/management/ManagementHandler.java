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
package com.tunnelbear.vpn.management;

import android.content.Context;
import android.util.Log;

import com.tunnelbear.pub.aidl.VpnConnectionStatus;
import com.tunnelbear.vpn.CidrBlock;
import com.tunnelbear.vpn.TunnelConfigListener;
import com.tunnelbear.vpn.models.VpnConfig;
import com.tunnelbear.vpn.models.VpnConnectionConfig;

import net.openvpn.ovpn3.ClientAPI_Config;
import net.openvpn.ovpn3.ClientAPI_EvalConfig;
import net.openvpn.ovpn3.ClientAPI_Event;
import net.openvpn.ovpn3.ClientAPI_ExternalPKICertRequest;
import net.openvpn.ovpn3.ClientAPI_ExternalPKISignRequest;
import net.openvpn.ovpn3.ClientAPI_LogInfo;
import net.openvpn.ovpn3.ClientAPI_OpenVPNClient;
import net.openvpn.ovpn3.ClientAPI_ProvideCreds;
import net.openvpn.ovpn3.ClientAPI_Status;
import net.openvpn.ovpn3.ClientAPI_TransportStats;

/**
 * Created by raed on 2018-05-16.
 */
public class ManagementHandler extends ClientAPI_OpenVPNClient {

    static {
        System.loadLibrary("ovpn3");
    }

    private static final String TAG = "ManagementHandler";

    private TunnelConfigListener mListener;
    private Context mContext;
    private VpnConnectionConfig mCurrentConfig;

    private boolean mLoggingEnabled;

    public ManagementHandler(TunnelConfigListener listener,
                             Context context,
                             boolean loggingEnabled) {
        init_process();
        this.mContext = context;
        this.mListener = listener;
        this.mLoggingEnabled = loggingEnabled;
        this.mCurrentConfig = new VpnConnectionConfig();
    }

    public void connect_new() {
        Log.i(TAG, "connect_new");
        VpnConfig config = VpnConfig.getDefaultConfig(this.mContext);
        String configstr = config.getConfigFile();
        if (!setConfig(configstr)) {
            return;
        }
        setUserPW(config);

        StatusPoller statuspoller = new StatusPoller(5 * 1000);
        new Thread(statuspoller, "Status Poller").start();

        ClientAPI_Status status = connect();
        Log.i(TAG, "connect  finished:" + status.getStatus() + " : " + status.getMessage() + " : "+ status.getError());
        statuspoller.stop();
    }

    private boolean setConfig(String vpnconfig) {
        ClientAPI_Config config = new ClientAPI_Config();
        config.setContent(vpnconfig);

        ClientAPI_EvalConfig ec = eval_config(config);
        Log.i(TAG, " called eval_config");

        if (ec.getExternalPki()) {
            Log.d(TAG, "OpenVPN3 core assumes an external PKI config");
        }
        if (ec.getError()) {
            Log.e(TAG, "OpenVPN config file parse error: " + ec.getMessage());
            return false;
        } else {
            return true;
        }
    }

    private void setUserPW(VpnConfig config) {
        String token = config.getVpnToken();
        ClientAPI_ProvideCreds creds = new ClientAPI_ProvideCreds();
        creds.setCachePassword(true);
        creds.setPassword(token);
        creds.setUsername(token);
        provide_creds(creds);
    }

    @Override
    public boolean tun_builder_set_remote_address(String address, boolean ipv6) {
        Log.i(TAG, "tun_builder_set_remote_address");
        mCurrentConfig.setMtu(1500);
        return true;
    }

    @Override
    public boolean tun_builder_set_mtu(int mtu) {
        Log.i(TAG, "tun_builder_set_mtu");
        return true;
    }

    @Override
    public boolean tun_builder_add_dns_server(String address, boolean ipv6) {
        Log.i(TAG, "tun_builder_add_dns_server");
        mCurrentConfig.addDNS(address);
        return true;
    }

    @Override
    public boolean tun_builder_add_route(String address, int prefix_length, int metric, boolean ipv6) {
        Log.i(TAG, "tun_builder_add_route: " + address + " : " + prefix_length + " : " + metric + " : " + ipv6);
        if (address.equals("remote_host"))
            return false;
        if (ipv6)
            mCurrentConfig.addRoutev6(address + "/" + prefix_length, "tun");
        else
            mCurrentConfig.addRoute(new CidrBlock(address, prefix_length), true);
        return true;
    }

    @Override
    public boolean tun_builder_exclude_route(String address, int prefix_length, int metric, boolean ipv6) {
        Log.i(TAG, "tun_builder_exclude_route");
        return true;
    }

    @Override
    public boolean tun_builder_add_search_domain(String domain) {
        Log.i(TAG, "tun_builder_add_search_domain");
        return true;
    }

    @Override
    public int tun_builder_establish() {
        Log.i(TAG, "tun_builder_establish");
        return mListener.onOpenTun(mCurrentConfig).detachFd();
    }

    @Override
    public boolean tun_builder_set_session_name(String name) {
        Log.i(TAG, "tun_builder_set_session_name");
        return true;
    }

    @Override
    public boolean tun_builder_add_address(String address, int prefix_length, String gateway, boolean ipv6, boolean net30) {
        Log.i(TAG, "tun_builder_add_address: " + address + " : " + prefix_length + " : " + gateway + " : " + ipv6 + " : " + net30);
        if (!ipv6)
            mCurrentConfig.setLocalIP(new CidrBlock(address, prefix_length));
        else
            mCurrentConfig.setLocalIPv6(address + "/" + prefix_length);
        return true;
    }

    @Override
    public boolean tun_builder_new() {
        Log.i(TAG, "tun_builder_new");
        return true;
    }

    @Override
    public boolean tun_builder_set_layer(int layer) {
        Log.i(TAG, "tun_builder_set_layer");
        return layer == 3;
    }

    @Override
    public boolean tun_builder_reroute_gw(boolean ipv4, boolean ipv6, long flags) {
        Log.i(TAG, "tun_builder_reroute_gw");

        if (ipv4) {
            mCurrentConfig.addRoute("0.0.0.0", "0.0.0.0", "127.0.0.1", VpnConnectionConfig.VPNSERVICE_TUN);
        }

        if (ipv6) {
            mCurrentConfig.addRoutev6("::/0", VpnConnectionConfig.VPNSERVICE_TUN);
        }

        return true;
    }

    @Override
    public void external_pki_cert_request(ClientAPI_ExternalPKICertRequest certreq) {
        Log.i(TAG, "external_pki_cert_request");

    }

    @Override
    public void external_pki_sign_request(ClientAPI_ExternalPKISignRequest signreq) {
        Log.i(TAG, "external_pki_sign_request");
    }

    @Override
    public boolean socket_protect(int socket, String remote, boolean ipv6) {
        Log.i(TAG, "socket_protect");
        return mListener.onProtectFileDescriptor(socket);
    }

    @Override
    public net.openvpn.ovpn3.ClientAPI_StringVec tun_builder_get_local_networks(boolean ipv6) {
        Log.i(TAG, "tun_builder_get_local_networks");
        return new net.openvpn.ovpn3.ClientAPI_StringVec();
    }

    @Override
    public void event(ClientAPI_Event event) {
        String name = event.getName();
        if (name == null) {
            name = "";
        }
        String info = event.getInfo();
        Log.i(TAG, "event:" + name + " info: " + info);

        VpnConnectionStatus result = null;

        switch (name) {
            case "CONNECTED":
                result = VpnConnectionStatus.CONNECTED;
                break;
            case "CONNECTING":
            case "WAIT":
                result = VpnConnectionStatus.CONNECTING;
                break;
            case "RECONNECTING":
                result = VpnConnectionStatus.RECONNECTING;
                break;
            case "EXITING":
                result = VpnConnectionStatus.DISCONNECTED;
                if (info.contains("auth-failure")) {
                    mListener.onNotifyConnectionStatus(VpnConnectionStatus.AUTHENTICATION_FAILURE);
                }
                break;
        }
        if (result != null) {
            mListener.onNotifyConnectionStatus(result);
        }
    }

    @Override
    public void log(ClientAPI_LogInfo arg0) {
        String logmsg = arg0.getText();
        while (logmsg.endsWith("\n"))
            logmsg = logmsg.substring(0, logmsg.length() - 1);

        Log.i(TAG, "LOG:" + logmsg);
    }

    public void disconnect() {
        Log.i(TAG, "Stopping VPN");
        stop();
    }

    public void updateLoggingEnabled(boolean loggingEnabled) {
        mLoggingEnabled = loggingEnabled;
    }

    class StatusPoller implements Runnable {
        boolean mStopped = false;
        private long mSleeptime;
        private long mLastBytesIn = 0;
        private long mLastBytesOut = 0;

        public StatusPoller(long sleeptime) {
            mSleeptime = sleeptime;
        }

        public void run() {
            while (!mStopped) {
                try {
                    Thread.sleep(mSleeptime);
                } catch (InterruptedException ignored) {
                }
                ClientAPI_TransportStats t = transport_stats();
                long in = t.getBytesIn();
                long out = t.getBytesOut();


                long bytesInDifference = in - mLastBytesIn;
                long bytesOutDifference = out - mLastBytesOut;

                mLastBytesIn = in;
                mLastBytesOut = out;
                Log.e(TAG, "in: " + in + " out: " + out);
                mListener.onNotifyData(in + out);
                mListener.onNotifySpeed(bytesInDifference + bytesOutDifference);
            }
        }

        public void stop() {
            mStopped = true;
        }
    }
}
