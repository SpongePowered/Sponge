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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.math.vector.Vector3d;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin implements WorldBridge, IWorld {

    // @formatter: off
    @Mutable @Shadow @Final private DimensionType dimensionType;

    @Shadow public abstract void shadow$updateSkyBrightness();
    @Shadow public abstract RegistryKey<World> shadow$dimension();
    @Shadow public abstract DimensionType shadow$dimensionType();
    @Shadow public abstract Chunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract DifficultyInstance shadow$getCurrentDifficultyAt(BlockPos p_175649_1_);
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

    @Override
    @SuppressWarnings("unchecked")
    public <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(final EntityType<E> type, final Vector3d position, final boolean naturally) throws IllegalArgumentException, IllegalStateException {
        if (type == net.minecraft.entity.EntityType.PLAYER) {
            // Unable to construct these
            throw new IllegalArgumentException("A Player cannot be created by the API!");
        }

        net.minecraft.entity.Entity entity = null;
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        final net.minecraft.world.World thisWorld = (net.minecraft.world.World) (Object) this;
        // Not all entities have a single World parameter as their constructor
        if (type == net.minecraft.entity.EntityType.LIGHTNING_BOLT) {
            entity = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(thisWorld);
            entity.moveTo(x, y, z);
            ((LightningBoltEntity) entity).setVisualOnly(false);
        }
        // TODO - archetypes should solve the problem of calling the correct constructor
        if (type == net.minecraft.entity.EntityType.ENDER_PEARL) {
            final ArmorStandEntity tempEntity = new ArmorStandEntity(thisWorld, x, y, z);
            tempEntity.setPos(tempEntity.getX(), tempEntity.getY() - tempEntity.getEyeHeight(), tempEntity.getZ());
            entity = new EnderPearlEntity(thisWorld, tempEntity);
            ((EnderPearl) entity).offer(Keys.SHOOTER, UnknownProjectileSource.UNKNOWN);
        }
        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (type == net.minecraft.entity.EntityType.FALLING_BLOCK) {
            entity = new FallingBlockEntity(thisWorld, x, y, z, Blocks.SAND.defaultBlockState());
        }
        if (type == net.minecraft.entity.EntityType.ITEM) {
            entity = new ItemEntity(thisWorld, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            final ResourceKey key = (ResourceKey) (Object) Registry.ENTITY_TYPE.getKey((net.minecraft.entity.EntityType<?>) type);
            try {
                entity = ((net.minecraft.entity.EntityType) type).create(thisWorld);
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

        if (naturally && entity instanceof MobEntity) {
            // Adding the default equipment
            final DifficultyInstance difficulty = this.shadow$getCurrentDifficultyAt(new BlockPos(x, y, z));
            ((MobEntityAccessor)entity).invoker$populateDefaultEquipmentSlots(difficulty);
        }

        if (entity instanceof PaintingEntity) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((PaintingEntity) entity).motive = PaintingType.KEBAB;
        }

        return (E) entity;
    }
}
