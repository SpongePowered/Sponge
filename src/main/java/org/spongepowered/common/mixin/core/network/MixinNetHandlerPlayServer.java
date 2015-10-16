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
import net.minecraft.item.ItemBucket;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
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
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.living.player.ResourcePackStatusEvent.ResourcePackStatus;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.text.sink.MessageSinks;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.SpongeImplFactory;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.network.IMixinC08PacketPlayerBlockPlacement;
import org.spongepowered.common.text.SpongeTexts;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection {

    @Shadow private static Logger logger;
    @Shadow public NetworkManager netManager;
    @Shadow public EntityPlayerMP playerEntity;
    @Shadow private MinecraftServer serverController;

    @Shadow public abstract void sendPacket(final Packet packetIn);
    @Shadow private IntHashMap field_147372_n;

    private boolean justTeleported = false;
    private Location<World> lastMoveLocation = null;

    private final Map<String, ResourcePack> sentResourcePacks = new HashMap<String, ResourcePack>();

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
                SpongeEventFactory.createChangeSignEvent(Sponge.getGame(), Cause.of(this.playerEntity), changedSignData.asImmutable(),
                        changedSignData, (Sign) tileentitysign);
        if (!Sponge.getGame().getEventManager().post(event)) {
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
    public String dontNormalizeStringBecauseSomeMojangDevSucks(String input) {
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
            Location<World> to = new Location<World>(player.getWorld(), packetIn.getPositionX(), packetIn.getPositionY(), packetIn.getPositionZ());

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

            double deltaSquared = to.getPosition().distanceSquared(from.getPosition());
            double deltaAngleSquared = fromrot.distanceSquared(torot);

            // These magic numbers are sad but help prevent excessive lag from this event.
            // eventually it would be nice to not have them
            if (deltaSquared > ((1f / 16) * (1f / 16)) || deltaAngleSquared > (.15f * .15f)) {
                Transform<World> fromTransform = player.getTransform().setLocation(from).setRotation(fromrot);
                Transform<World> toTransform = player.getTransform().setLocation(to).setRotation(torot);
                DisplaceEntityEvent.Move.TargetPlayer event =
                        SpongeEventFactory.createDisplaceEntityEventMoveTargetPlayer(Sponge.getGame(), fromTransform, toTransform, player);
                Sponge.getGame().getEventManager().post(event);
                if (event.isCancelled()) {
                    player.setTransform(fromTransform);
                    this.lastMoveLocation = from;
                    ci.cancel();
                } else if (!event.getToTransform().equals(toTransform)) {
                    player.setTransform(event.getToTransform());
                    this.lastMoveLocation = event.getToTransform().getLocation();
                    ci.cancel();
                } else if (!from.equals(player.getLocation()) && this.justTeleported) {
                    this.lastMoveLocation = player.getLocation();
                    // Prevent teleports during the move event from causing odd behaviors
                    this.justTeleported = false;
                    ci.cancel();
                } else {
                    this.lastMoveLocation = event.getToTransform().getLocation();
                }
            }
        }
    }

    private ChatComponentTranslation tmpQuitMessage;

    /**
     * @author Simon816
     *
     * Store the quit message and ServerConfigurationManager instance for use in
     * {@link #onDisconnectPlayer}.
     */
    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;sendChatMsg(Lnet/minecraft/util/IChatComponent;)V"))
    public void onSendChatMsgCall(ServerConfigurationManager thisCtx, IChatComponent chatcomponenttranslation) {
        this.tmpQuitMessage = (ChatComponentTranslation) chatcomponenttranslation;
    }

    /**
     * @author Simon816
     *
     * Fire the PlayerQuitEvent before playerLoggedOut is called in order for
     * event handlers to change the quit message captured from
     * {@link #onSendChatMsgCall}.
     */
    @Inject(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/ServerConfigurationManager;playerLoggedOut(Lnet/minecraft/entity/player/EntityPlayerMP;)V"))
    public void onDisconnectPlayer(IChatComponent reason, CallbackInfo ci) {
        Text message = SpongeTexts.toText(this.tmpQuitMessage);
        Text newMessage = Texts.of(message);
        Player player = (Player) this.playerEntity;
        Set<CommandSource> sources = new HashSet<CommandSource>();
        sources.add(player);
        MessageSink originalSink = MessageSinks.to(sources);
        ClientConnectionEvent.Disconnect event =
                SpongeImplFactory.createClientConnectionEventDisconnect(Sponge.getGame(), Cause.of(player), message, newMessage,
                        originalSink, player.getMessageSink(), player);
        this.tmpQuitMessage = null;
        Sponge.getGame().getEventManager().post(event);
        event.getSink().sendMessage(event.getMessage());
    }

    @Inject(method = "handleResourcePackStatus", at = @At("HEAD"))
    public void onResourcePackStatus(C19PacketResourcePackStatus packet, CallbackInfo ci) {
        String hash = packet.hash;
        ResourcePackStatus status;
        ResourcePack pack = this.sentResourcePacks.get(hash);
        switch (packet.status) {
            case ACCEPTED:
                status = ResourcePackStatus.ACCEPTED;
                break;
            case DECLINED:
                status = ResourcePackStatus.DECLINED;
                break;
            case SUCCESSFULLY_LOADED:
                status = ResourcePackStatus.SUCCESSFULLY_LOADED;
                break;
            case FAILED_DOWNLOAD:
                status = ResourcePackStatus.FAILED;
                break;
            default:
                throw new AssertionError();
        }
        if (status.wasSuccessful().isPresent()) {
            this.sentResourcePacks.remove(hash);
        }
        Sponge.getGame().getEventManager()
                .post(SpongeEventFactory.createResourcePackStatusEvent(Sponge.getGame(), pack, (Player)this.playerEntity, status));
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
        if (packetIn.getPlacedBlockDirection() == 255) {
            if (packetIn.getStack() != null && packetIn.getStack().getItem() == this.lastItem && this.lastPacket != null && ((IMixinC08PacketPlayerBlockPlacement)packetIn).getTimeStamp() - this.lastPacket < 100) {
                this.lastPacket = null;
                ci.cancel();
            }
        } else {
            this.lastItem = null;
            if (packetIn.getStack() != null) {
                // ignore placement of liquids
                if (!(packetIn.getStack().getItem() instanceof ItemBucket)) {
                    this.lastItem = packetIn.getStack().getItem() ;
                    this.lastPacket = ((IMixinC08PacketPlayerBlockPlacement)packetIn).getTimeStamp();
                }
            }
        }
    }

//    @Overwrite
//    public void processClickWindow(C0EPacketClickWindow packetIn) {
//        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (INetHandler) this, this.playerEntity.getServerForPlayer());
//        this.playerEntity.markPlayerActive();
//
//        if (this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity))
//        {
//            if (this.playerEntity.isSpectator())
//            {
//                ArrayList arraylist = Lists.newArrayList();
//
//                for (int i = 0; i < this.playerEntity.openContainer.inventorySlots.size(); ++i)
//                {
//                    arraylist.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(i)).getStack());
//                }
//
//                this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, arraylist);
//            }
//            else
//            {
//                ItemStack clientStack = packetIn.getClickedItem();
//                if (clientStack == null) {
//                    clientStack = (ItemStack) ItemStackSnapshot.NONE.createStack();
//                }
//                ItemStack serverStack = this.playerEntity.openContainer.getSlot(packetIn.getSlotId()).getStack();
//                if (serverStack == null) {
//                    serverStack = (ItemStack) ItemStackSnapshot.NONE.createStack();
//                }
//
//                // Let API set the stack before running Vanilla click logic
//                final Transaction<ItemStackSnapshot> transaction = new Transaction<>(((org.spongepowered.api.item.inventory.ItemStack)
//                        serverStack).createSnapshot(), ItemStackSnapshot.NONE);
//                final InteractInventoryEvent.Click event = SpongeEventFactory.createInteractInventoryEventClick(Sponge.getGame(), Cause.of
//                        (playerEntity), transaction, (org.spongepowered.api.item.inventory.Slot) this.playerEntity.openContainer.getSlot(packetIn
//                        .getSlotId()));
//                Sponge.getGame().getEventManager().post(event);
//                ItemStack pluginStack = (ItemStack) event.getItemStackTransaction().getFinal().createStack();
//                System.out.println("Client Stack: " + clientStack);
//                System.out.println("Server Stack: " + serverStack);
//                System.out.println("Plugin Stack: " + pluginStack);
//                if (pluginStack.getItem().getUnlocalizedName().equals("none")) {
//                    pluginStack = null;
//                }
//                if (event.getItemStackTransaction().getCustom().isPresent()) {
//                    this.playerEntity.inventory.setInventorySlotContents(packetIn.getSlotId(), null);
//                    this.playerEntity.inventory.setItemStack(pluginStack);
//                }
//
//                // Run click logic
//                ItemStack postLogicStack = this.playerEntity.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getMode
//                        (), this.playerEntity);
//                System.out.println("Post Logic Stack: " + postLogicStack);
//                if (ItemStack.areItemStacksEqual(pluginStack, postLogicStack))
//                {
//                    this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
//                    this.playerEntity.isChangingQuantityOnly = true;
//                    this.playerEntity.openContainer.detectAndSendChanges();
//                    this.playerEntity.updateHeldItem();
//                    this.playerEntity.isChangingQuantityOnly = false;
//                } else {
//                    this.field_147372_n.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(packetIn.getActionNumber()));
//                    this.playerEntity.playerNetServerHandler.sendPacket(
//                            new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
//                    this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
//                    ArrayList arraylist1 = Lists.newArrayList();
//
//                    for (int j = 0; j < this.playerEntity.openContainer.inventorySlots.size(); ++j)
//                    {
//                        arraylist1.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(j)).getStack());
//                    }
//
//                    this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, arraylist1);
//                }
//            }
//        }
//    }
}
