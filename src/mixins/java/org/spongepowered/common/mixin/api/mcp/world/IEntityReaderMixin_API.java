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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IEntityReader;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.volume.entity.ReadableEntityVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@Mixin(IEntityReader.class)
public interface IEntityReaderMixin_API extends ReadableEntityVolume {

    //@formatter:off
    @Shadow List<net.minecraft.entity.Entity> shadow$getEntitiesInAABBexcluding(@Nullable net.minecraft.entity.Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super net.minecraft.entity.Entity> p_175674_3_);
    @Shadow<T extends net.minecraft.entity.Entity> List<T> shadow$getEntitiesWithinAABB(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_);
    @Shadow List<? extends PlayerEntity> shadow$getPlayers();
    @Nullable @Shadow PlayerEntity shadow$getClosestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_, @Nullable Predicate<net.minecraft.entity.Entity> p_190525_9_);

    //@formatter:on

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
    default boolean containsBlock(final int x, final int y, final int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default boolean isAreaAvailable(final int x, final int y, final int z) {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Optional<Entity> getEntity(final UUID uuid) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<? extends Player> getPlayers() {
        return Collections.unmodifiableCollection((List<? extends Player>) (List<?>) this.shadow$getPlayers());
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    @Override
    default Collection<? extends Entity> getEntities(final AABB box, final Predicate<? super Entity> filter) {
        return (Collection) this
                .shadow$getEntitiesInAABBexcluding(null, VecHelper.toMinecraftAABB(box), entity -> entity instanceof Entity && filter.test((Entity) entity));
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    @Override
    default <E extends Entity> Collection<? extends E> getEntities(final Class<? extends E> entityClass, final AABB box, @Nullable
    final Predicate<? super E> predicate) {
        final Predicate<? super net.minecraft.entity.Entity> filter = entity -> predicate == null || (entityClass.isInstance(entity) && predicate.test((E) entity));
        final List<net.minecraft.entity.Entity> ts = this.shadow$getEntitiesWithinAABB((Class<net.minecraft.entity.Entity>) (Class<?>) entityClass,
                VecHelper.toMinecraftAABB(box), filter);
        return (Collection) ts;
    }
}
