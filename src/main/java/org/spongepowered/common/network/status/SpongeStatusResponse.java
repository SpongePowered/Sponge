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

import static com.google.common.base.Preconditions.checkNotNull;

import net.kyori.adventure.text.Component;
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
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.api.network.status.StatusClient;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.NetworkUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javax.imageio.ImageIO;

public final class SpongeStatusResponse implements ClientPingServerEvent.Response {

    private final ServerStatus originalStatus;

    private net.minecraft.network.chat.Component description;
    private SpongeStatusPlayers players;
    private boolean hiddenPlayers;
    private SpongeStatusVersion version;
    private ServerStatus.Favicon favicon;

    private SpongeStatusResponse(ServerStatus status) {
        this.originalStatus = status;
        this.description = status.description();
        this.players = new SpongeStatusPlayers(status.players().orElse(null));
        this.hiddenPlayers = false;
        this.version = new SpongeStatusVersion(status.version().orElse(null));
        this.favicon = status.favicon().orElse(null);
    }

    public final static class SpongeStatusPlayers implements ClientPingServerEvent.Response.Players {

        private int max;
        private int online;
        private final List<GameProfile> profiles = new ArrayList<>();

        public SpongeStatusPlayers(final ServerStatus.Players players) {
            if (players == null) {
                this.max = -1;
                this.online = 0;
            } else {
                this.max = players.max();
                this.online = players.online();
                players.sample().forEach(profile -> this.profiles.add(SpongeGameProfile.of(profile)));
            }
        }

        public int max() {
            return max;
        }

        public int online() {
            return online;
        }

        @Override
        public List<org.spongepowered.api.profile.GameProfile> profiles() {
            return profiles;
        }

        @Override
        public void setOnline(final int online) {
            this.online = online;
        }

        @Override
        public void setMax(final int max) {
            this.max = max;
        }

        public ServerStatus.Players toVanilla() {
            return new ServerStatus.Players(this.max, this.online, this.profiles.stream()
                    // Make sure profiles are sent with non-null UUIDs and names because everything else
                    // will make the response invalid on the client. Some plugins use empty UUIDs to create
                    // custom lines in the player list that do not refer to a specific player.
                    .map(SpongeGameProfile::toMcProfileNonNull)
                    .toList());
        }
    }

    public final static class SpongeStatusVersion implements ClientPingServerEvent.Response.Version {
        private String name;
        private int protocol;

        public SpongeStatusVersion(final ServerStatus.Version version) {
            if (version != null) {
                this.name = version.name();
                this.protocol = version.protocol();
            } else {
                this.name = "";
                this.protocol = 0;
            }
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public void setName(String name) {
            this.name = checkNotNull(name, "name");
        }

        @Override
        public boolean isLegacy() {
            return false;
        }

        @Override
        public int protocolVersion() {
            return this.protocol;
        }

        @Override
        public void setProtocolVersion(int protocolVersion) {
            this.protocol = protocolVersion;
        }

        @Override
        public OptionalInt dataVersion() {
            return OptionalInt.empty();
        }

        public ServerStatus.Version toVanilla() {
            return new ServerStatus.Version(this.name, this.protocol);
        }
    }

    @Override
    public void setDescription(final Component description) {
        this.description = SpongeAdventure.asVanilla(checkNotNull(description, "description"));
    }

    @Override
    public Component description() {
        return SpongeAdventure.asAdventure(this.description);
    }

    @Override
    public Optional<Players> players() {
        if (this.hiddenPlayers) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.players);
    }

    @Override
    public void setHidePlayers(final boolean hide) {
        this.hiddenPlayers = hide;
    }

    @Override
    public void setFavicon(@Nullable final Favicon favicon) {
        if (favicon == null) {
            this.favicon = null;
            return;
        }
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            ImageIO.write(favicon.image(), "PNG", bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.favicon = new ServerStatus.Favicon(bytes.toByteArray());
    }

    @Override
    public ClientPingServerEvent.Response.Version version() {
        return this.version;
    }

    @Override
    public Optional<Favicon> favicon() {
        return Optional.ofNullable((Favicon) (Object) this.favicon);
    }

    private ServerStatus toVanilla() {
        return PlatformHooks.INSTANCE.getGeneralHooks().createServerStatus(
                this.originalStatus,
                this.description,
                Optional.ofNullable(this.hiddenPlayers || this.players == null ? null : this.players.toVanilla()),
                Optional.of(this.version.toVanilla()),
                Optional.ofNullable(this.favicon));
    }

    public String motd()
    {
        return SpongeStatusResponse.getFirstLine(LegacyComponentSerializer.legacySection().serialize(SpongeAdventure.asAdventure(this.description)));
    }

    public String unformattedMotd()
    {
        return SpongeStatusResponse.getFirstLine(PlainTextComponentSerializer.plainText().serialize(SpongeAdventure.asAdventure(this.description)));
    }


    public int onlinePlayers() {
        return !this.hiddenPlayers && this.players != null ? this.players.online : 0;
    }

    public int maxPlayers() {
        return !this.hiddenPlayers && this.players != null ? this.players.max : -1;
    }

    @Nullable
    public static ServerStatus post(final ServerStatus status, final StatusClient client) {
        final SpongeStatusResponse response = SpongeStatusResponse.call(status, client);
        return response != null ? response.toVanilla() : null;
    }

    @Nullable
    public static SpongeStatusResponse postLegacy(final MinecraftServer server, final InetSocketAddress address, final MinecraftVersion version,
            final InetSocketAddress virtualHost) {
        return SpongeStatusResponse.call(server.getStatus(), new SpongeLegacyStatusClient(address, version, virtualHost));
    }

    @Nullable
    private static SpongeStatusResponse call(final ServerStatus response, final StatusClient client) {
        final SpongeStatusResponse mutableResponse = new SpongeStatusResponse(response);
        if (!SpongeCommon.post(
                SpongeEventFactory.createClientPingServerEvent(Cause.of(EventContext.empty(), Sponge.server()), client, mutableResponse))) {
            return mutableResponse;
        }
        return null;
    }
    private static String getFirstLine(final String s) {
        return NetworkUtil.substringBefore(s, '\n');
    }

}
