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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.network.EngineConnection;
import org.spongepowered.api.network.EngineConnectionState;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.network.SpongeEngineConnection;

import java.util.Set;

public final class ConnectionUtil {

    public static boolean isLoginPhase(final EngineConnection connection) {
        final EngineConnectionState state = (EngineConnectionState) ((SpongeEngineConnection) connection).connection().getPacketListener();
        return state instanceof EngineConnectionState.Login;
    }

    public static Set<ResourceKey> getRegisteredChannels(final EngineConnection connection) {
        final Connection networkManager = ((SpongeEngineConnection) connection).connection();
        return ((ConnectionBridge) networkManager).bridge$getRegisteredChannels();
    }

    public static TransactionStore getTransactionStore(final EngineConnection connection) {
        final Connection networkManager = ((SpongeEngineConnection) connection).connection();
        return ((ConnectionBridge) networkManager).bridge$getTransactionStore();
    }

    public static void checkHandshakePhase(final EngineConnection connection) {
        if (!ConnectionUtil.isLoginPhase(connection)) {
            throw new IllegalStateException("This dispatcher may only be used for connections in the handshake phase.");
        }
    }

    public static void checkPlayPhase(final EngineConnection connection) {
        if (ConnectionUtil.isLoginPhase(connection)) {
            throw new IllegalStateException("This dispatcher may only be used for connections in the play phase.");
        }
    }

    private ConnectionUtil() {
    }
}
