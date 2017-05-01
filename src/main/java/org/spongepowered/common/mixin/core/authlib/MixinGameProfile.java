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
package org.spongepowered.common.mixin.core.authlib;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.util.DataQueries;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(value = GameProfile.class, remap = false)
@Implements(value = @Interface(iface = org.spongepowered.api.profile.GameProfile.class, prefix = "profile$"))
public abstract class MixinGameProfile {

    @Shadow public abstract UUID getId();
    @Nullable @Shadow public abstract String getName();
    @Shadow public abstract PropertyMap getProperties();
    @Shadow public abstract boolean isComplete();

    public UUID profile$getUniqueId() {
        return this.getId();
    }

    public Optional<String> profile$getName() {
        return Optional.ofNullable(this.getName());
    }

    @SuppressWarnings("unchecked")
    public Multimap<String, ProfileProperty> profile$getPropertyMap() {
        return (Multimap<String, ProfileProperty>) (Object) this.getProperties();
    }

    public boolean profile$isFilled() {
        return this.isComplete();
    }

    public DataContainer profile$toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(DataQueries.USER_UUID, this.profile$getUniqueId().toString());
        if (this.profile$getName() != null) {
            container.set(DataQueries.USER_NAME, this.profile$getName());
        }

        return container;
    }

}
