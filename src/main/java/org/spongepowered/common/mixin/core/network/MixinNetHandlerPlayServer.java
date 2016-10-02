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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.network.PacketUtil;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements PlayerConnection, IMixinNetHandlerPlayServer {

    private static final String UPDATE_SIGN = "Lnet/minecraft/network/play/client/CPacketUpdateSign;getLines()[Ljava/lang/String;";

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public NetworkManager netManager;
    @Shadow @Final private MinecraftServer serverController;
    @Shadow @Final private IntHashMap<Short> pendingTransactions;
    @Shadow public EntityPlayerMP playerEntity;
    @Shadow private int itemDropThreshold;
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow private double lastGoodX;
    @Shadow private double lastGoodY;
    @Shadow private double lastGoodZ;
    @Shadow private int lastPositionUpdate;
    @Shadow private Vec3d targetPos;
    @Shadow private int networkTickCount;
    @Shadow private int movePacketCounter;
    @Shadow private int lastMovePacketCounter;
    @Shadow private boolean floating;


    @Shadow public abstract void sendPacket(final Packet<?> packetIn);
    @Shadow public abstract void kickPlayerFromServer(String reason);
    @Shadow private void captureCurrentPosition() {}
    @Shadow public abstract void setPlayerLocation(double x, double y, double z, float yaw, float pitch);
    @Shadow private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) { return false; } // Shadowed

    private boolean justTeleported = false;
    @Nullable private Location<World> lastMoveLocation = null;

    private final Map<String, ResourcePack> sentResourcePacks = new HashMap<>();

    // Store the last block right-clicked
    private boolean allowClientLocationUpdate = true;
    @Nullable private Item lastItem;

    @Override
    public void captureCurrentPlayerPosition() {
        this.captureCurrentPosition();
    }

    @Override
    public Map<String, ResourcePack> getSentResourcePacks() {
        return this.sentResourcePacks;
    }

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
    public int getLatency() {
        return this.playerEntity.ping;
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;onUpdateEntity()V"))
    private void onPlayerTick(EntityPlayerMP playerEntity) {
        if (playerEntity.worldObj.isRemote || !CauseTracker.ENABLED) {
            playerEntity.onUpdateEntity();
            return;
        }
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) playerEntity.worldObj;
        final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
        causeTracker.switchToPhase(TickPhase.Tick.PLAYER, PhaseContext.start()
                .add(NamedCause.source(playerEntity))
                .addCaptures()
                .addEntityDropCaptures()
                //.addBlockCaptures()
                .complete());
        for (WorldServer worldServer : WorldManager.getWorlds()) {
            if (worldServer == mixinWorldServer) { // we don't care about entering the phase for this world server of which we already entered
                continue;
            }
            final IMixinWorldServer otherMixinWorldServer = (IMixinWorldServer) worldServer;
            otherMixinWorldServer.getCauseTracker().switchToPhase(TickPhase.Tick.PLAYER, PhaseContext.start()
                    .add(NamedCause.source(playerEntity))
                    .addCaptures()
                    .addEntityDropCaptures()
                    //.addBlockCaptures()
                    .complete());
        }
        playerEntity.onUpdateEntity();
        causeTracker.completePhase();
        for (WorldServer worldServer : WorldManager.getWorlds()) {
            if (worldServer == mixinWorldServer) { // we don't care about entering the phase for this world server of which we already entered
                continue;
            }
            final IMixinWorldServer otherMixinWorldServer = (IMixinWorldServer) worldServer;
            otherMixinWorldServer.getCauseTracker().completePhase();
        }
    }

    @Override
    public void setAllowClientLocationUpdate(boolean flag) {
        this.allowClientLocationUpdate = flag;
    }

    /**
     * @param manager The player network connection
     * @param packet The original packet to be sent
     * @author kashike
     */
    @Redirect(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void onSendPacket(NetworkManager manager, Packet<?> packet) {
        if (!this.allowClientLocationUpdate && packet instanceof SPacketPlayerPosLook) {
            return;
        }
        manager.sendPacket(this.rewritePacket(packet));
    }

    /**
     * This method wraps packets being sent to perform any additional actions,
     * such as rewriting data in the packet.
     *
     * @param packetIn The original packet to be sent
     * @return The rewritten packet if we performed any changes, or the original
     *     packet if we did not perform any changes
     * @author kashike
     */
    private Packet<?> rewritePacket(final Packet<?> packetIn) {
        // Update the tab list data
        if (packetIn instanceof SPacketPlayerListItem) {
            ((SpongeTabList) ((Player) this.playerEntity).getTabList()).updateEntriesOnSend((SPacketPlayerListItem) packetIn);
        }
        // Store the resource pack for use when processing resource pack statuses
        else if (packetIn instanceof IMixinPacketResourcePackSend) {
            IMixinPacketResourcePackSend packet = (IMixinPacketResourcePackSend) packetIn;
            this.sentResourcePacks.put(packet.setFakeHash(), packet.getResourcePack());
        }

        return packetIn;
    }

    /**
     * @author Zidane
     *
     * Invoke before {@code System.arraycopy(packetIn.getLines(), 0, tileentitysign.signText, 0, 4);} (line 1156 in source) to call SignChangeEvent.
     * @param packetIn Injected packet param
     * @param ci Info to provide mixin on how to handle the callback
     * @param worldserver Injected world param
     * @param blockpos Injected blockpos param
     * @param tileentity Injected tilentity param
     * @param tileentitysign Injected tileentitysign param
     */
    @Inject(method = "processUpdateSign", at = @At(value = "INVOKE", target = UPDATE_SIGN), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void callSignChangeEvent(CPacketUpdateSign packetIn, CallbackInfo ci, WorldServer worldserver, BlockPos blockpos, IBlockState iblockstate, TileEntity tileentity, TileEntitySign tileentitysign) {
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
            lines.set(i, SpongeTexts.toText(new TextComponentString(packetIn.getLines()[i])));
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
        worldserver.getPlayerChunkMap().markBlockForUpdate(blockpos);
    }

    /**
     * @author blood - June 6th, 2016
     * @author gabizou - June 20th, 2016 - Update for 1.9.4 and minor refactors.
     * @reason Since mojang handles creative packets different than survival, we need to
     * restructure this method to prevent any packets being sent to client as we will
     * not be able to properly revert them during drops.
     *
     * @param packetIn The creative inventory packet
     */
    @Overwrite
    public void processCreativeInventoryAction(CPacketCreativeInventoryAction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayServer) (Object) this, this.playerEntity.getServerWorld());

        if (this.playerEntity.interactionManager.isCreative()) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) this.playerEntity.getServerWorld();
            final PhaseData peek = mixinWorldServer.getCauseTracker().getCurrentPhaseData();
            final PhaseContext context = peek.context;
            final boolean ignoresCreative = context.firstNamed(InternalNamedCauses.Packet.IGNORING_CREATIVE, Boolean.class).get();
            boolean clickedOutside = packetIn.getSlotId() < 0;
            ItemStack itemstack = packetIn.getStack();

            if (itemstack != null && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
                NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

                if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
                    BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                    TileEntity tileentity = this.playerEntity.worldObj.getTileEntity(blockpos);

                    if (tileentity != null) {
                        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                        tileentity.writeToNBT(nbttagcompound1);
                        nbttagcompound1.removeTag("x");
                        nbttagcompound1.removeTag("y");
                        nbttagcompound1.removeTag("z");
                        itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
                    }
                }
            }

            boolean clickedHotbar = packetIn.getSlotId() >= 1 && packetIn.getSlotId() <= 45;
            boolean itemValidCheck = itemstack == null || itemstack.getItem() != null;
            boolean itemValidCheck2 = itemstack == null || itemstack.getMetadata() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;

            // Sponge start - handle CreativeInventoryEvent
            if (itemValidCheck && itemValidCheck2) {
                if (!ignoresCreative) {
                    ClickInventoryEvent.Creative clickEvent = SpongeCommonEventFactory.callCreativeClickInventoryEvent(this.playerEntity, packetIn);
                    if (clickEvent.isCancelled()) {
                        // Reset slot on client
                        if (packetIn.getSlotId() >= 0) {
                            this.playerEntity.connection.sendPacket(
                                    new SPacketSetSlot(this.playerEntity.inventoryContainer.windowId, packetIn.getSlotId(),
                                            this.playerEntity.inventoryContainer.getSlot(packetIn.getSlotId()).getStack()));
                            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, null));
                        }
                        return;
                    }
                }

                if (clickedHotbar) {
                    if (itemstack == null) {
                        this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), null);
                    } else {
                        this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
                    }

                    this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
                } else if (clickedOutside && this.itemDropThreshold < 200) {
                    this.itemDropThreshold += 20;
                    EntityItem entityitem = this.playerEntity.dropItem(itemstack, true);

                    if (entityitem != null)
                    {
                        entityitem.setAgeToCreativeDespawnTime();
                    }
                }
            }
            // Sponge end
        }
    }

    @Redirect(method = "processChatMessage", at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/StringUtils;normalizeSpace(Ljava/lang/String;)Ljava/lang/String;", remap = false))
    public String onNormalizeSpace(String input) {
        return input;
    }

    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At(value = "RETURN"))
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<?> relativeSet, CallbackInfo ci) {
        this.justTeleported = true;
    }

    /**
     * @author gabizou - June 22nd, 2016
     * @reason Sponge has to throw the movement events before we consider moving the player and there's
     * no clear way to go about it with the target position being null and the last position update checks.
     * @param packetIn
     */
    @Redirect(method = "processPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;playerConqueredTheEnd:Z"))
    private boolean throwMoveEvent(EntityPlayerMP playerMP, CPacketPlayer packetIn) {
        if (!playerMP.playerConqueredTheEnd) {

            // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
            // We don't fire an event to avoid confusing plugins.
            if (!packetIn.moving && !packetIn.rotating) {
                return playerMP.playerConqueredTheEnd;
            }

            // Sponge Start - Movement event
            Player player = (Player) this.playerEntity;
            IMixinEntityPlayerMP mixinPlayer = (IMixinEntityPlayerMP) this.playerEntity;
            Vector3d fromrot = player.getRotation();

            // If Sponge used the player's current location, the delta might never be triggered which could be exploited
            Location<World> from = player.getLocation();
            if (this.lastMoveLocation != null) {
                from = this.lastMoveLocation;
            }

            Vector3d torot = new Vector3d(packetIn.pitch, packetIn.yaw, 0);
            Location<World> to = new Location<>(player.getWorld(), packetIn.x, packetIn.y, packetIn.z);

            // Minecraft sends a 0, 0, 0 position when rotation only update occurs, this needs to be recognized and corrected
            boolean rotationOnly = !packetIn.moving && packetIn.rotating;

            if (rotationOnly) {
                // Correct the to location so it's not misrepresented to plugins, only when player rotates without moving
                // In this case it's only a rotation update, which isn't related to the to location
                from = player.getLocation();
                to = from;
            }

            // Minecraft does the same with rotation when it's only a positional update
            boolean positionOnly = packetIn.moving && !packetIn.rotating;
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
                MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(Cause.of(NamedCause.source(player)), fromTransform, toTransform, player);
                SpongeImpl.postEvent(event);
                if (event.isCancelled()) {
                    mixinPlayer.setLocationAndAngles(fromTransform);
                    this.lastMoveLocation = from;
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    return true;
                } else if (!event.getToTransform().equals(toTransform)) {
                    mixinPlayer.setLocationAndAngles(event.getToTransform());
                    this.lastMoveLocation = event.getToTransform().getLocation();
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    return true;
                } else if (!from.equals(player.getLocation()) && this.justTeleported) {
                    this.lastMoveLocation = player.getLocation();
                    // Prevent teleports during the move event from causing odd behaviors
                    this.justTeleported = false;
                    ((IMixinEntityPlayerMP) this.playerEntity).setVelocityOverride(null);
                    return true;
                } else {
                    this.lastMoveLocation = event.getToTransform().getLocation();
                }
            }
        }
        return playerMP.playerConqueredTheEnd;
    }

    /**
     * @author gabizou - June 22nd, 2016
     * @reason Redirects the {@link Entity#getLowestRidingEntity()} call to throw our
     * {@link MoveEntityEvent}. The peculiarity of this redirect is that the entity
     * returned is perfectly valid to be {@link this#playerEntity} since, if the player
     * is NOT riding anything, the lowest riding entity is themselves. This way, if
     * the event is cancelled, the player can be returned instead of the actual riding
     * entity.
     *
     * @param playerMP The player
     * @param packetIn The packet movement
     * @return The lowest riding entity
     */
    @Redirect(method = "processVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getLowestRidingEntity()Lnet/minecraft/entity/Entity;"))
    private Entity processVehicleMoveEvent(EntityPlayerMP playerMP, CPacketVehicleMove packetIn) {


        return playerMP.getLowestRidingEntity();
    }


    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendChatMsg(Lnet/minecraft/util/text/ITextComponent;)V"))
    public void onDisconnectHandler(PlayerList this$0, ITextComponent component) {
        final Player player = ((Player) this.playerEntity);
        final Text message = SpongeTexts.toText(component);
        final MessageChannel originalChannel = player.getMessageChannel();
        final ClientConnectionEvent.Disconnect event = SpongeEventFactory.createClientConnectionEventDisconnect(
                Cause.of(NamedCause.source(player)), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(message),
                player, false
        );
        SpongeImpl.postEvent(event);
        if (!event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
    }

    @Inject(method = "processRightClickBlock", at = @At(value = "HEAD"))
    public void onProcessPlayerBlockPlacement(CPacketPlayerTryUseItemOnBlock packetIn, CallbackInfo ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
        }
    }

    @Redirect(method = "processRightClickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;processRightClickBlock(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;"))
    public EnumActionResult onProcessRightClickBlock(PlayerInteractionManager interactionManager, EntityPlayer player, net.minecraft.world.World worldIn, @Nullable ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumActionResult actionResult = interactionManager.processRightClickBlock(this.playerEntity, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
        // If result is not SUCCESS, we need to avoid throwing an InteractBlockEvent.Secondary for AIR
        // since the client will send the server a CPacketTryUseItem right after this packet is done processing.
        if (actionResult != EnumActionResult.SUCCESS) {
            SpongeCommonEventFactory.ignoreRightClickAirEvent = true;
            // If a plugin or mod has changed the item, avoid restoring
            if (!SpongeCommonEventFactory.playerInteractItemChanged) {
                final CauseTracker causeTracker = ((IMixinWorldServer) player.worldObj).getCauseTracker();
                final PhaseData peek = causeTracker.getCurrentPhaseData();
                final ItemStack itemStack = peek.context.firstNamed(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class).orElse(null);

                // Only do a restore if something actually changed. The client does an identity check ('==')
                // to determine if it should continue using an itemstack. If we always resend the itemstack, we end up
                // cancelling item usage (e.g. eating food) that occurs while targeting a block
                if (!ItemStack.areItemStacksEqual(itemStack, player.getHeldItem(hand)) || SpongeCommonEventFactory.interactBlockEventCancelled) {
                    PacketPhaseUtil.handlePlayerSlotRestore((EntityPlayerMP) player, itemStack, hand);
                }
            }
        }
        SpongeCommonEventFactory.playerInteractItemChanged = false;
        SpongeCommonEventFactory.interactBlockEventCancelled = false;
        return actionResult;
    }

    @Inject(method = "processPlayerDigging", at = @At("HEAD"), cancellable = true)
    public void injectDig(CPacketPlayerDigging packetIn, CallbackInfo ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeImpl.getServer().getTickCounter();
        }
    }

    @Nullable
    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;"))
    public EntityItem onPlayerDropItem(EntityPlayerMP player, boolean dropAll) {
        EntityItem item = null;
        ItemStack stack = this.playerEntity.inventory.getCurrentItem();
        if (stack != null) {
            int size = stack.stackSize;
            item = this.playerEntity.dropItem(dropAll);
            // force client itemstack update if drop event was cancelled
            if (item == null) {
                Slot slot = this.playerEntity.openContainer.getSlotFromInventory(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
                int windowId = this.playerEntity.openContainer.windowId;
                stack.stackSize = size;
                this.sendPacket(new SPacketSetSlot(windowId, slot.slotNumber, stack));
            }
        }

        return item;
    }

    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;onBlockClicked(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)V"))
    public void handleLeftBlockClick(PlayerInteractionManager interactionManager, BlockPos pos, EnumFacing side) {
        Location<World> location = new Location<>((World) this.playerEntity.worldObj, VecHelper.toVector3d(pos));
        InteractBlockEvent.Primary event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Cause.of(NamedCause.source(this.playerEntity)),
                Optional.empty(), location.createSnapshot(), DirectionFacingProvider.getInstance().getKey(side).get());
        if (SpongeImpl.postEvent(event)) {
            this.playerEntity.connection.sendPacket(new SPacketBlockChange(this.playerEntity.worldObj, pos));
            return;
        }

        interactionManager.onBlockClicked(pos, side);
    }

    /**
     * @author blood - April 5th, 2016
     *
     * @reason Due to all the changes we now do for this packet, it is much easier
     * to read it all with an overwrite. Information detailing on why each change
     * was made can be found in comments below.
     *
     * @param packetIn The entity use packet
     */
    @Overwrite
    public void processUseEntity(CPacketUseEntity packetIn) {
        // All packets received by server are handled first on the Netty Thread
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
                // This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
                // aimed at any entity that is not an armor stand. We shouldn't need the INTERACT packet as the
                // INTERACT_AT packet contains the same data but also includes a hitVec.
                return;
            } else { // queue packet for main thread
                PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayServer) (Object) this, this.playerEntity.getServerWorld());
                return;
            }
        }

        WorldServer worldserver = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        Entity entity = packetIn.getEntityFromWorld(worldserver);
        this.playerEntity.markPlayerActive();

        if (entity != null) {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D; // 6 blocks

            if (!flag) {
                d0 = 9.0D; // 1.5 blocks
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < d0) {
                // Since we ignore any packet that has hitVec set to null, we can safely ignore this check
                // if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT) {
                //    this.playerEntity.interactWith(entity);
                // } else
                if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                    InteractEntityEvent.Secondary event;
                    if (packetIn.getHand() == EnumHand.MAIN_HAND) {
                        event = SpongeEventFactory.createInteractEntityEventSecondaryMainHand(
                                Cause.of(NamedCause.source(this.playerEntity)), Optional.of(VecHelper.toVector3d(packetIn.getHitVec())), EntityUtil.fromNative(entity));
                    } else {
                        event = SpongeEventFactory.createInteractEntityEventSecondaryOffHand(
                                Cause.of(NamedCause.source(this.playerEntity)), Optional.of(VecHelper.toVector3d(packetIn.getHitVec())), EntityUtil.fromNative(entity));
                    }

                    SpongeImpl.postEvent(event);
                    if (!event.isCancelled()) {

                        EnumHand hand = packetIn.getHand();
                        ItemStack itemstack = this.playerEntity.getHeldItem(hand);

                        // If INTERACT_AT returns a false result, we assume this packet was meant for interactWith
                        if (entity.applyPlayerInteraction(this.playerEntity, packetIn.getHitVec(), itemstack, hand) != EnumActionResult.SUCCESS) {
                            this.playerEntity.interact(entity, itemstack, hand);
                        }
                    }
                } else if (packetIn.getAction() == CPacketUseEntity.Action.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.playerEntity) {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    if (entity instanceof Player && !((World) this.playerEntity.worldObj).getProperties().isPVPEnabled()) {
                        return; // PVP is disabled, ignore
                    }

                    InteractEntityEvent.Primary event;
                    final Optional<Vector3d> interactionPoint = packetIn.getHitVec() == null
                                                                ? Optional.empty()
                                                                : Optional.of(VecHelper.toVector3d(packetIn.getHitVec()));
                    if (packetIn.getHand() == EnumHand.MAIN_HAND) {
                        event = SpongeEventFactory.createInteractEntityEventPrimaryMainHand(
                                Cause.of(NamedCause.source(this.playerEntity)),
                                interactionPoint, EntityUtil.fromNative(entity));
                    } else {
                        event = SpongeEventFactory.createInteractEntityEventPrimaryOffHand(
                                Cause.of(NamedCause.source(this.playerEntity)),
                                interactionPoint, EntityUtil.fromNative(entity));
                    }
                    SpongeImpl.postEvent(event);
                    if (event.isCancelled()) {
                        ((IMixinEntityPlayerMP) this.playerEntity).restorePacketItem();
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    // Format disconnection message properly, causes weird messages with our console colors
    // Also see https://bugs.mojang.com/browse/MC-59535
    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0, remap = false))
    private void logDisconnect(Logger logger, String message, Object[] args) {
        args[1] = SpongeTexts.toLegacy((ITextComponent) args[1]);
        logger.info(message, args);
    }

    @Inject(method = "handleAnimation", at = @At(value = "HEAD"))
    public void onProcessAnimationHead(CPacketAnimation packetIn, CallbackInfo ci) {
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeCommonEventFactory.lastAnimationPacketTick = SpongeImpl.getServer().getTickCounter();
            SpongeCommonEventFactory.lastAnimationPlayer = this.playerEntity;
        }
    }

    @Override
    public void setLastMoveLocation(Location<World> location) {
        this.lastMoveLocation = location;
    }
}
