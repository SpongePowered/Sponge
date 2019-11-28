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
package org.spongepowered.common.data.builder.world;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import java.util.Optional;
import java.util.UUID;

public class LocationBuilder extends AbstractDataBuilder<Location<World>> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LocationBuilder() {
        super((Class) Location.class, 1);
    }

    @Override
    protected Optional<Location<World>> buildContent(DataView container) throws InvalidDataException {
        if (!container.contains(Queries.WORLD_NAME, Queries.WORLD_ID, Queries.POSITION_X, Queries.POSITION_Y, Queries.POSITION_Z)) {
            return Optional.empty();
        }
        if (container.contains(Queries.CHUNK_X, Queries.CHUNK_Y, Queries.CHUNK_Z)) {
            return Optional.empty();
        }
        final String worldId = container.getString(Queries.WORLD_ID).get();
        final UUID worldUuid = UUID.fromString(worldId);
        final Optional<World> world = Sponge.getGame().getServer().getWorld(worldUuid);
        if (!world.isPresent()) {
            throw new InvalidDataException("Could not find world by UUID: " + worldId);
        }
        final double x = container.getDouble(Queries.POSITION_X).get();
        final double y = container.getDouble(Queries.POSITION_Y).get();
        final double z = container.getDouble(Queries.POSITION_Z).get();
        return Optional.of(new Location<>(world.get(), x, y, z));
    }
}
