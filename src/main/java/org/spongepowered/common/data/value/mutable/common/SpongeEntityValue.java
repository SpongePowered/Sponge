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
package org.spongepowered.common.data.value.mutable.common;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * This class provides a safe reference for an {@link Entity} such that
 * references aren't maintained and therefor leaked. Provided that
 */
public class SpongeEntityValue implements Value<Entity> {

    private final Key<? extends BaseValue<Entity>> key;
    private UUID entityid;
    private WeakReference<Entity> weakReference = new WeakReference<Entity>(null);

    public SpongeEntityValue(Key<? extends BaseValue<Entity>> key, Entity actualValue) {
        this.key = checkNotNull(key);
        this.entityid = checkNotNull(actualValue).getUniqueId();
        this.weakReference = new WeakReference<Entity>(actualValue);

    }

    @Override
    public Entity get() {
        @Nullable Entity entity = this.weakReference.get();
        if (entity == null) {
            for (World world : Sponge.getGame().getServer().getWorlds()) {
                final Optional<Entity> optional = world.getEntity(this.entityid);
                if (optional.isPresent()) {
                    return optional.get();
                }
            }
        }
        throw new IllegalStateException("The entity has expired or has been permanently removed! The entity's id was: " + this.entityid.toString());
    }

    @Override
    public boolean exists() {
        return this.weakReference.get() != null;
    }

    @Override
    public Entity getDefault() {
        checkState(!exists(), "The entity reference expired!");
        return this.weakReference.get();
    }

    @Override
    public Optional<Entity> getDirect() {
        return Optional.ofNullable(this.weakReference.get());
    }

    @Override
    public Key<? extends BaseValue<Entity>> getKey() {
        return this.key;
    }

    @Override
    public Value<Entity> set(Entity value) {
        this.entityid = checkNotNull(value).getUniqueId();
        this.weakReference = new WeakReference<Entity>(value);
        return this;
    }

    @Override
    public Value<Entity> transform(Function<Entity, Entity> function) {
        final Entity entity = checkNotNull(checkNotNull(function).apply(this.weakReference.get()));
        this.weakReference = new WeakReference<Entity>(entity);
        this.entityid = checkNotNull(entity).getUniqueId();
        return this;
    }

    @Override
    public ImmutableValue<Entity> asImmutable() {
        return new ImmutableSpongeValue<Entity>(getKey(), this.weakReference.get());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("key", this.key)
            .add("entityid", this.entityid)
            .add("weakReference", this.weakReference)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key, this.entityid, this.weakReference);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeEntityValue other = (SpongeEntityValue) obj;
        return Objects.equal(this.key, other.key)
               && Objects.equal(this.entityid, other.entityid)
               && Objects.equal(this.weakReference, other.weakReference);
    }
}
