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

import static org.spongepowered.common.util.SpongeCommonTranslationHelper.t;

import com.google.common.base.Optional;
import io.netty.buffer.Unpooled;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.component.tileentity.SignComponent;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.tileentity.SignChangeEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection {
    @Shadow private static Logger logger;

    @Shadow public NetworkManager netManager;

    @Shadow public EntityPlayerMP playerEntity;

    @Shadow private MinecraftServer serverController;

    @Shadow public abstract void sendPacket(final Packet packetIn);

    @Override
    public Player getPlayer() {
        return (Player) this.playerEntity;
    }

    @Override
    public InetSocketAddress getAddress() {
        return ((IMixinNetworkManager) this.netManager).getAddress();
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return ((IMixinNetworkManager) this.netManager).getVirtualHost();
    }

    @Override
    public int getPing() {
        return this.playerEntity.ping;
    }

    @Override
    public void sendCustomPayload(Object plugin, String channel, ChannelBuf dataStream) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void sendCustomPayload(Object plugin, String channel, byte[] data) {
        sendPacket(new S3FPacketCustomPayload(channel, new PacketBuffer(Unpooled.wrappedBuffer(data))));
    }

    /**
     * @Author Zidane
     *
     * Invoke before {@code System.arraycopy(packetIn.getLines(), 0, tileentitysign.signText, 0, 4);} (line 1156 in source) to call SignChangeEvent.
     * @param packetIn Injected packet param
     * @param ci Info to provide mixin on how to handle the callback
     * @param worldserver Injected world param
     * @param blockpos Injected blockpos param
     * @param tileentity Injected tilentity param
     * @param tileentitysign Injected tileentitysign param
     */
    @Inject(method = "processUpdateSign", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C12PacketUpdateSign;getLines()[Lnet/minecraft/util/IChatComponent;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void callSignChangeEvent(C12PacketUpdateSign packetIn, CallbackInfo ci, WorldServer worldserver, BlockPos blockpos, TileEntity tileentity, TileEntitySign tileentitysign) {
        ci.cancel();
        final Optional<SignComponent> existingSignData = ((Sign) tileentitysign).getData();
        if (!existingSignData.isPresent()) {
            // TODO Unsure if this is the best to do here...
            throw new RuntimeException("Critical error! Sign data not present on sign!");
        }
        final SignComponent changedSignData = existingSignData.get().copy();

        for (int i = 0; i < packetIn.getLines().length; i++) {
            changedSignData.setLine(i, SpongeTexts.toText(packetIn.getLines()[i]));
        }
        // I pass changedSignData in here twice to emulate the fact that even-though the current sign data doesn't have the lines from the packet
        // applied, this is what it "is" right now. If the data shown in the world is desired, it can be fetched from Sign.getData
        final SignChangeEvent event = SpongeEventFactory.createSignChange(Sponge.getGame(), new Cause(null, this.playerEntity, null), (Sign)
                tileentitysign, changedSignData, changedSignData);
        if (!Sponge.getGame().getEventManager().post(event)) {
            ((Sign) tileentitysign).offer(event.getNewData());
        } else {
            // If cancelled, I set the data back that was fetched from the sign. This means that if its a new sign, the sign will be empty else
            // it will be the text of the sign that was showing in the world
            ((Sign) tileentitysign).offer(existingSignData.get());
        }
        tileentitysign.markDirty();
        worldserver.markBlockForUpdate(blockpos);
    }

    /**
     * @author zml
     *
     * Purpose: replace the logic used for command blocks to make functional
     *
     * @param ci callback
     * @param packetIn method param
     */
    @Inject(method = "processVanilla250Packet", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "net/minecraft/network/PacketThreadUtil.checkThreadAndEnqueue(Lnet/minecraft/network/Packet;"
                    + "Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V"), cancellable = true)
    public void processCommandBlock(C17PacketCustomPayload packetIn, CallbackInfo ci) {
        if ("MC|AdvCdm".equals(packetIn.getChannelName())) {
            PacketBuffer packetbuffer;
            try {
                if (!this.serverController.isCommandBlockEnabled()) {
                    this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notEnabled", new Object[0]));
                    // Sponge: Check permissions for command block usage TODO: Maybe throw an event instead?
                    // } else if (this.playerEntity.canCommandSenderUseCommand(2, "") && this.playerEntity.capabilities.isCreativeMode) {
                } else {
                    packetbuffer = packetIn.getBufferData();

                    try {
                        byte b0 = packetbuffer.readByte();
                        CommandBlockLogic commandblocklogic = null;

                        String permissionCheck = null; // Sponge
                        if (b0 == 0) {
                            TileEntity tileentity = this.playerEntity.worldObj
                                    .getTileEntity(new BlockPos(packetbuffer.readInt(), packetbuffer.readInt(), packetbuffer.readInt()));

                            if (tileentity instanceof TileEntityCommandBlock) {
                                commandblocklogic = ((TileEntityCommandBlock) tileentity).getCommandBlockLogic();
                                permissionCheck = "minecraft.commandblock.edit.block." + commandblocklogic.getCommandSenderName(); // Sponge
                            }
                        } else if (b0 == 1) {
                            Entity entity = this.playerEntity.worldObj.getEntityByID(packetbuffer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklogic = ((EntityMinecartCommandBlock) entity).getCommandBlockLogic();
                                permissionCheck = "minecraft.commandblock.edit.minecart." + commandblocklogic.getCommandSenderName(); // Sponge
                            }
                            // Sponge begin
                        } else {
                            throw new IllegalArgumentException("Unknown command block type!");
                        }
                        Player spongePlayer = ((Player) this.playerEntity);
                        if (permissionCheck == null || !spongePlayer.hasPermission(permissionCheck)) {
                            spongePlayer.sendMessage(t("You do not have permission to edit this command block!").builder()
                                    .color(TextColors.RED).build());
                            return;
                            // Sponge end
                        }

                        String s1 = packetbuffer.readStringFromBuffer(packetbuffer.readableBytes());
                        boolean flag = packetbuffer.readBoolean();

                        if (commandblocklogic != null) {
                            commandblocklogic.setCommand(s1);
                            commandblocklogic.setTrackOutput(flag);

                            if (!flag) {
                                commandblocklogic.setLastOutput((IChatComponent) null);
                            }

                            commandblocklogic.func_145756_e();
                            this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", new Object[]{s1}));
                        }
                    } catch (Exception exception1) {
                        logger.error("Couldn\'t set command block", exception1);
                    } finally {
                        packetbuffer.release();
                    }
                /*} else { // Sponge: Give more accurate no permission message
                    this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notAllowed", new Object[0]));*/
                }
            } finally {
                ci.cancel();
            }
        }

    }

}
