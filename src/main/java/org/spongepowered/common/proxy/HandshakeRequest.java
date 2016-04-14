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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.network.handshake.client.C00Handshake;

/**
 * Represents a handshake request.
 *
 * <p>This feature requires the "proxy" mixin-module to be enabled.</p>
 */
public final class HandshakeRequest {

    /**
     * The original handshake string.
     */
    private final String originalHandshake;
    /**
     * The handshake string.
     */
    private String handshake;
    /**
     * If this handshake has an {@link Proxy#FML_MARKER FML marker}.
     */
    private boolean hasFMLMarker;

    public HandshakeRequest(String originalHandshake) {
        this.originalHandshake = checkNotNull(originalHandshake, "original handshake");
        this.handshake = this.originalHandshake; // Copy over by default
    }

    /**
     * Gets the original handshake string.
     *
     * <p>This is the handshake string before the {@link
     * Proxy#earlyHandshake(HandshakeRequest) early handshake} has
     * been processed.</p>
     *
     * <p>{@link #getHandshake()} should be used to perform
     * the actual handshake.</p>
     *
     * <p>This is from the "ip" field of {@link C00Handshake}.</p>
     *
     * @return The original handshake string
     */
    @SuppressWarnings("WeakerAccess")
    public String getOriginalHandshake() {
        return this.originalHandshake;
    }

    /**
     * Gets the handshake string.
     *
     * @return The handshake string
     */
    public String getHandshake() {
        return this.handshake;
    }

    /**
     * Sets the handshake string.
     *
     * @param handshake The handshake string
     */
    public void setHandshake(String handshake) {
        this.handshake = checkNotNull(handshake, "handshake");
    }

    /**
     * Determines if this handshake has an {@link Proxy#FML_MARKER FML marker}.
     *
     * @return {@code true} if this handshake has an FML
     *     marker, {@code false} otherwise
     */
    public boolean hasFMLMarker() {
        return this.hasFMLMarker;
    }

    /**
     * Sets if this handshake has an {@link Proxy#FML_MARKER FML marker}.
     *
     * @param hasFMLMarker {@code true} if this handshake has an FML
     *     marker, {@code false} otherwise
     */
    @SuppressWarnings("WeakerAccess")
    public void setHasFMLMarker(boolean hasFMLMarker) {
        this.hasFMLMarker = hasFMLMarker;
    }

}
