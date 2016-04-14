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
import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.UUID;

/**
 * Represents a handshake response.
 *
 * <p>This feature requires the "proxy" mixin-module to be enabled.</p>
 */
public final class HandshakeResponse {

    // Connection
    /**
     * The hostname of the server.
     */
    @SuppressWarnings("NullableProblems")
    private String serverHostname;
    /**
     * The hostname of the client.
     */
    @SuppressWarnings("NullableProblems")
    private String clientHostname;
    // Client
    /**
     * The game profile of the client.
     */
    @SuppressWarnings("NullableProblems")
    private GameProfile profile;
    // Failure
    /**
     * If this handshake has cancelled.
     */
    private boolean cancelled;
    /**
     * The message to display to the client if handshaking fails.
     */
    private Text cancelMessage = Text.of(t("Please enable client detail forwarding (also known as \"ip forwarding\") on"
        + " your proxy if you wish to use it on this server."));

    /**
     * Gets the server hostname string.
     *
     * <p>This should not include the port.</p>
     *
     * @return The server hostname string
     */
    public String getServerHostname() {
        return this.serverHostname;
    }

    /**
     * Sets the server hostname string.
     *
     * <p>This should not include the port.</p>
     *
     * @param serverHostname The server hostname string
     */
    @SuppressWarnings("WeakerAccess")
    public void setServerHostname(String serverHostname) {
        this.serverHostname = checkNotNull(serverHostname, "server hostname");
    }

    /**
     * Gets the client hostname string.
     *
     * <p>This should not include the port.</p>
     *
     * @return The client hostname string
     */
    public String getClientHostname() {
        return this.clientHostname;
    }

    /**
     * Sets the client hostname string.
     *
     * <p>This should not include the port.</p>
     *
     * @param clientHostname The client hostname string
     */
    @SuppressWarnings("WeakerAccess")
    public void setClientHostname(String clientHostname) {
        this.clientHostname = checkNotNull(clientHostname, "client hostname");
    }

    /**
     * Gets the resolved {@link GameProfile} of this client.
     *
     * @return The resolved profile if present, or {@code null}
     */
    public GameProfile getProfile() {
        return this.profile;
    }

    /**
     * Sets the resolved profile of this client.
     *
     * <p>The profile is used to retrieve the {@link UUID}
     + and profile properties for this client.</p>
     *
     * @param profile The resolved profile
     */
    public void setProfile(GameProfile profile) {
        this.profile = checkNotNull(profile, "profile");
    }

    /**
     * Determines if the handshake is cancelled.
     *
     * <p>When {@code true}, the client connecting will be disconnected
     * with the {@link #getCancelMessage() fail message}.</p>
     *
     * @return {@code true} if cancelled, {@code false} otherwise
     */
    public boolean isFailed() {
        return this.cancelled;
    }

    /**
     * Sets if the handshake is cancelled and the client should be disconnected.
     *
     * <p>When {@code true}, the client connecting will be disconnected
     * with the {@link #getCancelMessage() fail message}.</p>
     *
     * @param cancelled {@code true} if cancelled, {@code false} otherwise
     */
    public void setFailed(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Gets the message to display to the client when handshaking fails.
     *
     * @return the message to display to the client
     */
    public Text getCancelMessage() {
        return this.cancelMessage;
    }

    /**
     * Sets the message to display to the client when handshaking fails.
     *
     * @param cancelMessage the message to display to the client
     */
    public void setCancelMessage(Text cancelMessage) {
        this.cancelMessage = checkNotNull(cancelMessage, "cancel message");
    }

}
