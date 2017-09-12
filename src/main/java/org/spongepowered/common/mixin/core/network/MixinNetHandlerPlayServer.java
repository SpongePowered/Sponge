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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketResourcePackSend;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.PlayerConnection;
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
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinNetworkManager;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.VecHelper;

import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.LinkedList;
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
    @Shadow public EntityPlayerMP player;
    @Shadow private Entity lowestRiddenEnt;
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
    @Shadow public abstract void disconnect(ITextComponent reason);
    @Shadow private void captureCurrentPosition() {}
    @Shadow public abstract void setPlayerLocation(double x, double y, double z, float yaw, float pitch);
    @Shadow private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) { return false; } // Shadowed

    private boolean justTeleported = false;
    @Nullable private Location<World> lastMoveLocation = null;

    private final Deque<SPacketResourcePackSend> resourcePackRequests = new LinkedList<>();

    // Store the last block right-clicked
    private boolean allowClientLocationUpdate = true;
    @Nullable private Item lastItem;

    @Override
    public void captureCurrentPlayerPosition() {
        this.captureCurrentPosition();
    }

    @Override
    public Deque<SPacketResourcePackSend> getPendingResourcePackQueue() {
        return this.resourcePackRequests;
    }

    @Override
    public Player getPlayer() {
        return (Player) this.player;
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
        return this.player.ping;
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;onUpdateEntity()V"))
    private void onPlayerTick(EntityPlayerMP player) {
        if (player.world.isRemote || !CauseTracker.ENABLED) {
            player.onUpdateEntity();
            return;
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            final CauseTracker causeTracker = CauseTracker.getInstance();
            final PhaseContext complete = PhaseContext.start()
                .source(player)
                .addCaptures()
                .addEntityDropCaptures()
                .complete();
            causeTracker.switchToPhase(TickPhase.Tick.PLAYER, complete, () -> {
                player.onUpdateEntity();
                return null;
            });
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
        packet = this.rewritePacket(packet);
        if (packet != null) {
            manager.sendPacket(packet);
        }
    }

    /**
     * This method wraps packets being sent to perform any additional actions,
     * such as rewriting data in the packet.
     *
     * @param packetIn The original packet to be sent
     * @return The rewritten packet if we performed any changes, the original
     *     packet if we did not perform any changes, or {@code null} to not
     *     send anything
     * @author kashike
     */
    @Nullable
    private Packet<?> rewritePacket(final Packet<?> packetIn) {
        // Update the tab list data
        if (packetIn instanceof SPacketPlayerListItem) {
            ((SpongeTabList) ((Player) this.player).getTabList()).updateEntriesOnSend((SPacketPlayerListItem) packetIn);
        }
        // Store the resource pack for use when processing resource pack statuses
        else if (packetIn instanceof SPacketResourcePackSend) {
            SPacketResourcePackSend packet = (SPacketResourcePackSend) packetIn;
            if (this.resourcePackRequests.isEmpty()) {
                this.resourcePackRequests.add(packet);
                return packet;
            }
            if (this.resourcePackRequests.contains(packet)) {
                // This must be a resend.
                return packet;
            }
            this.resourcePackRequests.add(packet);
            return null;
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
        Sponge.getCauseStackManager().pushCause(this.player);
        final ChangeSignEvent event =
                SpongeEventFactory.createChangeSignEvent(Sponge.getCauseStackManager().getCurrentCause(),
                    changedSignData.asImmutable(), changedSignData, (Sign) tileentitysign);
        if (!SpongeImpl.postEvent(event)) {
            ((Sign) tileentitysign).offer(event.getText());
        } else {
            // If cancelled, I set the data back that was fetched from the sign. This means that if its a new sign, the sign will be empty else
            // it will be the text of the sign that was showing in the world
            ((Sign) tileentitysign).offer(existingSignData.get());
        }
        Sponge.getCauseStackManager().popCause();
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
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayServer) (Object) this, this.player.getServerWorld());

        if (this.player.interactionManager.isCreative()) {
            final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();
            final PhaseContext context = peek.context;
            final boolean ignoresCreative = context.getRequiredExtra(InternalNamedCauses.Packet.IGNORING_CREATIVE, Boolean.class);
            boolean clickedOutside = packetIn.getSlotId() < 0;
            ItemStack itemstack = packetIn.getStack();

            if (!itemstack.isEmpty() && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
                NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");

                if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
                    BlockPos blockpos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
                    TileEntity tileentity = this.player.world.getTileEntity(blockpos);

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
            boolean itemValidCheck = itemstack.isEmpty() || itemstack.getMetadata() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();

            // Sponge start - handle CreativeInventoryEvent
            if (itemValidCheck) {
                if (!ignoresCreative) {
                    ClickInventoryEvent.Creative clickEvent = SpongeCommonEventFactory.callCreativeClickInventoryEvent(this.player, packetIn);
                    if (clickEvent.isCancelled()) {
                        // Reset slot on client
                        if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < this.player.inventoryContainer.inventorySlots.size()) {
                            this.player.connection.sendPacket(
                                    new SPacketSetSlot(this.player.inventoryContainer.windowId, packetIn.getSlotId(),
                                            this.player.inventoryContainer.getSlot(packetIn.getSlotId()).getStack()));
                            this.player.connection.sendPacket(new SPacketSetSlot(-1, -1, ItemStack.EMPTY));
                        }
                        return;
                    }
                }

                if (clickedHotbar) {
                    if (itemstack.isEmpty()) {
                        this.player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), ItemStack.EMPTY);
                    } else {
                        this.player.inventoryContainer.putStackInSlot(packetIn.getSlotId(), itemstack);
                    }

                    this.player.inventoryContainer.setCanCraft(this.player, true);
                } else if (clickedOutside && this.itemDropThreshold < 200) {
                    this.itemDropThreshold += 20;
                    EntityItem entityitem = this.player.dropItem(itemstack, true);

                    if (entityitem != null)
                    {
                        entityitem.setAgeToCreativeDespawnTime();
                    }
                }
            }
            // Sponge end
        }
    }

    @Inject(method = "processClickWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IntHashMap;addKey(ILjava/lang/Object;)V"))
    public void onInvalidClick(CPacketClickWindow packet, CallbackInfo ci) {
        // We want to treat an 'invalid' click just like a regular click - we still fire events, do restores, etc.

        // Vanilla doesn't call detectAndSendChanges for 'invalid' clicks, since it restores the entire inventory
        // Passing 'captureOnly' as 'true' allows capturing to happen for event firing, but doesn't send any pointless packets
        ((IMixinContainer) this.player.openContainer).detectAndSendChanges(true);
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
    @Redirect(method = "processPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;queuedEndExit:Z"))
    private boolean throwMoveEvent(EntityPlayerMP playerMP, CPacketPlayer packetIn) {
        if (!playerMP.queuedEndExit) {

            // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
            // We don't fire an event to avoid confusing plugins.
            if (!packetIn.moving && !packetIn.rotating) {
                return playerMP.queuedEndExit;
            }

            // Sponge Start - Movement event
            Player player = (Player) this.player;
            IMixinEntityPlayerMP mixinPlayer = (IMixinEntityPlayerMP) this.player;
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

            ((IMixinEntityPlayerMP) this.player).setVelocityOverride(to.getPosition().sub(from.getPosition()));

            double deltaSquared = to.getPosition().distanceSquared(from.getPosition());
            double deltaAngleSquared = fromrot.distanceSquared(torot);

            // These magic numbers are sad but help prevent excessive lag from this event.
            // eventually it would be nice to not have them
            if (deltaSquared > ((1f / 16) * (1f / 16)) || deltaAngleSquared > (.15f * .15f)) {
                Transform<World> fromTransform = player.getTransform().setLocation(from).setRotation(fromrot);
                Transform<World> toTransform = player.getTransform().setLocation(to).setRotation(torot);
                Sponge.getCauseStackManager().pushCause(player);
                MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, player);
                SpongeImpl.postEvent(event);
                Sponge.getCauseStackManager().popCause();
                if (event.isCancelled()) {
                    mixinPlayer.setLocationAndAngles(fromTransform);
                    this.lastMoveLocation = from;
                    ((IMixinEntityPlayerMP) this.player).setVelocityOverride(null);
                    return true;
                } else if (!event.getToTransform().equals(toTransform)) {
                    mixinPlayer.setLocationAndAngles(event.getToTransform());
                    this.lastMoveLocation = event.getToTransform().getLocation();
                    ((IMixinEntityPlayerMP) this.player).setVelocityOverride(null);
                    return true;
                } else if (!from.equals(player.getLocation()) && this.justTeleported) {
                    this.lastMoveLocation = player.getLocation();
                    // Prevent teleports during the move event from causing odd behaviors
                    this.justTeleported = false;
                    ((IMixinEntityPlayerMP) this.player).setVelocityOverride(null);
                    return true;
                } else {
                    this.lastMoveLocation = event.getToTransform().getLocation();
                }
                this.resendLatestResourcePackRequest();
            }
        }
        return playerMP.queuedEndExit;
    }

    /**
     * @author gabizou - June 22nd, 2016
     * @author blood - May 6th, 2017
     * @reason Redirects the {@link Entity#getLowestRidingEntity()} call to throw our
     * {@link MoveEntityEvent}. The peculiarity of this redirect is that the entity
     * returned is perfectly valid to be {@link this#player} since, if the player
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
        final Entity ridingEntity = this.player.getLowestRidingEntity();
        if (ridingEntity == this.player || ridingEntity.getControllingPassenger() != this.player || ridingEntity != this.lowestRiddenEnt) {
            return ridingEntity;
        }

        // Sponge Start - Movement event
        org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) ridingEntity;
        Vector3d fromrot = spongeEntity.getRotation();

        Location<World> from = spongeEntity.getLocation();
        Vector3d torot = new Vector3d(packetIn.getPitch(), packetIn.getYaw(), 0);
        Location<World> to = new Location<>(spongeEntity.getWorld(), packetIn.getX(), packetIn.getY(), packetIn.getZ());
        Transform<World> fromTransform = spongeEntity.getTransform().setLocation(from).setRotation(fromrot);
        Transform<World> toTransform = spongeEntity.getTransform().setLocation(to).setRotation(torot);
        MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, this.getPlayer());
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            // There is no need to change the current riding entity position as it hasn't changed yet.
            // Send packet to client in order to update rider position.
            this.netManager.sendPacket(new SPacketMoveVehicle(ridingEntity));
            return this.player;
        }
        return ridingEntity;
    }


    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    public void onDisconnectHandler(PlayerList this$0, ITextComponent component) {
        // If this happens, the connection has not been fully established yet so we've kicked them during ClientConnectionEvent.Login,
        // but FML has created this handler earlier to send their handshake. No message should be sent, no disconnection event should
        // be fired either.
        if (this.player.connection == null) {
            return;
        }
        final Player player = ((Player) this.player);
        final Text message = SpongeTexts.toText(component);
        final MessageChannel originalChannel = player.getMessageChannel();
        Sponge.getCauseStackManager().pushCause(player);
        final ClientConnectionEvent.Disconnect event = SpongeEventFactory.createClientConnectionEventDisconnect(
                Sponge.getCauseStackManager().getCurrentCause(), originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(message),
                player, false
        );
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
        if (!event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(player, event.getMessage()));
        }
        ((IMixinEntityPlayerMP) this.player).getWorldBorderListener().onPlayerDisconnect();
    }

    @Redirect(method = "processTryUseItemOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;processRightClickBlock(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;"))
    public EnumActionResult onProcessRightClickBlock(PlayerInteractionManager interactionManager, EntityPlayer player, net.minecraft.world.World worldIn, @Nullable ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumActionResult actionResult = interactionManager.processRightClickBlock(this.player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
        // If result is not SUCCESS, we need to avoid throwing an InteractBlockEvent.Secondary for AIR
        // since the client will send the server a CPacketTryUseItem right after this packet is done processing.
        if (actionResult != EnumActionResult.SUCCESS) {
            //SpongeCommonEventFactory.ignoreRightClickAirEvent = true;
            // If a plugin or mod has changed the item, avoid restoring
            if (!SpongeCommonEventFactory.playerInteractItemChanged) {
                final CauseTracker causeTracker = CauseTracker.getInstance();
                final PhaseData peek = causeTracker.getCurrentPhaseData();
                final ItemStack itemStack = peek.context.getExtra(InternalNamedCauses.Packet.ITEM_USED, ItemStack.class);

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

    @Nullable
    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;"))
    public EntityItem onPlayerDropItem(EntityPlayerMP player, boolean dropAll) {
        EntityItem item = null;
        ItemStack stack = this.player.inventory.getCurrentItem();
        if (!stack.isEmpty()) {
            int size = stack.getCount();
            item = this.player.dropItem(dropAll);
            // force client itemstack update if drop event was cancelled
            if (item == null) {
                Slot slot = this.player.openContainer.getSlotFromInventory(this.player.inventory, this.player.inventory.currentItem);
                int windowId = this.player.openContainer.windowId;
                stack.setCount(size);
                this.sendPacket(new SPacketSetSlot(windowId, slot.slotNumber, stack));
            }
        }

        return item;
    }

    /**
     * Attempts to find the {@link DataParameter} that was potentially modified
     * when a player interacts with an entity.
     *
     * @param stack The item the player is holding
     * @param entity The entity
     * @return A possible data parameter or null if unknown
     */
    @Nullable
    private static DataParameter<?> findModifiedEntityInteractDataParameter(ItemStack stack, Entity entity) {
        Item item = stack.getItem();

        if (item == Items.DYE) {
            // ItemDye.itemInteractionForEntity
            if (entity instanceof EntitySheep) {
                return EntitySheep.DYE_COLOR;
            }

            // EntityWolf.processInteract
            if (entity instanceof EntityWolf) {
                return EntityWolf.COLLAR_COLOR;
            }

            return null;
        }

        if (item == Items.NAME_TAG) {
            // ItemNameTag.itemInteractionForEntity
            return entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer) && stack.hasDisplayName() ? Entity.CUSTOM_NAME : null;
        }

        if (item == Items.SADDLE) {
            // ItemSaddle.itemInteractionForEntity
            return entity instanceof EntityPig ? EntityPig.SADDLED : null;
        }

        if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() == Blocks.CHEST) {
            // AbstractChestHorse.processInteract
            return entity instanceof AbstractChestHorse ? AbstractChestHorse.DATA_ID_CHEST : null;
        }

        return null;
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
        // Sponge start
        // All packets received by server are handled first on the Netty Thread
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
                // This packet is only sent by client when CPacketUseEntity.Action.INTERACT_AT is
                // not successful. We can safely ignore this packet as we handle the INTERACT logic
                // when INTERACT_AT does not return a successful result.
                return;
            } else { // queue packet for main thread
                PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayServer) (Object) this, this.player.getServerWorld());
                return;
            }
        }
        // Sponge end

        WorldServer worldserver = this.serverController.getWorld(this.player.dimension);
        Entity entity = packetIn.getEntityFromWorld(worldserver);
        this.player.markPlayerActive();

        if (entity != null) {
            boolean flag = this.player.canEntityBeSeen(entity);
            double d0 = 36.0D; // 6 blocks

            if (!flag) {
                d0 = 9.0D; // 1.5 blocks
            }

            if (this.player.getDistanceSqToEntity(entity) < d0) {
                // Sponge start - Ignore CPacketUseEntity.Action.INTERACT
                /*if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
                    // The client will only send this packet if INTERACT_AT is not successful.
                    // We can safely ignore this as we handle interactOn below during INTERACT_AT.
                    //EnumHand enumhand = packetIn.getHand();
                    //this.player.interactOn(entity, enumhand);
                } else */
                // Sponge end

                if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT_AT) {

                    // Sponge start - Fire interact events
                    EnumHand hand = packetIn.getHand();
                    ItemStack itemstack = hand != null ? this.player.getHeldItem(hand) : ItemStack.EMPTY;
                    Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(itemstack));

                    SpongeCommonEventFactory.lastSecondaryPacketTick = this.serverController.getTickCounter();
                    Optional<Vector3d> interactionPoint = Optional.of(VecHelper.toVector3d(packetIn.getHitVec()));

                    // Is interaction allowed with item in hand
                    if (SpongeCommonEventFactory.callInteractItemEventSecondary(this.player, itemstack, hand, interactionPoint, entity).isCancelled()
                            || SpongeCommonEventFactory.callInteractEntityEventSecondary(this.player, entity, hand, interactionPoint).isCancelled()) {
                        // Restore held item in hand
                        int index = ((IMixinInventoryPlayer) this.player.inventory).getHeldItemIndex(hand);
                        Slot slot = this.player.openContainer.getSlotFromInventory(this.player.inventory, index);
                        sendPacket(new SPacketSetSlot(this.player.openContainer.windowId, slot.slotNumber, itemstack));

                        // Handle a few special cases where the client assumes that the interaction is successful,
                        // which means that we need to force an update
                        if (itemstack.getItem() == Items.LEAD) {
                            // Detach entity again
                            sendPacket(new SPacketEntityAttach(entity, null));
                        } else {
                            // Other cases may involve a specific DataParameter of the entity
                            // We fix the client state by marking it as dirty so it will be updated on the client the next tick
                            DataParameter<?> parameter = findModifiedEntityInteractDataParameter(itemstack, entity);
                            if (parameter != null) {
                                entity.getDataManager().setDirty(parameter);
                            }
                        }

                        return;
                    }

                    // If INTERACT_AT is not successful, run the INTERACT logic
                    if (entity.applyPlayerInteraction(this.player, packetIn.getHitVec(), hand) != EnumActionResult.SUCCESS) {
                        this.player.interactOn(entity, hand);
                    }
                    // Sponge end
                } else if (packetIn.getAction() == CPacketUseEntity.Action.ATTACK) {
                    // Sponge start - Call interact event
                    EnumHand hand = EnumHand.MAIN_HAND; // Will be null in the packet during ATTACK
                    ItemStack itemstack = this.player.getHeldItem(hand);
                    SpongeCommonEventFactory.lastPrimaryPacketTick = this.serverController.getTickCounter();
                    if (SpongeCommonEventFactory.callInteractItemEventPrimary(this.player, itemstack, hand, Optional.empty(), entity).isCancelled()) {
                        ((IMixinEntityPlayerMP) this.player).restorePacketItem(hand);
                        return;
                    }
                    // Sponge end

                    if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.player) {
                        this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_entity_attacked"));
                        this.serverController.logWarning("Player " + this.player.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    // Sponge start
                    if (entity instanceof Player && !((World) this.player.world).getProperties().isPVPEnabled()) {
                        return; // PVP is disabled, ignore
                    }

                    if (SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player, entity, hand, null).isCancelled()) {
                        ((IMixinEntityPlayerMP) this.player).restorePacketItem(hand);
                        return;
                    }
                    // Sponge end

                    this.player.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    @Override
    public void setLastMoveLocation(Location<World> location) {
        this.lastMoveLocation = location;
    }

    @Inject(method = "handleResourcePackStatus(Lnet/minecraft/network/play/client/CPacketResourcePackStatus;)V", at = @At("HEAD"))
    private void onProcessResourcePackStatus(CPacketResourcePackStatus packet, CallbackInfo ci) {
        // Propagate the packet to the main thread so the cause tracker picks
        // it up. See MixinPacketThreadUtil.
        PacketThreadUtil.checkThreadAndEnqueue(packet, (INetHandlerPlayServer) this, this.player.getServerWorld());
    }

    @Override
    public void resendLatestResourcePackRequest() {
        // The vanilla client doesn't send any resource pack status if the user presses Escape to close the prompt.
        // If the user moves around, they must have closed the GUI, so resend it to get a real answer.
        if (!this.resourcePackRequests.isEmpty()) {
            this.sendPacket(this.resourcePackRequests.peek());
        }
    }
}
