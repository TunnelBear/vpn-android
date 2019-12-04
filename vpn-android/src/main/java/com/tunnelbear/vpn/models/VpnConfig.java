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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.tunnelbear.pub.aidl.VpnServerItem;
import com.tunnelbear.vpn.utils.IOHelper;
import com.tunnelbear.vpn.utils.ObjectSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.tunnelbear.pub.Constants.BUNDLE_MAX_CONNECTION_ATTEMPTS;
import static com.tunnelbear.pub.Constants.BUNDLE_NETWORK_INACTIVITY_TIMEOUT;

public class VpnConfig implements Serializable {

    private static final String TAG = "VpnConfig";
    private static final String CA = "ca.crt";
    private static final String CLIENT_CERTIFICATE = "key.crt";
    private static final String CONFIG_RAW = "vpnconfig"; // serializing and deserializing a config object.

    /**
     * A couple of things worth addressing here:
     * <p>
     * - This key represents the local peer's private key. In order to establish a secure connection
     * with the peer, one of the options we need to bundle in includes this key. We have the option
     * to bundle it with all of our applications or retrieve it through some external configuration
     * file. For now, we've opted to continue bundling it in with our applications.
     * <p>
     * - For some of our other files, ca.crt & key.crt, we're storing them as files in our assets
     * folder and just linking against them here. By doing the same with this key however, we get a
     * security alert in the Google Play Console when uploading release builds. Instead, embedding
     * the key inside a String might help side-step this issue.
     */
    private static final String CLIENT_KEY_RAW = "";

    private static VpnConfig mConfig;

    private static String mStaticConfigs;
    private static String mCaPath;
    private static String mClientCertPath;

    private ArrayList<VpnServerItem> mVpnServers = new ArrayList<>();
    private String mVpnToken;
    private int mMaxConnectionAttempts;
    private long mNetworkInactivityTimeout;

    @SuppressLint("ApplySharedPref")
    private VpnConfig(Context context,
                      ArrayList<VpnServerItem> vpnServers,
                      String vpnToken,
                      Bundle bundle) {
        mVpnToken = vpnToken;
        for (VpnServerItem server : vpnServers) mVpnServers.add(server);
        moveAssetsToCacheDir(context);
        readStaticVpnConfig(context);

        mConfig = this;
        mMaxConnectionAttempts = bundle.getInt(BUNDLE_MAX_CONNECTION_ATTEMPTS, -1);
        mNetworkInactivityTimeout = bundle.getLong(BUNDLE_NETWORK_INACTIVITY_TIMEOUT, -1);

        // Serialize config file. This is because static fields are not shared across instances
        // of the JVM, so when one process tries to use static initConfig() to access a variable set via IPC,
        // it will always fail.
        // When we rewrite the Config files this will probably change.
        String serializedConfig = null;
        try {
            serializedConfig = ObjectSerializer.serialize(mConfig);
        } catch (IOException e) {
            Log.e(TAG, "IOException serializing config: " + e.getMessage());
        }

        // Note - if we were to use apply() over commit() we'd get a race condition.
        context.getSharedPreferences(CONFIG_RAW, MODE_PRIVATE)
                .edit()
                .putString(CONFIG_RAW, serializedConfig)
                .commit();
    }

    /**
     * Create a new {@link VpnConfig} and overwrite existing configuration.
     * Since one configuration is used at a time, prefer this over constructing a
     * config object from outside classes.
     * <p>
     * Note that config is also serialized and stored during construction so that it can be retrieved
     * by other processes--a call to {@link VpnConfig#initConfig(Context, ArrayList, String, Bundle)} overwrites
     * the previous serialization, whereas a call to {@link VpnConfig#getDefaultConfig(Context)} attempts
     * to retrieve the most recent configuration.
     *
     * @todo: the config file is already serialized elsewhere, but since it includes credentials (vpnToken) and app info (server list), review security implications of serializing.
     */
    public static VpnConfig initConfig(Context context,
                                       ArrayList<VpnServerItem> vpnServers,
                                       String vpnToken,
                                       Bundle bundle) {
        mConfig = new VpnConfig(context, vpnServers, vpnToken, bundle);
        return mConfig;
    }

    /**
     * Return the active config object.
     * If config is null, try to rebuild config based on most recent serialization.
     *
     * @param cxt
     * @return
     */
    public static VpnConfig getDefaultConfig(Context cxt) {
        if (mConfig == null) {
            try {
                String configString = cxt
                        .getSharedPreferences(CONFIG_RAW, MODE_PRIVATE)
                        .getString(CONFIG_RAW, null);
                if (configString != null) {
                    mConfig = (VpnConfig)ObjectSerializer.deserialize(configString);
                }
            } catch (ArrayIndexOutOfBoundsException | IOException | ClassNotFoundException ex) {
                // We can't rebuild the profile
                Log.e(TAG, "Exception de-serializing config: " + ex.getClass() + " : " + ex.getMessage());
            }
        }
        return mConfig;
    }

    public static boolean moveAssetsToCacheDir(Context context) {
        Set<String> assetsToMove = new HashSet<>();

        File caFile = new File(context.getCacheDir(), CA);
        File clientCertFile = new File(context.getCacheDir(), CLIENT_CERTIFICATE);

        mCaPath = caFile.getPath();
        mClientCertPath = clientCertFile.getPath();

        if (!caFile.exists()) {
            assetsToMove.add(CA);
        }

        if (!clientCertFile.exists()) {
            assetsToMove.add(CLIENT_CERTIFICATE);
        }

        return assetsToMove.size() == 0 || IOHelper.moveAssetsToCacheDir(context, assetsToMove);
    }

    /**
     * Generate a configuration file via OpenVPN's management interface.
     * <p>
     * The management interface allows OpenVPN to be controlled from an external program via sockets.
     *
     * @return
     * @see <a href="https://openvpn.net/index.php/open-source/documentation/miscellaneous/79-management-interface.html">https://openvpn.net/index.php/open-source/documentation/miscellaneous/79-management-interface.html</a>
     * @see <a href="https://community.openvpn.net/openvpn/wiki/Openvpn23ManPage">https://community.openvpn.net/openvpn/wiki/Openvpn23ManPage</a>
     */
    public String getConfigFile() {
        StringBuilder builder = new StringBuilder();

        builder.append(mStaticConfigs);

        // Specify the maximum number of retries, under certain conditions.
        builder.append("connect-retry-max " + String.valueOf(mMaxConnectionAttempts >= 0 ?
                mMaxConnectionAttempts : 10) + "\n");

        // Specify the network inactivity timeout.
        builder.append("ping-restart " + String.valueOf(mNetworkInactivityTimeout >= 0 ?
                mNetworkInactivityTimeout : 30) + "\n");

        for (VpnServerItem server : mVpnServers) {
            builder.append("remote ");
            builder.append(server.getHost());
            builder.append(" ");
            builder.append(server.getPort());
            if (server.isUdp())
                builder.append(" udp\n");
            else
                builder.append(" tcp\n");
        }

        // The following three options are used in TLS mode. Each of them are necessary for
        // establishing a verified connection with our VPN servers.

        // Root certificate.
        //builder.append("ca " + mCaPath + "\n");
        // Peer's signed certificate. Signed by root cert above.
        //builder.append("cert " + mClientCertPath + "\n");
        // Key used when the signed certificate above was generated.
        builder.append(String.format("<key>\n%s\n</key>\n", CLIENT_KEY_RAW));

        return builder.toString();
    }

    public String getVpnToken() {
        return mVpnToken;
    }

    private void readStaticVpnConfig(Context context) {
        if (TextUtils.isEmpty(mStaticConfigs)) {
            BufferedReader reader = null;
            try {
                StringBuilder builder = new StringBuilder();
                String line;

                reader = new BufferedReader(new InputStreamReader(context.getAssets()
                        .open("vpn_config.txt"), "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                mStaticConfigs = builder.toString();
            } catch (IOException e) {
                Log.e(TAG, "IOException readStaticVpnConfig: " + e.getMessage());
            }

            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException readStaticVpnConfig reader.close: " + e.getMessage());
            }
        }
    }
}
