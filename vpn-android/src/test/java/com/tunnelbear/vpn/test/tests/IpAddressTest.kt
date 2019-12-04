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
package com.tunnelbear.vpn.test.tests

import com.tunnelbear.vpn.CidrBlock
import com.tunnelbear.vpn.management.IpAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IpAddressTest {

    @Test
    fun testFirst() {
        val test = IpAddress(CidrBlock("0.0.0.1", 1), false)
        assertEquals(0.toBigInteger(), test.firstAddress)
    }

    @Test
    fun testLast() {
        val test = IpAddress(CidrBlock("0.0.0.1", 1), false)
        assertEquals(2147483647.toBigInteger(), test.lastAddress)
    }

    @Test
    fun testSplit() {
        val test = IpAddress(CidrBlock("192.168.1.1", 1), false)
        val split = test.split()
        assertEquals(2, split.size)
        val second = split[1]
        assertTrue(second.lastAddress == test.lastAddress)
    }

}