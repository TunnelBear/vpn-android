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
import android.os.Build;
import android.util.Log;

import java.io.File;

/**
 * Created by raed on 2018-06-05.
 */
public class VpnHelper {

    private static final String TAG = "VpnHelper";

    public static String getBinaryName(Context context) {
        String[] abis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                Build.SUPPORTED_ABIS : new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        String piePrefix = getPiePrefix();

        for (String abi : abis) {
            String binaryName = piePrefix + "openvpn." + abi;
            File binary = new File(context.getCacheDir(), binaryName);

            if ((binary.exists() && binary.canExecute())) {
                return binaryName;
            } else {
                try {
                    if (IOHelper.copyBinaryToCacheDir(context, binaryName)) return binaryName;
                } catch (Exception e) {
                    Log.e(TAG, "copyBinaryToCacheDir failed");
                    e.printStackTrace();
                }
            }
        }

        Log.e(TAG, "Executable not found for device ABI: " + String.valueOf(abis));
        return "";
    }

    private static String getPiePrefix() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? "pie_" : "nopie_";
    }
}
