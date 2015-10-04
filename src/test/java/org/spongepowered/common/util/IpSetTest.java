/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpSetTest {
    @Test
    public void testIpv4Set() throws UnknownHostException {
        IpSet spec = IpSet.fromCidr("10.42.0.0/16");
        assertTrue(spec.apply(InetAddress.getByName("10.42.2.5")));
        assertFalse(spec.apply(InetAddress.getByName("10.43.2.5")));
    }

    @Test
    public void testIpv6Set() throws UnknownHostException {
        IpSet spec = IpSet.fromCidr("[fc00::]/8");
        assertTrue(spec.apply(InetAddress.getByName("fcc0:c0b2:2a14:7afc:5216:1854:1a2f:2c13")));
        spec = IpSet.fromCidr("::/0");
        assertTrue(spec.apply(InetAddress.getByName("::dead:beef")));
    }

    @Test
    public void testNonByteAlignedSets() throws UnknownHostException {
        IpSet spec = IpSet.fromCidr("[2064:45:300::]/40");
        assertTrue(spec.apply(InetAddress.getByName("2064:45:310::cafe")));
        assertFalse(spec.apply(InetAddress.getByName("2064:45:410::cafe")));
    }

    @Test
    public void testFullLengthSets() throws UnknownHostException {
        IpSet specv4 = IpSet.fromCidr("10.0.0.1/32");
        assertTrue(specv4.apply(InetAddress.getByName("10.0.0.1")));
        assertFalse(specv4.apply(InetAddress.getByName("10.0.0.2")));
    }
}
