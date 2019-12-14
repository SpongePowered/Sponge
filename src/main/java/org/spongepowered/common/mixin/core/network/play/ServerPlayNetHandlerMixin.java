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
package org.spongepowered.common.mixin.core.network.play;

import io.netty.util.collection.LongObjectHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SKeepAlivePacket;
import net.minecraft.network.play.server.SMountEntityPacket;
import net.minecraft.network.play.server.SMoveVehiclePacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.ListValue.Mutable;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.AnimateHandEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Transform;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.bridge.inventory.container.PlayerContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.network.ServerPlayNetHandlerBridge;
import org.spongepowered.common.bridge.network.play.server.SSendResourcePackPacketBridge;
import org.spongepowered.common.bridge.server.management.PlayerInteractionManagerBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.tick.PlayerTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin implements ServerPlayNetHandlerBridge {

    @Shadow @Final public NetworkManager netManager;
    @Shadow @Final private MinecraftServer server;
    @Shadow public ServerPlayerEntity player;
    @Shadow private Entity lowestRiddenEnt;
    @Shadow private int itemDropThreshold;
    // Appears to be the last keep-alive packet ID. Currently the same as
    // field_194402_f, but _f is time (which the ID just so happens to match).
    @Shadow private long field_194404_h;

    @Shadow public abstract void sendPacket(final IPacket<?> packetIn);
    @Shadow public abstract void disconnect(ITextComponent reason);
    @Shadow private void captureCurrentPosition() {}
    @Shadow protected abstract long currentTimeMillis();

    private boolean impl$justTeleported = false;
    private long impl$lastTryBlockPacketTimeStamp = 0;
    @Nullable private Location<World> impl$lastMoveLocation = null;
    @Nullable private ResourcePack impl$lastReceivedPack, lastAcceptedPack;
    private final AtomicInteger impl$numResourcePacksInTransit = new AtomicInteger();
    private final LongObjectHashMap<Runnable> impl$customKeepAliveCallbacks = new LongObjectHashMap<>();
    @Nullable private Transform<World> impl$spectatingTeleportLocation;

    @Override
    public void bridge$captureCurrentPlayerPosition() {
        this.captureCurrentPosition();
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;onUpdateEntity()V"))
    private void impl$onPlayerTick(final ServerPlayerEntity player) {
        if (player.world.isRemote) {
            player.playerTick();
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
             final PlayerTickContext context = TickPhase.Tick.PLAYER.createPhaseContext().source(player)) {
            context.buildAndSwitch();
            frame.pushCause(player);
            player.playerTick();
        }
    }

    /**
     * @param manager The player network connection
     * @param packet The original packet to be sent
     * @author kashike
     */
    @Redirect(method = "sendPacket(Lnet/minecraft/network/Packet;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void impl$onSendPacket(final NetworkManager manager, IPacket<?> packet) {
        // Update the tab list data
        if (packet instanceof SPlayerListItemPacket) {
            ((SpongeTabList) ((Player) this.player).getTabList()).updateEntriesOnSend((SPlayerListItemPacket) packet);
        } else if (packet instanceof SSendResourcePackPacket) {
            // Send a custom keep-alive packet that doesn't match vanilla.
            long now = this.currentTimeMillis() - 1;
            while (now == this.field_194404_h || this.impl$customKeepAliveCallbacks.containsKey(now)) {
                now--;
            }
            final ResourcePack resourcePack = ((SSendResourcePackPacketBridge) packet).bridge$getSpongePack();
            this.impl$numResourcePacksInTransit.incrementAndGet();
            this.impl$customKeepAliveCallbacks.put(now, () -> {
                this.impl$lastReceivedPack = resourcePack; // TODO do something with the old value
                this.impl$numResourcePacksInTransit.decrementAndGet();
            });
            this.netManager.sendPacket(new SKeepAlivePacket(now));
        } else if (packet instanceof SSetExperiencePacket) {
            // Ensures experience is in sync server-side.
            ((PlayerEntityBridge) this.player).bridge$recalculateTotalExperience();
        }

        packet = packet;
        if (packet != null) {
            manager.sendPacket(packet);
        }
    }

    @Inject(method = "processKeepAlive", at = @At("HEAD"), cancellable = true)
    private void impl$checkSpongeKeepAlive(final CKeepAlivePacket packetIn, final CallbackInfo ci) {
        final Runnable callback = this.impl$customKeepAliveCallbacks.get(packetIn.getKey());
        if (callback != null) {
            PacketThreadUtil.checkThreadAndEnqueue(packetIn, (IServerPlayNetHandler) this, this.player.getServerWorld());
            this.impl$customKeepAliveCallbacks.remove(packetIn.getKey());
            callback.run();
            ci.cancel();
        }
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
    @Inject(method = "processUpdateSign", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/CPacketUpdateSign;getLines()[Ljava/lang/String;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void impl$callSignChangeEvent(final CUpdateSignPacket packetIn, final CallbackInfo ci, final ServerWorld worldserver, final BlockPos blockpos, final BlockState iblockstate, final TileEntity tileentity, final SignTileEntity tileentitysign) {
        ci.cancel();
        final Optional<SignData> existingSignData = ((Sign) tileentitysign).get(SignData.class);
        if (!existingSignData.isPresent()) {
            // TODO Unsure if this is the best to do here...
            throw new RuntimeException("Critical error! Sign data not present on sign!");
        }
        final SignData changedSignData = existingSignData.get().copy();
        final Mutable<Text> lines = changedSignData.lines();
        for (int i = 0; i < packetIn.getLines().length; i++) {
            lines.set(i, SpongeTexts.toText(new StringTextComponent(packetIn.getLines()[i])));
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

    @Redirect(method = "processChatMessage",
        at = @At(
            value = "INVOKE",
            target = "Lorg/apache/commons/lang3/StringUtils;normalizeSpace(Ljava/lang/String;)Ljava/lang/String;",
            remap = false))
    private String impl$provideinputNoNormalization(final String input) {
        return input;
    }

    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At(value = "RETURN"))
    private void impl$setTeleported(
        final double x, final double y, final double z, final float yaw, final float pitch, final Set<?> relativeSet, final CallbackInfo ci) {
        this.impl$justTeleported = true;
    }

    /**
     * @author gabizou - June 22nd, 2016
     * @reason Sponge has to throw the movement events before we consider moving the player and there's
     * no clear way to go about it with the target position being null and the last position update checks.
     * @param packetIn
     */
    @Redirect(method = "processPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;queuedEndExit:Z"))
    private boolean throwMoveEvent(final ServerPlayerEntity playerMP, final CPlayerPacket packetIn) {
        if (!playerMP.queuedEndExit) {

            // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
            // We don't fire an event to avoid confusing plugins.
            if (!packetIn.moving && !packetIn.rotating) {
                return playerMP.queuedEndExit;
            }

            // Sponge Start - Movement event
            final Player player = (Player) this.player;
            final ServerPlayerEntityBridge mixinPlayer = (ServerPlayerEntityBridge) this.player;
            final Vector3d fromRotation = player.getRotation();

            // If Sponge used the player's current location, the delta might never be triggered which could be exploited
            Location<World> fromLocation = player.getLocation();
            if (this.impl$lastMoveLocation != null) {
                fromLocation = this.impl$lastMoveLocation;
            }

            Location<World> toLocation = new Location<>(player.getWorld(), packetIn.x, packetIn.y, packetIn.z);
            Vector3d toRotation = new Vector3d(packetIn.pitch, packetIn.yaw, 0);

            // If we have zero movement, we have rotation only, we might as well note that now.
            final boolean zeroMovement = !packetIn.moving || toLocation.getPosition().equals(fromLocation.getPosition());

            // Minecraft does the same with rotation when it's only a positional update
            // Branch executed for CPacketPlayer.Position
            boolean firePositionEvent = packetIn.moving && !packetIn.rotating;
            if (firePositionEvent) {
                // Correct the new rotation to match the old rotation
                toRotation = fromRotation;

                firePositionEvent = !zeroMovement && ShouldFire.MOVE_ENTITY_EVENT_POSITION;
            }

            // Minecraft sends a 0, 0, 0 position when rotation only update occurs, this needs to be recognized and corrected
            // Branch executed for CPacketPlayer.Rotation
            boolean fireRotationEvent = !packetIn.moving && packetIn.rotating;

            if (fireRotationEvent) {
                // Correct the to location so it's not misrepresented to plugins, only when player rotates without moving
                // In this case it's only a rotation update, which isn't related to the to location
                fromLocation = player.getLocation();
                toLocation = fromLocation;

                fireRotationEvent = ShouldFire.ROTATE_ENTITY_EVENT;
            }

            // Branch executed for CPacketPlayer.PositionRotation
            if (packetIn.moving && packetIn.rotating) {
                firePositionEvent = !zeroMovement && ShouldFire.MOVE_ENTITY_EVENT_POSITION;
                fireRotationEvent = ShouldFire.ROTATE_ENTITY_EVENT;
            }

            mixinPlayer.bridge$setVelocityOverride(toLocation.getPosition().sub(fromLocation.getPosition()));

            // These magic numbers are sad but help prevent excessive lag from this event.
            // eventually it would be nice to not have them
            final boolean significantMovement =
                    !zeroMovement && toLocation.getPosition().distanceSquared(fromLocation.getPosition())  > ((1f / 16) * (1f / 16));
            final boolean significantRotation = fromRotation.distanceSquared(toRotation) > (.15f * .15f);

            if (significantMovement || significantRotation) {
                final Transform<World> fromTransform = player.getTransform().setLocation(fromLocation).setRotation(fromRotation);
                Transform<World> toTransform = player.getTransform().setLocation(toLocation).setRotation(toRotation);
                final Transform<World> originalToTransform = toTransform;

                // We should only have fireRotationEvent set to true only if there is no movement and so we are not
                // firing the MoveEntityEvent.Position event anyway. Otherwise, there would be a bug (pointed out in
                // https://github.com/SpongePowered/SpongeCommon/pull/2373#issuecomment-541351230) where the rotate event will be
                // fired if ShouldFire.MOVE_ENTITY_EVENT_POSITION is false and the event would normally be the MoveEntityEvent.Position,
                // rather than a RotateEntityEvent.
                //
                // Note that, as the code is written above, if there is a significant rotation but NOT a significant movement (but still
                // non-zero), then a MoveEntityEvent.Position will be fired, not a rotation event, as some movement is still involved.
                //
                // See the API javadocs for RotateEntityEvent for this restriction.
                fireRotationEvent = fireRotationEvent && zeroMovement;

                if (fireRotationEvent || firePositionEvent) {
                    final Event event;
                    if (fireRotationEvent) {
                        event = SpongeEventFactory.createRotateEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, player);
                    } else {
                        event = SpongeEventFactory.createMoveEntityEventPosition(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, player);
                    }
                    if (SpongeImpl.postEvent(event)) {
                        ((EntityBridge) mixinPlayer).bridge$setLocationAndAngles(fromTransform);
                        this.impl$lastMoveLocation = fromLocation;
                        mixinPlayer.bridge$setVelocityOverride(null);
                        return true;
                    }
                    if (fireRotationEvent) {
                        toTransform = ((RotateEntityEvent) event).getToTransform();
                    } else {
                        toTransform = ((MoveEntityEvent) event).getToTransform();
                    }
                }
                if (!toTransform.equals(originalToTransform)) {
                    ((EntityBridge) mixinPlayer).bridge$setLocationAndAngles(toTransform);
                    this.impl$lastMoveLocation = toTransform.getLocation();
                    mixinPlayer.bridge$setVelocityOverride(null);
                    return true;
                } else if (!fromTransform.getLocation().equals(player.getLocation()) && this.impl$justTeleported) {
                    this.impl$lastMoveLocation = player.getLocation();
                    // Prevent teleports during the move event from causing odd behaviors
                    this.impl$justTeleported = false;
                    mixinPlayer.bridge$setVelocityOverride(null);
                    return true;
                } else {
                    this.impl$lastMoveLocation = toTransform.getLocation();
                }
                this.bridge$resendLatestResourcePackRequest();
            }
        }
        return playerMP.queuedEndExit;
    }

    @Inject(
            method = "handleSpectate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;world:Lnet/minecraft/world/World;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void impl$onSpectateTeleportCallMoveEvent(CSpectatePacket packetIn, CallbackInfo ci, Entity spectatingEntity) {
        final MoveEntityEvent.Teleport event = EntityUtil.handleDisplaceEntityTeleportEvent(
                this.player,
                spectatingEntity.posX,
                spectatingEntity.posY,
                spectatingEntity.posZ,
                spectatingEntity.rotationYaw,
                spectatingEntity.rotationPitch);
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            this.impl$spectatingTeleportLocation = event.getToTransform();
        }
    }

    @Redirect(method = "handleSpectate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;world:Lnet/minecraft/world/World;", ordinal = 1))
    private net.minecraft.world.World impl$onSpectateGetEntityWorld(Entity entity) {
        return (net.minecraft.world.World) this.impl$spectatingTeleportLocation.getExtent();
    }

    @Inject(method = "handleSpectate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getServerWorld()Lnet/minecraft/world/WorldServer;", ordinal = 1), cancellable = true)
    private void impl$cancelIfSameWorld(CallbackInfo ci) {
        //noinspection ConstantConditions
        if (this.player.getServerWorld() == (ServerWorld) this.impl$spectatingTeleportLocation.getExtent()) {
            final Vector3d position = this.impl$spectatingTeleportLocation.getPosition();
            this.impl$spectatingTeleportLocation = null;
            this.player.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
            ci.cancel();
        }
    }

    @Redirect(method = "handleSpectate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;setLocationAndAngles(DDDFF)V"))
    private void impl$onSpectateLocationAndAnglesUpdate(ServerPlayerEntity player, double x, double y, double z, float yaw, float pitch) {
        //noinspection ConstantConditions
        player.dimension = ((ServerWorld) this.impl$spectatingTeleportLocation.getExtent()).dimension.getType().getId();
        final Vector3d position = this.impl$spectatingTeleportLocation.getPosition();
        player.setLocationAndAngles(
                position.getX(), position.getY(), position.getZ(),
                (float) this.impl$spectatingTeleportLocation.getYaw(),
                (float) this.impl$spectatingTeleportLocation.getPitch()
        );
    }

    @Redirect(method = "handleSpectate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;setPositionAndUpdate(DDD)V"))
    private void impl$onSpectatePositionUpdate(ServerPlayerEntity player, double x, double y, double z) {
        //noinspection ConstantConditions
        final Vector3d position = this.impl$spectatingTeleportLocation.getPosition();
        player.setPositionAndUpdate(position.getX(), position.getY(), position.getZ());
        this.impl$spectatingTeleportLocation = null;
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
    private Entity processVehicleMoveEvent(final ServerPlayerEntity playerMP, final CMoveVehiclePacket packetIn) {
        final Entity ridingEntity = this.player.getLowestRidingEntity();
        if (ridingEntity == this.player || ridingEntity.getControllingPassenger() != this.player || ridingEntity != this.lowestRiddenEnt) {
            return ridingEntity;
        }

        // Sponge Start - Movement event
        final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) ridingEntity;
        final Vector3d fromrot = spongeEntity.getRotation();

        final Location<World> from = spongeEntity.getLocation();
        final Vector3d torot = new Vector3d(packetIn.getPitch(), packetIn.getYaw(), 0);
        final Location<World> to = new Location<>(spongeEntity.getWorld(), packetIn.getX(), packetIn.getY(), packetIn.getZ());
        final Transform<World> fromTransform = spongeEntity.getTransform().setLocation(from).setRotation(fromrot);
        final Transform<World> toTransform = spongeEntity.getTransform().setLocation(to).setRotation(torot);
        final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), fromTransform, toTransform, (Player) this.player);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            // There is no need to change the current riding entity position as it hasn't changed yet.
            // Send packet to client in order to update rider position.
            this.netManager.sendPacket(new SMoveVehiclePacket(ridingEntity));
            return this.player;
        }
        return ridingEntity;
    }


    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;sendMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    public void onDisconnectHandler(final PlayerList this$0, final ITextComponent component) {
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
        ((ServerPlayerEntityBridge) this.player).bridge$getWorldBorderListener().onPlayerDisconnect();
    }

    @Redirect(method = "processTryUseItemOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerInteractionManager;processRightClickBlock(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;"))
    private ActionResultType impl$checkState(final PlayerInteractionManager interactionManager, final PlayerEntity player, final net.minecraft.world.World worldIn, @Nullable final ItemStack stack, final Hand hand, final BlockPos pos, final Direction facing, final float hitX, final float hitY, final float hitZ) {
        final ActionResultType actionResult = interactionManager.processRightClickBlock(this.player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
        if (PhaseTracker.getInstance().getCurrentContext().isEmpty()) {
            return actionResult;
        }
        final PacketContext<?> context = ((PacketContext<?>) PhaseTracker.getInstance().getCurrentContext());

        // If a plugin or mod has changed the item, avoid restoring
        if (!context.getInteractItemChanged()) {
            final ItemStack itemStack = ItemStackUtil.toNative(context.getItemUsed());

            // Only do a restore if something actually changed. The client does an identity check ('==')
            // to determine if it should continue using an itemstack. If we always resend the itemstack, we end up
            // cancelling item usage (e.g. eating food) that occurs while targeting a block
            if (!ItemStack.areItemStacksEqual(itemStack, player.getHeldItem(hand)) && ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$isInteractBlockRightClickCancelled()) {
                PacketPhaseUtil.handlePlayerSlotRestore((ServerPlayerEntity) player, itemStack, hand);
            }
        }
        context.interactItemChanged(false);
        ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$setInteractBlockRightClickCancelled(false);
        return actionResult;
    }

    @Redirect(method = "processTryUseItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerInteractionManager;processRightClick(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;"))
    private ActionResultType impl$checkStateAfter(final PlayerInteractionManager interactionManager, final PlayerEntity player, final net.minecraft.world.World worldIn, @Nullable final ItemStack stack, final Hand hand) {
        final ActionResultType actionResult = interactionManager.processRightClick(this.player, worldIn, stack, hand);
        // If a plugin or mod has changed the item, avoid restoring
        if (PhaseTracker.getInstance().getCurrentContext().isEmpty()) {
            return actionResult;
        }
        final PacketContext<?> packetContext = (PacketContext<?>) PhaseTracker.getInstance().getCurrentContext();
        if (!packetContext.getInteractItemChanged()) {
            final ItemStack itemStack = ItemStackUtil.toNative(packetContext.getItemUsed());

            // Only do a restore if something actually changed. The client does an identity check ('==')
            // to determine if it should continue using an itemstack. If we always resend the itemstack, we end up
            // cancelling item usage (e.g. eating food) that occurs while targeting a block
            if (!ItemStack.areItemStacksEqual(itemStack, player.getHeldItem(hand))  && ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$isInteractBlockRightClickCancelled()) {
                PacketPhaseUtil.handlePlayerSlotRestore((ServerPlayerEntity) player, itemStack, hand);
            }
        }
        packetContext.interactItemChanged(false);
        ((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$setInteractBlockRightClickCancelled(false);
        return actionResult;
    }

    @Nullable
    @Redirect(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;"))
    private ItemEntity impl$performDropThroughPhase(final ServerPlayerEntity player, final boolean dropAll) {
        ItemEntity item = null;
        final ItemStack stack = this.player.inventory.getCurrentItem();
        if (!stack.isEmpty()) {
            final int size = stack.getCount();
            item = this.player.dropItem(dropAll);
            // force client itemstack update if drop event was cancelled
            if (item == null && ((PlayerEntityBridge) player).bridge$shouldRestoreInventory()) {
                final Slot slot = this.player.openContainer.getSlotFromInventory(this.player.inventory, this.player.inventory.currentItem);
                final int windowId = this.player.openContainer.windowId;
                stack.setCount(size);
                this.sendPacket(new SSetSlotPacket(windowId, slot.slotNumber, stack));
            }
        }

        return item;
    }

    @Inject(method = "handleAnimation",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;markPlayerActive()V"), cancellable = true)
    private void impl$throwAnimationEvent(final CAnimateHandPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getCurrentContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = SpongeImpl.getServer().getTickCounter();
        SpongeCommonEventFactory.lastAnimationPlayer = new WeakReference<>(this.player);
        if (ShouldFire.ANIMATE_HAND_EVENT) {
            final HandType handType = (HandType) (Object) packetIn.getHand();
            final ItemStack heldItem = this.player.getHeldItem(packetIn.getHand());
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
            Sponge.getCauseStackManager().addContext(EventContextKeys.USED_HAND, handType);
            final AnimateHandEvent event =
                SpongeEventFactory.createAnimateHandEvent(Sponge.getCauseStackManager().getCurrentCause(), handType, (Humanoid) this.player);
            if (SpongeImpl.postEvent(event)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(I)Lnet/minecraft/world/WorldServer;"))
    private void impl$updateLastPrimaryPacket(final CPlayerDiggingPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getCurrentContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastPrimaryPacketTick = SpongeImpl.getServer().getTickCounter();
    }

    @Inject(method = "processPlayerDigging", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;dropItem(Z)Lnet/minecraft/entity/item/EntityItem;"))
    private void onProcessPlayerDiggingDropItem(final CPlayerDiggingPacket packetIn, final CallbackInfo ci) {
        final ItemStack stack = this.player.getHeldItemMainhand();
        if (!stack.isEmpty()) {
            ((ServerPlayerEntityBridge) this.player).bridge$setPacketItem(stack.copy());
        }
    }

    @Inject(method = "processTryUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(I)Lnet/minecraft/world/WorldServer;"), cancellable = true)
    private void onProcessTryUseItem(final CPlayerTryUseItemPacket packetIn, final CallbackInfo ci) {
        SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();
        final long packetDiff = System.currentTimeMillis() - this.impl$lastTryBlockPacketTimeStamp;
        // If the time between packets is small enough, use the last result.
        if (packetDiff < 100) {
            // Use previous result and avoid firing a second event
            if (((PlayerInteractionManagerBridge) this.player.interactionManager).bridge$isLastInteractItemOnBlockCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "processTryUseItemOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(I)Lnet/minecraft/world/WorldServer;"))
    private void onProcessTryUseItemOnBlockSetCountersForSponge(final CPlayerTryUseItemOnBlockPacket packetIn, final CallbackInfo ci) {
        // InteractItemEvent on block must be handled in PlayerInteractionManager to support item/block results.
        // Only track the timestamps to support our block animation events
        this.impl$lastTryBlockPacketTimeStamp = System.currentTimeMillis();
        SpongeCommonEventFactory.lastSecondaryPacketTick = SpongeImpl.getServer().getTickCounter();

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
    public void processUseEntity(final CUseEntityPacket packetIn) {
        // Sponge start
        // All packets received by server are handled first on the Netty Thread
        if (!SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            if (packetIn.getAction() == CUseEntityPacket.Action.INTERACT) {
                // This packet is only sent by client when CPacketUseEntity.Action.INTERACT_AT is
                // not successful. We can safely ignore this packet as we handle the INTERACT logic
                // when INTERACT_AT does not return a successful result.
                return;
            } else { // queue packet for main thread
                PacketThreadUtil.checkThreadAndEnqueue(packetIn, (ServerPlayNetHandler) (Object) this, this.player.getServerWorld());
                return;
            }
        }
        // Sponge end

        final ServerWorld worldserver = this.server.getWorld(this.player.dimension);
        final Entity entity = packetIn.getEntityFromWorld(worldserver);
        this.player.markPlayerActive();

        if (entity != null) {
            final boolean flag = this.player.canEntityBeSeen(entity);
            double d0 = 36.0D; // 6 blocks

            if (!flag) {
                d0 = 9.0D; // 1.5 blocks
            }

            if (this.player.getDistanceSq(entity) < d0) {
                // Sponge start - Ignore CPacketUseEntity.Action.INTERACT
                /*if (packetIn.getAction() == CPacketUseEntity.Action.INTERACT) {
                    // The client will only send this packet if INTERACT_AT is not successful.
                    // We can safely ignore this as we handle interactOn below during INTERACT_AT.
                    //EnumHand enumhand = packetIn.getHand();
                    //this.player.interactOn(entity, enumhand);
                } else */
                // Sponge end

                if (packetIn.getAction() == CUseEntityPacket.Action.INTERACT_AT) {

                    // Sponge start - Fire interact events
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        final Hand hand = packetIn.getHand();
                        final ItemStack itemstack = hand != null ? this.player.getHeldItem(hand) : ItemStack.EMPTY;

                        SpongeCommonEventFactory.lastSecondaryPacketTick = this.server.getTickCounter();

                        // Is interaction allowed with item in hand
                        if (SpongeCommonEventFactory.callInteractEntityEventSecondary(this.player, itemstack,
                            entity, hand, VecHelper.toVector3d(entity.getPositionVector().add(packetIn.getHitVec()))).isCancelled()) {

                            // Restore held item in hand
                            final int index = ((PlayerInventoryBridge) this.player.inventory).bridge$getHeldItemIndex(hand);

                            if (hand == Hand.OFF_HAND) {
                                // A window id of -2 can be used to set the off hand, even if a container is open.
                                // TODO is it correct to just use slotindex 45?
                                this.sendPacket(new SSetSlotPacket(-2, 45, itemstack));
                            } else { // MAIN_HAND
                                // TODO correct?
                                this.sendPacket(new SSetSlotPacket(this.player.openContainer.windowId, this.player.inventory.currentItem, itemstack));
                            }


                            // Handle a few special cases where the client assumes that the interaction is successful,
                            // which means that we need to force an update
                            if (itemstack.getItem() == Items.LEAD) {
                                // Detach entity again
                                this.sendPacket(new SMountEntityPacket(entity, null));
                            } else {
                                // Other cases may involve a specific DataParameter of the entity
                                // We fix the client state by marking it as dirty so it will be updated on the client the next tick
                                final DataParameter<?> parameter = PacketPhaseUtil.findModifiedEntityInteractDataParameter(itemstack, entity);
                                if (parameter != null) {
                                    entity.getDataManager().setDirty(parameter);
                                }
                            }

                            return;
                        }

                        // If INTERACT_AT is not successful, run the INTERACT logic
                        if (entity.applyPlayerInteraction(this.player, packetIn.getHitVec(), hand) != ActionResultType.SUCCESS) {
                            this.player.interactOn(entity, hand);
                        }
                    }
                    // Sponge end
                } else if (packetIn.getAction() == CUseEntityPacket.Action.ATTACK) {
                    // Sponge start - Call interact event
                    final Hand hand = Hand.MAIN_HAND; // Will be null in the packet during ATTACK
                    final ItemStack itemstack = this.player.getHeldItem(hand);
                    SpongeCommonEventFactory.lastPrimaryPacketTick = this.server.getTickCounter();

                    Vector3d hitVec = null;

                    if (packetIn.getHitVec() == null) {
                        final RayTraceResult result = SpongeImplHooks.rayTraceEyes(this.player, SpongeImplHooks.getBlockReachDistance(this.player));
                        hitVec = result == null ? null : VecHelper.toVector3d(result.hitResult);
                    }

                    if (SpongeCommonEventFactory.callInteractItemEventPrimary(this.player, itemstack, hand, hitVec, entity).isCancelled()) {
                        ((ServerPlayerEntityBridge) this.player).bridge$restorePacketItem(hand);
                        return;
                    }
                    // Sponge end

                    if (entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || entity instanceof AbstractArrowEntity || entity == this.player) {
                        this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_entity_attacked"));
                        this.server.logWarning("Player " + this.player.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    // Sponge start
                    if (SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player, itemstack, entity, hand, hitVec).isCancelled()) {
                        ((ServerPlayerEntityBridge) this.player).bridge$restorePacketItem(hand);
                        return;
                    }
                    // Sponge end

                    this.player.attackTargetEntityWithCurrentItem(entity);
                }
            }
        }
    }

    @Override
    public void bridge$setLastMoveLocation(final Location<World> location) {
        this.impl$lastMoveLocation = location;
    }

    @Inject(method = "handleResourcePackStatus(Lnet/minecraft/network/play/client/CPacketResourcePackStatus;)V", at = @At("HEAD"))
    private void onProcessResourcePackStatus(final CResourcePackStatusPacket packet, final CallbackInfo ci) {
        // Propagate the packet to the main thread so the cause tracker picks
        // it up. See PacketThreadUtil_1Mixin.
        PacketThreadUtil.checkThreadAndEnqueue(packet, (IServerPlayNetHandler) this, this.player.getServerWorld());
    }

    @Override
    public void bridge$resendLatestResourcePackRequest() {
        final ResourcePack pack = this.impl$lastReceivedPack;
        if (this.impl$numResourcePacksInTransit.get() > 0 || pack == null) {
            return;
        }
        this.impl$lastReceivedPack = null;
        ((Player) this.player).sendResourcePack(pack);
    }

    @Override
    public ResourcePack bridge$popReceivedResourcePack(final boolean markAccepted) {
        final ResourcePack pack = this.impl$lastReceivedPack;
        this.impl$lastReceivedPack = null;
        if (markAccepted) {
            this.lastAcceptedPack = pack; // TODO do something with the old value
        }
        return pack;
    }

    @Override
    public ResourcePack bridge$popAcceptedResourcePack() {
        final ResourcePack pack = this.lastAcceptedPack;
        this.lastAcceptedPack = null;
        return pack;
    }

    @Override
    public long bridge$getLastTryBlockPacketTimeStamp() {
        return this.impl$lastTryBlockPacketTimeStamp;
    }
}
