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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Model for individual country server.
 * This implements Serializable so that VpnConfig can serialize its list of servers.
 */
public class VpnServerItem implements Parcelable, Serializable {

    private String host;
    private String port;
    private String protocol;

    public VpnServerItem(String host, String port, String protocol) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(host);
        dest.writeString(port);
        dest.writeString(protocol);
    }

    public static final Creator<VpnServerItem> CREATOR = new Creator<VpnServerItem>() {
        @Override
        public VpnServerItem createFromParcel(Parcel in) {
            String host = in.readString();
            String port = in.readString();
            String protocol = in.readString();

            return new VpnServerItem(host, port, protocol);
        }

        @Override
        public VpnServerItem[] newArray(int size) {
            return new VpnServerItem[size];
        }
    };

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public boolean isUdp() {
        return "udp".equals(protocol);
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol= protocol;
    }
}
