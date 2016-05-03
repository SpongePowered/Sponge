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

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.spongepowered.common.SpongeImpl;

/**
 * A proxy implementation for BungeeCord.
 *
 * <p>This feature requires the "proxy" mixin-module to be enabled.</p>
 */
public class BungeeCordProxy implements Proxy {

    private static final Gson GSON = new Gson();
    // By default, enable this proxy if the "proxy" module is enabled in the configuration.
    private boolean enabled = SpongeImpl.getGlobalConfig().getConfig().getModules().usePluginProxy();
    // By default, enable client detail forwarding if it's enabled in the configuration.
    private boolean forwardsClientDetails = SpongeImpl.getGlobalConfig().getConfig().getProxy().isForwardClientDetails();

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean forwardsClientDetails() {
        return this.forwardsClientDetails;
    }

    @Override
    public void setForwardsClientDetails(boolean forwardsClientDetails) {
        this.forwardsClientDetails = forwardsClientDetails;
    }

    @Override
    public void earlyHandshake(HandshakeRequest request) {
        String split[] = request.getOriginalHandshake().split("\0\\|", 2);
        request.setHandshake(split[0]);
        // If we have extra data, check to see if it is telling us we have a
        // FML marker
        if (split.length == 2) {
            request.setHasFMLMarker(split[1].contains(Proxy.FML_MARKER));
        }
    }

    @Override
    public void handshake(HandshakeRequest request, HandshakeResponse response) {
        String[] split = request.getHandshake().split("\00\\|", 2)[0].split("\00"); // ignore any extra data

        if (split.length == 3 || split.length == 4) {
            response.setServerHostname(split[0]);
            response.setClientHostname(split[1]);

            GameProfile profile = new GameProfile(UUIDTypeAdapter.fromString(split[2]), null);

            if (split.length == 4) {
                Property[] properties = GSON.fromJson(split[3], Property[].class);
                for (Property property : properties) {
                    profile.getProperties().put(property.getName(), property);
                }
            }

            response.setProfile(profile);
        } else {
            response.setFailed(true);
        }
    }

}
