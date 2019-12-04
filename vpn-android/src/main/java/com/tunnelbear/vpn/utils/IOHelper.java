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
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by raed on 2018-04-25.
 */

public class IOHelper {

    private static final String TAG = "IOHelper";
    private static final int BUFFER_SIZE = 4096;

    public static boolean moveAssetsToCacheDir(Context context,
                                               Set<String> assetsToMove) {
        AssetManager assetManager = context.getAssets();
        String[] assetFiles;

        try {
            assetFiles = assetManager.list("");

            for (String file : assetFiles) {
                if (assetsToMove.contains(file)) {
                    try {
                        File result = new File(context.getCacheDir(), file);
                        copyToFile(result, assetManager.open(file));
                    } catch (Exception e) {
                        Log.e(TAG, "moveAssetsToCacheDir failed 1: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "moveAssetsToCacheDir failed 2: " + e.getMessage());
        }

        return false;
    }

    public static boolean copyBinaryToCacheDir(Context context,
                                               String destinationName) throws Exception {
        File destinationFile = new File(context.getCacheDir(), destinationName);
        if (destinationFile.exists() && destinationFile.canExecute()) return true;

        copyToFile(destinationFile, getBinaryInputStream(context, destinationName));
        return destinationFile.setExecutable(true);
    }

    private static InputStream getBinaryInputStream(Context context, String filename)
            throws Exception {
        if (context != null) {
            return context.getAssets().open(filename);
        } else {
            throw new IllegalArgumentException("Null context while attempting to open exec binary");
        }
    }

    private static void copyToFile(File destinationFile,
                                   InputStream inputStream) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }
}
