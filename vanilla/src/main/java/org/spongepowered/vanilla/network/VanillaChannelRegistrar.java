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
package org.spongepowered.vanilla.network;

import com.google.inject.Singleton;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelRegistrationException;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;

@Singleton
public final class VanillaChannelRegistrar extends SpongeNetworkManager {

    @Override
    public ChannelBinding.IndexedMessageChannel createChannel(PluginContainer plugin, ResourceKey channelKey) throws ChannelRegistrationException {
        return null;
    }

    @Override
    public ChannelBinding.RawDataChannel createRawChannel(PluginContainer plugin, ResourceKey channelKey) throws ChannelRegistrationException {
        return null;
    }

    @Override
    public Optional<ChannelBinding> getChannel(ResourceKey channelkey) {
        return Optional.empty();
    }

    @Override
    public void unbindChannel(ChannelBinding channel) {

    }

    @Override
    public Set<ResourceKey> getRegisteredChannels(Platform.Type side) {
        return null;
    }

    @Override
    public boolean isChannelAvailable(ResourceKey channelKey) {
        return false;
    }
}
