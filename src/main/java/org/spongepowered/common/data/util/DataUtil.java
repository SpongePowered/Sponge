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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.fixer.entity.EntityTrackedUser;
import org.spongepowered.common.data.fixer.entity.player.PlayerRespawnData;
import org.spongepowered.common.data.fixer.world.SpongeLevelFixer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class DataUtil {

    // TODO Bump this when needing to fix sponge added file data
    public static final int DATA_VERSION = 1;
    public static final DataFixer spongeDataFixer = new DataFixer(DATA_VERSION);

    static {
        spongeDataFixer.registerFix(FixTypes.LEVEL, new SpongeLevelFixer());
        spongeDataFixer.registerFix(FixTypes.ENTITY, new EntityTrackedUser());
        spongeDataFixer.registerFix(FixTypes.PLAYER, new PlayerRespawnData());
    }

    public static DataView checkDataExists(final DataView dataView, final DataQuery query) throws InvalidDataException {
        if (!checkNotNull(dataView).contains(checkNotNull(query))) {
            throw new InvalidDataException("Missing data for query: " + query.asString('.'));
        } else {
            return dataView;
        }
    }

    public static <T> T getData(final DataView dataView, final Key<? extends BaseValue<T>> key) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        return (T) object;
    }

    public static <T> T getData(final DataView dataView, final Key<?> key, Class<T> clazz) throws InvalidDataException {
        checkDataExists(dataView, checkNotNull(key).getQuery());
        final Object object = dataView.get(key.getQuery()).get();
        if (clazz.isInstance(object)) {
            return (T) object;
        } else {
            throw new InvalidDataException("Could not cast to the correct class type!");
        }
    }

    public static <T> T getData(final DataView dataView, final DataQuery query, Class<T> data) throws InvalidDataException {
        checkDataExists(dataView, query);
        final Object object = dataView.get(query).get();
        if (data.isInstance(object)) {
            return (T) object;
        } else {
            throw new InvalidDataException("Data does not match!");
        }
    }

    public static List<DataView> getSerializedManipulatorList(Iterable<DataManipulator<?, ?>> manipulators) {
        checkNotNull(manipulators);
        final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
        for (DataManipulator<?, ?> manipulator : manipulators) {
            final DataContainer container = new MemoryDataContainer();
            container.set(DataQueries.DATA_CLASS, manipulator.getClass().getName())
                .set(DataQueries.INTERNAL_DATA, manipulator.toContainer());
            builder.add(container);
        }
        return builder.build();
    }

    public static List<DataView> getSerializedImmutableManipulatorList(Iterable<ImmutableDataManipulator<?, ?>> manipulators) {
        checkNotNull(manipulators);
        final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
        for (ImmutableDataManipulator<?, ?> manipulator : manipulators) {
            final DataContainer container = new MemoryDataContainer();
            container.set(DataQueries.DATA_CLASS, manipulator.getClass().getName())
                .set(DataQueries.INTERNAL_DATA, manipulator.toContainer());
            builder.add(container);
        }
        return builder.build();
    }

    @SuppressWarnings("rawtypes")
    public static ImmutableList<DataManipulator<?, ?>> deserializeManipulatorList(List<DataView> containers) {
        checkNotNull(containers);
        final ImmutableList.Builder<DataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataView view : containers) {
            final String clazzName = view.getString(DataQueries.DATA_CLASS).get();
            final DataView manipulatorView = view.getView(DataQueries.INTERNAL_DATA).get();
            try {
                final Class<?> clazz = Class.forName(clazzName);
                final Optional<DataManipulatorBuilder<?, ?>> optional = SpongeDataManager.getInstance().getBuilder((Class) clazz);
                if (optional.isPresent()) {
                    final Optional<? extends DataManipulator<?, ?>> manipulatorOptional = optional.get().build(manipulatorView);
                    if (manipulatorOptional.isPresent()) {
                        builder.add(manipulatorOptional.get());
                    }
                }
            } catch (Exception e) {
                new InvalidDataException("Could not translate " + clazzName
                                         + "! Don't worry though, we'll try to translate the rest of the data.", e).printStackTrace();
            }
        }
        return builder.build();
    }

    @SuppressWarnings("rawtypes")
    public static ImmutableList<ImmutableDataManipulator<?, ?>> deserializeImmutableManipulatorList(List<DataView> containers) {
        checkNotNull(containers);
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        for (DataView view : containers) {
            final String clazzName = view.getString(DataQueries.DATA_CLASS).get();
            final DataView manipulatorView = view.getView(DataQueries.INTERNAL_DATA).get();
            try {
                final Class<?> clazz = Class.forName(clazzName);
                final Optional<DataManipulatorBuilder<?, ?>> optional = SpongeDataManager.getInstance().getImmutableManipulatorBuilder((Class) clazz);
                if (optional.isPresent()) {
                    final Optional<? extends DataManipulator<?, ?>> manipulatorOptional = optional.get().build(manipulatorView);
                    if (manipulatorOptional.isPresent()) {
                        builder.add(manipulatorOptional.get().asImmutable());
                    }
                }
            } catch (Exception e) {
                new InvalidDataException("Could not translate " + clazzName + "!", e).printStackTrace();
            }
        }
        return builder.build();
    }

    public static Location<World> getLocation(DataView view, boolean castToInt) {
        final UUID worldUuid = UUID.fromString(view.getString(Queries.WORLD_ID).get());
        final double x = view.getDouble(Queries.POSITION_X).get();
        final double y = view.getDouble(Queries.POSITION_Y).get();
        final double z = view.getDouble(Queries.POSITION_Z).get();
        if (castToInt) {
            return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).get(), (int) x, (int) y, (int) z);
        } else {
            return new Location<>(SpongeImpl.getGame().getServer().getWorld(worldUuid).get(), x, y, z);
        }

    }

    public static Vector3i getPosition3i(DataView view) {
        return getPosition3i(view, DataQueries.SNAPSHOT_WORLD_POSITION);
    }

    public static Vector3i getPosition3i(DataView view, DataQuery query) {
        checkDataExists(view, DataQueries.SNAPSHOT_WORLD_POSITION);
        final DataView internal = view.getView(DataQueries.SNAPSHOT_WORLD_POSITION).get();
        final int x = internal.getInt(Queries.POSITION_X).get();
        final int y = internal.getInt(Queries.POSITION_Y).get();
        final int z = internal.getInt(Queries.POSITION_Z).get();
        return new Vector3i(x, y, z);
    }

    public static Vector3d getPosition3d(DataView view) {
        return getPosition3d(view, DataQueries.SNAPSHOT_WORLD_POSITION);
    }

    public static Vector3d getPosition3d(DataView view, DataQuery query) {
        checkDataExists(view, query);
        final DataView internal = view.getView(query).get();
        final double x = internal.getDouble(Queries.POSITION_X).get();
        final double y = internal.getDouble(Queries.POSITION_Y).get();
        final double z = internal.getDouble(Queries.POSITION_Z).get();
        return new Vector3d(x, y, z);
    }

}
