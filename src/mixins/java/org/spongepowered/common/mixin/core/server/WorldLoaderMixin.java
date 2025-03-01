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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.Lifecycle;



@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {

    @WrapOperation(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/WorldLoader$PackConfig;createResourceManager()Lcom/mojang/datafixers/util/Pair;"))
    private static Pair<WorldDataConfiguration, CloseableResourceManager> impl$onCreateResourceManager(final WorldLoader.PackConfig instance,
            final Operation<Pair<WorldDataConfiguration, CloseableResourceManager>> original) {
        final Pair<WorldDataConfiguration, CloseableResourceManager> pair = original.call(instance);
        final CloseableResourceManager resourceManager = pair.getSecond();
        final Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.setWorldDataConfiguration(pair.getFirst());
        lifecycle.beginEstablishServerRegistries((RegistryHolder) resourceManager);
        return pair;
    }

    @ModifyExpressionValue(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader$WorldDataSupplier;get(Lnet/minecraft/server/WorldLoader$DataLoadContext;)Lnet/minecraft/server/WorldLoader$DataLoadOutput;"))
    private static WorldLoader.DataLoadOutput<?> impl$onBakedDimensionRegistries(final WorldLoader.DataLoadOutput<?> original,
            final @Local CloseableResourceManager resourceManager) {
        original.finalDimensions().registries().forEach(r -> ((WritableRegistryBridge<?>) r.value()).bridge$unfreeze());
        Launch.instance().lifecycle().processServerRegistries((RegistryHolder) resourceManager, original.finalDimensions().registries().map(e -> (Registry<?>) e.value()));
        original.finalDimensions().registries().forEach(r -> r.value().freeze());
        return original;
    }
}
