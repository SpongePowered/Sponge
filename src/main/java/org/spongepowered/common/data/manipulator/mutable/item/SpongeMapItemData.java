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
package org.spongepowered.common.data.manipulator.mutable.item;

import com.flowpowered.math.vector.Vector2i;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.world.World;
import org.spongepowered.common.bridge.optimization.OptimizedMapDataBridge;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeMapItemData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

import static com.google.common.base.Preconditions.checkNotNull;

@ImplementationRequiredForTest
public class SpongeMapItemData extends AbstractData<MapItemData, ImmutableMapItemData> implements MapItemData {

    private Vector2i center;
    private World world;
    private boolean trackingPosition;
    private boolean unlimitedTracking;
    private byte scale;

    public SpongeMapItemData() {
        this(Vector2i.ZERO, Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get(),
                Constants.ItemStack.DEFAULT_TRACKS_PLAYERS,
                Constants.ItemStack.DEFAULT_UNLIMITED_TRACKING, Constants.ItemStack.DEFAULT_MAP_SCALE);
    }

    public SpongeMapItemData(Vector2i center, World world, boolean trackingPosition, boolean unlimitedTracking, byte scale) {
        super(MapItemData.class);
        this.center = center;
        this.world = world;
        this.trackingPosition = trackingPosition;
        this.unlimitedTracking = unlimitedTracking;
        this.scale = scale;
        registerGettersAndSetters();
    }

    public SpongeMapItemData(MapData mapData) {
        this(
                new Vector2i(mapData.xCenter, mapData.zCenter),
                (org.spongepowered.api.world.World)WorldManager
                        .getWorldByDimensionId(((OptimizedMapDataBridge) mapData).getWorldId()).get(),
                mapData.trackingPosition,
                mapData.unlimitedTracking,
                mapData.scale
        );
    }

    @Override
    public Value<Vector2i> location() {
        return new SpongeValue<>(Keys.MAP_LOCATION, Vector2i.ZERO, this.center);
    }

    @Override
    public Value<World> world() {
        return new SpongeValue<>(Keys.MAP_WORLD,
                Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get(), this.world);
    }

    @Override
    public Value<Boolean> trackPosition() {
        return new SpongeValue<>(Keys.MAP_TRACKS_PLAYERS,
                Constants.ItemStack.DEFAULT_TRACKS_PLAYERS, this.trackingPosition);
    }

    @Override
    public Value<Boolean> unlimitedTracking() {
        return new SpongeValue<>(Keys.MAP_UNLIMITED_TRACKING,
                Constants.ItemStack.DEFAULT_UNLIMITED_TRACKING, this.unlimitedTracking);
    }

    @Override
    public Value<Byte> scale() {
        return new SpongeValue<>(Keys.MAP_SCALE,
                Constants.ItemStack.DEFAULT_MAP_SCALE, this.scale);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.MAP_LOCATION, this.center)
                .set(Keys.MAP_WORLD, this.world)
                .set(Keys.MAP_TRACKS_PLAYERS, this.trackingPosition)
                .set(Keys.MAP_UNLIMITED_TRACKING, this.unlimitedTracking)
                .set(Keys.MAP_SCALE, this.scale);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(Keys.MAP_LOCATION, SpongeMapItemData.this::location);
        registerKeyValue(Keys.MAP_WORLD, SpongeMapItemData.this::world);
        registerKeyValue(Keys.MAP_TRACKS_PLAYERS, SpongeMapItemData.this::trackPosition);
        registerKeyValue(Keys.MAP_UNLIMITED_TRACKING, SpongeMapItemData.this::unlimitedTracking);
        registerKeyValue(Keys.MAP_SCALE, SpongeMapItemData.this::scale);

        registerFieldGetter(Keys.MAP_LOCATION, () -> this.center);
        registerFieldGetter(Keys.MAP_WORLD, () -> this.world);
        registerFieldGetter(Keys.MAP_TRACKS_PLAYERS, () -> this.trackingPosition);
        registerFieldGetter(Keys.MAP_UNLIMITED_TRACKING, () -> this.unlimitedTracking);
        registerFieldGetter(Keys.MAP_SCALE, () -> this.scale);

        registerFieldSetter(Keys.MAP_LOCATION, location -> this.center = checkNotNull(location));
        registerFieldSetter(Keys.MAP_WORLD, world -> this.world = checkNotNull(world));
        registerFieldSetter(Keys.MAP_TRACKS_PLAYERS, tracksPlayers -> this.trackingPosition = checkNotNull(tracksPlayers));
        registerFieldSetter(Keys.MAP_UNLIMITED_TRACKING, unlimitedTracking -> this.unlimitedTracking = checkNotNull(unlimitedTracking));
        registerFieldSetter(Keys.MAP_SCALE, scale -> this.scale = checkNotNull(scale));
    }

    @Override
    public MapItemData copy() {
        return new SpongeMapItemData(this.center, this.world,
                this.trackingPosition, this.unlimitedTracking, this.scale);
    }

    @Override
    public ImmutableMapItemData asImmutable() {
        return new ImmutableSpongeMapItemData(this.center, this.world,
                this.trackingPosition, this.unlimitedTracking, this.scale);
    }
}
