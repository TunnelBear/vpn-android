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

import com.tunnelbear.pub.aidl.VpnServerItem
import com.tunnelbear.vpn.utils.ObjectSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.io.Serializable

class ObjectSerializerTest {
    /* Dummy class that implements Serializable */
    class SerializeMe(var foo: String? = null,
                      var bar: Int? = null,
                      var myBool: Boolean = true,
                      var innerClass: SerializeMe? = null) : Serializable

    class VpnServerContainer(var servers: List<VpnServerItem> = emptyList()) : Serializable

    @Test
    fun nullSerializesToEmptyString() {
        assertEquals("", ObjectSerializer.serialize(null))
    }

    @Test
    fun deserializeNullOrEmptyStringReturnsNull() {
        assertNull(ObjectSerializer.deserialize(null))
        assertNull(ObjectSerializer.deserialize(""))
    }

    @Test(expected = IOException::class)
    fun deserializeGarbageStringThrowsIOException() {
        ObjectSerializer.deserialize("asdfasdfasdfasdfasdf")
    }

    @Test
    fun serializeStringAndInt() {
        // serialize a dummy object to a string:
        val dummyFoo = "this is foo"
        val dummyBar = 42
        val serialized = ObjectSerializer.serialize(SerializeMe(dummyFoo, dummyBar))

        // deserialize back to an object, and make sure we get the same values out:
        val newObj: SerializeMe = ObjectSerializer.deserialize(serialized) as SerializeMe
        assertEquals(dummyFoo, newObj.foo)
        assertEquals(dummyBar, newObj.bar)
        assertNull(newObj.innerClass)
    }

    @Test
    fun serializeInnerObject() {
        // serialize an object + an inner object to a string:
        val dummyFoo = "this is foo"
        val dummyBar = 42
        val dummyInnerClass = SerializeMe("asdf", 1234)
        val serialized = ObjectSerializer.serialize(SerializeMe(dummyFoo, dummyBar, true, dummyInnerClass))

        // deserialize, and make sure we get the same values out:
        val newObj: SerializeMe = ObjectSerializer.deserialize(serialized) as SerializeMe
        assertEquals(dummyFoo, newObj.foo)
        assertEquals(dummyBar, newObj.bar)
        assertTrue(newObj.myBool)
        assertEquals("asdf", newObj.innerClass?.foo)
        assertEquals(1234, newObj.innerClass?.bar)
    }

    @Test
    fun serializeContainerWithVpnServerItems() {
        val server1 = VpnServerItem("192.168.1.1", "443", "udp")
        val server2 = VpnServerItem("192.168.1.200", "444", "tcp")
        val serialized = ObjectSerializer.serialize(VpnServerContainer(listOf(server1, server2)))

        val newObj : VpnServerContainer = ObjectSerializer.deserialize(serialized) as VpnServerContainer
        assertEquals(2, newObj.servers.size)
        assertServersEqual(server1, newObj.servers[0])
        assertServersEqual(server2, newObj.servers[1])
    }

    /**
     * don't rely on VpnServerItem's equals() implementation - check it here
     */
    private fun assertServersEqual(server1: VpnServerItem, server2: VpnServerItem) {
        assertEquals(server1.host, server2.host)
        assertEquals(server1.port, server2.port)
        // TODO: assertEquals(server1.protocol, server2.protocol)
    }
}