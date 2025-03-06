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
package org.spongepowered.common.mixin.core.resources;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Decoder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import org.spongepowered.api.adventure.ChatTypes;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.resources.RegistryDataLoader_LoaderAccessor;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;
import org.spongepowered.common.bridge.resources.RegistryDataLoader_LoaderBridge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.registry.SpongeRegistryDependencyEntry;
import org.spongepowered.common.registry.SpongeRegistryHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "loadContentsFromManager", at = @At("RETURN"))
    private static <E> void impl$afterLoadRegistryContents(
            final ResourceManager $$0,
            final RegistryOps.RegistryInfoLookup $$1,
            final WritableRegistry<E> $$2,
            final Decoder<E> $$3,
            final Map<ResourceKey<?>, Exception> $$4,
            final CallbackInfo ci)
    {
        if (Registries.CHAT_TYPE.equals($$2.key())) {
            final ChatTypeDecoration narration = ChatTypeDecoration.withSender("chat.type.text.narrate");
            $$2.register(ResourceKey.create($$2.key(), (ResourceLocation) (Object) ChatTypes.CUSTOM_CHAT.location()), (E) new ChatType(ChatTypeDecoration.withSender("%s%s"), narration), RegistrationInfo.BUILT_IN);
            $$2.register(ResourceKey.create($$2.key(), (ResourceLocation) (Object) ChatTypes.CUSTOM_MESSAGE.location()), (E) new ChatType(ChatTypeDecoration.teamMessage("%s%s%s"), narration), RegistrationInfo.BUILT_IN);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Ljava/util/List;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
        at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 1))
    private static void impl$onLoad(final List<RegistryDataLoader_LoaderAccessor<?>> instance, final Consumer<?> consumer, final Operation<Void> original) {
        final DependencySorter<RegistryType<?>, SpongeRegistryDependencyEntry<RegistryDataLoader_LoaderAccessor<?>>> dependencies = new DependencySorter<>();
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        instance.stream()
            .collect(Collectors.groupingBy(l -> ((RegistryDataLoader_LoaderBridge) l).bridge$registryHolder(), Collectors.toSet()))
            .forEach((k, v) -> {
                ((SpongeRegistryHolder) k).setRootMinecraftRegistry(new RegistryAccess.ImmutableRegistryAccess(
                    (List) Stream.concat(k.streamRegistries(RegistryRoots.MINECRAFT), v.stream().map(RegistryDataLoader_LoaderAccessor::accessor$registry)).toList()));
                lifecycle.processServerRegistries(k, v.stream()
                    .filter(l -> !RegistryDataLoader.DIMENSION_REGISTRIES.contains(l.accessor$data())) // NOTE: DIMENSION_REGISTRIES are special!
                    .map(l -> (Registry<?>) l.accessor$registry()));
                v.forEach(l -> dependencies.addEntry(((Registry<?>) l.accessor$registry()).type(),
                    new SpongeRegistryDependencyEntry<>(l, ((WritableRegistryBridge<?>) l.accessor$registry()).bridge$pendingDependencies().toList())));
            });

        List<RegistryDataLoader_LoaderAccessor<?>> loaders = new ArrayList<>(instance.size());
        dependencies.orderByDependencies(($, v) -> loaders.add(v.cookie()));

        original.call(Collections.unmodifiableList(loaders), consumer);
    }
}
