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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpSet implements Predicate<InetAddress> {
    private final InetAddress addr;
    private final int prefixLen;

    private IpSet(InetAddress addr, int prefixLen) {
        this.addr = addr;
        this.prefixLen = prefixLen;
    }

    @Override
    public boolean apply(InetAddress input) {
        byte[] address = input.getAddress();
        byte[] checkAddr = this.addr.getAddress();
        if (address.length != checkAddr.length) {
            return false;
        }

        byte completeSegments = (byte) (this.prefixLen >> 3);
        byte overlap = (byte) (this.prefixLen & 7);
        for (byte i = 0; i < completeSegments; ++i) {
            if (address[i] != checkAddr[i]) {
                return false;
            }
        }
        for (byte i = 0; i < overlap; ++i) {
            if (((checkAddr[completeSegments + 1] >> (7 - i)) & 0x1) != ((address[completeSegments + 1] >> (7 - i)) & 0x1)) {
                return false;
            }
        }

        return true;
    }

    public static IpSet fromAddrPrefix(InetAddress address, int prefixLen) {
        validatePrefixLength(checkNotNull(address, "address"), checkNotNull(prefixLen, "prefixLen"));
        return new IpSet(address, prefixLen);
    }

    /**
     *
     * @param spec
     * @return
     */
    public static IpSet fromCidr(String spec) {
        String addrString;
        int prefixLen;
        int slashIndex = checkNotNull(spec, "spec").lastIndexOf("/");
        if (slashIndex == -1) {
            prefixLen = 32;
            addrString = spec;
        } else {
            prefixLen = Integer.parseInt(spec.substring(slashIndex + 1));
            addrString = spec.substring(0, slashIndex);
        }

        InetAddress addr;
        try {
            addr = InetAddress.getByName(addrString);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(addrString + " does not contain a valid IP address");
        }

        return fromAddrPrefix(addr, prefixLen);
    }

    private static void validatePrefixLength(InetAddress address, int prefixLen) throws IllegalArgumentException {
        if (prefixLen < 0) {
            throw new IllegalArgumentException("Minimum prefix length for an IP address is 0!");
        }
        int maxLen = getMaxPrefixLength(address);
        if (prefixLen > maxLen) {
            throw new IllegalArgumentException("Maximum prefix length for a " + address.getClass().getSimpleName() + " is " + maxLen);
        }
    }

    private static int getMaxPrefixLength(InetAddress address) {
        if (address instanceof Inet4Address) {
            return 32;
        } else if (address instanceof Inet6Address) {
            return 128;
        }
        throw new IllegalArgumentException("Unknown IP address type " + address);
    }

    @Override
    public String toString() {
        return this.addr.getHostAddress() + "/" + this.prefixLen;
    }

    public static final class IpSetSerializer implements TypeSerializer<IpSet> {

        @Override
        public IpSet deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            try {
                return IpSet.fromCidr(value.getString());
            } catch (IllegalArgumentException e) {
                throw new ObjectMappingException(e);
            }
        }

        @Override
        public void serialize(TypeToken<?> type, IpSet obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.toString());
        }
    }
}
