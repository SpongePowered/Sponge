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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHumanoidTextureData;
import org.spongepowered.api.data.manipulator.mutable.entity.HumanoidTextureData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeHumanoidTextureData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public final class SpongeHumanoidTextureData extends AbstractData<HumanoidTextureData, ImmutableHumanoidTextureData> implements HumanoidTextureData {

    @Nullable private ProfileProperty property;
    @Nullable private UUID uniqueId;

    public SpongeHumanoidTextureData() {
        this(null, null);
    }

    public SpongeHumanoidTextureData(@Nullable ProfileProperty property, @Nullable UUID uniqueId) {
        super(HumanoidTextureData.class);
        this.property = property;
        this.uniqueId = uniqueId;
        this.registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        this.registerKeyValue(Keys.HUMANOID_TEXTURES_PROPERTY, this::property);
        this.registerFieldGetter(Keys.HUMANOID_TEXTURES_PROPERTY, this::getProperty);
        this.registerFieldSetter(Keys.HUMANOID_TEXTURES_PROPERTY, this::setProperty);

        this.registerKeyValue(Keys.HUMANOID_TEXTURES_UNIQUE_ID, this::uniqueId);
        this.registerFieldGetter(Keys.HUMANOID_TEXTURES_UNIQUE_ID, this::getUniqueId);
        this.registerFieldSetter(Keys.HUMANOID_TEXTURES_UNIQUE_ID, this::setUniqueId);
    }

    @Override
    public OptionalValue<ProfileProperty> property() {
        return new SpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_PROPERTY, Optional.ofNullable(this.property));
    }

    private Optional<ProfileProperty> getProperty() {
        return Optional.ofNullable(this.property);
    }

    private void setProperty(Optional<ProfileProperty> property) {
        this.property = property.orElse(null);
    }

    @Override
    public OptionalValue<UUID> uniqueId() {
        return new SpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_UNIQUE_ID, Optional.ofNullable(this.uniqueId));
    }

    private Optional<UUID> getUniqueId() {
        return Optional.ofNullable(this.uniqueId);
    }

    private void setUniqueId(Optional<UUID> uniqueId) {
        this.uniqueId = uniqueId.orElse(null);
    }

    @Override
    public HumanoidTextureData copy() {
        return new SpongeHumanoidTextureData(this.property, this.uniqueId);
    }

    @Override
    public ImmutableHumanoidTextureData asImmutable() {
        return ImmutableSpongeHumanoidTextureData.of(this.property, this.uniqueId);
    }

}
