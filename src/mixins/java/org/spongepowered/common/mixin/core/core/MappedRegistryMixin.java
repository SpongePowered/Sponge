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
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
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
import org.spongepowered.common.registry.InitialRegistryData;
import org.spongepowered.common.registry.SpongeRegistryEntry;
import org.spongepowered.common.registry.SpongeRegistryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RegistryBridge<T>, WritableRegistryBridge<T>, MappedRegistryBridge<T> {

    // @formatter:off
    @Shadow private boolean frozen;
    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
    @Shadow @Final private Map<net.minecraft.resources.ResourceKey<T>, Holder.Reference<T>> byKey;
    @Shadow @Final private Map<T, Holder.Reference<T>> byValue;
    @Shadow @Final private Map<net.minecraft.resources.ResourceKey<T>, RegistrationInfo> registrationInfos;
    @Shadow @Final private net.minecraft.resources.ResourceKey<? extends Registry<T>> key;
    @Shadow MappedRegistry.TagSet<T> allTags;

    @Shadow public abstract Holder.Reference<T> shadow$register(net.minecraft.resources.ResourceKey<T> arg, T object, RegistrationInfo arg2);
    // @formatter:on

    private RegistryHolder impl$registryHolder;
    private RegistryType<T> impl$type;
    private final Map<ResourceKey, RegistryEntry<T>> impl$entries = new LinkedHashMap<>();

    private final Set<RegistryType<?>> impl$dependencies = new HashSet<>();
    private final List<Runnable> impl$preFreezeTasks = new ArrayList<>();

    private boolean impl$isDynamic = true;
    private boolean impl$eventCalled = false;

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
    public @Nullable RegistryEntry<T> bridge$getEntry(final ResourceKey resourceKey) {
        return this.impl$entries.get(resourceKey);
    }

    @Override
    public Stream<RegistryEntry<T>> bridge$streamEntries() {
        return this.impl$entries.values().stream();
    }

    @Override
    public void bridge$forceRemoveValue(net.minecraft.resources.ResourceKey<T> key) {
        //WARNING: THIS DESYNCS THE IDS!
        final Holder.Reference<T> value = this.byKey.remove(key);
        final int id = this.toId.removeInt(value.value());
        this.byId.remove(id);
        for (int i = id; i < this.byId.size(); i++) {
            this.toId.put(this.byId.get(i).value(), i);
        }
        this.byLocation.remove(key.location());
        this.byValue.remove(value.value());
        this.registrationInfos.remove(key);
        this.impl$entries.remove((ResourceKey) (Object) key.location());
    }

    @Inject(method = "freeze", at = @At(value = "FIELD", target = "Lnet/minecraft/core/MappedRegistry;frozen:Z", opcode = Opcodes.PUTFIELD))
    private void impl$onFreeze(final CallbackInfoReturnable<Registry<T>> cir) {
        this.impl$dependencies.forEach(t -> {
            if (!this.impl$registryHolder.findRegistry(t)
                .map(r -> ((MappedRegistryMixin<?>) r).frozen)
                .orElse(false)) {
                throw new ValueNotFoundException(String.format("Dependency %s was not found!", t));
            }
        });
        this.impl$preFreezeTasks.forEach(Runnable::run);
        this.impl$preFreezeTasks.clear();
    }

    @Override
    public void bridge$setRegistryHolder(final RegistryHolder registryHolder) {
        this.impl$registryHolder = registryHolder;
    }

    @Override
    public void bridge$addDependencies(final Supplier<InitialRegistryData<T>> supplier, final RegistryType<?>... dependencies) {
        if (Arrays.stream(dependencies).allMatch(d -> this.impl$registryHolder.findRegistry(d)
            .map(r -> ((MappedRegistryMixin<?>) r).frozen)
            .orElse(false))) {
            this.impl$appendRegister(supplier);
            return;
        }
        this.bridge$addDependencies(() -> this.impl$appendRegister(supplier), dependencies);
    }

    private void impl$appendRegister(final Supplier<InitialRegistryData<T>> supplier) {
        supplier.get().forEach((vk, vi, vv) ->
            this.shadow$register(
                net.minecraft.resources.ResourceKey.create(this.key, (ResourceLocation) (Object) vk),
                vv,
                RegistrationInfo.BUILT_IN
            ));
    }

    @Override
    public void bridge$addDependencies(final Runnable runnable, final RegistryType<?>... dependencies) {
        this.impl$dependencies.addAll(List.of(dependencies));
        this.impl$preFreezeTasks.add(runnable);
    }

    @Override
    public Stream<RegistryType<?>> bridge$pendingDependencies() {
        return this.impl$dependencies.stream();
    }

    @Override
    public void bridge$markEventCalled() {
        this.impl$eventCalled = true;
    }

    @Override
    public boolean bridge$eventCalled() {
        return this.impl$eventCalled;
    }

    @Override
    public void bridge$unfreeze() {
        this.frozen = false;
        this.allTags = MappedRegistry.TagSet.unbound();
    }
}
