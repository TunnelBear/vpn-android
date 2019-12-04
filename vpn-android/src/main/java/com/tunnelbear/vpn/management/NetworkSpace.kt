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
package com.tunnelbear.vpn.management

import android.os.Build
import com.tunnelbear.vpn.CidrBlock
import java.net.Inet6Address
import java.util.*

class NetworkSpace {
    private val ipAddresses = TreeSet<IpAddress>()

    fun clear() {
        ipAddresses.clear()
    }

    fun addIPv4(cidrBlock: CidrBlock, include: Boolean) {
        ipAddresses.add(IpAddress(cidrBlock, include))
    }

    fun addIPv6(address: Inet6Address, mask: Int, included: Boolean) {
        ipAddresses.add(IpAddress(address, mask, included))
    }

    val positiveIPList: Collection<IpAddress>
        get() {
            val sortedIps = generateIPList()
            val ips: Vector<IpAddress> = Vector(sortedIps.filter {
                it.included
            })
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                for (origIp in ipAddresses) {
                    if (!origIp.included) continue
                    if (sortedIps.contains(origIp)) continue
                    var skipIp = false
                    for (calculatedIp in sortedIps) {
                        if (!calculatedIp.included && origIp.containsNet(calculatedIp)) {
                            skipIp = true
                            break
                        }
                    }
                    if (skipIp) continue
                    ips.add(origIp)
                }
            }
            return ips
        }

    private fun generateIPList(): TreeSet<IpAddress> {
        val networks = PriorityQueue(ipAddresses)
        val ipsDone = TreeSet<IpAddress>()
        var currentNet: IpAddress? = networks.poll() ?: return ipsDone
        while (currentNet != null) {
            val nextNet = networks.poll()
            if (nextNet == null || currentNet.lastAddress.compareTo(nextNet.firstAddress) == -1) {
                ipsDone.add(currentNet)
                currentNet = nextNet
            } else {
                if (currentNet.firstAddress == nextNet.firstAddress &&
                        currentNet.networkMask >= nextNet.networkMask) {
                    if (currentNet.included == nextNet.included) {
                        currentNet = nextNet
                    } else {
                        val newNets = nextNet.split()
                        if (!networks.contains(newNets[1])) networks.add(newNets[1])
                        if (newNets[0].lastAddress != currentNet.lastAddress) {
                            if (!networks.contains(newNets[0])) networks.add(newNets[0])
                        }
                    }
                } else {
                    if (currentNet.included != nextNet.included) {
                        val newNets = currentNet.split()
                        if (newNets[1].networkMask != nextNet.networkMask) {
                            networks.add(newNets[1])
                        }
                        networks.add(nextNet)
                        currentNet = newNets[0]
                    }
                }
            }
        }
        return ipsDone
    }
}