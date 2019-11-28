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
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.event.world.SaveWorldEvent;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SpongeInternalListeners {

    public static SpongeInternalListeners getInstance() {
        return Holder.INSTANCE;
    }

    private Multimap<Class<?>, Predicate<Object>> serviceCallbacks = HashMultimap.create();

    @SuppressWarnings("unchecked")
    public <T> void registerExpirableServiceCallback(Class<T> service, Predicate<T> callback) {
        this.serviceCallbacks.put(service, (Predicate<Object>) callback);
    }

    public <T> void registerServiceCallback(Class<T> service, Consumer<T> callback) {
        this.registerExpirableServiceCallback(service, o -> {
            callback.accept(o);
            return true;
        });
    }

    @Listener
    public void onServiceChange(ChangeServiceProviderEvent event) {
        Iterator<Predicate<Object>> it = this.serviceCallbacks.get(event.getService()).iterator();
        while (it.hasNext()) {
            if (!it.next().test(event.getNewProvider())) {
                it.remove();
            }
        }
    }

    @Listener
    public void onWorldSave(SaveWorldEvent event) {
        if (Sponge.getServer().getDefaultWorld().isPresent()) {
            if (event.getTargetWorld().getUniqueId().equals(Sponge.getServer().getDefaultWorld().get().getUniqueId())) {
                SpongeUsernameCache.save();
                final MinecraftServer server = SpongeImpl.getServer();
                ((PlayerProfileCacheBridge) server.func_152358_ax()).bridge$setCanSave(true);
                server.func_152358_ax().func_152658_c();
                ((PlayerProfileCacheBridge) server.func_152358_ax()).bridge$setCanSave(false);
            }
        }
    }

    SpongeInternalListeners() {}

    private static final class Holder {
        static final SpongeInternalListeners INSTANCE = new SpongeInternalListeners();
    }

}
