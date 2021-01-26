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

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.StatusClient;
import org.spongepowered.api.network.status.StatusResponse;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.util.NetworkUtil;

import javax.annotation.Nullable;
import java.net.InetSocketAddress;

public final class SpongeStatusResponse {

    private SpongeStatusResponse() {
    }

    @Nullable
    public static ServerStatus post(MinecraftServer server, StatusClient client) {
        return SpongeStatusResponse.call(SpongeStatusResponse.create(server), client);
    }

    @Nullable
    public static ServerStatus postLegacy(MinecraftServer server, InetSocketAddress address, MinecraftVersion version,
            InetSocketAddress virtualHost) {
        ServerStatus response = SpongeStatusResponse.create(server);
        response.setVersion(new ServerStatus.Version(response.getVersion().getName(), Byte.MAX_VALUE));
        response = SpongeStatusResponse.call(response, new SpongeLegacyStatusClient(address, version, virtualHost));
        if (response != null && response.getPlayers() == null) {
            response.setPlayers(new ServerStatus.Players(-1, 0));
        }
        return response;
    }

    @Nullable
    private static ServerStatus call(ServerStatus response, StatusClient client) {
        if (!SpongeCommon.postEvent(SpongeEventFactory.createClientPingServerEvent(Cause.of(EventContext.empty(), Sponge.getServer()), client,
            (ClientPingServerEvent.Response) response))) {
            return response;
        }
        return null;
    }

    public static ServerStatus create(MinecraftServer server) {
        return SpongeStatusResponse.clone(server.getStatus());
    }

    private static ServerStatus clone(ServerStatus original) {
        ServerStatus clone = new ServerStatus();
        clone.setDescription(original.getDescription());
        if (original.getFavicon() != null) {
            ((ClientPingServerEvent.Response) clone).setFavicon(((StatusResponse) original).getFavicon().get());
        }

        clone.setPlayers(SpongeStatusResponse.clone(original.getPlayers()));
        clone.setVersion(SpongeStatusResponse.clone(original.getVersion()));
        return clone;
    }

    @Nullable
    private static ServerStatus.Players clone(@Nullable ServerStatus.Players original) {
        if (original != null) {
            ServerStatus.Players clone = new ServerStatus.Players(original.getMaxPlayers(),
                    original.getNumPlayers());
            clone.setSample(original.getSample());
            return clone;
        }
        return null;
    }

    @Nullable
    private static ServerStatus.Version clone(@Nullable ServerStatus.Version original) {
        return original != null ? new ServerStatus.Version(original.getName(), original.getProtocol()) : null;
    }

    public static String getMotd(ServerStatus response) {
        return SpongeStatusResponse.getFirstLine(SpongeAdventure.legacySection(SpongeAdventure.asAdventure(response.getDescription())));
    }

    public static String getUnformattedMotd(ServerStatus response) {
        return SpongeStatusResponse.getFirstLine(SpongeAdventure.plain(SpongeAdventure.asAdventure(response.getDescription())));
    }

    private static String getFirstLine(String s) {
        return NetworkUtil.substringBefore(s, '\n');
    }

}
