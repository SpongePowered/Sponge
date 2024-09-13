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
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.MappedRegistryBridge;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryEntry;
import org.spongepowered.common.registry.SpongeRegistryType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RegistryBridge<T>, WritableRegistryBridge<T>, MappedRegistryBridge<T> {

    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    private RegistryType<T> impl$type;
    private final Map<ResourceKey, RegistryEntry<T>> impl$entries = new LinkedHashMap<>();

    private boolean impl$isDynamic = true;

    @Override
    public boolean bridge$isDynamic() {
        return this.impl$isDynamic;
    }

    @Override
    public void bridge$setDynamic(final boolean isDynamic) {
        this.impl$isDynamic = isDynamic;
    }


    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
    private void impl$setType(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key,
            final Lifecycle lifecycle, boolean $$2, final CallbackInfo ci) {
        this.impl$type = new SpongeRegistryType<T>((ResourceKey) (Object) ((ResourceKeyAccessor) key).accessor$registryName(),
                (ResourceKey) (Object) key.location());
    }


    @Inject(method = "register", at = @At("TAIL"))
    private void impl$cacheRegistryEntry(final net.minecraft.resources.ResourceKey<T> $$0, final T $$1,
            final RegistrationInfo $$3, final CallbackInfoReturnable<Holder<T>> cir) {

        final net.minecraft.resources.ResourceKey<? extends Registry<T>> resourceKey = ((MappedRegistry<T>) (Object) this).key();
        final ResourceKey root = (ResourceKey) (Object) ((ResourceKeyAccessor<T>) resourceKey).accessor$registryName();
        final ResourceKey location = (ResourceKey) (Object) resourceKey.location();
        this.bridge$register(new SpongeRegistryEntry<>(new SpongeRegistryType<>(root, location),
                (ResourceKey) (Object) $$0.location(), $$1));
    }

    @Override
    public RegistryType<T> bridge$type() {
        return this.impl$type;
    }

    @Override
    public void bridge$register(final RegistryEntry<T> entry) {
        this.impl$entries.put(entry.key(), entry);
    }

    @Override
    public Optional<RegistryEntry<T>> bridge$get(final ResourceKey resourceKey) {
        return Optional.ofNullable(this.impl$entries.get(resourceKey));
    }

    @Override
    public Stream<RegistryEntry<T>> bridge$streamEntries() {
        return this.impl$entries.values().stream();
    }

    @Override
    public void bridge$forceRemoveValue(net.minecraft.resources.ResourceKey<Registry<T>> key) {
        this.byLocation.remove(key);
        this.byValue.remove(key);
    }
}
