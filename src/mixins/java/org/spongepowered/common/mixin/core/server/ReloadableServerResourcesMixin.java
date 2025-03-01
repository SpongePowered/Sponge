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
package org.spongepowered.common.mixin.core.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.accessor.core.MappedRegistryAccessor;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.registry.SpongeRegistryHolder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @ModifyExpressionValue(method = "loadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerRegistries;reload(Lnet/minecraft/core/LayeredRegistryAccess;Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static CompletableFuture<ReloadableServerRegistries.LoadResult> impl$onLoadResources(final CompletableFuture<ReloadableServerRegistries.LoadResult> original,
            final ResourceManager $$0, final LayeredRegistryAccess<RegistryLayer> $$1, final List<Registry.PendingTags<?>> $$2,
            final FeatureFlagSet $$3, final Commands.CommandSelection $$4, final int $$5, final Executor $$6, final Executor $$7) {
        return original.thenApply(r -> {
            final SpongeRegistryHolder spongeRegistryHolder = (SpongeRegistryHolder) $$0;
            spongeRegistryHolder.setRootMinecraftRegistry(r.layers().compositeAccess());
            spongeRegistryHolder.registryHolder().freezeSpongeDynamicRegistries(false);

            final List<Registry> spongeRegistries = (List) Stream.concat(
                spongeRegistryHolder.streamRegistries(RegistryRoots.SPONGE),
                Sponge.game().streamRegistries(RegistryRoots.SPONGE)).toList();

            final RegistryAccess.Frozen reloadable = r.layers().getLayer(RegistryLayer.RELOADABLE);
            final RegistryAccess.Frozen combined = new RegistryAccess.ImmutableRegistryAccess((Stream) Stream.concat(
                reloadable.registries(),
                spongeRegistries.stream().filter(v -> ((MappedRegistryAccessor) v).accessor$frozen()).map(v -> new RegistryAccess.RegistryEntry<>(v.key(), v))
            )).freeze();

            return new ReloadableServerRegistries.LoadResult(
                r.layers().replaceFrom(RegistryLayer.RELOADABLE, combined),
                HolderLookup.Provider.create((Stream) Stream.concat(r.lookupWithUpdatedTags().listRegistries(), spongeRegistries.stream()))
            );
        });
    }

    @ModifyReturnValue(method = "loadResources", at = @At(value = "RETURN"))
    private static CompletableFuture<ReloadableServerResources> impl$onLoaded(final CompletableFuture<ReloadableServerResources> original,
            final ResourceManager $$0, final LayeredRegistryAccess<RegistryLayer> $$1, final List<Registry.PendingTags<?>> $$2,
            final FeatureFlagSet $$3, final Commands.CommandSelection $$4, final int $$5, final Executor $$6, final Executor $$7) {
        return original.thenApply(r -> {
            Launch.instance().lifecycle().endEstablishServerRegistries((RegistryHolder) $$0);
            return r;
        });
    }
}
