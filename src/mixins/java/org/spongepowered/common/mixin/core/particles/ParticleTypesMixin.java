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
package org.spongepowered.common.mixin.core.particles;

import com.mojang.serialization.Codec;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.ResourceKeyBridge;

import java.util.function.Function;

@Mixin(ParticleTypes.class)
public class ParticleTypesMixin {

    @Inject(method = "register(Ljava/lang/String;Lnet/minecraft/particles/IParticleData$IDeserializer;Ljava/util/function/Function;)Lnet/minecraft/particles/ParticleType;", at = @At("RETURN"))
    private static <T extends IParticleData> void inpl$setCatalogKey(final String key, final IParticleData.IDeserializer<T> deserializer,
            final Function<ParticleType<T>, Codec<T>> codec, final CallbackInfoReturnable<ParticleType<T>> cir) {
        final ParticleType<T> returnValue = cir.getReturnValue();
        ((ResourceKeyBridge) returnValue).bridge$setKey(ResourceKey.minecraft(key));
    }

    @Inject(method = "register(Ljava/lang/String;Z)Lnet/minecraft/particles/BasicParticleType;", at = @At("RETURN"))
    private static void inpl$setCatalogKey2(final String key, final boolean alwaysShow, final CallbackInfoReturnable<BasicParticleType> cir) {
        final BasicParticleType returnValue = cir.getReturnValue();
        ((ResourceKeyBridge) returnValue).bridge$setKey(ResourceKey.minecraft(key));
    }
}
