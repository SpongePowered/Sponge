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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.Nullable;
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

import java.net.InetSocketAddress;

public final class SpongeStatusResponse {

    private SpongeStatusResponse() {
    }

    public static @Nullable ServerStatus post(final MinecraftServer server, final StatusClient client) {
        return SpongeStatusResponse.call(SpongeStatusResponse.create(server), client);
    }

    public static @Nullable ServerStatus postLegacy(final MinecraftServer server, final InetSocketAddress address, final MinecraftVersion version,
            final InetSocketAddress virtualHost) {
        ServerStatus response = SpongeStatusResponse.create(server);
        response.setVersion(new ServerStatus.Version(response.getVersion().getName(), Byte.MAX_VALUE));
        response = SpongeStatusResponse.call(response, new SpongeLegacyStatusClient(address, version, virtualHost));
        if (response != null && response.getPlayers() == null) {
            response.setPlayers(new ServerStatus.Players(-1, 0));
        }
        return response;
    }

    private static @Nullable ServerStatus call(final ServerStatus response, final StatusClient client) {
        if (!SpongeCommon.postEvent(SpongeEventFactory.createClientPingServerEvent(Cause.of(EventContext.empty(), Sponge.server()), client,
            (ClientPingServerEvent.Response) response))) {
            return response;
        }
        return null;
    }

    public static ServerStatus create(final MinecraftServer server) {
        return SpongeStatusResponse.clone(server.getStatus());
    }

    private static ServerStatus clone(final ServerStatus original) {
        final ServerStatus clone = new ServerStatus();
        clone.setDescription(original.getDescription());
        if (original.getFavicon() != null) {
            ((ClientPingServerEvent.Response) clone).setFavicon(((StatusResponse) original).favicon().get());
        }

        clone.setPlayers(SpongeStatusResponse.clone(original.getPlayers()));
        clone.setVersion(SpongeStatusResponse.clone(original.getVersion()));
        return clone;
    }

    private static ServerStatus.@Nullable Players clone(final ServerStatus.@Nullable Players original) {
        if (original != null) {
            final ServerStatus.Players clone = new ServerStatus.Players(original.getMaxPlayers(),
                    original.getNumPlayers());
            clone.setSample(original.getSample());
            return clone;
        }
        return null;
    }

    private static ServerStatus.@Nullable Version clone(final ServerStatus.@Nullable Version original) {
        return original != null ? new ServerStatus.Version(original.getName(), original.getProtocol()) : null;
    }

    public static String getMotd(final ServerStatus response) {
        return SpongeStatusResponse.getFirstLine(LegacyComponentSerializer.legacySection().serialize(SpongeAdventure.asAdventure(response.getDescription())));
    }

    public static String getUnformattedMotd(final ServerStatus response) {
        return SpongeStatusResponse.getFirstLine(PlainTextComponentSerializer.plainText().serialize(SpongeAdventure.asAdventure(response.getDescription())));
    }

    private static String getFirstLine(final String s) {
        return NetworkUtil.substringBefore(s, '\n');
    }

}
