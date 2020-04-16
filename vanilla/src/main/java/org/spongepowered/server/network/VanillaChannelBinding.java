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
package org.spongepowered.server.network;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.network.SpongeNetworkManager;

public abstract class VanillaChannelBinding extends SpongeNetworkManager.AbstractChannelBinding {

    private boolean valid = true;

    public VanillaChannelBinding(ChannelRegistrar registrar, String channelName, PluginContainer owner) {
        super(registrar, channelName, owner);
    }

    protected void validate() {
        checkState(this.valid, "Channel binding in invalid state (was it unbound?)");
    }

    final void invalidate() {
        this.valid = false;
    }

    public abstract void post(RemoteConnection connection, PacketBuffer payload);

}
