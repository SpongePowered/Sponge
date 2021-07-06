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
import net.minecraft.core.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.registry.SpongeRegistryType;

@Mixin(Registry.class)
public abstract class RegistryMixin<T> implements RegistryBridge<T> {

    private RegistryType<T> impl$type;

    @Override
    public RegistryType<T> bridge$type() {
        return this.impl$type;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void impl$setType(final net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> key,
                                 final Lifecycle p_i232510_2_, final CallbackInfo ci) {
        this.impl$type = new SpongeRegistryType<T>((ResourceKey) (Object) ((ResourceKeyAccessor) key).accessor$registryName(),
                (ResourceKey) (Object) key.location());
    }

}
