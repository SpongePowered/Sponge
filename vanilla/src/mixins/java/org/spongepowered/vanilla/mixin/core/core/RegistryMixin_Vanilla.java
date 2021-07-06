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
package org.spongepowered.vanilla.mixin.core.core;

import net.minecraft.core.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.core.RegistryBridge;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(Registry.class)
public abstract class RegistryMixin_Vanilla<T> implements RegistryBridge<T> {

    // @formatter:off
    @Shadow public abstract net.minecraft.resources.ResourceKey<? extends Registry<T>> shadow$key();
    // @formatter:on

    private final Map<ResourceKey, RegistryEntry<T>> vanilla$entries = new LinkedHashMap<>();
    private Supplier<Stream<RegistryEntry<T>>> vanilla$streamOverride = null;

    @Override
    public void bridge$overrideStream(final Supplier<Stream<RegistryEntry<T>>> override) {
        this.vanilla$streamOverride = override;
    }

    @Override
    public void bridge$register(final RegistryEntry<T> entry) {
        this.vanilla$entries.put(entry.key(), entry);
    }

    @Override
    public Optional<RegistryEntry<T>> bridge$get(final ResourceKey resourceKey) {
        if (this.vanilla$streamOverride != null) {
            return this.vanilla$streamOverride.get()
                    .filter(x -> x.key().equals(resourceKey))
                    .findFirst();
        }
        return Optional.ofNullable(this.vanilla$entries.get(resourceKey));
    }

    @Override
    public Stream<RegistryEntry<T>> bridge$streamEntries() {
        if (this.vanilla$streamOverride != null) {
            return this.vanilla$streamOverride.get();
        }
        return this.vanilla$entries.values().stream();
    }

    @Inject(method = "stream", at = @At("HEAD"), cancellable = true)
    private void vanilla$useStreamOverride(final CallbackInfoReturnable<Stream<T>> cir) {
        if (this.vanilla$streamOverride != null) {
            cir.setReturnValue(this.vanilla$streamOverride.get().map(RegistryEntry::value));
        }
    }

}
