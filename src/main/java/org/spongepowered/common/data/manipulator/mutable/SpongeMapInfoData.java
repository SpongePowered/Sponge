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
package org.spongepowered.common.data.manipulator.mutable;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMapInfoData;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeMapInfoData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.map.canvas.SpongeMapByteCanvas;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@ImplementationRequiredForTest
public class SpongeMapInfoData extends AbstractData<MapInfoData, ImmutableMapInfoData> implements MapInfoData {

    private Vector2i center;
    private World world;
    private boolean trackingPosition;
    private boolean unlimitedTracking;
    private int scale;
    private MapCanvas canvas;
    private boolean locked;
    private Set<MapDecoration> decorations;

    public SpongeMapInfoData() {
        this(Vector2i.ZERO,
                (World)WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get(),
                Constants.Map.DEFAULT_TRACKS_PLAYERS,
                Constants.Map.DEFAULT_UNLIMITED_TRACKING, Constants.Map.DEFAULT_MAP_SCALE,
                MapCanvas.blank(), Constants.Map.DEFAULT_MAP_LOCKED, Sets.newHashSet());
    }

    public SpongeMapInfoData(Vector2i center, World world, boolean trackingPosition, boolean unlimitedTracking, int scale, MapCanvas canvas, boolean locked, Set<MapDecoration> mapDecorations) {
        super(MapInfoData.class);
        this.center = center;
        this.world = world;
        this.trackingPosition = trackingPosition;
        this.unlimitedTracking = unlimitedTracking;
        this.scale = scale;
        this.canvas = canvas;
        this.locked = locked;
        this.decorations = mapDecorations;

        registerGettersAndSetters();
    }

    public SpongeMapInfoData(MapData mapData) {
        this(
                new Vector2i(mapData.xCenter, mapData.zCenter),
                (org.spongepowered.api.world.World)WorldManager
                        .getWorldByDimensionId(((MapDataBridge) mapData).bridge$getDimensionId()).get(),
                mapData.trackingPosition,
                mapData.unlimitedTracking,
                mapData.scale,
                new SpongeMapByteCanvas(mapData.colors),
                ((MapDataBridge) mapData).bridge$isLocked(),
                mapData.mapDecorations.values().stream()
                    .map(value -> (MapDecoration)value)
                    .collect(Collectors.toSet())
        );
    }

    @Override
    public Value<Vector2i> location() {
        return new SpongeValue<>(Keys.MAP_LOCATION, Vector2i.ZERO, this.center);
    }

    @Override
    public Value<World> world() {
        return new SpongeValue<>(Keys.MAP_WORLD,
                (World)WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get(), this.world);
    }

    @Override
    public Value<Boolean> trackPosition() {
        return new SpongeValue<>(Keys.MAP_TRACKS_PLAYERS,
                Constants.Map.DEFAULT_TRACKS_PLAYERS, this.trackingPosition);
    }

    @Override
    public Value<Boolean> unlimitedTracking() {
        return new SpongeValue<>(Keys.MAP_UNLIMITED_TRACKING,
                Constants.Map.DEFAULT_UNLIMITED_TRACKING, this.unlimitedTracking);
    }

    @Override
    public MutableBoundedValue<Integer> scale() {
        return new SpongeValueFactory.SpongeBoundedValueBuilder<>(Keys.MAP_SCALE)
                .defaultValue(Constants.Map.DEFAULT_MAP_SCALE)
                .actualValue(this.scale)
                .minimum(Constants.Map.MIN_MAP_SCALE)
                .maximum(Constants.Map.MAX_MAP_SCALE)
                .build();
    }

    @Override
    public Value<MapCanvas> canvas() {
        return new SpongeValue<>(Keys.MAP_CANVAS, MapCanvas.blank(), this.canvas);
    }

    @Override
    public SetValue<MapDecoration> decorations() {
        return new SpongeSetValue<>(Keys.MAP_DECORATIONS, this.decorations);
    }

    @Override
    public Value<Boolean> locked() {
        return new SpongeValue<>(Keys.MAP_LOCKED, Constants.Map.DEFAULT_MAP_LOCKED, this.locked);
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
    protected void registerGettersAndSetters() {
        registerKeyValue(Keys.MAP_LOCATION, SpongeMapInfoData.this::location);
        registerKeyValue(Keys.MAP_WORLD, SpongeMapInfoData.this::world);
        registerKeyValue(Keys.MAP_TRACKS_PLAYERS, SpongeMapInfoData.this::trackPosition);
        registerKeyValue(Keys.MAP_UNLIMITED_TRACKING, SpongeMapInfoData.this::unlimitedTracking);
        registerKeyValue(Keys.MAP_SCALE, SpongeMapInfoData.this::scale);
        registerKeyValue(Keys.MAP_CANVAS, SpongeMapInfoData.this::canvas);
        registerKeyValue(Keys.MAP_LOCKED, SpongeMapInfoData.this::locked);
        registerKeyValue(Keys.MAP_DECORATIONS, SpongeMapInfoData.this::decorations);

        registerFieldGetter(Keys.MAP_LOCATION, () -> this.center);
        registerFieldGetter(Keys.MAP_WORLD, () -> this.world);
        registerFieldGetter(Keys.MAP_TRACKS_PLAYERS, () -> this.trackingPosition);
        registerFieldGetter(Keys.MAP_UNLIMITED_TRACKING, () -> this.unlimitedTracking);
        registerFieldGetter(Keys.MAP_SCALE, () -> this.scale);
        registerFieldGetter(Keys.MAP_CANVAS, () -> this.canvas);
        registerFieldGetter(Keys.MAP_LOCKED, () -> this.locked);
        registerFieldGetter(Keys.MAP_DECORATIONS, () -> this.decorations);

        registerFieldSetter(Keys.MAP_LOCATION, location -> this.center = checkNotNull(location));
        registerFieldSetter(Keys.MAP_WORLD, world -> this.world = checkNotNull(world));
        registerFieldSetter(Keys.MAP_TRACKS_PLAYERS, tracksPlayers -> this.trackingPosition = checkNotNull(tracksPlayers));
        registerFieldSetter(Keys.MAP_UNLIMITED_TRACKING, unlimitedTracking -> this.unlimitedTracking = checkNotNull(unlimitedTracking));
        registerFieldSetter(Keys.MAP_SCALE, scale -> this.scale = checkNotNull(scale));
        registerFieldSetter(Keys.MAP_CANVAS, canvas -> this.canvas = checkNotNull(canvas));
        registerFieldSetter(Keys.MAP_LOCKED, locked -> this.locked = checkNotNull(locked));
        registerFieldSetter(Keys.MAP_DECORATIONS, decorations -> this.decorations = checkNotNull(decorations));
    }

    @Override
    public MapInfoData copy() {
        return new SpongeMapInfoData(this.center, this.world,
                this.trackingPosition, this.unlimitedTracking, this.scale,
                this.canvas, this.locked, this.decorations);
    }

    @Override
    public ImmutableMapInfoData asImmutable() {
        return new ImmutableSpongeMapInfoData(this.center, this.world,
                this.trackingPosition, this.unlimitedTracking, this.scale,
                this.canvas, this.locked, ImmutableSet.copyOf(this.decorations));
    }
}
