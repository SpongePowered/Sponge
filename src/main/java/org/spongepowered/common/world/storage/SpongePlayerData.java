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
package org.spongepowered.common.world.storage;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.common.util.Constants;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

public final class SpongePlayerData implements DataSerializable {

    private UUID uniqueId;
    private long firstJoined;
    private long lastJoined;

    SpongePlayerData() {
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(final UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public long getFirstJoined() {
        return this.firstJoined;
    }

    public void setFirstJoined(final long firstJoined) {
        this.firstJoined = firstJoined;
    }

    public long getLastJoined() {
        return this.lastJoined;
    }

    public void setLastJoined(final long lastJoined) {
        this.lastJoined = lastJoined;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Entity.Player.UUID, this.uniqueId.toString())
                .set(Constants.Sponge.PlayerData.PLAYER_DATA_JOIN, this.firstJoined)
                .set(Constants.Sponge.PlayerData.PLAYER_DATA_LAST, this.lastJoined);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongePlayerData.class.getSimpleName() + "[", "]")
                .add("uniqueId=" + this.uniqueId)
                .add("firstJoined=" + this.firstJoined)
                .add("lastJoined=" + this.lastJoined)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId, this.firstJoined, this.lastJoined);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final SpongePlayerData other = (SpongePlayerData) obj;
        return Objects.equals(this.uniqueId, other.uniqueId)
                && Objects.equals(this.firstJoined, other.firstJoined)
                && Objects.equals(this.lastJoined, other.lastJoined);
    }
}
