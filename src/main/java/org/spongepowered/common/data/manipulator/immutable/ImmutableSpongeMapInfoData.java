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
package org.spongepowered.common.data.manipulator.immutable;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMapInfoData;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

@ImplementationRequiredForTest
public class ImmutableSpongeMapInfoData extends AbstractImmutableData<ImmutableMapInfoData, MapInfoData> implements ImmutableMapInfoData {

    private final Vector2i center;
    private final World world;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    private final int scale;
    private final MapCanvas canvas;
    private final boolean locked;
    private final ImmutableSet<MapDecoration> decorations;

    private final ImmutableBoundedValue<Integer> scaleValue;

    public ImmutableSpongeMapInfoData() {
        this(Vector2i.ZERO,
                (World) WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get(),
                Constants.Map.DEFAULT_TRACKS_PLAYERS,
                Constants.Map.DEFAULT_UNLIMITED_TRACKING, Constants.Map.DEFAULT_MAP_SCALE,
                MapCanvas.blank(), Constants.Map.DEFAULT_MAP_LOCKED, ImmutableSet.of());
    }

    public ImmutableSpongeMapInfoData(Vector2i center, World world, boolean trackingPosition, boolean unlimitedTracking, int scale, MapCanvas canvas, boolean locked, ImmutableSet<MapDecoration> mapDecorations) {
        this.center = center;
        this.world = world;
        this.trackingPosition = trackingPosition;
        this.unlimitedTracking = unlimitedTracking;
        this.scale = scale;
        this.canvas = canvas;
        this.locked = locked;
        this.decorations = mapDecorations;

        scaleValue = SpongeValueFactory.boundedBuilder(Keys.MAP_SCALE)
                .defaultValue(this.scale)
                .actualValue(scale)
                .minimum(Constants.Map.MIN_MAP_SCALE)
                .maximum(Constants.Map.MAX_MAP_SCALE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.MAP_LOCATION, ImmutableSpongeMapInfoData.this::location);
        registerKeyValue(Keys.MAP_WORLD, ImmutableSpongeMapInfoData.this::world);
        registerKeyValue(Keys.MAP_TRACKS_PLAYERS, ImmutableSpongeMapInfoData.this::trackPosition);
        registerKeyValue(Keys.MAP_UNLIMITED_TRACKING, ImmutableSpongeMapInfoData.this::unlimitedTracking);
        registerKeyValue(Keys.MAP_SCALE, ImmutableSpongeMapInfoData.this::scale);
        registerKeyValue(Keys.MAP_CANVAS, ImmutableSpongeMapInfoData.this::canvas);
        registerKeyValue(Keys.MAP_LOCKED, ImmutableSpongeMapInfoData.this::locked);
        registerKeyValue(Keys.MAP_DECORATIONS, ImmutableSpongeMapInfoData.this::decorations);

        registerFieldGetter(Keys.MAP_LOCATION, () -> this.center);
        registerFieldGetter(Keys.MAP_WORLD, () -> this.world);
        registerFieldGetter(Keys.MAP_TRACKS_PLAYERS, () -> this.trackingPosition);
        registerFieldGetter(Keys.MAP_UNLIMITED_TRACKING, () -> this.unlimitedTracking);
        registerFieldGetter(Keys.MAP_SCALE, () -> this.scale);
        registerFieldGetter(Keys.MAP_CANVAS, () -> this.canvas);
        registerFieldGetter(Keys.MAP_LOCKED, () -> this.locked);
        registerFieldGetter(Keys.MAP_DECORATIONS, () -> this.decorations);
    }

    @Override
    public ImmutableValue<Vector2i> location() {
        return new ImmutableSpongeValue<>(Keys.MAP_LOCATION, Vector2i.ZERO, this.center);
    }

    @Override
    public ImmutableValue<World> world() {
        return new ImmutableSpongeValue<>(Keys.MAP_WORLD,
                (World) WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get(),
                this.world);
    }

    @Override
    public ImmutableValue<Boolean> trackPosition() {
        return new ImmutableSpongeValue<>(Keys.MAP_TRACKS_PLAYERS,
                Constants.Map.DEFAULT_TRACKS_PLAYERS, this.trackingPosition);
    }

    @Override
    public ImmutableValue<Boolean> unlimitedTracking() {
        return new ImmutableSpongeValue<>(Keys.MAP_UNLIMITED_TRACKING,
                Constants.Map.DEFAULT_UNLIMITED_TRACKING, this.unlimitedTracking);
    }

    @Override
    public ImmutableBoundedValue<Integer> scale() {
        return this.scaleValue;

    }

    @Override
    public ImmutableValue<MapCanvas> canvas() {
        return new ImmutableSpongeValue<>(Keys.MAP_CANVAS, MapCanvas.blank(), canvas);
    }

    @Override
    public ImmutableSetValue<MapDecoration> decorations() {
        return new ImmutableSpongeSetValue<>(Keys.MAP_DECORATIONS, this.decorations);
    }

    @Override
    public ImmutableValue<Boolean> locked() {
        return new ImmutableSpongeValue<>(Keys.MAP_LOCKED, Constants.Map.DEFAULT_MAP_LOCKED, this.locked);
    }

    @Override
    public MapInfoData asMutable() {
        return new SpongeMapInfoData(this.center, this.world,
                this.trackingPosition, this.unlimitedTracking, this.scale,
                this.canvas, this.locked, Sets.newHashSet(this.decorations.iterator()));
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.MAP_LOCATION, this.center)
                .set(Keys.MAP_WORLD, this.world)
                .set(Keys.MAP_TRACKS_PLAYERS, this.trackingPosition)
                .set(Keys.MAP_UNLIMITED_TRACKING, this.unlimitedTracking)
                .set(Keys.MAP_SCALE, this.scale)
                .set(Keys.MAP_CANVAS, this.canvas)
                .set(Keys.MAP_LOCKED, this.locked)
                .set(Keys.MAP_DECORATIONS, this.decorations);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }
}
