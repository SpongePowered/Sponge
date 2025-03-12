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
package org.spongepowered.common.mixin.core.tags;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.accessor.tags.TagEntryAccessor;
import org.spongepowered.common.bridge.server.packs.resources.ResourceManagerBridge;
import org.spongepowered.common.bridge.tags.TagLoaderBridge;
import org.spongepowered.common.bridge.tags.TagLoader_EntryWithSourceBridge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.tag.SpongePluginTagModifier;
import org.spongepowered.common.tag.SpongePluginTagPredicate;
import org.spongepowered.common.tag.SpongePluginTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin<T> implements TagLoaderBridge<T> {

    private @MonotonicNonNull RegistryType<?> impl$registryType;
    private @MonotonicNonNull Map<ResourceKey, SpongePluginTagModifier<?>> impl$modifiers;

    private ResourceLocation impl$buildingTagKey;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "load", at = @At("TAIL"))
    private void impl$onLoad(final ResourceManager $$0, final CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        final SpongePluginTags tags = ((ResourceManagerBridge) $$0).bridge$pluginProvidedTags();
        tags.get(this.impl$registryType).ifPresent(m -> {
            this.impl$modifiers = (Map) m;
            m.forEach((tagKey, modifiers) -> {
                final List<TagLoader.EntryWithSource> entries = cir.getReturnValue().computeIfAbsent((ResourceLocation) (Object) tagKey, $ -> new ArrayList<>());
                modifiers.append().forEach((k, v) -> {
                    final TagLoader.EntryWithSource entry = new TagLoader.EntryWithSource(k.tag()
                        ? TagEntry.optionalTag((ResourceLocation) (Object) k.key())
                        : TagEntry.optionalElement((ResourceLocation) (Object) k.key()), "sponge");
                    ((TagLoader_EntryWithSourceBridge) (Object) entry).bridge$predicates((Set) v);
                    entries.add(entry);
                });
            });
        });
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean bridge$acceptTag(final TagEntry instance, final TagEntry.Lookup<T> lookup, final Consumer<T> consumer,
            final AcceptTag<T> original, final List<TagLoader.EntryWithSource> tags, final TagLoader.EntryWithSource entry) {
        if (this.impl$modifiers == null) {
            return original.accept(instance, lookup, consumer);
        }

        final SpongePluginTagModifier<?> modifier = this.impl$modifiers.get(this.impl$buildingTagKey);
        if (modifier == null) {
            return original.accept(instance, lookup, consumer);
        }

        final @Nullable Set<SpongePluginTagPredicate<?>> filters = ((TagLoader_EntryWithSourceBridge) (Object) entry).bridge$predicates();
        if (filters == null || filters.isEmpty()) {
            return this.impl$acceptTag(instance, lookup, consumer, original, modifier);
        } else {
            for (final TagLoader.EntryWithSource e : tags) {
                if (((TagLoader_EntryWithSourceBridge) (Object) e).bridge$predicates() != null
                    && !((TagLoader_EntryWithSourceBridge) (Object) e).bridge$predicates().isEmpty()) {
                    // TODO: We could handle recursive predicates if there is a need for it.
                    continue;
                } else if (!this.bridge$isAdd(e)) {
                    continue;
                }

                if (this.impl$filterBase(e.entry(), Tristate.TRUE, filters)) {
                    return this.impl$acceptTag(instance, lookup, consumer, original, modifier);
                } else if (((TagEntryAccessor) e.entry()).accessor$tag()) {
                    final @Nullable Collection<T> values = lookup.tag(((TagEntryAccessor) e.entry()).accessor$id());
                    if (values == null) {
                        continue;
                    }

                    final Tag tag = Tag.of(this.impl$registryType, (ResourceKey) (Object) ((TagEntryAccessor) e.entry()).accessor$id());
                    for (final T value : values) {
                        final ResourceKey key = (ResourceKey) (Object) ((Holder.Reference<?>) value).key().location();
                        final DefaultedRegistryReference reference = RegistryKey.of(this.impl$registryType, key).asDefaultedReference(Sponge::game);
                        for (final SpongePluginTagPredicate<?> predicate : filters) {
                            if (predicate.apply(reference, tag) == Tristate.TRUE) {
                                return this.impl$acceptTag(instance, lookup, consumer, original, modifier);
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean impl$acceptTag(final TagEntry instance, final TagEntry.Lookup<T> lookup, final Consumer<T> consumer,
            final TagLoaderBridge.AcceptTag<T>original, final SpongePluginTagModifier<?> modifier) {
        if (this.impl$filterBase(instance, Tristate.FALSE, (Set) modifier.filters())) {
            return true;
        } else if (((TagEntryAccessor) instance).accessor$tag()) {
            final Tag tag = Tag.of(this.impl$registryType, (ResourceKey) (Object) ((TagEntryAccessor) instance).accessor$id());
            return original.accept(instance, lookup, i -> {
                final ResourceKey key = (ResourceKey) (Object) ((Holder.Reference<?>) i).key().location();
                final DefaultedRegistryReference reference = RegistryKey.of(this.impl$registryType, key).asDefaultedReference(Sponge::game);
                for (final SpongePluginTagPredicate<?> predicate : modifier.filters()) {
                    if (predicate.apply(reference, tag) == Tristate.FALSE) {
                        return;
                    }
                }
                consumer.accept(i);
            });
        }
        return original.accept(instance, lookup, consumer);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean impl$filterBase(final TagEntry instance, final Tristate match, final Set<SpongePluginTagPredicate<?>> filters) {
        final ResourceKey key = (ResourceKey) (Object) ((TagEntryAccessor) instance).accessor$id();
        if (((TagEntryAccessor) instance).accessor$tag()) {
            final Tag tag = Tag.of(this.impl$registryType, key);
            for (final SpongePluginTagPredicate<?> predicate : filters) {
                if (predicate.apply(null, tag) == match) {
                    return true;
                }
            }
        } else {
            final DefaultedRegistryReference reference = RegistryKey.of(this.impl$registryType, key).asDefaultedReference(Sponge::game);
            for (final SpongePluginTagPredicate<?> predicate : filters) {
                if (predicate.apply(reference, null) == match) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "loadTagsForRegistry", at = @At(value = "NEW", target = "Lnet/minecraft/tags/TagLoader;"))
    private static <T> TagLoader<T> impl$onLoadTagsForRegistry(final TagLoader.ElementLookup<T> $$0, final String $$1, final Operation<TagLoader<T>> original,
            final ResourceManager resourceManager, final WritableRegistry<T> registry) {
        final TagLoader<T> loader = original.call($$0, $$1);
        ((TagLoaderBridge<T>) loader).bridge$registryKey(registry.key());
        return loader;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "loadPendingTags", at = @At(value = "NEW", target = "Lnet/minecraft/tags/TagLoader;"))
    private static <T> TagLoader<T> impl$onLoadPendingTags(final TagLoader.ElementLookup<T> $$0, final String $$1, final Operation<TagLoader<T>> original,
            final ResourceManager resourceManager, final Registry<T> registry) {
        final TagLoader<T> loader = original.call($$0, $$1);
        ((TagLoaderBridge<T>) loader).bridge$registryKey(registry.key());
        return loader;
    }

    @Inject(method = "loadTagsForExistingRegistries", at = @At("HEAD"))
    private static void impl$onLoadTagsForExistingRegistries(final ResourceManager $$0, final RegistryAccess $$1, final CallbackInfoReturnable<List<Registry.PendingTags<?>>> cir) {
        Launch.instance().lifecycle().establishTags($$0);
    }

    @Override
    public void bridge$registryKey(final net.minecraft.resources.ResourceKey<? extends Registry<?>> registryKey) {
        this.impl$registryType =
            RegistryType.of((ResourceKey) (Object) registryKey.registry(), (ResourceKey) (Object) registryKey.location());
    }

    @Override
    public void bridge$buildingTagKey(final @Nullable ResourceLocation key) {
        this.impl$buildingTagKey = key;
    }
}
