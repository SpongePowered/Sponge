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
package org.spongepowered.common.mixin.core.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DataUtil;
import org.spongepowered.math.vector.Vector3d;

import java.util.function.Predicate;

@Mixin(net.minecraft.world.level.Level.class)
public abstract class LevelMixin implements LevelBridge, LevelAccessor {

    // @formatter: off
    @Mutable @Shadow @Final private DimensionType dimensionType;
    @Shadow protected float oRainLevel;
    @Shadow protected float rainLevel;
    @Shadow protected float oThunderLevel;
    @Shadow protected float thunderLevel;

    @Shadow public abstract LevelData shadow$getLevelData();
    @Shadow public abstract void shadow$updateSkyBrightness();
    @Shadow public abstract net.minecraft.resources.ResourceKey<Level> shadow$dimension();
    @Shadow public abstract DimensionType shadow$dimensionType();
    @Shadow public abstract LevelChunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract DifficultyInstance shadow$getCurrentDifficultyAt(BlockPos p_175649_1_);
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract WorldBorder shadow$getWorldBorder();
    // @formatter on

    @Override
    public boolean bridge$isFake() {
        return this.isClientSide();
    }

    @Override
    public void bridge$adjustDimensionLogic(final DimensionType dimensionType) {
        this.dimensionType = dimensionType;

        // TODO Minecraft 1.16.4 - Re-create the WorldBorder due to new coordinate scale, send that updated packet to players
    }

    @SuppressWarnings("unchecked")
    public <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(
            final DataContainer dataContainer,
            final @Nullable Vector3d position,
            final @Nullable Predicate<Vector3d> positionCheck) throws IllegalArgumentException, IllegalStateException {

        final EntityType<@NonNull ?> type = dataContainer.getRegistryValue(Constants.Entity.TYPE, RegistryTypes.ENTITY_TYPE)
                .orElseThrow(() -> new IllegalArgumentException("DataContainer does not contain a valid entity type."));
        final Vector3d proposedPosition;
        if (position == null) {
            proposedPosition = DataUtil.getPosition3d(dataContainer, Constants.Sponge.SNAPSHOT_WORLD_POSITION);
        } else {
            proposedPosition = position;
        }

        if (positionCheck != null && !positionCheck.test(proposedPosition)) {
            throw new IllegalArgumentException(String.format("Position (%.2f, %.2f, %.2f) is not a valid position in this context.",
                    proposedPosition.x(),
                    proposedPosition.y(),
                    proposedPosition.z()));
        }

        final @Nullable Vector3d rotation;
        if (dataContainer.contains(Constants.Entity.ROTATION)) {
            rotation = DataUtil.getPosition3d(dataContainer, Constants.Entity.ROTATION);
        } else {
            rotation = null;
        }

        final @Nullable Vector3d scale;
        if (dataContainer.contains(Constants.Entity.SCALE)) {
            scale = DataUtil.getPosition3d(dataContainer, Constants.Entity.SCALE);
        } else {
            scale = null;
        }

        final Entity createdEntity = this.bridge$createEntity(type, position, false);
        dataContainer.getView(Constants.Sponge.UNSAFE_NBT)
                .map(NBTTranslator.INSTANCE::translate)
                .ifPresent(x -> {
                    final net.minecraft.world.entity.Entity e = ((net.minecraft.world.entity.Entity) createdEntity);
                    // mimicing Entity#restoreFrom
                    x.remove("Dimension");
                    e.load(x);
                    // position needs a reset
                    e.moveTo(proposedPosition.x(), proposedPosition.y(), proposedPosition.z());
                });
        if (rotation != null) {
            createdEntity.setRotation(rotation);
        }
        if (scale != null) {
            createdEntity.setScale(scale);
        }

        return (E) createdEntity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(final EntityType<E> type, final Vector3d position, final boolean naturally) throws IllegalArgumentException, IllegalStateException {
        if (type == net.minecraft.world.entity.EntityType.PLAYER) {
            // Unable to construct these
            throw new IllegalArgumentException("A Player cannot be created by the API!");
        }

        net.minecraft.world.entity.Entity entity = null;
        final double x = position.x();
        final double y = position.y();
        final double z = position.z();
        final net.minecraft.world.level.Level thisWorld = (net.minecraft.world.level.Level) (Object) this;
        // Not all entities have a single World parameter as their constructor
        if (type == net.minecraft.world.entity.EntityType.LIGHTNING_BOLT) {
            entity = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(thisWorld);
            entity.moveTo(x, y, z);
            ((LightningBolt) entity).setVisualOnly(false);
        }
        // TODO - archetypes should solve the problem of calling the correct constructor
        if (type == net.minecraft.world.entity.EntityType.ENDER_PEARL) {
            final ArmorStand tempEntity = new ArmorStand(thisWorld, x, y, z);
            tempEntity.setPos(tempEntity.getX(), tempEntity.getY() - tempEntity.getEyeHeight(), tempEntity.getZ());
            entity = new ThrownEnderpearl(thisWorld, tempEntity);
            ((EnderPearl) entity).offer(Keys.SHOOTER, UnknownProjectileSource.UNKNOWN);
        }
        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (type == net.minecraft.world.entity.EntityType.FALLING_BLOCK) {
            entity = new FallingBlockEntity(thisWorld, x, y, z, Blocks.SAND.defaultBlockState());
        }
        if (type == net.minecraft.world.entity.EntityType.ITEM) {
            entity = new ItemEntity(thisWorld, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            final ResourceKey key = (ResourceKey) (Object) Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) type);
            try {
                entity = ((net.minecraft.world.entity.EntityType) type).create(thisWorld);
                entity.moveTo(x, y, z);
            } catch (final Exception e) {
                throw new RuntimeException("There was an issue attempting to construct " + key, e);
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

        if (naturally && entity instanceof Mob) {
            // Adding the default equipment
            final DifficultyInstance difficulty = this.shadow$getCurrentDifficultyAt(new BlockPos(x, y, z));
            ((MobAccessor)entity).invoker$populateDefaultEquipmentSlots(difficulty);
        }

        if (entity instanceof Painting) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((Painting) entity).motive = Motive.KEBAB;
        }

        return (E) entity;
    }
}
