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
package org.spongepowered.common.network.channel;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.LocalPlayerConnection;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.ServerPlayerConnection;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.network.channel.Channel;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.ChannelExceptionHandler;
import org.spongepowered.api.network.channel.ChannelNotSupportedException;
import org.spongepowered.common.SpongeCommon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public abstract class SpongeChannel implements Channel {

    private final ResourceKey key;
    private final SpongeChannelManager manager;
    private final Logger logger;
    private final int type;

    private volatile ChannelExceptionHandler<EngineConnection> exceptionHandler =
            ChannelExceptionHandler.logEverything().suppress(ChannelNotSupportedException.class);

    public SpongeChannel(final int type, final ResourceKey key, final SpongeChannelManager manager) {
        this.type = type;
        this.key = key;
        this.manager = manager;
        this.logger = LogManager.getLogger("channel/" + key.formatted());
    }

    public int getType() {
        return this.type;
    }

    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public SpongeChannelManager manager() {
        return this.manager;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public void setExceptionHandler(final ChannelExceptionHandler<EngineConnection> handler) {
        Objects.requireNonNull(handler, "handler");
        this.exceptionHandler = handler;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", this.key)
                .toString();
    }

    public boolean checkSupported(final EngineConnection connection, final CompletableFuture<?> future) {
        if (!ConnectionUtil.getRegisteredChannels(connection).contains(this.key())) {
            this.handleException(connection, new ChannelNotSupportedException("The channel \"" + this.key() + "\" isn't supported."), future);
            return false;
        }
        return true;
    }

    /**
     * Handles a normal payload packet. Can be run on the client or server side.
     *
     * @param connection The connection to decode the payload for
     * @param payload The payload
     */
    protected abstract void handlePlayPayload(EngineConnection connection, ChannelBuf payload);

    /**
     * Handles a login request payload packet. This is currently guaranteed to be run on the client.
     *
     * @param connection The connection to decode the payload for
     * @param transactionId The transaction id of the request
     * @param payload The payload, or null if no response
     */
    protected abstract void handleLoginRequestPayload(EngineConnection connection, int transactionId, ChannelBuf payload);

    /**
     * Handles the response to a transaction for the stored data.
     *
     * @param connection The connection to decode the payload for
     * @param stored The data that was stored alongside the payload
     * @param result The result of the transaction
     */
    protected abstract void handleTransactionResponse(EngineConnection connection, Object stored, TransactionResult result);

    public void handleException(final EngineConnection connection, final Throwable cause, final @Nullable CompletableFuture<?> future) {
        try {
            this.exceptionHandler.handle(connection, this, ChannelExceptionUtil.of(cause), future);
        } catch (final Throwable ex) {
            SpongeCommon.logger().error("The exception handler of the channel " + this.key() + " failed to handle an exception.", ex);
        }
    }

    public static <C extends EngineConnection> Class<C> getConnectionClass(final EngineConnectionSide<C> side) {
        return (Class<C>) (side == EngineConnectionSide.CLIENT ? ClientSideConnection.class : ServerSideConnection.class);
    }

    public static <H> @Nullable H getRequestHandler(final EngineConnection connection, final Map<Class<?>, H> handlersMap) {
        H handler = null;
        if (connection instanceof LocalPlayerConnection) {
            handler = handlersMap.get(LocalPlayerConnection.class);
        } else if (connection instanceof ServerPlayerConnection) {
            handler = handlersMap.get(ServerPlayerConnection.class);
        }
        if (handler == null && connection instanceof PlayerConnection) {
            handler = handlersMap.get(PlayerConnection.class);
        }
        if (handler == null) {
            if (connection instanceof ClientSideConnection) {
                handler = handlersMap.get(ClientSideConnection.class);
            } else if (connection instanceof ServerSideConnection) {
                handler = handlersMap.get(ServerSideConnection.class);
            }
        }
        if (handler == null) {
            handler = handlersMap.get(EngineConnection.class);
        }
        return handler;
    }

    public static <H> Collection<H> getResponseHandlers(final EngineConnection connection, final Multimap<Class<?>, H> handlersMap) {
        Collection<H> handlers = null;
        boolean modifiable = false;
        for (final Map.Entry<Class<?>, Collection<H>> entry : handlersMap.asMap().entrySet()) {
            if (entry.getKey().isInstance(connection)) {
                if (handlers == null) {
                    handlers = entry.getValue();
                } else if (!modifiable) {
                    handlers = new ArrayList<>(handlers);
                    modifiable = true;
                } else {
                    handlers.addAll(entry.getValue());
                }
            }
        }
        return handlers == null ? Collections.emptyList() : handlers;
    }
}
