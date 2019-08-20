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
package org.spongepowered.common.world.schematic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.ImmutableBlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.StorageType;
import org.spongepowered.api.world.extent.UnmodifiableBlockVolume;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.util.gen.AbstractBlockBuffer;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SpongeArchetypeVolume extends AbstractBlockBuffer implements ArchetypeVolume {

    private final MutableBlockVolume backing;
    private final Map<Vector3i, TileEntityArchetype> tiles;
    private final Collection<EntityArchetype> entities;

    public SpongeArchetypeVolume(MutableBlockVolume backing, Map<Vector3i, TileEntityArchetype> tiles, Collection<EntityArchetype> entities) {
        super(backing.getBlockMin(), backing.getBlockSize());
        this.backing = backing instanceof SpongeArchetypeVolume ? ((SpongeArchetypeVolume) backing).backing : backing;
        this.tiles = Maps.newHashMap(tiles);
        this.entities = new ArrayList<>(entities);
    }

    @Override
    public Palette<BlockState> getPalette() {
        return ((AbstractBlockBuffer) this.backing).getPalette();
    }

    @Override
    public Optional<TileEntityArchetype> getTileEntityArchetype(int x, int y, int z) {
        return Optional.ofNullable(this.tiles.get(getBlockMin().add(x, y, z)));
    }

    @Override
    public Map<Vector3i, TileEntityArchetype> getTileEntityArchetypes() {
        return this.tiles;
    }

    @Override
    public Optional<EntityArchetype> getEntityArchetype(double x, double y, double z) {
        if (this.entities.isEmpty()) {
            return Optional.empty();
        }
        for (EntityArchetype entity : this.entities) {
            Optional<Vector3d> position = ((SpongeEntityArchetype) entity).getPosition();
            if (position.isPresent()) {
                if (position.get().getX() == x && position.get().getY() == y && position.get().getZ() == z) {
                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public ListMultimap<Vector3d, EntityArchetype> getEntitiesByPosition() {
        if (this.entities.isEmpty()) {
            return ImmutableListMultimap.of();
        }
        ListMultimap<Vector3d, EntityArchetype> multimap = ArrayListMultimap.create();
        for (EntityArchetype entity : this.entities) {
            multimap.put(((SpongeEntityArchetype) entity).getPosition().get(), entity);

        }
        return multimap;
    }

    @Override
    public Collection<EntityArchetype> getEntityArchetypes() {
        return this.entities;
    }

    @Override
    public MutableBlockVolumeWorker<? extends ArchetypeVolume> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @Override
    public void apply(Location<World> location, BlockChangeFlag changeFlag) {
        this.backing.getBlockWorker().iterate((v, x, y, z) -> {
            location.getExtent().setBlock(x + location.getBlockX(), y + location.getBlockY(), z + location.getBlockZ(), v.getBlock(x, y, z), changeFlag);
        });
        for (Vector3i pos : this.tiles.keySet()) {
            TileEntityArchetype archetype = this.tiles.get(pos);
            archetype.apply(location.add(pos));
        }
        if (!this.entities.isEmpty()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
                for (EntityArchetype entity : this.entities) {
                    final Vector3d vector3d = ((SpongeEntityArchetype) entity).getPosition().get();
                    final Location<World> target = location.add(vector3d);
                    entity.apply(target);
                }
            }
        }
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        this.backing.setBlock(x, y, z, block);
        return true;
    }

    @Override
    public MutableBlockVolume getBlockView(Vector3i newMin, Vector3i newMax) {
        return this.backing.getBlockView(newMin, newMax);
    }

    @Override
    public MutableBlockVolume getBlockView(DiscreteTransform3 transform) {
        return this.backing.getBlockView(transform);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return this.backing.getBlock(x, y, z);
    }

    @Override
    public UnmodifiableBlockVolume getUnmodifiableBlockView() {
        return this.backing.getUnmodifiableBlockView();
    }

    @Override
    public MutableBlockVolume getBlockCopy(StorageType type) {
        return this.backing.getBlockCopy(type);
    }

    @Override
    public ImmutableBlockVolume getImmutableBlockCopy() {
        return this.backing.getImmutableBlockCopy();
    }

    public MutableBlockVolume getBacking() {
        return this.backing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpongeArchetypeVolume that = (SpongeArchetypeVolume) o;
        final boolean equals = this.backing.equals(that.backing);
        final boolean equals1 = this.tiles.equals(that.tiles);
        final boolean equals2 = this.entities.equals(that.entities);
        return equals &&
               equals1 &&
               equals2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.backing, this.tiles, this.entities);
    }
}
