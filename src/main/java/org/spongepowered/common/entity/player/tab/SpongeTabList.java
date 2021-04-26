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
package org.spongepowered.common.entity.player.tab;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.level.GameType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SpongeTabList implements TabList {

    private static final net.minecraft.network.chat.Component EMPTY_COMPONENT = new TextComponent("");
    private final net.minecraft.server.level.ServerPlayer player;
    private @Nullable Component header;
    private @Nullable Component footer;
    private final Map<UUID, TabListEntry> entries = Maps.newHashMap();

    public SpongeTabList(final net.minecraft.server.level.ServerPlayer player) {
        this.player = player;
    }

    @Override
    public ServerPlayer player() {
        return (ServerPlayer) this.player;
    }

    @Override
    public Optional<Component> header() {
        return Optional.ofNullable(this.header);
    }

    @Override
    public TabList setHeader(final @Nullable Component header) {
        this.header = header;

        this.refreshClientHeaderFooter();

        return this;
    }

    @Override
    public Optional<Component> footer() {
        return Optional.ofNullable(this.footer);
    }

    @Override
    public TabList setFooter(final @Nullable Component footer) {
        this.footer = footer;

        this.refreshClientHeaderFooter();

        return this;
    }

    @Override
    public TabList setHeaderAndFooter(@Nullable final Component header, @Nullable final Component footer) {
        // Do not call the methods, set directly
        this.header = header;
        this.footer = footer;

        this.refreshClientHeaderFooter();

        return this;
    }

    @SuppressWarnings("ConstantConditions")
    private void refreshClientHeaderFooter() {
        // MC-98180 - Sending null as header or footer will cause an exception on the client
        final ClientboundTabListPacket packet = new ClientboundTabListPacket(
            this.header == null ? SpongeTabList.EMPTY_COMPONENT : SpongeAdventure.asVanilla(this.header),
            this.footer == null ? SpongeTabList.EMPTY_COMPONENT : SpongeAdventure.asVanilla(this.footer)
        );
        this.player.connection.send(packet);
    }

    @Override
    public Collection<TabListEntry> entries() {
        return Collections.unmodifiableCollection(this.entries.values());
    }

    @Override
    public Optional<TabListEntry> entry(final UUID uniqueId) {
        checkNotNull(uniqueId, "unique id");
        return Optional.ofNullable(this.entries.get(uniqueId));
    }

    @Override
    public TabList addEntry(final TabListEntry entry) throws IllegalArgumentException {
        checkNotNull(entry, "builder");
        checkState(entry.list().equals(this), "the provided tab list entry was not created for this tab list");

        this.addEntry(entry, true);

        return this;
    }

    private void addEntry(final ClientboundPlayerInfoPacket.PlayerUpdate entry) {
        final GameProfile profile = entry.getProfile();
        if (!this.entries.containsKey(profile.getId())) {
            final net.minecraft.network.chat.Component displayName = entry.getDisplayName();
            this.addEntry(new SpongeTabListEntry(
                    this,
                    SpongeGameProfile.of(profile),
                    displayName == null ? null : SpongeAdventure.asAdventure(displayName),
                    entry.getLatency(),
                    (GameMode) (Object) entry.getGameMode()
            ), false);
        }
    }

    private void addEntry(final TabListEntry entry, final boolean exceptionOnDuplicate) {
        final UUID uniqueId = entry.profile().uniqueId();

        if (exceptionOnDuplicate) {
            checkArgument(!this.entries.containsKey(uniqueId), "cannot add duplicate entry");
        }

        if (!this.entries.containsKey(uniqueId)) {
            this.entries.put(uniqueId, entry);

            this.sendUpdate(entry, ClientboundPlayerInfoPacket.Action.ADD_PLAYER);
            entry.displayName().ifPresent(text -> this.sendUpdate(entry, ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME));
            this.sendUpdate(entry, ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY);
            this.sendUpdate(entry, ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE);
        }
    }

    @Override
    public Optional<TabListEntry> removeEntry(final UUID uniqueId) {
        checkNotNull(uniqueId, "unique id");

        final TabListEntry entry = this.entries.remove(uniqueId);
        if (entry != null) {
            this.sendUpdate(entry, ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER);
            return Optional.of(entry);
        }
        return Optional.empty();
    }

    /**
     * Send an entry update.
     *
     * @param entry The entry to update
     * @param action The update action to perform
     */
    @SuppressWarnings("ConstantConditions")
    void sendUpdate(final TabListEntry entry, final ClientboundPlayerInfoPacket.Action action) {
        final ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(action, new ArrayList<>());
        final ClientboundPlayerInfoPacket.PlayerUpdate data = new ClientboundPlayerInfoPacket.PlayerUpdate(SpongeGameProfile.toMcProfile(entry.profile()),
            entry.latency(), (GameType) (Object) entry.gameMode(),
            entry.displayName().isPresent() ? SpongeAdventure.asVanilla(entry.displayName().get()) : null);
        packet.getEntries().add(data);
        this.player.connection.send(packet);
    }

    /**
     * Update this tab list with data from the provided packet.
     *
     * <p>This method should not be called manually, it is automatically
     * called in the player's network connection when the packet is sent.</p>
     *
     * @param packet The packet to process
     */
    @SuppressWarnings("ConstantConditions")
    public void updateEntriesOnSend(final ClientboundPlayerInfoPacket packet) {
        for (final ClientboundPlayerInfoPacket.PlayerUpdate update : packet.getEntries()) {
            final ClientboundPlayerInfoPacket.Action action = packet.getAction();
            if (action == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
                // If an entry with the same id exists nothing will be done
                this.addEntry(update);
            } else if (action == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
                this.removeEntry(update.getProfile().getId());
            } else {
                this.entry(update.getProfile().getId()).ifPresent(entry -> {
                    if (action == ClientboundPlayerInfoPacket.Action.UPDATE_DISPLAY_NAME) {
                        ((SpongeTabListEntry) entry).updateWithoutSend();
                        entry.setDisplayName(update.getDisplayName() == null ? null : SpongeAdventure.asAdventure(update.getDisplayName()));
                    } else if (action == ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY) {
                        ((SpongeTabListEntry) entry).updateWithoutSend();
                        entry.setLatency(update.getLatency());
                    } else if (action == ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE) {
                        ((SpongeTabListEntry) entry).updateWithoutSend();
                        entry.setGameMode((GameMode) (Object) update.getGameMode());
                    } else {
                        throw new IllegalArgumentException("unknown packet action: " + action);
                    }
                });
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("player", this.player)
                .add("header", this.header)
                .add("footer", this.footer)
                .add("entries", this.entries)
                .toString();
    }

}
