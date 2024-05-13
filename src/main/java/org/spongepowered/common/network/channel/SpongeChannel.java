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

import com.google.common.collect.Multimap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.ClientConnectionState;
import org.spongepowered.api.network.ClientSideConnection;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.api.network.ServerConnectionState;
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
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public abstract class SpongeChannel implements Channel {

    private final ResourceKey key;
    private final SpongeChannelManager manager;
    private final Logger logger;
    private final int type;
    private final CustomPacketPayload.Type<SpongeChannelPayload> payloadType;

    private volatile ChannelExceptionHandler<EngineConnectionState> exceptionHandler =
            ChannelExceptionHandler.logEverything().suppress(ChannelNotSupportedException.class);

    public SpongeChannel(final int type, final ResourceKey key, final SpongeChannelManager manager) {
        this.type = type;
        this.key = key;
        this.manager = manager;
        this.logger = LogManager.getLogger("channel/" + key.formatted());
        this.payloadType = CustomPacketPayload.createType(key.toString());
    }

    public int getType() {
        return this.type;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public CustomPacketPayload.Type<SpongeChannelPayload> payloadType() {
        return this.payloadType;
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
    public void setExceptionHandler(final ChannelExceptionHandler<EngineConnectionState> handler) {
        Objects.requireNonNull(handler, "handler");
        this.exceptionHandler = handler;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeChannel.class.getSimpleName() + "[", "]")
                .add("key=" + this.key)
                .toString();
    }

    public boolean checkSupported(final EngineConnection connection, final EngineConnectionState state, final CompletableFuture<?> future) {
        if (!ConnectionUtil.getRegisteredChannels(connection).contains(this.key())) {
            this.handleException(connection, state, new ChannelNotSupportedException("The channel \"" + this.key() + "\" isn't supported."), future);
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
    protected abstract void handlePlayPayload(EngineConnection connection, EngineConnectionState state, ChannelBuf payload);

    /**
     * Handles a login request payload packet. This is currently guaranteed to be run on the client.
     *
     * @param connection The connection to decode the payload for
     * @param transactionId The transaction id of the request
     * @param payload The payload, or null if no response
     */
    protected abstract void handleLoginRequestPayload(EngineConnection connection, EngineConnectionState state, int transactionId, ChannelBuf payload);

    /**
     * Handles the response to a transaction for the stored data.
     *
     * @param connection The connection to decode the payload for
     * @param stored The data that was stored alongside the payload
     * @param result The result of the transaction
     */
    protected abstract void handleTransactionResponse(EngineConnection connection, EngineConnectionState state, Object stored, TransactionResult result);

    public void handleException(final EngineConnection connection, final EngineConnectionState state, final Throwable cause, final @Nullable CompletableFuture<?> future) {
        try {
            this.exceptionHandler.handle(state, this, ChannelExceptionUtil.of(cause), future);
        } catch (final Throwable ex) {
            SpongeCommon.logger().error("The exception handler of the channel " + this.key() + " failed to handle an exception.", ex);
        }
    }

    public static <C extends EngineConnection> Class<C> getConnectionClass(final EngineConnectionSide<C> side) {
        return (Class<C>) (side == EngineConnectionSide.CLIENT ? ClientSideConnection.class : ServerSideConnection.class);
    }

    public static <H> @Nullable H getRequestHandler(final EngineConnectionState state, final Map<Class<?>, H> handlersMap) {
        H handler = null;
        if (state instanceof ClientConnectionState) {
            if (state instanceof ClientConnectionState.Game) {
                handler = handlersMap.get(ClientConnectionState.Game.class);
            } else if (state instanceof ClientConnectionState.Configuration) {
                handler = handlersMap.get(ClientConnectionState.Configuration.class);
            } else if (state instanceof ClientConnectionState.Login) {
                handler = handlersMap.get(ClientConnectionState.Login.class);
            }
            if (handler == null && state instanceof ClientConnectionState.Authenticated) {
                handler = handlersMap.get(ClientConnectionState.Authenticated.class);
            }
            if (handler == null) {
                handler = handlersMap.get(ClientConnectionState.class);
            }
        } else if (state instanceof ServerConnectionState) {
            if (state instanceof ServerConnectionState.Game) {
                handler = handlersMap.get(ServerConnectionState.Game.class);
            } else if (state instanceof ServerConnectionState.Configuration) {
                handler = handlersMap.get(ServerConnectionState.Configuration.class);
            } else if (state instanceof ServerConnectionState.Login) {
                handler = handlersMap.get(ServerConnectionState.Login.class);
            }
            if (handler == null && state instanceof ServerConnectionState.Authenticated) {
                handler = handlersMap.get(ServerConnectionState.Authenticated.class);
            }
            if (handler == null) {
                handler = handlersMap.get(ServerConnectionState.class);
            }
        }
        if (handler == null) {
            if (state instanceof EngineConnectionState.Game) {
                handler = handlersMap.get(EngineConnectionState.Game.class);
            } else if (state instanceof EngineConnectionState.Configuration) {
                handler = handlersMap.get(EngineConnectionState.Configuration.class);
            } else if (state instanceof EngineConnectionState.Login) {
                handler = handlersMap.get(EngineConnectionState.Login.class);
            }
            if (handler == null && state instanceof EngineConnectionState.Authenticated) {
                handler = handlersMap.get(EngineConnectionState.Authenticated.class);
            }
        }
        if (handler == null) {
            handler = handlersMap.get(EngineConnectionState.class);
        }
        return handler;
    }

    public static <H> Collection<H> getResponseHandlers(final EngineConnectionState state, final Multimap<Class<?>, H> handlersMap) {
        Collection<H> handlers = null;
        boolean modifiable = false;
        for (final Map.Entry<Class<?>, Collection<H>> entry : handlersMap.asMap().entrySet()) {
            if (entry.getKey().isInstance(state)) {
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
