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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.network.status.StatusClient;
import org.spongepowered.api.network.status.StatusResponse;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.NetworkUtil;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

public final class SpongeStatusResponse {

    private SpongeStatusResponse() {
    }

    @Nullable
    public static ServerStatusResponse post(MinecraftServer server, StatusClient client) {
        return call(create(server), client);
    }

    @Nullable
    public static ServerStatusResponse postLegacy(MinecraftServer server, InetSocketAddress address, MinecraftVersion version,
            InetSocketAddress virtualHost) {
        ServerStatusResponse response = create(server);
        response.func_151321_a(new ServerStatusResponse.Version(response.func_151322_c().func_151303_a(), Byte.MAX_VALUE));
        response = call(response, new SpongeLegacyStatusClient(address, version, virtualHost));
        if (response != null && response.func_151318_b() == null) {
            response.func_151319_a(new ServerStatusResponse.Players(-1, 0));
        }
        return response;
    }

    @Nullable
    private static ServerStatusResponse call(ServerStatusResponse response, StatusClient client) {
        if (!SpongeImpl.postEvent(SpongeEventFactory.createClientPingServerEvent(Cause.of(EventContext.empty(), Sponge.getServer()), client,
            (ClientPingServerEvent.Response) response))) {
            return response;
        }
        return null;
    }

    public static ServerStatusResponse create(MinecraftServer server) {
        return clone(server.func_147134_at());
    }

    private static ServerStatusResponse clone(ServerStatusResponse original) {
        ServerStatusResponse clone = new ServerStatusResponse();
        clone.func_151315_a(original.func_151317_a());
        if (original.func_151316_d() != null) {
            ((ClientPingServerEvent.Response) clone).setFavicon(((StatusResponse) original).getFavicon().get());
        }

        clone.func_151319_a(clone(original.func_151318_b()));
        clone.func_151321_a(clone(original.func_151322_c()));
        return clone;
    }

    @Nullable
    private static ServerStatusResponse.Players clone(@Nullable ServerStatusResponse.Players original) {
        if (original != null) {
            ServerStatusResponse.Players clone = new ServerStatusResponse.Players(original.func_151332_a(),
                    original.func_151333_b());
            clone.func_151330_a(original.func_151331_c());
            return clone;
        }
        return null;
    }

    @Nullable
    private static ServerStatusResponse.Version clone(@Nullable ServerStatusResponse.Version original) {
        return original != null ? new ServerStatusResponse.Version(original.func_151303_a(), original.func_151304_b()) : null;
    }

    public static String getMotd(ServerStatusResponse response) {
        return getFirstLine(SpongeTexts.toLegacy(response.func_151317_a()));
    }

    public static String getUnformattedMotd(ServerStatusResponse response) {
        return getFirstLine(LegacyTexts.stripAll(response.func_151317_a().func_150260_c(), COLOR_CHAR));
    }

    private static String getFirstLine(String s) {
        return NetworkUtil.substringBefore(s, '\n');
    }

}
