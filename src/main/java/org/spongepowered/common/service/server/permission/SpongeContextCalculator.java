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
package org.spongepowered.common.service.server.permission;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A context calculator handling world contexts.
 */
public class SpongeContextCalculator implements ContextCalculator {

    private final LoadingCache<RemoteConnection, Set<Context>> remoteIpCache =
            this.buildAddressCache(Context.REMOTE_IP_KEY, rs -> SpongeContextCalculator.address(rs, RemoteConnection::address));
    private final LoadingCache<RemoteConnection, Set<Context>> localIpCache =
            this.buildAddressCache(Context.LOCAL_IP_KEY, rs -> SpongeContextCalculator.address(rs, RemoteConnection::virtualHost));

    private static InetAddress address(final RemoteConnection input, final Function<RemoteConnection, InetSocketAddress> func) {
        final InetSocketAddress socket = func.apply(input);
        if (!socket.isUnresolved()) {
            return socket.getAddress();
        }
        try {
            return InetAddress.getByName(socket.getHostName());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private LoadingCache<RemoteConnection, Set<Context>> buildAddressCache(final String contextKey, final Function<RemoteConnection, InetAddress> function) {
        return Caffeine.newBuilder()
            .weakKeys()
            .<RemoteConnection, Set<Context>>build((key) -> {
                final ImmutableSet.Builder<Context> builder = ImmutableSet.builder();
                final InetAddress addr = function.apply(key);
                if (addr == null) {
                    return builder.build();
                }
                builder.add(new Context(contextKey, addr.getHostAddress()));
                for (final Map.Entry<String, Predicate<InetAddress>> entry : SpongeConfigs.getCommon().get().getIpSets().entrySet()) {
                    if (entry.getValue().test(addr)) {
                        builder.add(new Context(contextKey, entry.getKey()));
                    }
                }
                return builder.build();
            });
    }

    @Override
    public void accumulateContexts(final Cause causes, final Consumer<Context> accumulator) {
        final /* @Nullable */ ServerLocation location = causes.context().get(EventContextKeys.LOCATION).orElse(null);
        if (location != null) {
            final ServerWorld world = location.worldIfAvailable().orElse(null);
            if (world != null) {
                accumulator.accept(world.context());
                accumulator.accept(world.worldType().context());
            }
        }
        causes.first(RemoteConnection.class).ifPresent(connection -> { // TODO(zml): Wrong way to get a connection, add API?
            this.remoteIpCache.get(connection).forEach(accumulator);
            this.localIpCache.get(connection).forEach(accumulator);
            accumulator.accept(new Context(Context.LOCAL_PORT_KEY, String.valueOf(connection.virtualHost().getPort())));
            accumulator.accept(new Context(Context.LOCAL_HOST_KEY, connection.virtualHost().getHostName()));

        });
    }

}
