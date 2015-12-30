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
package org.spongepowered.common.network.status;

import static org.spongepowered.common.text.SpongeTexts.COLOR_CHAR;

import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.StatusClient;
import org.spongepowered.api.network.status.StatusResponse;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;

public final class SpongeStatusResponse {

    private SpongeStatusResponse() {
    }

    public static ServerStatusResponse post(MinecraftServer server, StatusClient client) {
        return call(create(server), client);
    }

    public static ServerStatusResponse postLegacy(MinecraftServer server, InetSocketAddress address, MinecraftVersion version,
            InetSocketAddress virtualHost) {
        ServerStatusResponse response = create(server);
        response.setProtocolVersionInfo(
                new ServerStatusResponse.MinecraftProtocolVersionIdentifier(response.getProtocolVersionInfo().getName(), Byte.MAX_VALUE));
        response = call(response, new SpongeLegacyStatusClient(address, version, virtualHost));
        if (response != null && response.getPlayerCountData() == null) {
            response.setPlayerCountData(new ServerStatusResponse.PlayerCountData(-1, 0));
        }
        return response;
    }

    private static ServerStatusResponse call(ServerStatusResponse response, StatusClient client) {
        if (!SpongeImpl.postEvent(SpongeEventFactory.createClientPingServerEvent(Cause.of(NamedCause.source(client)), client,
                                                                                 (ClientPingServerEvent.Response) response))) {
            return response;
        } else {
            return null;
        }
    }

    public static ServerStatusResponse create(MinecraftServer server) {
        return clone(server.getServerStatusResponse());
    }

    private static ServerStatusResponse clone(ServerStatusResponse original) {
        ServerStatusResponse clone = new ServerStatusResponse();
        clone.setServerDescription(original.getServerDescription());
        if (original.getFavicon() != null) {
            ((ClientPingServerEvent.Response) clone).setFavicon(((StatusResponse) original).getFavicon().get());
        }

        clone.setPlayerCountData(clone(original.getPlayerCountData()));
        clone.setProtocolVersionInfo(clone(original.getProtocolVersionInfo()));
        return clone;
    }

    private static ServerStatusResponse.PlayerCountData clone(ServerStatusResponse.PlayerCountData original) {
        if (original != null) {
            ServerStatusResponse.PlayerCountData clone = new ServerStatusResponse.PlayerCountData(original.getMaxPlayers(),
                    original.getOnlinePlayerCount());
            clone.setPlayers(original.getPlayers());
            return clone;
        } else {
            return null;
        }
    }

    private static ServerStatusResponse.MinecraftProtocolVersionIdentifier clone(ServerStatusResponse.MinecraftProtocolVersionIdentifier original) {
        if (original != null) {
            return new ServerStatusResponse.MinecraftProtocolVersionIdentifier(original.getName(), original.getProtocol());
        } else {
            return null;
        }
    }

    private static String getFirstLine(String s) {
        int i = s.indexOf('\n');
        return i == -1 ? s : s.substring(0, i);
    }

    public static String getMotd(ServerStatusResponse response) {
        return getFirstLine(SpongeTexts.toLegacy(response.getServerDescription()));
    }

    public static String getUnformattedMotd(ServerStatusResponse response) {
        return getFirstLine(LegacyTexts.stripAll(response.getServerDescription().getUnformattedText(), COLOR_CHAR));
    }

}
