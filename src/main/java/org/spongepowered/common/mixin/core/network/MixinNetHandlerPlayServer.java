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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.network.IMixinC08PacketPlayerBlockPlacement;
import org.spongepowered.common.network.PacketUtil;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection {

    @Shadow @Final private static Logger logger;
    @Shadow @Final public NetworkManager netManager;
    @Shadow @Final private MinecraftServer serverController;
    @Shadow public EntityPlayerMP playerEntity;
    @Shadow private IntHashMap field_147372_n;

    @Shadow public abstract void sendPacket(final Packet packetIn);

    private boolean justTeleported = false;
    private Location<World> lastMoveLocation = null;

    private final Map<String, ResourcePack> sentResourcePacks = new HashMap<>();

    private Long lastPacket;
    // Store the last block right-clicked
    private Item lastItem;

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
        if (!PacketUtil.processSignPacket(packetIn, ci, tileentitysign, this.playerEntity)) {
            return;
        }
        final Optional<SignData> existingSignData = ((Sign) tileentitysign).get(SignData.class);
        if (!existingSignData.isPresent()) {
            // TODO Unsure if this is the best to do here...
            throw new RuntimeException("Critical error! Sign data not present on sign!");
        }
        final SignData changedSignData = existingSignData.get().copy();
        final ListValue<Text> lines = changedSignData.lines();
        for (int i = 0; i < packetIn.getLines().length; i++) {
            lines.set(i, SpongeTexts.toText(packetIn.getLines()[i]));
        }
        changedSignData.set(lines);
        // I pass changedSignData in here twice to emulate the fact that even-though the current sign data doesn't have the lines from the packet
        // applied, this is what it "is" right now. If the data shown in the world is desired, it can be fetched from Sign.getData
        final ChangeSignEvent event =
                SpongeEventFactory.createChangeSignEvent(Cause.of(NamedCause.source(this.playerEntity)),
                    changedSignData.asImmutable(), changedSignData, (Sign) tileentitysign);
        if (!SpongeImpl.postEvent(event)) {
            ((Sign) tileentitysign).offer(event.getText());
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
                                permissionCheck = "minecraft.commandblock.edit.block." + commandblocklogic.getName(); // Sponge
                            }
                        } else if (b0 == 1) {
                            Entity entity = this.playerEntity.worldObj.getEntityByID(packetbuffer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklogic = ((EntityMinecartCommandBlock) entity).getCommandBlockLogic();
                                permissionCheck = "minecraft.commandblock.edit.minecart." + commandblocklogic.getName(); // Sponge
                            }
                            // Sponge begin
                        } else {
                            throw new IllegalArgumentException("Unknown command block type!");
                        }
                        Player spongePlayer = ((Player) this.playerEntity);
                        if (permissionCheck == null || !spongePlayer.hasPermission(permissionCheck)) {
                            spongePlayer.sendMessage(t("You do not have permission to edit this command block!").toBuilder()
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

                            commandblocklogic.updateCommand();
                            this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", new Object[] {s1}));
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

    @Redirect(method = "processChatMessage", at = @At(value = "INVOKE", target = "org.apache.commons.lang3.StringUtils.normalizeSpace"
            + "(Ljava/lang/String;)Ljava/lang/String;", remap = false))
    public String onNormalizeSpace(String input) {
        return input;
    }

    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At(value = "RETURN"))
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<?> relativeSet, CallbackInfo ci) {
        this.justTeleported = true;
    }

    @Inject(method = "processPlayer", at = @At(value = "FIELD", target = "net.minecraft.network.NetHandlerPlayServer.hasMoved:Z", ordinal = 2), cancellable = true)
    public void proccesPlayerMoved(C03PacketPlayer packetIn, CallbackInfo ci){
        if (packetIn.isMoving() || packetIn.getRotating() && !this.playerEntity.isDead) {
            Player player = (Player) this.playerEntity;
            Vector3d fromrot = player.getRotation();

            // If Sponge used the player's current location, the delta might never be triggered which could be exploited
            Location<World> from = player.getLocation();
            if (this.lastMoveLocation != null) {
                from = this.lastMoveLocation;
            }

            Vector3d torot = new Vector3d(packetIn.getPitch(), packetIn.getYaw(), 0);
            Location<World> to = new Location<>(player.getWorld(), packetIn.getPositionX(), packetIn.getPositionY(), packetIn.getPositionZ());

            // Minecraft sends a 0, 0, 0 position when rotation only update occurs, this needs to be recognized and corrected
            boolean rotationOnly = !packetIn.isMoving() && packetIn.getRotating();
            if (rotationOnly) {
                // Correct the to location so it's not misrepresented to plugins, only when player rotates without moving
                // In this case it's only a rotation update, which isn't related to the to location
                from = player.getLocation();
                to = from;
            }

            // Minecraft does the same with rotation when it's only a positional update
            boolean positionOnly = packetIn.isMoving() && !packetIn.getRotating();
            if (positionOnly) {
                // Correct the new rotation to match the old rotation
                torot = fromrot;
            }

            ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(to.getPosition().sub(from.getPosition()));

            double deltaSquared = to.getPosition().distanceSquared(from.getPosition());
            double deltaAngleSquared = fromrot.distanceSquared(torot);

            // These magic numbers are sad but help prevent excessive lag from this event.
            // eventually it would be nice to not have them
            if (deltaSquared > ((1f / 16) * (1f / 16)) || deltaAngleSquared > (.15f * .15f)) {
                Transform<World> fromTransform = player.getTransform().setLocation(from).setRotation(fromrot);
                Transform<World> toTransform = player.getTransform().setLocation(to).setRotation(torot);
                DisplaceEntityEvent.Move.TargetPlayer event =
                        SpongeEventFactory.createDisplaceEntityEventMoveTargetPlayer(Cause.of(NamedCause.source(player)), fromTransform, toTransform, player);
                SpongeImpl.postEvent(event);
                if (event.isCancelled()) {
                    player.setTransform(fromTransform);
                    this.lastMoveLocation = from;
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    ci.cancel();
                } else if (!event.getToTransform().equals(toTransform)) {
                    player.setTransform(event.getToTransform());
                    this.lastMoveLocation = event.getToTransform().getLocation();
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    ci.cancel();
                } else if (!from.equals(player.getLocation()) && this.justTeleported) {
                    this.lastMoveLocation = player.getLocation();
                    // Prevent teleports during the move event from causing odd behaviors
                    this.justTeleported = false;
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    ci.cancel();
                } else {
                    this.lastMoveLocation = event.getToTransform().getLocation();
                }
            }
        }
    }

    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;sendChatMsg(Lnet/minecraft/util/IChatComponent;)V"))
    public void onDisconnectHandler(ServerConfigurationManager this$0, IChatComponent component) {
        final Player player = ((Player) this.playerEntity);
        final Text message = SpongeTexts.toText(component);
        final MessageChannel originalChannel = player.getMessageChannel();
        final ClientConnectionEvent.Disconnect event = SpongeImplHooks.createClientConnectionEventDisconnect(
                Cause.of(NamedCause.source(player)), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(message),
                player, false
        );
        SpongeImpl.postEvent(event);
        if (!event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
    }

    @Inject(method = "handleResourcePackStatus", at = @At("HEAD"))
    public void onResourcePackStatus(C19PacketResourcePackStatus packet, CallbackInfo ci) {
        String hash = packet.hash;
        ResourcePackStatusEvent.ResourcePackStatus status;
        ResourcePack pack = this.sentResourcePacks.get(hash);
        switch (packet.status) {
            case ACCEPTED:
                status = ResourcePackStatusEvent.ResourcePackStatus.ACCEPTED;
                break;
            case DECLINED:
                status = ResourcePackStatusEvent.ResourcePackStatus.DECLINED;
                break;
            case SUCCESSFULLY_LOADED:
                status = ResourcePackStatusEvent.ResourcePackStatus.SUCCESSFULLY_LOADED;
                break;
            case FAILED_DOWNLOAD:
                status = ResourcePackStatusEvent.ResourcePackStatus.FAILED;
                break;
            default:
                throw new AssertionError();
        }
        if (status.wasSuccessful().isPresent()) {
            this.sentResourcePacks.remove(hash);
        }
        SpongeImpl.postEvent(SpongeEventFactory.createResourcePackStatusEvent(Cause.of(NamedCause.source(SpongeImpl.getGame())), pack,
            (Player)this.playerEntity, status));
    }

    @Inject(method = "sendPacket", at = @At("HEAD"))
    public void onResourcePackSend(Packet packet, CallbackInfo ci) {
        if (packet instanceof IMixinPacketResourcePackSend) {
            IMixinPacketResourcePackSend p = (IMixinPacketResourcePackSend) packet;
            this.sentResourcePacks.put(p.setFakeHash(), p.getResourcePack());
        }
    }

    @Inject(method = "processPlayerBlockPlacement", at = @At("HEAD"), cancellable = true)
    public void injectBlockPlacement(C08PacketPlayerBlockPlacement packetIn, CallbackInfo ci) {
        // This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
        // aimed at a block. We shouldn't need to get the second packet if the data is handled
        // but we cannot know what the client will do, so we might still get it
        //
        // If the time between packets is small enough, and the 'signature' similar, we discard the
        // second one. This sadly has to remain until Mojang makes their packets saner. :(
        //  -- Grum
        if (!MinecraftServer.getServer().isCallingFromMinecraftThread()) {
            if (packetIn.getPlacedBlockDirection() == 255) {
                if (packetIn.getStack() != null && packetIn.getStack().getItem() == this.lastItem && this.lastPacket != null && ((IMixinC08PacketPlayerBlockPlacement)packetIn).getTimeStamp() - this.lastPacket < 100) {
                    this.lastPacket = null;
                    ci.cancel();
                }
            } else {
                this.lastItem = packetIn.getStack() == null ? null : packetIn.getStack().getItem();
                this.lastPacket = ((IMixinC08PacketPlayerBlockPlacement)packetIn).getTimeStamp();
            }
        }
    }

    @Inject(method = "processClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;slotClick(IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    public void onBeforeSlotClick(C0EPacketClickWindow packetIn, CallbackInfo ci) {
        ((IMixinContainer) this.playerEntity.openContainer).setCaptureInventory(true);
    }

    @Inject(method = "processClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;updateCraftingInventory(Lnet/minecraft/inventory/Container;Ljava/util/List;)V", shift = At.Shift.AFTER))
    public void onAfterSecondUpdateCraftingInventory(C0EPacketClickWindow packetIn, CallbackInfo ci) {
        ((IMixinContainer) this.playerEntity.openContainer).setCaptureInventory(false);
    }

    @Inject(method = "processClickWindow", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;isChangingQuantityOnly:Z", shift = At.Shift.AFTER, ordinal = 1))
    public void onThirdUpdateCraftingInventory(C0EPacketClickWindow packetIn, CallbackInfo ci) {
        ((IMixinContainer) this.playerEntity.openContainer).setCaptureInventory(false);
    }

    @Inject(method = "processCreativeInventoryAction", at = @At(value = "HEAD"))
    public void onProcessCreativeInventoryActionHead(C10PacketCreativeInventoryAction packetIn, CallbackInfo ci) {
        ((IMixinContainer) this.playerEntity.inventoryContainer).setCaptureInventory(true);
    }

    @Inject(method = "processCreativeInventoryAction", at = @At(value = "RETURN"))
    public void onProcessCreativeInventoryActionReturn(C10PacketCreativeInventoryAction packetIn, CallbackInfo ci) {
        ((IMixinContainer) this.playerEntity.inventoryContainer).setCaptureInventory(false);
    }

    @Inject(method = "processHeldItemChange", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/C09PacketHeldItemChange;getSlotId()I", ordinal = 2), cancellable = true)
    public void onGetSlotId(C09PacketHeldItemChange packetIn, CallbackInfo ci) {
        SpongeCommonEventFactory.callChangeInventoryHeldEvent(this.playerEntity, packetIn);
        ci.cancel();
    }

    @Redirect(method = "processUseEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V"))
    public void onAttackTargetEntity(EntityPlayerMP player, net.minecraft.entity.Entity entityIn) {
        if (entityIn instanceof Player && !((World) player.worldObj).getProperties().isPVPEnabled()) {
            return; // PVP is disabled, ignore
        }

        InteractEntityEvent.Primary event = SpongeEventFactory.createInteractEntityEventPrimary(
            Cause.of(NamedCause.source(this.playerEntity)), Optional.empty(), (org.spongepowered.api.entity.Entity) entityIn);
        SpongeImpl.postEvent(event);
        if (!event.isCancelled()) {
            player.attackTargetEntityWithCurrentItem(entityIn);
        }
    }

    // Format disconnection message properly, causes weird messages with our console colors
    // Also see https://bugs.mojang.com/browse/MC-59535
    @Redirect(method = "onDisconnect(Lnet/minecraft/util/IChatComponent;)V", at = @At(value = "INVOKE",
            target = "Ljava/lang/StringBuilder;append(Ljava/lang/Object;)Ljava/lang/StringBuilder;", ordinal = 0, remap = false))
    private StringBuilder onDisconnectReasonToString(StringBuilder builder, Object reason) {
        return builder.append(SpongeTexts.toLegacy((IChatComponent) reason));
    }

}
