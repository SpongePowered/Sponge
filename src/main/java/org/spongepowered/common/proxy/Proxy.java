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
package org.spongepowered.common.proxy;

import com.mojang.authlib.properties.Property;

/**
 * Represents a proxy.
 *
 * <p>This feature requires the "proxy" mixin-module to be enabled.</p>
 */
public interface Proxy {

    /**
     * A token FML uses to mark the connection as a "FML connection"
     */
    String FML_MARKER = "\0FML\0";
    /**
     * The name of a {@link Property} used to mark a connection
     * as an "FML connection".
     */
    String FML_PROPERTY_NAME = "forgeClient";
    /**
     * The value of a {@link Property} used to mark a connection
     * as an "FML connection".
     */
    String FML_PROPERTY_VALUE = "true";

    /**
     * Determines if this proxy is enabled.
     *
     * @return {@code true} if this proxy is enabled, {@code false} otherwise
     */
    boolean isEnabled();

    /**
     * Sets if this proxy is enabled.
     *
     * @param enabled {@code true} if this proxy is enabled, {@code false} otherwise
     */
    void setEnabled(boolean enabled);

    /**
     * Determines if this proxy forwards client details to this server.
     *
     * @return {@code true} if this proxy forwards client details to
     *     this server, {@code false} otherwise
     */
    boolean forwardsClientDetails();

    /**
     * Sets if this proxy forwards client details to this server.
     *
     * @param forwardsClientDetails {@code true} if this proxy forwards client
     *     details to this server, {@code false} otherwise
     */
    void setForwardsClientDetails(boolean forwardsClientDetails);

    /**
     * Performs an early handshake.
     *
     * <p>The early handshake is used to transform the handshake data
     * before the handshake is performed.</p>
     *
     * @param request The handshake request
     */
    void earlyHandshake(HandshakeRequest request);

    /**
     * Performs a handshake.
     *
     * @param request The handshake request
     * @param response The handshake response
     */
    void handshake(HandshakeRequest request, HandshakeResponse response);

}
