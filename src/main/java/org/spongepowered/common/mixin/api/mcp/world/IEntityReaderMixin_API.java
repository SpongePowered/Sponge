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

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IEntityReader;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.ReadableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Mixin(IEntityReader.class)
public interface IEntityReaderMixin_API extends ReadableEntityVolume {
    @Shadow List<net.minecraft.entity.Entity> shadow$getEntitiesInAABBexcluding(@Nullable net.minecraft.entity.Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super net.minecraft.entity.Entity> p_175674_3_);
    @Shadow<T extends net.minecraft.entity.Entity> List<T> shadow$getEntitiesWithinAABB(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_);
    @Shadow <T extends net.minecraft.entity.Entity> List<T> shadow$func_225316_b(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_);
    @Shadow List<? extends PlayerEntity> shadow$getPlayers();
    @Shadow List<net.minecraft.entity.Entity> shadow$getEntitiesWithinAABBExcludingEntity(@Nullable net.minecraft.entity.Entity p_72839_1_, AxisAlignedBB p_72839_2_);
    @Shadow boolean shadow$checkNoEntityCollision(@Nullable net.minecraft.entity.Entity p_195585_1_, VoxelShape p_195585_2_);
    @Shadow <T extends net.minecraft.entity.Entity> List<T> shadow$getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_);
    @Shadow <T extends net.minecraft.entity.Entity> List<T> shadow$func_225317_b(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_);
    @Shadow Stream<VoxelShape> shadow$getEmptyCollisionShapes(@Nullable net.minecraft.entity.Entity p_223439_1_, AxisAlignedBB p_223439_2_, Set<net.minecraft.entity.Entity> p_223439_3_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_, @Nullable Predicate<net.minecraft.entity.Entity> p_190525_9_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(net.minecraft.entity.Entity p_217362_1_, double p_217362_2_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(double p_217366_1_, double p_217366_3_, double p_217366_5_, double p_217366_7_, boolean p_217366_9_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(double p_217365_1_, double p_217365_3_, double p_217365_5_);
    @Shadow boolean shadow$isPlayerWithin(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(EntityPredicate p_217370_1_, LivingEntity p_217370_2_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(EntityPredicate p_217372_1_, LivingEntity p_217372_2_, double p_217372_3_, double p_217372_5_, double p_217372_7_);
    @Nullable
    @Shadow PlayerEntity shadow$getClosestPlayer(EntityPredicate p_217359_1_, double p_217359_2_, double p_217359_4_, double p_217359_6_);
    @Nullable
    @Shadow <T extends LivingEntity> T shadow$getClosestEntityWithinAABB(Class<? extends T> p_217360_1_, EntityPredicate p_217360_2_, @Nullable LivingEntity p_217360_3_, double p_217360_4_, double p_217360_6_, double p_217360_8_, AxisAlignedBB p_217360_10_);
    @Nullable
    @Shadow <T extends LivingEntity> T shadow$func_225318_b(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_);
    @Nullable
    @Shadow <T extends LivingEntity> T shadow$getClosestEntity(List<? extends T> p_217361_1_, EntityPredicate p_217361_2_, @Nullable LivingEntity p_217361_3_, double p_217361_4_, double p_217361_6_, double p_217361_8_);
    @Shadow List<PlayerEntity> shadow$getTargettablePlayersWithinAABB(EntityPredicate p_217373_1_, LivingEntity p_217373_2_, AxisAlignedBB p_217373_3_) ;
    @Shadow <T extends LivingEntity> List<T> shadow$getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_);
    @Nullable
    @Shadow PlayerEntity shadow$getPlayerByUuid(UUID p_217371_1_);

    @Override
    default Vector3i getBlockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i getBlockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i getBlockSize() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default boolean containsBlock(int x, int y, int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default boolean isAreaAvailable(int x, int y, int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default ReadableEntityVolume getView(Vector3i newMin, Vector3i newMax) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default UnmodifiableEntityVolume<?> asUnmodifiableEntityVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default ImmutableEntityVolume asImmutableEntityVolume() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Optional<Entity> getEntity(UUID uuid) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<? extends Player> getPlayers() {
        return Collections.unmodifiableCollection((List<? extends Player>) (List<?>) this.shadow$getPlayers());
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<? extends Entity> getEntities(AABB box, Predicate<? super Entity> filter) {
        return (Collection<? extends Entity>) this
                .shadow$getEntitiesInAABBexcluding(null, VecHelper.toMinecraftAABB(box), entity -> entity instanceof Entity && filter.test((Entity) entity));
    }

    @SuppressWarnings("unchecked")
    @Override
    default <E extends Entity> Collection<? extends E> getEntities(Class<? extends E> entityClass, AABB box, @Nullable Predicate<? super E> predicate) {
        final Predicate<? super net.minecraft.entity.Entity> filter = entity -> predicate == null || (entityClass.isInstance(entity) && predicate.test((E) entity));
        final List<net.minecraft.entity.Entity>
            ts =
                this.shadow$getEntitiesWithinAABB((Class<net.minecraft.entity.Entity>) (Class<?>) entityClass, VecHelper.toMinecraftAABB(box), filter);
        return (Collection<? extends E>) ts;
    }
}
