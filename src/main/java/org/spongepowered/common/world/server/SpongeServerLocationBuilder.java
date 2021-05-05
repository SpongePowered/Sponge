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
package org.spongepowered.common.world.server;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Optional;

public final class SpongeServerLocationBuilder extends AbstractDataBuilder<ServerLocation> {

    public SpongeServerLocationBuilder() {
        super(ServerLocation.class, 1);
    }

    @Override
    protected Optional<ServerLocation> buildContent(final DataView container) throws InvalidDataException {
        final ResourceKey worldKey = container.getResourceKey(Queries.WORLD_KEY)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.WORLD_KEY + "] entry"));
        final int x = container.getInt(Queries.POSITION_X)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_X + "] entry"));
        final int y = container.getInt(Queries.POSITION_Y)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_Y + "] entry"));
        final int z = container.getInt(Queries.POSITION_Z)
                .orElseThrow(() -> new InvalidDataException("Missing [" + Queries.POSITION_Z + "] entry"));
        return Optional.of(ServerLocation.of(worldKey, x, y, z));
    }
}
