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
package org.spongepowered.common.mixin.api.minecraft.world.level;

import net.minecraft.world.level.EntityGetter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.volume.entity.EntityVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Mixin(EntityGetter.class)
public interface EntityGetterMixin_API extends EntityVolume {

    //@formatter:off
    @Shadow List<net.minecraft.world.entity.Entity> shadow$getEntities(@Nullable net.minecraft.world.entity.Entity p_175674_1_, net.minecraft.world.phys.AABB p_175674_2_, @Nullable Predicate<? super net.minecraft.world.entity.Entity> p_175674_3_);
    @Shadow<T extends net.minecraft.world.entity.Entity> List<T> shadow$getEntitiesOfClass(Class<? extends T> p_175647_1_, net.minecraft.world.phys.AABB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_);
    @Shadow List<? extends net.minecraft.world.entity.player.Player> shadow$players();
    //@formatter:on

    @Override
    default Vector3i blockMin() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i blockMax() {
        throw new UnsupportedOperationException("Unfortunately, you've found an extended class of IEntityReader that isn't part of Sponge API");
    }

    @Override
    default Vector3i blockSize() {
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
    default Optional<Entity> entity(final UUID uuid) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<? extends Player> players() {
        return Collections.unmodifiableCollection((List<? extends Player>) (List<?>) this.shadow$players());
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    @Override
    default Collection<? extends Entity> entities(final AABB box, final Predicate<? super Entity> filter) {
        return (Collection) this.shadow$getEntities(null, VecHelper.toMinecraftAABB(box), VolumeStreamUtils.apiToImplPredicate(filter));
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    @Override
    default <E extends Entity> Collection<? extends E> entities(final Class<? extends E> entityClass, final AABB box, @Nullable
    final Predicate<? super E> predicate) {
        final Predicate<? super net.minecraft.world.entity.Entity> filter = entity -> predicate == null || (entityClass.isInstance(entity) && predicate.test((E) entity));
        final List<net.minecraft.world.entity.Entity> ts = this.shadow$getEntitiesOfClass((Class<net.minecraft.world.entity.Entity>) (Class<?>) entityClass,
                VecHelper.toMinecraftAABB(box), filter);
        return (Collection) ts;
    }
}
