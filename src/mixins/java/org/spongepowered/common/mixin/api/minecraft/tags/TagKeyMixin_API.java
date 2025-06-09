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
package org.spongepowered.common.mixin.api.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.DefaultedTag;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.tag.SpongeDefaultedTag;

import java.util.function.Supplier;

@Mixin(TagKey.class)
public abstract class TagKeyMixin_API<T> implements Tag<T> {

    // @formatter:off
    @Shadow @Final private net.minecraft.resources.ResourceKey<? extends Registry<T>> registry;
    @Shadow @Final private ResourceLocation location;
    // @formatter:on

    @Override
    public RegistryType<T> registry() {
        return RegistryType.of((ResourceKey) (Object) this.registry.registry(), (ResourceKey) (Object) this.registry.location());
    }

    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.location;
    }

    @Override
    public DefaultedTag<T> asDefaultedTag(final Supplier<RegistryHolder> holder) {
        return new SpongeDefaultedTag<>(this, holder);
    }

    @Override
    public DefaultedTag<T> asScopedTag() {
        return new SpongeDefaultedTag<>(this, SpongeCommon::scopedHolder);
    }
}
