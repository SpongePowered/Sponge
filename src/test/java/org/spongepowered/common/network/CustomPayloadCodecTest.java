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

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.server.MinecraftServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;

/**
 * The Sponge channel codecs are appended to the payload codecs vanilla
 * registers, so the vanilla ones still have to be there.
 */
public final class CustomPayloadCodecTest {

    private static final BrandPayload BRAND = new BrandPayload("sponge");

    @Test
    public void serverboundBrandKeepsItsCodec() {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerboundCustomPayloadPacket.STREAM_CODEC.encode(buffer, new ServerboundCustomPayloadPacket(CustomPayloadCodecTest.BRAND));
        Assertions.assertEquals(CustomPayloadCodecTest.BRAND, ServerboundCustomPayloadPacket.STREAM_CODEC.decode(buffer).payload());
    }

    @Test
    public void clientboundGameplayBrandKeepsItsCodec() {
        final RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), ((MinecraftServer) Sponge.server()).registryAccess());
        ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.encode(buffer, new ClientboundCustomPayloadPacket(CustomPayloadCodecTest.BRAND));
        Assertions.assertEquals(CustomPayloadCodecTest.BRAND, ClientboundCustomPayloadPacket.GAMEPLAY_STREAM_CODEC.decode(buffer).payload());
    }

    @Test
    public void clientboundConfigurationBrandKeepsItsCodec() {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.encode(buffer, new ClientboundCustomPayloadPacket(CustomPayloadCodecTest.BRAND));
        Assertions.assertEquals(CustomPayloadCodecTest.BRAND, ClientboundCustomPayloadPacket.CONFIG_STREAM_CODEC.decode(buffer).payload());
    }
}
