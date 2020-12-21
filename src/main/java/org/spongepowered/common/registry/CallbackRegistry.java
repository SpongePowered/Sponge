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
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.BiConsumer;

public final class CallbackRegistry<T> extends SimpleRegistry<T> {

    private final BiConsumer<RegistryKey<T>, T> callback;
    private boolean callbackEnabled;

    public CallbackRegistry(final RegistryKey<? extends Registry<T>> key, final Lifecycle lifecycle, final BiConsumer<RegistryKey<T>, T> callback) {
        super(key, lifecycle);
        this.callback = callback;
    }

    @Override
    public <V extends T> V register(final RegistryKey<T> key, final V instance, final Lifecycle lifecycle) {
        V value = super.register(key, instance, lifecycle);
        if (this.callbackEnabled) {
            this.callback.accept(key, instance);
        }
        return value;
    }

    public void setCallbackEnabled(boolean callbackEnabled) {
        this.callbackEnabled = callbackEnabled;
    }
}
