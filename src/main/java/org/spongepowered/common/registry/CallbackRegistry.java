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
package org.spongepowered.common.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiConsumer;

public final class CallbackRegistry<T> extends MappedRegistry<T> {

    private final BiConsumer<ResourceKey<T>, T> callback;
    private boolean callbackEnabled;

    public CallbackRegistry(final ResourceKey<? extends Registry<T>> key, final Lifecycle lifecycle, final BiConsumer<ResourceKey<T>, T> callback) {
        super(key, lifecycle);
        this.callback = callback;
    }

    @Override
    public Holder.Reference<T> register(final ResourceKey<T> key, final T instance, final RegistrationInfo lifecycle) {
        final Holder.Reference<T> value = super.register(key, instance, lifecycle);
        if (this.callbackEnabled) {
            this.callback.accept(key, instance);
        }
        return value;
    }

    public void setCallbackEnabled(final boolean callbackEnabled) {
        this.callbackEnabled = callbackEnabled;
    }
}
