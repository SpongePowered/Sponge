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
package org.spongepowered.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.permission.PermissionService;

import java.util.function.Consumer;

public class SpongeInternalListeners {

    public static SpongeInternalListeners getInstance() {
        return Holder.INSTANCE;
    }

    private Multimap<Class<?>, Consumer<Object>> serviceCallbacks = HashMultimap.create();

    @SuppressWarnings("unchecked")
    public <T> void registerServiceCallback(Class<T> service, Consumer<T> callback) {
        Sponge.getServiceManager().provide(service).ifPresent(callback::accept);
        this.serviceCallbacks.put(service, (Consumer<Object>) callback);
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        for (Consumer<Object> consumer: this.serviceCallbacks.get(event.getService())) {
            consumer.accept(event.getNewProvider());
        }
    }

    private SpongeInternalListeners() {}

    private static final class Holder {
        private static final SpongeInternalListeners INSTANCE = new SpongeInternalListeners();
    }

}
