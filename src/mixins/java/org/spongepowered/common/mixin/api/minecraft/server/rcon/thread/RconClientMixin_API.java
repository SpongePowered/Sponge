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
package org.spongepowered.common.mixin.api.minecraft.server.rcon.thread;

import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import net.minecraft.server.rcon.thread.GenericThread;
import net.minecraft.server.rcon.thread.RconClient;

@Mixin(RconClient.class)
public abstract class RconClientMixin_API extends GenericThread implements RemoteConnection {

    // @formatter:off
    @Shadow @Final private Socket client;
    // @formatter:on

    protected RconClientMixin_API(final String threadName) {
        super(threadName);
    }

    @Override
    public InetSocketAddress address() {
        return (InetSocketAddress) this.client.getRemoteSocketAddress();
    }

    @Override
    public InetSocketAddress virtualHost() {
        return (InetSocketAddress) this.client.getLocalSocketAddress();
    }

    @Override
    public void close() {
        try {
            this.client.close();
        } catch (final IOException ex) {
            SpongeCommon.getLogger().error("An error occurred while closing a RCON connection.", ex);
        }
    }
}
