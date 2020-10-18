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
package org.spongepowered.common.data.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class DataUtil {

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }

    public static ServerLocation getLocation(final DataView view, final boolean castToInt) {
        final ResourceKey world = view.getKey(Queries.WORLD_KEY).orElseThrow(DataUtil.dataNotFound());
        final Vector3d pos = DataUtil.getPosition3d(view, null);
        if (castToInt) {
            return ServerLocation.of(SpongeCommon.getGame().getServer().getWorldManager().getWorld(world).orElseThrow(DataUtil.dataNotFound()), pos.toInt());
        }
        return ServerLocation.of(SpongeCommon.getGame().getServer().getWorldManager().getWorld(world).orElseThrow(DataUtil.dataNotFound()), pos);
    }

    public static Vector3i getPosition3i(final DataView view) {
        DataUtil.checkDataExists(view, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(Constants.Sponge.SNAPSHOT_WORLD_POSITION).orElseThrow(DataUtil.dataNotFound());
        final int x = internal.getInt(Queries.POSITION_X).orElseThrow(DataUtil.dataNotFound());
        final int y = internal.getInt(Queries.POSITION_Y).orElseThrow(DataUtil.dataNotFound());
        final int z = internal.getInt(Queries.POSITION_Z).orElseThrow(DataUtil.dataNotFound());
        return new Vector3i(x, y, z);
    }

    private static Supplier<InvalidDataException> dataNotFound() {
        return () -> new InvalidDataException("not found");
    }

    public static Vector3d getPosition3d(final DataView view, @Nullable final DataQuery query) {
        final DataView internal = query == null ? view : view.getView(query).orElseThrow(DataUtil.dataNotFound());
        final double x = internal.getDouble(Queries.POSITION_X).orElseThrow(DataUtil.dataNotFound());
        final double y = internal.getDouble(Queries.POSITION_Y).orElseThrow(DataUtil.dataNotFound());
        final double z = internal.getDouble(Queries.POSITION_Z).orElseThrow(DataUtil.dataNotFound());
        return new Vector3d(x, y, z);
    }
}
