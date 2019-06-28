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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

import javax.annotation.Nullable;

@Mixin(value = PropertyMap.class, remap = false)
public abstract class PropertyMapMixin implements Multimap<String, ProfileProperty> {

    @Shadow protected abstract Multimap<String, ProfileProperty> delegate();

    @Override
    public boolean containsValue(@Nullable Object value) {
        this.sanityCheck(value);
        return this.delegate().containsValue(value);
    }

    @Override
    public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
        this.sanityCheck(value);
        return this.delegate().containsEntry(key, value);
    }

    @Override
    public boolean put(@Nullable String key, @Nullable ProfileProperty value) {
        this.sanityCheck(value);
        return this.delegate().put(key, value);
    }

    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        this.sanityCheck(value);
        return this.delegate().remove(key, value);
    }

    @Override
    public boolean putAll(@Nullable String key, Iterable<? extends ProfileProperty> values) {
        for (ProfileProperty property : values) {
            this.sanityCheck(property);
        }

        return this.delegate().putAll(key, values);
    }

    @Override
    public boolean putAll(Multimap<? extends String, ? extends ProfileProperty> multimap) {
        for (ProfileProperty property : multimap.values()) {
            this.sanityCheck(property);
        }

        return this.delegate().putAll(multimap);
    }

    @Override
    public Collection<ProfileProperty> replaceValues(@Nullable String key, Iterable<? extends ProfileProperty> values) {
        for (ProfileProperty property : values) {
            this.sanityCheck(property);
        }

        return this.delegate().replaceValues(key, values);
    }

    private void sanityCheck(@Nullable Object object) {
        if (object != null) {
            checkState(object instanceof Property, "Property must be of type " + ProfileProperty.class.getName());
        }
    }
}
