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
package org.spongepowered.common.tag;

import net.minecraft.core.MappedRegistry;
import net.minecraft.tags.TagKey;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.DefaultedTag;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.bridge.tags.TagBridge;

import java.util.Objects;
import java.util.function.Supplier;

public final class SpongeDefaultedTag<T> implements DefaultedTag<T>, TagBridge<T> {

    private final Supplier<RegistryHolder> defaultHolder;
    private final TagKey<T> vanillaTag;
    private final Tag<T> spongeTag;

    public SpongeDefaultedTag(final Tag<T> tag, final Supplier<RegistryHolder> defaultHolder) {
        this.defaultHolder = Objects.requireNonNull(defaultHolder, "defaultHolder");
        this.vanillaTag = ((TagBridge<T>) tag).bridge$asVanillaTag();
        this.spongeTag = (Tag<T>) (Object) this.vanillaTag;
    }

    @Override
    public Supplier<RegistryHolder> defaultHolder() {
        return this.defaultHolder;
    }

    @Override
    public RegistryType<T> registry() {
        return this.spongeTag.registry();
    }

    @Override
    public DefaultedTag<T> asDefaultedTag(final Supplier<RegistryHolder> holder) {
        return this.spongeTag.asDefaultedTag(holder);
    }

    @Override
    public DefaultedTag<T> asScopedTag() {
        return this.spongeTag.asScopedTag();
    }

    @Override
    public ResourceKey key() {
        return this.spongeTag.key();
    }

    @Override
    public TagKey<T> bridge$asVanillaTag() {
        return this.vanillaTag;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(final T value) {
        return ((MappedRegistry<T>) this.defaultHolder.get().registry(this.registry())).wrapAsHolder(value).is(this.vanillaTag);
    }
}
