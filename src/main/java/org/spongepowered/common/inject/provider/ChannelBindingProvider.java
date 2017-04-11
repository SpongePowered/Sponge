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
package org.spongepowered.common.inject.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.spi.InjectionPoint;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.ChannelRegistrar;

import java.lang.reflect.AnnotatedElement;
import java.util.function.BiFunction;

public final class ChannelBindingProvider<B extends ChannelBinding> implements Provider<B> {

    @Inject private ChannelRegistrar registrar;
    @Inject private Provider<InjectionPoint> point;
    private final BiFunction<ChannelRegistrar, String, B> function;

    public ChannelBindingProvider(final BiFunction<ChannelRegistrar, String, B> function) {
        this.function = function;
    }

    @Override
    public B get() {
        final String channel = ((AnnotatedElement) this.point.get().getMember()).getAnnotation(ChannelId.class).value();
        return this.function.apply(this.registrar, channel);
    }

}
