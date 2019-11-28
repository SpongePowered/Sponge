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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.network.play.server.SPlayerListItemPacket;

/*
 * This is intentionally not a mixin of SPacketPlayerListItem.AddPlayerData.
 */
public final class SpongeTabListEntry implements TabListEntry {

    private SpongeTabList list;
    private final GameProfile profile;
    @Nullable private Text displayName;
    private int latency;
    private GameMode gameMode;
    private boolean updateWithoutSend;

    public SpongeTabListEntry(TabList list, GameProfile profile, @Nullable Text displayName, int latency, GameMode gameMode) {
        checkState(list instanceof SpongeTabList, "list is not a SpongeTabList");
        this.list = (SpongeTabList) list;
        this.profile = checkNotNull(profile, "profile");
        this.displayName = displayName;
        this.latency = latency;
        this.gameMode = checkNotNull(gameMode, "game mode");
    }

    @Override
    public TabList getList() {
        return this.list;
    }

    @Override
    public GameProfile getProfile() {
        return this.profile;
    }

    @Override
    public Optional<Text> getDisplayName() {
        return Optional.ofNullable(this.displayName);
    }

    @Override
    public TabListEntry setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;
        this.sendUpdate(SPlayerListItemPacket.Action.UPDATE_DISPLAY_NAME);
        return this;
    }

    @Override
    public int getLatency() {
        return this.latency;
    }

    @Override
    public TabListEntry setLatency(int latency) {
        this.latency = latency;
        this.sendUpdate(SPlayerListItemPacket.Action.UPDATE_LATENCY);
        return this;
    }

    @Override
    public GameMode getGameMode() {
        return this.gameMode;
    }

    @Override
    public TabListEntry setGameMode(GameMode gameMode) {
        this.gameMode = checkNotNull(gameMode, "game mode");
        this.sendUpdate(SPlayerListItemPacket.Action.UPDATE_GAME_MODE);
        return this;
    }

    private void sendUpdate(SPlayerListItemPacket.Action action) {
        // We may be updating our values, so we don't want to send any updates
        // since that will result in a continuous loop.
        if (this.updateWithoutSend) {
            this.updateWithoutSend = false; // (╯°□°）╯︵ ┻━┻
            return;
        }

        this.list.sendUpdate(this, action);
    }

    /**
     * Set the {@link #updateWithoutSend} state to prevent sending on next update.
     */
    public void updateWithoutSend() {
        this.updateWithoutSend = true;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        TabListEntry that = (TabListEntry) other;
        return Objects.equal(this.profile.getUniqueId(), that.getProfile().getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.profile);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("profile", this.profile)
                .add("latency", this.latency)
                .add("displayName", this.displayName)
                .add("gameMode", this.gameMode)
                .toString();
    }

}
