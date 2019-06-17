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
package org.spongepowered.common.service.permission;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A context calculator handling world contexts.
 */
@SuppressWarnings("deprecation")
public class SpongeContextCalculator implements ContextCalculator<Subject> {

    private final LoadingCache<RemoteSource, Set<Context>> remoteIpCache = buildAddressCache(Context.REMOTE_IP_KEY, rs -> getAddress(rs, RemoteConnection::getAddress));
    private final LoadingCache<RemoteSource, Set<Context>> localIpCache = buildAddressCache(Context.LOCAL_IP_KEY, rs -> getAddress(rs, RemoteConnection::getVirtualHost));

    private static InetAddress getAddress(RemoteSource input, Function<RemoteConnection, InetSocketAddress> func) {
        InetSocketAddress socket = func.apply(input.getConnection());
        if (!socket.isUnresolved()) {
            return socket.getAddress();
        }
        try {
            return InetAddress.getByName(socket.getHostName());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private LoadingCache<RemoteSource, Set<Context>> buildAddressCache(final String contextKey, final Function<RemoteSource, InetAddress> function) {
        return CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<RemoteSource, Set<Context>>() {
                @Override
                public Set<Context> load(RemoteSource key) {
                    ImmutableSet.Builder<Context> builder = ImmutableSet.builder();
                    final InetAddress addr = function.apply(key);
                    if (addr == null) {
                        return builder.build();
                    }
                    builder.add(new Context(contextKey, addr.getHostAddress()));
                    for (String set : Maps.filterValues(SpongeImpl.getGlobalConfigAdapter().getConfig().getIpSets(), input -> input.apply(addr)).keySet()) {
                        builder.add(new Context(contextKey, set));
                    }
                    return builder.build();
                }
            });
    }

    @Override
    public void accumulateContexts(Subject subject, Set<Context> accumulator) {
        Optional<CommandSource> subjSource = subject.getCommandSource();
        if (subjSource.isPresent()) {
            CommandSource source = subjSource.get();
            if (source instanceof Locatable) {
                World currentExt = ((Locatable) source).getWorld();
                accumulator.add(currentExt.getContext());
                accumulator.add((currentExt.getDimension().getContext()));
            }
            if (source instanceof RemoteSource) {
                RemoteSource rem = (RemoteSource) source;
                accumulator.addAll(this.remoteIpCache.getUnchecked(rem));
                accumulator.addAll(this.localIpCache.getUnchecked(rem));
                accumulator.add(new Context(Context.LOCAL_PORT_KEY, String.valueOf(rem.getConnection().getVirtualHost().getPort())));
                accumulator.add(new Context(Context.LOCAL_HOST_KEY, rem.getConnection().getVirtualHost().getHostName()));
            }
        }

    }

    @Override
    public boolean matches(Context context, Subject subject) {
        Optional<CommandSource> subjSource = subject.getCommandSource();
        if (subjSource.isPresent()) {
            CommandSource source = subjSource.get();
            if (source instanceof Locatable && context.getType().equals(Context.WORLD_KEY)) {
                Locatable located = (Locatable) source;
                if (context.getType().equals(Context.WORLD_KEY)) {
                    return located.getWorld().getContext().equals(context);
                } else if (context.getType().equals(Context.DIMENSION_KEY)) {
                    return located.getWorld().getDimension().getContext().equals(context);
                }
            }
            if (source instanceof RemoteSource) {
                RemoteSource remote = (RemoteSource) source;
                if (context.getType().equals(Context.LOCAL_HOST_KEY)) {
                    return context.getValue().equals(remote.getConnection().getVirtualHost().getHostName());
                } else if (context.getType().equals(Context.LOCAL_PORT_KEY)) {
                    return context.getValue().equals(String.valueOf(remote.getConnection().getVirtualHost().getPort()));
                } else if (context.getType().equals(Context.LOCAL_IP_KEY)) {
                    return this.localIpCache.getUnchecked(remote).contains(context);
                } else if (context.getType().equals(Context.REMOTE_IP_KEY)) {
                    return this.remoteIpCache.getUnchecked(remote).contains(context);
                }
            }
        }
        return false;
    }
}
