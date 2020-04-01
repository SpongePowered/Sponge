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

import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings({"UnstableApiUsage"})
public final class DataUtil {

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        }
        return dataView;
    }

    public static Location getLocation(final DataView view, final boolean castToInt) {
        final UUID worldUuid = UUID.fromString(view.getString(Queries.WORLD_ID).orElseThrow(dataNotFound()));
        final double x = view.getDouble(Queries.POSITION_X).orElseThrow(dataNotFound());
        final double y = view.getDouble(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final double z = view.getDouble(Queries.POSITION_Z).orElseThrow(dataNotFound());
        if (castToInt) {
            return Location.of(SpongeImpl.getGame().getServer().getWorldManager().getWorld(worldUuid).orElseThrow(dataNotFound()), (int) x, (int) y, (int) z);
        }
        return Location.of(SpongeImpl.getGame().getServer().getWorldManager().getWorld(worldUuid).orElseThrow(dataNotFound()), x, y, z);
    }

    public static Vector3i getPosition3i(final DataView view) {
        checkDataExists(view, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(Constants.Sponge.SNAPSHOT_WORLD_POSITION).orElseThrow(dataNotFound());
        final int x = internal.getInt(Queries.POSITION_X).orElseThrow(dataNotFound());
        final int y = internal.getInt(Queries.POSITION_Y).orElseThrow(dataNotFound());
        final int z = internal.getInt(Queries.POSITION_Z).orElseThrow(dataNotFound());
        return new Vector3i(x, y, z);
    }

    private static Supplier<InvalidDataException> dataNotFound() {
        return () -> new InvalidDataException("not found");
    }

    public static DataRegistration getRegistrationFor(final Mutable manipulator) {
        return SpongeManipulatorRegistry.getInstance().getRegistrationFor(manipulator);
    }

}
