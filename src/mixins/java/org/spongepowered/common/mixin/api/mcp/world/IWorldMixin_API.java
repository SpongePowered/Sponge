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
package org.spongepowered.common.mixin.api.mcp.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ProtoWorld;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.NonNullArrayList;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mixin(IWorld.class)
public interface IWorldMixin_API<T extends ProtoWorld<T>> extends ProtoWorld<T> {

    @Shadow long shadow$getSeed();
    @Shadow net.minecraft.world.World shadow$getWorld();
    @Shadow WorldInfo shadow$getWorldInfo();
    @Shadow DifficultyInstance shadow$getDifficultyForLocation(BlockPos p_175649_1_);
    @Shadow AbstractChunkProvider shadow$getChunkProvider();
    @Shadow boolean shadow$chunkExists(int p_217354_1_, int p_217354_2_);
    @Shadow Random shadow$getRandom();

    // MutableBiomeVolume

    @Override
    default boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
        final IChunk iChunk = ((IWorld) this).getChunk(x >> 4, z >> 4, ChunkStatus.BIOMES, true);
        if (iChunk == null) {
            return false;
        }
        return ((ProtoChunk) iChunk).setBiome(x, y, z, biome);
    }

    // Volume

    @Override
    default Vector3i getBlockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Vector3i getBlockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default Vector3i getBlockSize() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IWorld that isn't part of Sponge API: " + this.getClass());
    }

    @Override
    default boolean containsBlock(final int x, final int y, final int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    @Override
    default boolean isAreaAvailable(final int x, final int y, final int z) {
        return this.shadow$chunkExists(x >> 4, z >> 4);
    }

    // ReadableEntityVolume

    @Override
    default Optional<Entity> getEntity(final UUID uuid) {
        return Optional.empty();
    }

    // RandomProvider

    @Override
    default Random getRandom() {
        return this.shadow$getRandom();
    }

    // ProtoWorld

    @Override
    default long getSeed() {
        return this.shadow$getSeed();
    }

    // MutableEntityVolume

    @Override
    default Entity createEntity(final EntityType<?> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return this.impl$createEntity(type, position, false);
    }

    @Override
    default Entity createEntityNaturally(final EntityType<?> type, final Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return this.impl$createEntity(type, position, true);
    }

    @Override
    default Optional<Entity> createEntity(final DataContainer entityContainer) {
        throw new UnsupportedOperationException("Implement me"); // TODO implement me
    }

    @Override
    default Optional<Entity> createEntity(final DataContainer entityContainer, final Vector3d position) {
        throw new UnsupportedOperationException("Implement me"); // TODO implement me
    }

    default Entity impl$createEntity(final EntityType<?> type, final Vector3d position, final boolean naturally) throws IllegalArgumentException, IllegalStateException {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");

        if (type == net.minecraft.entity.EntityType.PLAYER) {
            // Unable to construct these
            throw new IllegalArgumentException("Cannot construct " + type.getKey() + " please look to using entity types correctly!");
        }

        net.minecraft.entity.Entity entity = null;
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        final World thisWorld = this.shadow$getWorld();
        // Not all entities have a single World parameter as their constructor
        if (type == net.minecraft.entity.EntityType.LIGHTNING_BOLT) {
            entity = new LightningBoltEntity(thisWorld, x, y, z, false);
        }
        // TODO - archetypes should solve the problem of calling the correct constructor
        if (type == net.minecraft.entity.EntityType.ENDER_PEARL) {
            final ArmorStandEntity tempEntity = new ArmorStandEntity(thisWorld, x, y, z);
            tempEntity.setPosition(tempEntity.getPosX(), tempEntity.getPosY() - tempEntity.getEyeHeight(), tempEntity.getPosZ());
            entity = new EnderPearlEntity(thisWorld, tempEntity);
            ((EnderPearl) entity).offer(Keys.SHOOTER, UnknownProjectileSource.UNKNOWN);
        }
        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (type == net.minecraft.entity.EntityType.FALLING_BLOCK) {
            entity = new FallingBlockEntity(thisWorld, x, y, z, Blocks.SAND.getDefaultState());
        }
        if (type == net.minecraft.entity.EntityType.ITEM) {
            entity = new ItemEntity(thisWorld, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            try {
                entity = ((net.minecraft.entity.EntityType) type).create(thisWorld);
                entity.setPosition(x, y, z);
            } catch (final Exception e) {
                throw new RuntimeException("There was an issue attempting to construct " + type.getKey(), e);
            }
        }

        // TODO - replace this with an actual check
        /*
        if (entity instanceof EntityHanging) {
            if (((EntityHanging) entity).facingDirection == null) {
                // TODO Some sort of detection of a valid direction?
                // i.e scan immediate blocks for something to attach onto.
                ((EntityHanging) entity).facingDirection = EnumFacing.NORTH;
            }
            if (!((EntityHanging) entity).onValidSurface()) {
                return Optional.empty();
            }
        }*/

        if (naturally && entity instanceof MobEntity) {
            // Adding the default equipment
            final DifficultyInstance difficulty = this.shadow$getDifficultyForLocation(new BlockPos(x, y, z));
            ((MobEntityAccessor)entity).accessor$setEquipmentBasedOnDifficulty(difficulty);
        }

        if (entity instanceof PaintingEntity) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((PaintingEntity) entity).art = PaintingType.KEBAB;
        }

        return (Entity) entity;
    }

    @Override
    default Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        final List<Entity> entitiesToSpawn = new NonNullArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory
                .createSpawnEntityEventCustom(PhaseTracker.getCauseStackManager().getCurrentCause(), entitiesToSpawn);
        if (Sponge.getEventManager().post(event)) {
            return ImmutableList.of();
        }
        for (final Entity entity : event.getEntities()) {
            EntityUtil.processEntitySpawn(entity, Optional::empty);
        }

        final ImmutableList.Builder<Entity> builder = ImmutableList.builder();
        for (final Entity entity : event.getEntities()) {
            builder.add(entity);
        }
        return builder.build();
    }

    @Override
    default boolean spawnEntity(final Entity entity) {
        Preconditions.checkNotNull(entity, "The entity cannot be null!");
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> state = phaseTracker.getCurrentState();
        if (!state.alreadyCapturingEntitySpawns()) {
            try (final BasicPluginContext context = PluginPhase.State.CUSTOM_SPAWN.createPhaseContext(PhaseTracker.SERVER)) {
                context.buildAndSwitch();
                phaseTracker.spawnEntityWithCause((org.spongepowered.api.world.World<?>) (Object) this, entity);
                return true;
            }
        }
        return phaseTracker.spawnEntityWithCause((org.spongepowered.api.world.World<?>) (Object) this, entity);
    }

    // MutableBlockVolume

    @Override
    default boolean setBlock(
        final int x, final int y, final int z, final org.spongepowered.api.block.BlockState blockState, final BlockChangeFlag flag) {
        // TODO Minecraft 1.14 - PhaseTracker for gabizou

        if (!this.containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
        }
        final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentState();
        final boolean isWorldGen = state.isWorldGeneration();
        final boolean handlesOwnCompletion = state.handlesOwnStateCompletion();
        if (!isWorldGen) {
            Preconditions.checkArgument(flag != null, "BlockChangeFlag cannot be null!");
        }
        try (final PhaseContext<?> context = isWorldGen || handlesOwnCompletion ? null
                : PluginPhase.State.BLOCK_WORKER.createPhaseContext(PhaseTracker.SERVER)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return ((IWorld) this).setBlockState(new BlockPos(x, y, z), (BlockState) blockState, ((SpongeBlockChangeFlag) flag).getRawFlag());
        }
    }

}
