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
package org.spongepowered.common.mixin.api.minecraft.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;

import java.util.Objects;
import java.util.Optional;

@Mixin(WritableRegistry.class)
public abstract class WritableRegistryMixin_API<T> extends RegistryMixin_API<T> {

    @Override
    public boolean isDynamic() {
        return ((WritableRegistryBridge<T>) this).bridge$isDynamic();
    }

    @Override
    public <V extends T> Optional<RegistryEntry<V>> register(final ResourceKey key, final V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        return Optional.ofNullable(
            ((WritableRegistryBridge<V>) this).bridge$register(
                (net.minecraft.resources.ResourceKey<V>) net.minecraft.resources.ResourceKey
                    .create((net.minecraft.resources.ResourceKey<? extends Registry<T>>) this.shadow$key(), (ResourceLocation) (Object) key),
                value, Lifecycle.stable()
            )
        );
    }
}
