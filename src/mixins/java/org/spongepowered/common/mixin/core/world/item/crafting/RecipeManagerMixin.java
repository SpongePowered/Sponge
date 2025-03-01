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
package org.spongepowered.common.mixin.core.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @Redirect(method = "fromJson", at = @At(
        value = "INVOKE",
        target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"
    ))
    private static <T> DataResult<Recipe<?>> impl$onParseRecipe(
        final Codec<Recipe<?>> instance, final DynamicOps<T> dynamicOps, final T element,
        ResourceKey<Recipe<?>> $$0, JsonObject $$1, HolderLookup.Provider $$2
    ) {
        final DataResult<Recipe<?>> parsed;
        try {
            parsed = instance.parse(dynamicOps, element);
        } catch (Exception e) {
            SpongeCommon.logger().error("Could not parse recipe {}", $$0, e);
            throw new RuntimeException(e);
        }
        if (parsed.error().isPresent()) {
            SpongeCommon.logger().error("Could not parse recipe {} {}", $$0, parsed.error().get().message());
        }
        return parsed;
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/world/item/crafting/RecipeMap;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleJsonResourceReloadListener;scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V"))
    private void impl$onPrepare(final ResourceManager $$0, final FileToIdConverter $$1, final DynamicOps<JsonElement> $$2, final Codec<Recipe<?>> $$3,
            final Map<ResourceLocation, Recipe<?>> $$4, final Operation<Void> original) {
        SortedMap<ResourceLocation, Recipe<?>> result = new TreeMap<>();
        original.call($$0, $$1, $$2, $$3, result);
        final RegistryHolderLogic registryHolder = ((SpongeRegistryHolder) $$0).registryHolder();
        final Registry<Recipe<?>> registry = (Registry<Recipe<?>>) (Object) registryHolder.registry(RegistryTypes.RECIPE);
        result.forEach((k, v) -> registry.register((org.spongepowered.api.ResourceKey) (Object) k, v));
        Launch.instance().lifecycle().processServerRegistries((RegistryHolder) $$0, Stream.of(registry));
        registry.streamEntries().forEach(e -> $$4.put((ResourceLocation) (Object) e.key(), e.value()));
    }
}
