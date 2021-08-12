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
package org.spongepowered.common.mixin.core.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(Registry.class)
public abstract class RegistryMixin<T> implements RegistryBridge<T> {

    // @formatter:off
    @Shadow public abstract net.minecraft.resources.ResourceKey<? extends Registry<T>> shadow$key();
    // @formatter:on

    private RegistryType<T> impl$type;
    private final Map<ResourceKey, RegistryEntry<T>> impl$entries = new LinkedHashMap<>();
    private Supplier<Stream<RegistryEntry<T>>> impl$streamOverride = null;

    @Override
    public RegistryType<T> bridge$type() {
        return this.impl$type;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setType(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key,
                                 final Lifecycle p_i232510_2_, final CallbackInfo ci) {
        this.impl$type = new SpongeRegistryType<T>((ResourceKey) (Object) ((ResourceKeyAccessor) key).accessor$registryName(),
                (ResourceKey) (Object) key.location());
    }

    @Override
    public void bridge$overrideStream(final Supplier<Stream<RegistryEntry<T>>> override) {
        this.impl$streamOverride = override;
    }

    @Override
    public void bridge$register(final RegistryEntry<T> entry) {
        this.impl$entries.put(entry.key(), entry);
    }

    @Override
    public Optional<RegistryEntry<T>> bridge$get(final ResourceKey resourceKey) {
        if (this.impl$streamOverride != null) {
            return this.impl$streamOverride.get()
                    .filter(x -> x.key().equals(resourceKey))
                    .findFirst();
        }
        return Optional.ofNullable(this.impl$entries.get(resourceKey));
    }

    @Override
    public Stream<RegistryEntry<T>> bridge$streamEntries() {
        if (this.impl$streamOverride != null) {
            return this.impl$streamOverride.get();
        }
        return this.impl$entries.values().stream();
    }

    @Inject(method = "stream", at = @At("HEAD"), cancellable = true)
    private void impl$useStreamOverride(final CallbackInfoReturnable<Stream<T>> cir) {
        if (this.impl$streamOverride != null) {
            cir.setReturnValue(this.impl$streamOverride.get().map(RegistryEntry::value));
        }
    }

}
