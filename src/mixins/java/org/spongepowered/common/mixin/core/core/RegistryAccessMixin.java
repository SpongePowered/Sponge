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

import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Opcodes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

@Mixin(RegistryAccess.class)
public abstract class RegistryAccessMixin {

    @Shadow private static <E> void put(ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryAccess> p_243602_0_,
            ResourceKey<? extends Registry<E>> p_243602_1_, Codec<E> p_243602_2_, Codec<E> p_243602_3_) {
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    opcode = Opcodes.INVOKESTATIC,
                    target = "Lnet/minecraft/core/RegistryAccess;put(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)V",
                    ordinal = 0
            )
    )
    private static <E> void impl$separateDimensionTypeNetworkCodec(final ImmutableMap.Builder<ResourceKey<? extends Registry<?>>, RegistryAccess> builder, final ResourceKey<? extends Registry<E>> key, final Codec<E> resourceCodec, final Codec<E> networkCodec) {
        RegistryAccessMixin.put(builder, (ResourceKey) key, SpongeWorldTypeTemplate.DIRECT_CODEC, (Codec<DimensionType>) networkCodec);
    }
}
