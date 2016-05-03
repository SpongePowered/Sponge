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

import javax.annotation.Nullable;

/**
 * Manages a {@link Proxy}.
 *
 * <p>This feature requires the "proxy" mixin-module to be enabled.</p>
 */
public enum ProxyManager {
    INSTANCE;

    /**
     * The active proxy.
     */
    @Nullable private Proxy proxy = new BungeeCordProxy(); // Default to BungeeCord

    /**
     * Gets the active proxy.
     *
     * @return The active proxy
     */
    @Nullable
    public Proxy getProxy() {
        return this.proxy;
    }

    /**
     * Sets the active proxy.
     *
     * @param proxy The active proxy
     */
    public void setProxy(@Nullable Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Determines if the active proxy is enabled.
     *
     * @return {@code true} if the active proxy is
     *     enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return this.proxy != null && this.proxy.isEnabled();
    }

    /**
     * Determines if the active proxy is enabled, and if it
     * forwards client details.
     *
     * @return {@code true} if the active proxy is enabled and
     *     it forwards client details, {@code false} otherwise
     */
    public boolean forwardsClientDetails() {
        return this.proxy != null && this.proxy.isEnabled() && this.proxy.forwardsClientDetails();
    }

    /**
     * Performs an early handshake.
     *
     * <p>The early handshake is used to transform the handshake data
     * before the handshake is performed.</p>
     *
     * @param request The handshake request
     */
    public void earlyHandshake(HandshakeRequest request) {
        if (this.proxy != null) {
            this.proxy.earlyHandshake(request);
        }
    }

    /**
     * Performs a handshake.
     *
     * @param request The handshake request
     * @param response The handshake response
     */
    public void handshake(HandshakeRequest request, HandshakeResponse response) {
        if (this.proxy != null) {
            this.proxy.handshake(request, response);
        }
    }
}
