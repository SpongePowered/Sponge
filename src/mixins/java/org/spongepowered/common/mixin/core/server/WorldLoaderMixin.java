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

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.launch.Launch;

@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {

    @Redirect(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/WorldLoader$PackConfig;createResourceManager()Lcom/mojang/datafixers/util/Pair;"))
    private static Pair<WorldDataConfiguration, CloseableResourceManager> impl$onGetSecond(final WorldLoader.PackConfig instance) {
        final Pair<WorldDataConfiguration, CloseableResourceManager> pair = instance.createResourceManager();
        Launch.instance().lifecycle().setWorldDataConfiguration(pair.getFirst());
        return pair;
    }

    @Redirect(method = "load", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/LayeredRegistryAccess;getAccessForLoading(Ljava/lang/Object;)Lnet/minecraft/core/RegistryAccess$Frozen;"))
    private static <T> RegistryAccess.Frozen impl$onGetAccess(final LayeredRegistryAccess instance, final T $$0) {
        final RegistryAccess.Frozen registryAccess = instance.getAccessForLoading($$0);
        final var lifecycle = Launch.instance().lifecycle();
        lifecycle.establishGlobalRegistries(registryAccess, (RegistryLayer) $$0);
        return registryAccess;
    }


    @Redirect(method = "load", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/core/LayeredRegistryAccess;replaceFrom(Ljava/lang/Object;[Lnet/minecraft/core/RegistryAccess$Frozen;)Lnet/minecraft/core/LayeredRegistryAccess;"))
    private static <T> LayeredRegistryAccess<T> impl$afterLoadDimensionRegistries(final LayeredRegistryAccess instance,
        final T $$0, final RegistryAccess.Frozen[] $$1) {
        final var lifecycle = Launch.instance().lifecycle();
        lifecycle.establishGlobalRegistries($$1[0], RegistryLayer.DIMENSIONS);
        return instance.replaceFrom($$0, $$1);
    }



}
