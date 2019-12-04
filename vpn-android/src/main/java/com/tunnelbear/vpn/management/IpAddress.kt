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

import com.tunnelbear.vpn.CidrBlock
import java.math.BigInteger
import java.net.Inet6Address
import java.util.*

class IpAddress : Comparable<IpAddress> {
    private var netAddress: BigInteger
    var networkMask: Int
    var included: Boolean
        private set
    private var isV4 = false
    val firstAddress: BigInteger by lazy {
        getMaskedAddress(false)
    }
    val lastAddress: BigInteger by lazy {
        getMaskedAddress(true)
    }

    constructor(cidrBlock: CidrBlock, include: Boolean) {
        included = include
        netAddress = BigInteger.valueOf(cidrBlock.int)
        networkMask = cidrBlock.length
        isV4 = true
    }

    constructor(address: Inet6Address, mask: Int, include: Boolean) {
        networkMask = mask
        included = include
        var s = 128
        netAddress = BigInteger.ZERO
        for (b in address.address) {
            s -= 8
            netAddress = netAddress.add(BigInteger.valueOf((b.toInt() and 0xFF).toLong()).shiftLeft(s))
        }
    }

    private constructor(baseAddress: BigInteger, mask: Int, included: Boolean, isV4: Boolean) {
        netAddress = baseAddress
        networkMask = mask
        this.included = included
        this.isV4 = isV4
    }

    private fun getMaskedAddress(one: Boolean): BigInteger {
        var numAddress = netAddress
        val numBits = if (isV4) {
            32 - networkMask
        } else {
            128 - networkMask
        }
        for (i in 0 until numBits) {
            numAddress = if (one) numAddress.setBit(i) else numAddress.clearBit(i)
        }
        return numAddress
    }

    fun split(): Array<IpAddress> {
        val first = IpAddress(firstAddress, networkMask + 1, included, isV4)
        val second = IpAddress(first.lastAddress.add(BigInteger.ONE), networkMask + 1, included, isV4)
        return arrayOf(first, second)
    }

    val iPv4Address: String
        get() {
            val ip: Long = netAddress.toLong()
            return String.format(Locale.US, "%d.%d.%d.%d", (ip shr 24) % 256, (ip shr 16) % 256, (ip shr 8) % 256, ip % 256)
        }

    val iPv6Address: String
        get() {
            var r = netAddress
            var ipv6str: String? = null
            var lastPart = true
            while (r.compareTo(BigInteger.ZERO) == 1) {
                val part: Long = r.mod(BigInteger.valueOf(0x10000)).toLong()
                if (ipv6str != null || part != 0L) {
                    if (ipv6str == null && !lastPart) ipv6str = ":"
                    val format = if (lastPart) "%x" else "%x:%s"
                    ipv6str = String.format(Locale.US, format, part, ipv6str)
                }
                r = r.shiftRight(16)
                lastPart = false
            }
            return ipv6str ?: "::"
        }

    fun containsNet(network: IpAddress): Boolean {
        val ourFirst = firstAddress
        val ourLast = lastAddress
        val netFirst = network.firstAddress
        val netLast = network.lastAddress
        val a = ourFirst.compareTo(netFirst) != 1
        val b = ourLast.compareTo(netLast) != -1
        return a && b
    }

    override fun toString(): String {
        val realIp = if (isV4) iPv4Address else iPv6Address
        return String.format(Locale.US, "%s/%d", realIp, networkMask)
    }

    override fun compareTo(other: IpAddress): Int {
        val comp = firstAddress.compareTo(other.firstAddress)
        return when {
            comp != 0 -> comp
            networkMask > other.networkMask -> -1
            other.networkMask == networkMask -> 0
            else -> 1
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IpAddress) return super.equals(other)
        return networkMask == other.networkMask && other.firstAddress == firstAddress
    }

    override fun hashCode(): Int {
        var result = netAddress.hashCode()
        result = 31 * result + networkMask
        result = 31 * result + included.hashCode()
        result = 31 * result + isV4.hashCode()
        return result
    }
}