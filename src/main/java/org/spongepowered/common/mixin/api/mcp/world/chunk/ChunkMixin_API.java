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
package org.spongepowered.common.mixin.api.mcp.world.chunk;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapEntryBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeVolumeWorker;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_API implements Chunk {

    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private long inhabitedTime;
    @Shadow private boolean loaded;
    @Shadow public boolean unloadQueued;

    // @formatter:off
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(int x, int y, int z);
    @Shadow public abstract Biome getBiome(BlockPos pos, BiomeProvider chunkManager);
    @Shadow public abstract byte[] getBiomeArray();
    @Shadow public abstract void setBiomeArray(byte[] biomeArray);
    @Shadow public abstract <T extends Entity> void getEntitiesOfTypeWithinAABB(Class <? extends T > entityClass, AxisAlignedBB aabb,
    List<T> listToFill, Predicate <? super T > p_177430_4_);
    @Shadow public abstract BlockPos getPrecipitationHeight(BlockPos pos);
    // @formatter:on

    @Shadow public abstract ChunkPos getPos();

    @Nullable private Vector3i api$chunkPos;
    @Nullable private Vector3i api$blockMin;
    @Nullable private Vector3i api$blockMax;
    @Nullable private Vector3i api$biomeMin;
    @Nullable private Vector3i api$biomeMax;
    @Nullable private UUID api$uuid;

    @Override
    public UUID getUniqueId() {
        if (this.api$uuid == null) {
            @Nullable final UUID uuid = ((org.spongepowered.api.world.World) this.world).getUniqueId();
            if (uuid != null) {
                this.api$uuid = new UUID(uuid.getMostSignificantBits() ^ (this.x * 2 + 1), uuid.getLeastSignificantBits() ^ this.z * 2 + 1);
            }
            this.api$uuid = UUID.randomUUID();
        }
        return this.api$uuid;
    }

    @Override
    public Vector3i getPosition() {
        if (this.api$chunkPos == null) {
            this.api$chunkPos = new Vector3i(this.x, 0, this.z);
        }
        return this.api$chunkPos;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public boolean loadChunk(boolean generate) {
        WorldServer worldserver = (WorldServer) this.world;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.getChunkProvider().chunkExists(this.x, this.z) || generate) {
            chunk = worldserver.getChunkProvider().loadChunk(this.x, this.z);
        }

        return chunk != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getInhabittedTime() {
        return (int) this.inhabitedTime;
    }

    @Override
    public int getInhabitedTime() {
        return (int) this.inhabitedTime;
    }

    @Override
    public double getRegionalDifficultyFactor() {
        final boolean flag = this.world.getDifficulty() == EnumDifficulty.HARD;
        float moon = this.world.getCurrentMoonPhaseFactor();
        float f2 = MathHelper.clamp((this.world.getWorldTime() - 72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
        float f3 = 0.0F;
        f3 += MathHelper.clamp(this.inhabitedTime / 3600000.0F, 0.0F, 1.0F) * (flag ? 1.0F : 0.75F);
        f3 += MathHelper.clamp(moon * 0.25F, 0.0F, f2);
        return f3;
    }

    @Override
    public double getRegionalDifficultyPercentage() {
        final double region = getRegionalDifficultyFactor();
        if (region < 2) {
            return 0;
        } else if (region > 4) {
            return 1.0;
        } else {
            return (region - 2.0) / 2.0;
        }
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return (org.spongepowered.api.world.World) this.world;
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkBiomeBounds(x, y, z);
        return (BiomeType) getBiome(new BlockPos(x, y, z), this.world.getBiomeProvider());
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkBiomeBounds(x, y, z);
        // Taken from Chunk#getBiome
        byte[] biomeArray = getBiomeArray();
        int i = x & 15;
        int j = z & 15;
        biomeArray[j << 4 | i] = (byte) (Biome.getIdForBiome((Biome) biome) & 255);
        setBiomeArray(biomeArray);

        if (this.world instanceof WorldServer) {
            final PlayerChunkMapEntry entry = ((WorldServer) this.world).getPlayerChunkMap().getEntry(this.x, this.z);
            if (entry != null) {
                ((PlayerChunkMapEntryBridge) entry).bridge$markBiomesForUpdate();
            }
        }
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        checkBlockBounds(x, y, z);
        return this.world.setBlockState(new BlockPos(x, y, z), (IBlockState) block, Constants.BlockChangeFlags.ALL);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block, BlockChangeFlag flag) {
        checkBlockBounds(x, y, z);
        return this.world.setBlockState(new BlockPos(x, y, z), (IBlockState) block, ((SpongeBlockChangeFlag) flag).getRawFlag());
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockType) getBlockState(x, y, z).getBlock();
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return ((org.spongepowered.api.world.World) this.world).createSnapshot((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return ((org.spongepowered.api.world.World) this.world).restoreSnapshot(snapshot, force, flag);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return ((org.spongepowered.api.world.World) this.world).restoreSnapshot((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), snapshot, force, flag);
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return ((org.spongepowered.api.world.World) this.world).getHighestYAt((this.x << 4) + (x & 15), (this.z << 4) + (z & 15));
    }

    @Override
    public int getPrecipitationLevelAt(int x, int z) {
        return this.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
    }

    @Override
    public Vector3i getBiomeMin() {
        if (this.api$biomeMin == null) {
            this.api$biomeMin = new Vector3i(this.getBlockMin().getX(), 0, this.getBlockMin().getZ());
        }
        return this.api$biomeMin;
    }

    @Override
    public Vector3i getBiomeMax() {
        if (this.api$biomeMax == null) {
            this.api$biomeMax = new Vector3i(this.getBlockMax().getX(), 0, this.getBlockMax().getZ());
        }
        return this.api$biomeMax;
    }

    @Override
    public Vector3i getBiomeSize() {
        return Constants.Chunk.BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        if (this.api$blockMin == null) {
            this.api$blockMin = SpongeChunkLayout.instance.forceToWorld(this.getPosition());
        }
        return this.api$blockMin;
    }

    @Override
    public Vector3i getBlockMax() {
        if (this.api$blockMax == null) {
            this.api$blockMax = this.getBlockMin().add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        }
        return this.api$blockMax;
    }

    @Override
    public Vector3i getBlockSize() {
        return SpongeChunkLayout.CHUNK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.getBiomeMin(), this.getBiomeMax());
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.getBlockMin(), this.getBlockMax());
    }

    private void checkBiomeBounds(int x, int y, int z) {
        if (!containsBiome(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.getBiomeMin(), this.getBiomeMax());
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.getBlockMin(), this.getBlockMax());
        }
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        checkBlockBounds(newMin.getX(), newMin.getY(), newMin.getZ());
        checkBlockBounds(newMax.getX(), newMax.getY(), newMax.getZ());
        return new ExtentViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeVolumeWorker<Chunk> getBiomeWorker() {
        return new SpongeMutableBiomeVolumeWorker<>(this);
    }

    @Override
    public MutableBlockVolumeWorker<Chunk> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @Override
    public org.spongepowered.api.entity.Entity createEntity(EntityType type, Vector3d position)
            throws IllegalArgumentException, IllegalStateException {
        return ((org.spongepowered.api.world.World) this.world).createEntity(type, this.getPosition().mul(16).toDouble().add(position.min(15, this.api$blockMax.getY(), 15)));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer) {
        return ((org.spongepowered.api.world.World) this.world).createEntity(entityContainer);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        return ((org.spongepowered.api.world.World) this.world).createEntity(entityContainer, this.getPosition().mul(16).toDouble().add(position.min(15, this.api$blockMax.getY(), 15)));
    }

    @Override
    public boolean spawnEntity(org.spongepowered.api.entity.Entity entity) {
        return ((org.spongepowered.api.world.World) this.world).spawnEntity(entity);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        Set<org.spongepowered.api.entity.Entity> entities = Sets.newHashSet();
        for (ClassInheritanceMultiMap entityList : this.entityLists) {
            entities.addAll(entityList);
        }
        return entities;
    }

    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities(java.util.function.Predicate<org.spongepowered.api.entity.Entity> filter) {
        Set<org.spongepowered.api.entity.Entity> entities = Sets.newHashSet();
        for (ClassInheritanceMultiMap<Entity> entityClassMap : this.entityLists) {
            for (Object entity : entityClassMap) {
                if (filter.test((org.spongepowered.api.entity.Entity) entity)) {
                    entities.add((org.spongepowered.api.entity.Entity) entity);
                }
            }
        }
        return entities;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity> getTileEntities() {
        return Sets.newHashSet((Collection) this.tileEntities.values());
    }

    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity>
    getTileEntities(java.util.function.Predicate<org.spongepowered.api.block.tileentity.TileEntity> filter) {
        Set<org.spongepowered.api.block.tileentity.TileEntity> tiles = Sets.newHashSet();
        for (Entry<BlockPos, TileEntity> entry : this.tileEntities.entrySet()) {
            if (filter.test((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue())) {
                tiles.add((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue());
            }
        }
        return tiles;
    }

    @Override
    public Optional<org.spongepowered.api.block.tileentity.TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((org.spongepowered.api.block.tileentity.TileEntity) this.getTileEntity(
                new BlockPos((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15)), EnumCreateEntityType.CHECK));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        return ((org.spongepowered.api.world.World) this.world).restoreSnapshot(snapshot, position);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        return ((org.spongepowered.api.world.World) this.world).getScheduledUpdates((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return ((org.spongepowered.api.world.World) this.world).addScheduledUpdate((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        ((org.spongepowered.api.world.World) this.world).removeScheduledUpdate((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), update);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).hitBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), side, profile);
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).interactBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), side, profile);
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).placeBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), block, side, profile);
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).interactBlockWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, side, profile);
    }

    @Override
    public boolean digBlock(int x, int y, int z, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).digBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), profile);
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).digBlockWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, profile);
    }

    @Override
    public int getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return ((org.spongepowered.api.world.World) this.world).getBlockDigTimeWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, profile);
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return ((org.spongepowered.api.world.World) this.world).getBlockSelectionBox((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public Set<org.spongepowered.api.entity.Entity> getIntersectingEntities(AABB box,
            java.util.function.Predicate<org.spongepowered.api.entity.Entity> filter) {
        checkNotNull(box, "box");
        checkNotNull(filter, "filter");
        final List<Entity> entities = new ArrayList<>();
        getEntitiesOfTypeWithinAABB(Entity.class, VecHelper.toMinecraftAABB(box), entities,
            entity -> filter.test((org.spongepowered.api.entity.Entity) entity));
        return entities.stream().map(entity -> (org.spongepowered.api.entity.Entity) entity).collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        final Vector3i max = this.api$blockMax.add(Vector3i.ONE);
        return ((org.spongepowered.api.world.World) this.world).getIntersectingBlockCollisionBoxes(box).stream()
                .filter(aabb -> VecHelper.inBounds(aabb.getCenter(), this.getBlockMin(), max))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(org.spongepowered.api.entity.Entity owner, AABB box) {
        final Vector3i max = this.api$blockMax.add(Vector3i.ONE);
        return ((org.spongepowered.api.world.World) this.world).getIntersectingCollisionBoxes(owner, box).stream()
                .filter(aabb -> VecHelper.inBounds(aabb.getCenter(), this.getBlockMin(), max))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, java.util.function.Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(end, "end");
        checkNotNull(filter, "filter");
        final Vector3d diff = end.sub(start);
        return getIntersectingEntities(start, end, diff.normalize(), diff.length(), filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(direction, "direction");
        checkNotNull(filter, "filter");
        direction = direction.normalize();
        return getIntersectingEntities(start, start.add(direction.mul(distance)), direction, distance, filter);
    }

    private Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter) {
        final Vector2d entryAndExitY = getEntryAndExitY(start, end, direction, distance);
        if (entryAndExitY == null) {
            // Doesn't intersect the chunk, ignore it
            return Collections.emptySet();
        }
        final Set<EntityHit> intersections = new HashSet<>();
        getIntersectingEntities(start, direction, distance, filter, entryAndExitY.getX(), entryAndExitY.getY(), intersections);
        return intersections;
    }

    @Nullable
    private Vector2d getEntryAndExitY(Vector3d start, Vector3d end, Vector3d direction, double distance) {
        // Modified from AABB.intersects(ray)
        // Increase the bounds to the whole chunk plus a margin of two blocks
        final Vector3i min = getBlockMin().sub(2, 2, 2);
        final Vector3i max = getBlockMax().add(3, 3, 3);
        // Find the intersections on the -x and +x planes, oriented by direction
        final double txMin;
        final double txMax;
        if (Math.copySign(1, direction.getX()) > 0) {
            txMin = (min.getX() - start.getX()) / direction.getX();
            txMax = (max.getX() - start.getX()) / direction.getX();
        } else {
            txMin = (max.getX() - start.getX()) / direction.getX();
            txMax = (min.getX() - start.getX()) / direction.getX();
        }
        // Find the intersections on the -z and +z planes, oriented by direction
        final double tzMin;
        final double tzMax;
        if (Math.copySign(1, direction.getZ()) > 0) {
            tzMin = (min.getZ() - start.getZ()) / direction.getZ();
            tzMax = (max.getZ() - start.getZ()) / direction.getZ();
        } else {
            tzMin = (max.getZ() - start.getZ()) / direction.getZ();
            tzMax = (min.getZ() - start.getZ()) / direction.getZ();
        }
        // The ray should intersect the -x plane before the +z plane and intersect
        // the -z plane before the +x plane, else it is outside the column
        if (txMin > tzMax || txMax < tzMin) {
            return null;
        }
        // The ray intersects only the furthest min plane on the column and only the closest
        // max plane on the column
        final double tMin = tzMin > txMin ? tzMin : txMin;
        final double tMax = tzMax < txMax ? tzMax : txMax;
        // If both intersection points are behind the start, there are no intersections
        if (tMax < 0) {
            return null;
        }
        // If the closest intersection is before the start, use the start y instead
        final double yEntry = tMin < 0 ? start.getY() : direction.getY() * tMin + start.getY();
        // If the furthest intersection is after the end, use the end y instead
        final double yExit = tMax > distance ? end.getY() : direction.getY() * tMax + start.getY();
        //noinspection SuspiciousNameCombination
        return new Vector2d(yEntry, yExit);
    }

    public void getIntersectingEntities(Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter, double entryY, double exitY, Set<EntityHit> intersections) {
        // Order the entry and exit y coordinates by magnitude
        final double yMin = Math.min(entryY, exitY);
        final double yMax = Math.max(entryY, exitY);
        // Added offset matches the one in Chunk.getEntitiesWithinAABBForEntity
        final int lowestSubChunk = GenericMath.clamp(GenericMath.floor((yMin - 2) / 16D), 0, this.entityLists.length - 1);
        final int highestSubChunk = GenericMath.clamp(GenericMath.floor((yMax + 2) / 16D), 0, this.entityLists.length - 1);
        // For each sub-chunk, perform intersections in its entity list
        for (int i = lowestSubChunk; i <= highestSubChunk; i++) {
            getIntersectingEntities(this.entityLists[i], start, direction, distance, filter, intersections);
        }
    }

    private void getIntersectingEntities(Collection<Entity> entities, Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter, Set<EntityHit> intersections) {
        // Check each entity in the list
        for (Entity entity : entities) {
            final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entity;
            final Optional<AABB> box = spongeEntity.getBoundingBox();
            // Can't intersect if the entity doesn't have a bounding box
            if (!box.isPresent()) {
                continue;
            }
            // Ignore entities that didn't intersect
            final Optional<Tuple<Vector3d, Vector3d>> optionalIntersection = box.get().intersects(start, direction);
            if (!optionalIntersection.isPresent()) {
                continue;
            }
            // Check that the entity isn't too far away
            final Tuple<Vector3d, Vector3d> intersection = optionalIntersection.get();
            final double distanceSquared = intersection.getFirst().sub(start).lengthSquared();
            if (distanceSquared > distance * distance) {
                continue;
            }
            // Now test the filter on the entity and intersection
            final EntityHit hit = new EntityHit(spongeEntity, intersection.getFirst(), intersection.getSecond(), Math.sqrt(distanceSquared));
            if (!filter.test(hit)) {
                continue;
            }
            // If everything passes we have an intersection!
            intersections.add(hit);
            // If the entity has part, recurse on these
            final Entity[] parts = entity.getParts();
            if (parts != null && parts.length > 0) {
                getIntersectingEntities(Arrays.asList(parts), start, direction, distance, filter, intersections);
            }
        }
    }

    @Override
    public Optional<Chunk> getNeighbor(Direction direction, boolean shouldLoad) {
        checkNotNull(direction, "direction");
        checkArgument(!direction.isSecondaryOrdinal(), "Secondary cardinal directions can't be used here");

        if (direction.isUpright() || direction == Direction.NONE) {
            return Optional.of(this);
        }

        int index = SpongeImpl.directionToIndex(direction);
        Direction secondary = SpongeImpl.getSecondaryDirection(direction);
        Chunk neighbor = null;
        neighbor = (Chunk) ((ChunkBridge) this).getNeighborArray()[index];

        if (neighbor == null && shouldLoad) {
            Vector3i neighborPosition = this.getPosition().add(SpongeImpl.getCardinalDirection(direction).asBlockOffset());
            Optional<Chunk> cardinal = this.getWorld().loadChunk(neighborPosition, true);
            if (cardinal.isPresent()) {
                neighbor = cardinal.get();
            }
        }

        if (neighbor != null && secondary != Direction.NONE) {
            return neighbor.getNeighbor(secondary, shouldLoad);
        }

        return Optional.ofNullable(neighbor);
    }


}
