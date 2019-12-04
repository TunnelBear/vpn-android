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
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow

class CidrBlockTest {

    @Test
    fun intShouldReturnAPowerOfTwo() {
        val zero = CidrBlock("0.0.0.0", 1).int
        assertEquals(0, zero)
        val twoToZero = CidrBlock("0.0.0.1", 1).int
        assertEquals(1, twoToZero)
        val twoToEight = CidrBlock("0.0.1.0", 1).int
        assertEquals(256, twoToEight)
        val twoToSixteen = CidrBlock("0.1.0.0", 1).int
        assertEquals(65536, twoToSixteen)
        val twoToTwentyfour = CidrBlock("1.0.0.0", 1).int
        assertEquals(16777216, twoToTwentyfour)
    }

    @Test
    fun intForAllOnes() {
        val block = CidrBlock("1.1.1.1", 1)
        // length should be 2^24 + 2^16 + 2^8 + 2^0
        val expected = (2 `**` 24) + (2 `**` 16) + (2 `**` 8) + (2 `**` 0)
        assertEquals(expected.toLong(), block.int)
    }

    @Test
    fun normalizeTrue() {
        val block = CidrBlock("192.168.1.1", 2)
        assertTrue(block.normalise())
        assertEquals("192.0.0.0", block.ip)
    }

    @Test
    fun normalizeFalse() {
        val block = CidrBlock("192.0.0.0", 2)
        assertFalse(block.normalise())
    }

    // workaround for ugly Kotlin pow() syntax that doesn't work on ints :(
    infix fun Int.`**`(exponent: Int): Int = toDouble().pow(exponent).toInt()
}