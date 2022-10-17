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

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.thread.BlockableEventLoop;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.common.bridge.network.ConnectionHolderBridge;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class PacketSender {

    public static void sendTo(final EngineConnection connection, final Packet<?> packet) {
        PacketSender.sendTo(connection, packet, (Consumer) null);
    }

    public static void sendTo(final EngineConnection connection, final Packet<?> packet,
            final @Nullable Consumer<Future<? super Void>> listener) {
        final Connection networkManager = ((ConnectionHolderBridge) connection).bridge$getConnection();
        GenericFutureListener<? extends Future<? super Void>> asyncListener = null;
        if (listener != null) {
            final EngineConnectionSide<?> side = connection.side();
            // Complete the netty callback on the sync thread
            asyncListener = future -> {
                final BlockableEventLoop<?> executor;
                if (side == EngineConnectionSide.CLIENT) {
                    executor = (BlockableEventLoop<?>) Sponge.client();
                } else {
                    executor = (BlockableEventLoop<?>) Sponge.server();
                }
                executor.execute(() -> listener.accept(future));
            };
        }
        networkManager.send(packet, asyncListener);
    }

    public static void sendTo(final EngineConnection connection, final Packet<?> packet, final CompletableFuture<Void> future) {
        PacketSender.sendTo(connection, packet, sendFuture -> {
            if (sendFuture.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(sendFuture.cause());
            }
        });
    }

    private PacketSender() {
    }
}
