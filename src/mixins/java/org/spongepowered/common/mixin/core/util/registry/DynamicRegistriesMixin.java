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
package org.spongepowered.common.mixin.core.util.registry;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.dimension.SpongeDimensionTypeRegistration;

@Mixin(DynamicRegistries.class)
public abstract class DynamicRegistriesMixin {

    @Shadow private static <E> void put(ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, DynamicRegistries> p_243602_0_,
            RegistryKey<? extends Registry<E>> p_243602_1_, Codec<E> p_243602_2_, Codec<E> p_243602_3_) {
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    opcode = Opcodes.INVOKESTATIC,
                    target = "Lnet/minecraft/util/registry/DynamicRegistries;put(Lcom/google/common/collect/ImmutableMap$Builder;Lnet/minecraft/util/RegistryKey;Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)V",
                    ordinal = 0
            )
    )
    private static <E> void impl$separateDimensionTypeNetworkCodec(final ImmutableMap.Builder<RegistryKey<? extends Registry<?>>, DynamicRegistries> builder, final RegistryKey<? extends Registry<E>> key, final Codec<E> resourceCodec, final Codec<E> networkCodec) {
        DynamicRegistriesMixin.put(builder, (RegistryKey) key, SpongeDimensionTypeRegistration.DIRECT_CODEC, (Codec<DimensionType>) networkCodec);
    }
}
