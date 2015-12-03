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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHumanoidTextureData;
import org.spongepowered.api.data.manipulator.mutable.entity.HumanoidTextureData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHumanoidTextureData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class ImmutableSpongeHumanoidTextureData extends AbstractImmutableData<ImmutableHumanoidTextureData, HumanoidTextureData> implements ImmutableHumanoidTextureData {

    private static final ImmutableHumanoidTextureData EMPTY = new ImmutableSpongeHumanoidTextureData(null, null);
    private static final ImmutableOptionalValue<ProfileProperty> EMPTY_PROPERTY = new ImmutableSpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_PROPERTY, Optional.empty());
    private static final ImmutableOptionalValue<UUID> EMPTY_UNIQUE_ID = new ImmutableSpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_UNIQUE_ID, Optional.empty());

    @Nullable private final ProfileProperty property;
    private final ImmutableOptionalValue<ProfileProperty> propertyValue;
    @Nullable private final UUID uniqueId;
    private final ImmutableOptionalValue<UUID> uniqueIdValue;

    public ImmutableSpongeHumanoidTextureData(@Nullable ProfileProperty property, @Nullable UUID uniqueId) {
        super(ImmutableHumanoidTextureData.class);
        this.property = property;
        this.propertyValue = this.property == null ? EMPTY_PROPERTY : new ImmutableSpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_PROPERTY, Optional.of(this.property));
        this.uniqueId = uniqueId;
        this.uniqueIdValue = this.uniqueId == null ? EMPTY_UNIQUE_ID : new ImmutableSpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_UNIQUE_ID, Optional.of(this.uniqueId));
        this.registerGetters();
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.HUMANOID_TEXTURES_PROPERTY, this::property);
        this.registerFieldGetter(Keys.HUMANOID_TEXTURES_PROPERTY, this::getProperty);

        this.registerKeyValue(Keys.HUMANOID_TEXTURES_UNIQUE_ID, this::uniqueId);
        this.registerFieldGetter(Keys.HUMANOID_TEXTURES_UNIQUE_ID, this::getUniqueId);
    }

    @Override
    public ImmutableOptionalValue<ProfileProperty> property() {
        return this.propertyValue;
    }

    public Optional<ProfileProperty> getProperty() {
        return Optional.ofNullable(this.property);
    }

    @Override
    public ImmutableOptionalValue<UUID> uniqueId() {
        return this.uniqueIdValue;
    }

    public Optional<UUID> getUniqueId() {
        return Optional.ofNullable(this.uniqueId);
    }

    @Override
    public HumanoidTextureData asMutable() {
        return new SpongeHumanoidTextureData(this.property, this.uniqueId);
    }

    public static ImmutableHumanoidTextureData of(@Nullable ProfileProperty property, @Nullable UUID uniqueId) {
        if (property == null && uniqueId == null) {
            return EMPTY;
        } else {
            return new ImmutableSpongeHumanoidTextureData(property, uniqueId);
        }
    }

}
