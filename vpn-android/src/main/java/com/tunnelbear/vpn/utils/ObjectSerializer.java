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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Simple serializer/deserializer using the Java Serializable interface
 */
public class ObjectSerializer {

    public static String serialize(Serializable obj) throws IOException {
        if (obj == null) return "";
        ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
        objStream.writeObject(obj);
        objStream.close();
        return encodeBytes(serialObj.toByteArray());
    }

    public static Object deserialize(String str) throws IOException, ClassNotFoundException {
        if (str == null || str.length() == 0) return null;
        ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
        ObjectInputStream objStream = new ObjectInputStream(serialObj);
        return objStream.readObject();
    }

    private static String encodeBytes(byte[] bytes) {
        StringBuilder strBuf = new StringBuilder();

        for (byte aByte : bytes) {
            strBuf.append((char) (((aByte >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((aByte) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

    private static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i += 2) {
            char c = str.charAt(i);
            bytes[i / 2] = (byte) ((c - 'a') << 4);
            c = str.charAt(i + 1);
            bytes[i / 2] += (c - 'a');
        }
        return bytes;
    }
}