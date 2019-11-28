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
package org.spongepowered.common.network;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.netty.buffer.Unpooled.wrappedBuffer;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;

import java.util.Optional;

public abstract class SpongeNetworkManager implements ChannelRegistrar {

    protected PluginContainer checkCreateChannelArgs(Object plugin, String channel) {
        if (checkNotNull(channel, "channel").length() > 20) {
            throw new ChannelRegistrationException("Channel name cannot be greater than 20 characters");
        }
        Optional<PluginContainer> optPlugin = SpongeImpl.getGame().getPluginManager().fromInstance(checkNotNull(plugin, "plugin"));
        checkArgument(optPlugin.isPresent(), "Provided plugin argument is not a plugin instance");
        return optPlugin.get();
    }

    protected static SCustomPayloadPlayPacket getRegPacket(String channelName) {
        return new SCustomPayloadPlayPacket("REGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    protected static SCustomPayloadPlayPacket getUnregPacket(String channelName) {
        return new SCustomPayloadPlayPacket("UNREGISTER", new PacketBuffer(wrappedBuffer(channelName.getBytes(Charsets.UTF_8))));
    }

    public static ChannelBuf toChannelBuf(ByteBuf buf) {
        return (ChannelBuf) (buf instanceof PacketBuffer ? buf : new PacketBuffer(buf));
    }

    public static abstract class AbstractChannelBinding implements ChannelBinding {

        private final ChannelRegistrar registrar;
        private final String channelName;
        private final PluginContainer owner;

        public AbstractChannelBinding(ChannelRegistrar registrar, String channelName, PluginContainer owner) {
            this.registrar = registrar;
            this.channelName = channelName;
            this.owner = owner;
        }

        @Override
        public ChannelRegistrar getRegistrar() {
            return this.registrar;
        }

        @Override
        public String getName() {
            return this.channelName;
        }

        @Override
        public PluginContainer getOwner() {
            return this.owner;
        }
    }
}
