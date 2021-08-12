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
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.core.RegistryBridge;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;

@Mixin(WritableRegistry.class)
public abstract class WritableRegistryMixin<T> extends RegistryMixin<T> implements WritableRegistryBridge<T>, RegistryBridge<T> {

    // @formatter:off
    @Shadow public abstract <V extends T> V shadow$register(ResourceKey<T> p_218381_1_, V p_218381_2_, Lifecycle p_218381_3_);
    // @formatter:on

    private boolean impl$isDynamic = true;

    @Override
    public boolean bridge$isDynamic() {
        return this.impl$isDynamic;
    }

    @Override
    public void bridge$setDynamic(final boolean isDynamic) {
        this.impl$isDynamic = isDynamic;
    }

    @Nullable
    @Override
    public RegistryEntry<T> bridge$register(final ResourceKey<T> key, final T value, final Lifecycle lifecycle) {
        this.shadow$register(key, value, lifecycle);

        return ((RegistryBridge<T>) this)
                .bridge$get((org.spongepowered.api.ResourceKey) (Object) key.location())
                .orElse(null);
    }

}
