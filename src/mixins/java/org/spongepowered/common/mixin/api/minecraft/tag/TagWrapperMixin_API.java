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
package org.spongepowered.common.mixin.api.minecraft.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;

@Mixin(targets = "net.minecraft.tags.StaticTagHelper$Wrapper")
@Implements(@Interface(iface = org.spongepowered.api.tag.Tag.class, prefix = "spongetag$", remap = Remap.NONE))
public abstract class TagWrapperMixin_API<T> implements Tag.Named<T>, org.spongepowered.api.tag.Tag<T> {

    // @formatter:on
    @Shadow @Final protected ResourceLocation name;
    @Shadow public abstract boolean shadow$contains(T param0);
    // @formatter:off

    @Override
    public Collection<T> values() {
        return this.getValues();
    }

    @Override
    public ResourceKey key() {
        return (ResourceKey) (Object) this.name;
    }

    @Intrinsic
    public boolean spongetag$contains(final T value) {
        return this.shadow$contains(value);
    }

}
