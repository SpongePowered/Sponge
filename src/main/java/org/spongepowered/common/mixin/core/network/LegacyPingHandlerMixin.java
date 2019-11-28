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
package org.spongepowered.common.mixin.core.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.LegacyPingHandler;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.network.status.SpongeLegacyMinecraftVersion;
import org.spongepowered.common.network.status.SpongeStatusResponse;
import org.spongepowered.common.util.NetworkUtil;

import java.net.InetSocketAddress;

@Mixin(LegacyPingHandler.class)
public abstract class LegacyPingHandlerMixin extends ChannelInboundHandlerAdapter {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private NetworkSystem networkSystem;

    @Shadow private void writeAndFlush(final ChannelHandlerContext ctx, final ByteBuf data) { }
    @Shadow private ByteBuf getStringBuffer(final String string) {
        return null; // Shadowed
    }

    private ByteBuf buf;

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        this.buf = ctx.alloc().buffer();
    }

    @Override
    public void handlerRemoved(final ChannelHandlerContext ctx) throws Exception {
        if (this.buf != null) {
            this.buf.release();
            this.buf = null;
        }
    }

    /**
     * @author Minecrell - January 18th, 2015
     * @reason Implements our Ping Status Response API for legacy clients
     *     (Minecraft 1.6.4 or older). Also attempts to fix several issues with
     *     the packet deserialization.
     *
     * @param ctx The context
     * @param msg The message
     * @throws Exception For reasons unexplained
     */
    @Override
    @Overwrite
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        final ByteBuf m = (ByteBuf) msg;
        this.buf.writeBytes(m);
        m.release();

        this.buf.markReaderIndex();
        boolean result = false;

        try {
            result = impl$readLegacy(ctx, this.buf);
        } finally {
            this.buf.resetReaderIndex();
            if (!result) {
                final ByteBuf buf = this.buf;
                this.buf = null;

                ctx.pipeline().remove("legacy_query");
                ctx.fireChannelRead(buf);
            }
        }
    }

    private boolean impl$readLegacy(final ChannelHandlerContext ctx, final ByteBuf buf) {
        if (buf.readUnsignedByte() != 0xFE) {
            return false;
        }

        final MinecraftServer server = this.networkSystem.func_151267_d();
        final InetSocketAddress client = (InetSocketAddress) ctx.channel().remoteAddress();
        final ServerStatusResponse response;

        final int i = buf.readableBytes();
        switch (i) {
            case 0:
                LOGGER.debug("Ping: (<=1.3) from {}:{}", client.getAddress(), client.getPort());

                response = SpongeStatusResponse.postLegacy(server, client, SpongeLegacyMinecraftVersion.V1_3, null);
                if (response != null) {
                    this.writeResponse(ctx, String.format("%s§%d§%d",
                            SpongeStatusResponse.getUnformattedMotd(response),
                            response.func_151318_b().func_151333_b(),
                            response.func_151318_b().func_151332_a()));
                } else {
                    ctx.close();
                }

                break;
            case 1:
                if (buf.readUnsignedByte() != 0x01) {
                    return false;
                }

                LOGGER.debug("Ping: (1.4-1.5) from {}:{}", client.getAddress(), client.getPort());

                response = SpongeStatusResponse.postLegacy(server, client, SpongeLegacyMinecraftVersion.V1_5, null);
                if (response != null) {
                    this.writeResponse(ctx, String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            response.func_151322_c().func_151304_b(),
                            response.func_151322_c().func_151303_a(),
                            SpongeStatusResponse.getMotd(response),
                            response.func_151318_b().func_151333_b(),
                            response.func_151318_b().func_151332_a()));
                } else {
                    ctx.close();
                }

                break;
            default:
                if (buf.readUnsignedByte() != 0x01 || buf.readUnsignedByte() != 0xFA) {
                    return false;
                }
                if (!buf.isReadable(2)) {
                    break;
                }
                short length = buf.readShort();
                if (!buf.isReadable(length * 2)) {
                    break;
                }
                if (!buf.readBytes(length * 2).toString(Charsets.UTF_16BE).equals("MC|PingHost")) {
                    return false;
                }
                if (!buf.isReadable(2)) {
                    break;
                }
                length = buf.readShort();
                if (!buf.isReadable(length)) {
                    break;
                }

                final int protocol = buf.readUnsignedByte();
                length = buf.readShort();
                final String host = buf.readBytes(length * 2).toString(Charsets.UTF_16BE);
                final int port = buf.readInt();

                LOGGER.debug("Ping: (1.6) from {}:{}", client.getAddress(), client.getPort());

                response =
                        SpongeStatusResponse.postLegacy(server, client,
                                new SpongeLegacyMinecraftVersion(SpongeLegacyMinecraftVersion.V1_6, protocol),
                                InetSocketAddress.createUnresolved(NetworkUtil.cleanVirtualHost(host), port));
                if (response != null) {
                    this.writeResponse(ctx, String.format("§1\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d",
                            response.func_151322_c().func_151304_b(),
                            response.func_151322_c().func_151303_a(),
                            SpongeStatusResponse.getMotd(response),
                            response.func_151318_b().func_151333_b(),
                            response.func_151318_b().func_151332_a()));
                } else {
                    ctx.close();
                }

                break;
        }

        return true;
    }

    private void writeResponse(final ChannelHandlerContext ctx, final String response) {
        writeAndFlush(ctx, getStringBuffer(response));
    }

}
