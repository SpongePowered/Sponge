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

import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.UUID;

public final class SpongePlayerDataBuilder extends AbstractDataBuilder<SpongePlayerData> implements DataBuilder<SpongePlayerData> {

    public SpongePlayerDataBuilder() {
        super(SpongePlayerData.class, 1);
    }

    @Override
    protected Optional<SpongePlayerData> buildContent(DataView container) throws InvalidDataException {
        if (container
                .contains(Constants.Entity.Player.UUID, Constants.Sponge.PlayerData.PLAYER_DATA_JOIN, Constants.Sponge.PlayerData.PLAYER_DATA_LAST)) {
            final String idString = container.getString(Constants.Entity.Player.UUID).get();
            final UUID uniqueId = UUID.fromString(idString);
            final long firstJoin = container.getLong(Constants.Sponge.PlayerData.PLAYER_DATA_JOIN).get();
            final long lastJoin = container.getLong(Constants.Sponge.PlayerData.PLAYER_DATA_LAST).get();
            final SpongePlayerData data = new SpongePlayerData();
            data.setUniqueId(uniqueId);
            data.setFirstJoined(firstJoin);
            data.setLastJoined(lastJoin);
            return Optional.of(data);
        }
        return Optional.empty();
    }
}
