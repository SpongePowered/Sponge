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

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.thread.BlockableEventLoop;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionSide;
import org.spongepowered.common.network.SpongeEngineConnection;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class PacketSender {

    public static void sendTo(final EngineConnection connection, final Packet<?> packet) {
        PacketSender.sendTo(connection, packet, (Consumer) null);
    }

    public static void sendTo(final EngineConnection connection, final Packet<?> packet, final @Nullable Consumer<@Nullable Throwable> listener) {
        final Connection networkManager = ((SpongeEngineConnection) connection).connection();
        networkManager.send(packet, listener == null ? null : new SpongePacketSendListener(connection.side(), listener));
    }

    public static void sendTo(final EngineConnection connection, final Packet<?> packet, final CompletableFuture<Void> future) {
        PacketSender.sendTo(connection, packet, throwable -> {
            if (throwable == null) {
                future.complete(null);
            } else {
                future.completeExceptionally(throwable);
            }
        });
    }

    public static final class SpongePacketSendListener implements PacketSendListener {
        private final BlockableEventLoop<?> executor;
        private final Consumer<@Nullable  Throwable> listener;

        public SpongePacketSendListener(final EngineConnectionSide<? extends EngineConnection> side, final Consumer<@Nullable Throwable> listener) {
            this.executor = (BlockableEventLoop<?>) (side == EngineConnectionSide.CLIENT ? Sponge.client() : Sponge.server());
            this.listener = listener;
        }

        public void accept(final @Nullable Throwable throwable) {
            this.executor.execute(() -> this.listener.accept(throwable));
        }
    }

    private PacketSender() {
    }
}
