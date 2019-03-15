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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.volume.biome.worker.MutableBiomeVolumeStream;
import org.spongepowered.api.world.volume.block.worker.MutableBlockVolumeStream;
import org.spongepowered.api.world.volume.entity.worker.MutableEntityStream;
import org.spongepowered.api.world.volume.tileentity.worker.MutableTileEntityStream;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldUtil;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
@Mixin(IWorld.class)
public interface MixinIWorld_API<P extends ProtoWorld<P>> extends MixinIWorldReaderBase_API<P>, MixinIWorldWriter_API, ProtoWorld<P> {

    @Shadow long shadow$getSeed();
    @Shadow World shadow$getWorld();
    @Shadow WorldInfo getWorldInfo();
    @Shadow IChunkProvider getChunkProvider();
    @Shadow IChunk getChunk(int chunkX, int chunkZ);
    @Shadow ITickList<Block> getPendingBlockTicks();
    @Shadow ITickList<Fluid> getPendingFluidTicks();

    default boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, WorldUtil.BIOME_MIN, WorldUtil.BIOME_MAX);
    }

    default boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, WorldUtil.BLOCK_MIN, WorldUtil.BLOCK_MAX);
    }

    default void checkBiomeBounds(int x, int y, int z) {
        if (!containsBiome(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), WorldUtil.BIOME_MIN, WorldUtil.BIOME_MAX);
        }
    }

    default void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), WorldUtil.BLOCK_MIN, WorldUtil.BLOCK_MAX);
        }
    }

    @Override
    default ProtoChunk<?> getChunk(int cx, int cy, int cz) {
        return (ProtoChunk) getChunk(cx, cz);
    }

    @Override
    default org.spongepowered.api.world.World getWorld() {
        return (org.spongepowered.api.world.World) shadow$getWorld();
    }

    @Override
    default boolean setBiome(int x, int y, int z, BiomeType biome) {
        checkBiomeBounds(x, y, z);
        final ProtoChunk<?> chunk = getChunk(x >> 4, y, z >> 4);
        checkState(!chunk.isEmpty(), "Requested chunk is empty!");
        return chunk.setBiome(x, y, z, biome);
    }

    @Override
    default P getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default MutableBiomeVolumeStream<P> toBiomeStream() {
        return null; // TODO - implement biome streams.
    }

    @Override
    default MutableBlockVolumeStream<P> toBlockStream() {
        return null; // TODO - implement streams
    }

    @Override
    default MutableEntityStream toEntityStream() {
        return null; // TODO - implement streams
    }

    @Intrinsic
    @Override
    default long getSeed() {
        return shadow$getSeed();
    }

    @Override
    default TerrainGenerator<?> getTerrainGenerator() {
        return (TerrainGenerator) getChunkProvider().getChunkGenerator();
    }

    @Override
    default WorldProperties getProperties() {
        return (WorldProperties) getWorldInfo();
    }

    @Override
    default boolean removeBlock(Vector3i position) {
        return removeBlock(VecHelper.toBlockPos(position));
    }

    @Override
    default boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default boolean placeBlock(int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default boolean digBlock(int x, int y, int z, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }

    @Override
    default Duration getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge implementation");
    }


    @Override
    default ScheduledUpdateList<FluidType> getScheduledFluidUpdates() {
        return (ScheduledUpdateList) getPendingFluidTicks();
    }

    @Override
    default ScheduledUpdateList<BlockType> getScheduledBlockUpdates() {
        return (ScheduledUpdateList) getPendingBlockTicks();
    }

    @Override
    default Entity createEntity(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        checkState(shadow$getWorld() != null, "Invalid World type");
        try {
            final net.minecraft.entity.Entity entity = ((net.minecraft.entity.EntityType) type).create(shadow$getWorld());
            entity.setPosition(position.getX(), position.getY(), position.getZ());
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
        return null;
    }

    @Override
    default Entity createEntityNaturally(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        checkState(shadow$getWorld() != null, "Invalid World Type!");
        try {
            final net.minecraft.entity.Entity entity = ((net.minecraft.entity.EntityType) type).makeEntity(shadow$getWorld(), null,
                null,
                null,
                VecHelper.toBlockPos(position),
                true,
                true);
            if (entity == null) {
                throw new IllegalArgumentException("Unknown reason why an entity would be null...");
            }
            return EntityUtil.fromNative(entity);
        } catch (Exception e) {
            throw new IllegalStateException("Some exception was thrown trying to create an entity: ", e);
        }

    }

    @Override
    default Optional<Entity> createEntity(DataContainer entityContainer) {
        final NBTTagCompound translate = NbtTranslator.getInstance().translate(entityContainer);
        if (translate.isEmpty()) {
            return Optional.empty();
        }
        final String string = translate.getString(NbtDataUtil.ENTITY_TYPE_ID);
        final net.minecraft.entity.EntityType<?> type = net.minecraft.entity.EntityType.getById(string);
        if (type == null) {
            return Optional.empty();
        }
        final NBTTagList position = translate.getList(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.TAG_DOUBLE);
        final BlockPos entityPosition = new BlockPos(position.getDouble(0), position.getDouble(1), position.getDouble(2));
        final net.minecraft.entity.Entity entity = type.makeEntity(shadow$getWorld(), translate, null, null, entityPosition, true, false);
        return Optional.ofNullable((Entity) entity);
    }

    @Override
    default Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        final NBTTagCompound translate = NbtTranslator.getInstance().translate(entityContainer);
        if (translate.isEmpty()) {
            return Optional.empty();
        }
        final String string = translate.getString(NbtDataUtil.ENTITY_TYPE_ID);
        final net.minecraft.entity.EntityType<?> type = net.minecraft.entity.EntityType.getById(string);
        if (type == null) {
            return Optional.empty();
        }
        final net.minecraft.entity.Entity entity = type.makeEntity(shadow$getWorld(), translate, null, null, VecHelper.toBlockPos(position), true, false);
        return Optional.ofNullable((Entity) entity);
    }


    @Override
    default Collection<Entity> spawnEntities(Iterable<? extends Entity> entities){
        return StreamSupport.stream(entities.spliterator(), false)
            .map(EntityUtil::toNative)
            .filter(this::spawnEntity)
            .map(EntityUtil::fromNative)
            .collect(Collectors.toList());
    }

    @Override
    default boolean spawnEntity(Entity entity) {
        return spawnEntity(EntityUtil.toNative(entity));
    }

    @Override
    default boolean removeBlock(int x, int y, int z) {
        return removeBlock(new BlockPos(x, y, z));
    }
}
