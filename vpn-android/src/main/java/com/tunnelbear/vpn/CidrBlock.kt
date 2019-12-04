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
package com.tunnelbear.vpn

import java.util.*

class CidrBlock(var ip: String, var length: Int) {
    val int: Long
        get() = getInt(ip)

    fun normalise(): Boolean {
        val ip = getInt(ip)

        val newip = ip and (0xffffffffL shl (32 - length))
        return if (newip != ip) {
            this.ip = String.format(Locale.US, "%d.%d.%d.%d", newip and -0x1000000 shr 24, newip and 0xff0000 shr 16, newip and 0xff00 shr 8, newip and 0xff)
            true
        } else {
            false
        }
    }

    companion object {

        @JvmStatic
        fun fromMask(ip: String, mask: String): CidrBlock {
            return CidrBlock(ip, getBlockLenFromMask(mask))
        }

        private fun getBlockLenFromMask(subnetMask: String): Int {
            var netmask = getInt(subnetMask)

            netmask += 1L shl 32

            var lenZeros = 0
            while (netmask and 0x1 == 0L) {
                lenZeros++
                netmask = netmask shr 1
            }
            return if (netmask != 0x1ffffffffL shr lenZeros) {
                32
            } else {
                32 - lenZeros
            }
        }

        @JvmStatic
        fun getInt(ipAddress: String): Long {
            val parts = ipAddress.split(".")
            var ipAsLong: Long = 0

            ipAsLong += java.lang.Long.parseLong(parts[0]) shl 24
            ipAsLong += (Integer.parseInt(parts[1]) shl 16).toLong()
            ipAsLong += (Integer.parseInt(parts[2]) shl 8).toLong()
            ipAsLong += Integer.parseInt(parts[3]).toLong()

            return ipAsLong
        }
    }
}