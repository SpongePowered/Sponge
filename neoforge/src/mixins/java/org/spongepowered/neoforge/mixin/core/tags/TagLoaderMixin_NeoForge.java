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
package org.spongepowered.neoforge.mixin.core.tags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.tags.TagLoaderBridge;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin_NeoForge<T> implements TagLoaderBridge<T> {

    @WrapOperation(method = "tryBuildTag(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z"))
    protected boolean neo$onBuildTag(final TagEntry instance, final TagEntry.Lookup<T> lookup, final Consumer<T> consumer, final Operation<Boolean> original,
            final TagEntry.Lookup<T> $$0, final List<TagLoader.EntryWithSource> $$1, final @Local TagLoader.EntryWithSource entry) {
        if (entry.remove()) {
            return original.call(instance, lookup, consumer);
        }
        return this.bridge$acceptTag(instance, lookup, consumer, original::call, $$1, entry);
    }

    @Inject(method = "lambda$build$6", at = @At("HEAD"))
    private void neo$onStartBuildingTag(final TagEntry.Lookup<T> $$0x, final Map<ResourceLocation, Collection<T>> $$1x, final ResourceLocation $$2x,
            final @Coerce Object $$3x, final CallbackInfo ci) {
        this.bridge$buildingTagKey($$2x);
    }

    @Inject(method = "lambda$build$6", at = @At("RETURN"))
    private void neo$onDoneBuildingTag(final CallbackInfo ci) {
        this.bridge$buildingTagKey(null);
    }

    @Override
    public boolean bridge$isAdd(final TagLoader.EntryWithSource entry) {
        return !entry.remove();
    }
}
