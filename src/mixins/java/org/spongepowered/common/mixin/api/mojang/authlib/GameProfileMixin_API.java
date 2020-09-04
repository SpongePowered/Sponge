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
package org.spongepowered.common.mixin.api.mojang.authlib;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings({"ConstantConditions", "unchecked", "rawtypes"})
@Mixin(value = GameProfile.class, remap = false)
@Implements(value = @Interface(iface = org.spongepowered.api.profile.GameProfile.class, prefix = "profile$"))
public abstract class GameProfileMixin_API implements org.spongepowered.api.profile.GameProfile {

    @Shadow public abstract UUID shadow$getId();
    @Nullable @Shadow public abstract String shadow$getName();
    @Shadow public abstract PropertyMap shadow$getProperties();
    @Shadow public abstract boolean shadow$isComplete();

    @Override
    public UUID getUniqueId() {
        return Objects.requireNonNull(this.shadow$getId(), "The unique id isn't initialized.");
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(this.shadow$getName());
    }

    @Override
    public List<ProfileProperty> getProperties() {
        return ImmutableList.copyOf((Collection) this.shadow$getProperties().values());
    }

    @Override
    public org.spongepowered.api.profile.GameProfile withoutProperties() {
        return (org.spongepowered.api.profile.GameProfile) new GameProfile(this.shadow$getId(), this.shadow$getName());
    }

    @Override
    public org.spongepowered.api.profile.GameProfile withoutProperties(final Predicate<ProfileProperty> filter) {
        return null;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew();

        container.set(Queries.CONTENT_VERSION, this.getContentVersion());
        container.set(Constants.Entity.Player.UUID, this.getUniqueId().toString());

        this.getName().ifPresent(name -> container.set(Constants.Entity.Player.NAME, name));

        return container;
    }

}
