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

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class ChannelUtils {
    public static final CustomPacketPayload.Type<SpongeChannelPayload> REGISTER = CustomPacketPayload.createType(Constants.Channels.REGISTER_KEY.toString());

    public static ArrayList spongeChannelCodecs(final int maxPayloadSize) {
        ArrayList channels = new ArrayList<>();
        channels.add(new CustomPacketPayload.TypeAndCodec<>(ChannelUtils.REGISTER, SpongeChannelPayload.streamCodec(ChannelUtils.REGISTER, maxPayloadSize)));
        Sponge.game().channelManager().channels().forEach(c ->
                channels.add(new CustomPacketPayload.TypeAndCodec<>(((SpongeChannel) c).payloadType(), SpongeChannelPayload.streamCodec(((SpongeChannel) c).payloadType(), maxPayloadSize))));
        return channels;
    }

    private ChannelUtils() {
    }

}
