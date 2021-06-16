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
package org.spongepowered.common.mixin.core.server.network;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.AnimateHandEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.protocol.game.ServerboundMovePlayerPacketAccessor;
import org.spongepowered.common.accessor.server.level.ServerPlayerGameModeAccessor;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.network.ConnectionHolderBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierBasedRegistrar;
import org.spongepowered.common.data.value.ImmutableSpongeListValue;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.BasicPacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.CommandUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ConnectionHolderBridge {

    // @formatter:off
    @Shadow @Final public Connection connection;
    @Shadow public net.minecraft.server.level.ServerPlayer player;
    @Shadow @Final private MinecraftServer server;
    @Shadow private Vec3 awaitingPositionFromClient;
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow private int receivedMovePacketCount;
    @Shadow private int knownMovePacketCount;
    @Shadow private int tickCount;
    @Shadow private int awaitingTeleportTime;

    @Shadow protected abstract boolean shadow$isSingleplayerOwner();
    @Shadow public abstract void shadow$teleport(double x, double y, double z, float yaw, float pitch);
    @Shadow protected abstract void shadow$filterTextPacket(List<String> p_244537_1_, Consumer<List<String>> p_244537_2_);
    // @formatter:on

    private int impl$ignorePackets;

    @Override
    public Connection bridge$getConnection() {
        return this.connection;
    }

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
        at = @At("HEAD")
    )
    private void impl$onClientboundPacketSend(final Packet<?> packet, final GenericFutureListener<? extends Future<? super Void>> listener, final CallbackInfo ci) {
        if (packet instanceof ClientboundPlayerInfoPacket) {
            ((SpongeTabList) ((ServerPlayer) this.player).tabList()).updateEntriesOnSend((ClientboundPlayerInfoPacket) packet);
        }
    }

    @Inject(method = "handleCustomCommandSuggestions", at = @At(value = "NEW", target = "com/mojang/brigadier/StringReader", remap = false),
            cancellable = true)
    private void impl$getSuggestionsFromNonBrigCommand(final ServerboundCommandSuggestionPacket packet, final CallbackInfo ci) {
        final String rawCommand = packet.getCommand();
        final String[] command = CommandUtil.extractCommandString(rawCommand);
        final CommandCause cause = CommandCause.create();
        final SpongeCommandManager manager = SpongeCommandManager.get(this.server);
        if (!rawCommand.contains(" ")) {
            final SuggestionsBuilder builder = new SuggestionsBuilder(command[0], 0);
            if (command[0].isEmpty()) {
                manager.getAliasesForCause(cause).forEach(builder::suggest);
            } else {
                manager.getAliasesThatStartWithForCause(cause, command[0]).forEach(builder::suggest);
            }
            this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), builder.build()));
            ci.cancel();
        } else {
            final Optional<CommandMapping> mappingOptional =
                    manager.commandMapping(command[0].toLowerCase(Locale.ROOT))
                            .filter(x -> !(x.registrar() instanceof BrigadierBasedRegistrar));
            if (mappingOptional.isPresent()) {
                final CommandMapping mapping = mappingOptional.get();
                if (mapping.registrar().canExecute(cause, mapping)) {
                    final SuggestionsBuilder builder = CommandUtil.createSuggestionsForRawCommand(rawCommand, command, cause, mapping);
                    this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), builder.build()));
                } else {
                    this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), Suggestions.empty().join()));
                }
                ci.cancel();
            }
        }
    }

    @Redirect(method = "handleCustomCommandSuggestions",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;",
                    remap = false
            )
    )
    private ParseResults<CommandSourceStack> impl$informParserThisIsASuggestionCheck(final CommandDispatcher<CommandSourceStack> commandDispatcher,
            final StringReader command,
            final Object source) {
        return SpongeCommandManager.get(this.server).getDispatcher().parse(command, (CommandSourceStack) source, true);
    }

    /**
     * Specifically hooks the reach distance to use the forge hook.
     */
    @ModifyConstant(
            method = "handleInteract",
            constant = @Constant(doubleValue = 36.0D)
    )
    private double impl$getPlatformReach(final double thirtySix, ServerboundInteractPacket p_147340_1_) {
        return PlatformHooks.INSTANCE.getGeneralHooks().getEntityReachDistanceSq(this.player, p_147340_1_.getTarget(this.player.level));
    }

    /**
     * Effectively, hooking into the following code block:
     * <pre>
     *       if (isMovePlayerPacketInvalid(packetIn)) {
     *          this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_player_movement"));
     *       } else {
     *          ServerWorld serverworld = this.server.world(this.player.dimension);
     *          if (!this.player.queuedEndExit) { // <---- Here is where we're injecting
     *             if (this.networkTickCount == 0) {
     *                this.captureCurrentPosition();
     *             }
     * </pre>
     * we can effectively short circuit the method to handle movement code where
     * returning {@code true} will escape the packet being processed further entirely and
     * {@code false} will allow the remaining processing of the method run.
     *
     * @param packetIn The movement packet
     */
    @Inject(method = "handleMovePlayer",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;awaitingPositionFromClient:Lnet/minecraft/world/phys/Vec3;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/server/level/ServerPlayer;wonGame:Z"
                    ),
                    to = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;tickCount:I",
                            ordinal = 1
                    )
            ),
            cancellable = true
    )
    private void impl$callMoveEntityEvent(final ServerboundMovePlayerPacket packetIn, final CallbackInfo ci) {

        // If the movement is modified we pretend that the player has queuedEndExit = true
        // so that vanilla wont process that packet further

        final ServerboundMovePlayerPacketAccessor packetInAccessor = (ServerboundMovePlayerPacketAccessor) packetIn;

        // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
        // We don't fire an event to avoid confusing plugins.
        if (!packetInAccessor.accessor$hasPos() && !packetInAccessor.accessor$hasRot()) {
            return;
        }

        final boolean goodMovementPacket = this.receivedMovePacketCount - this.knownMovePacketCount <= 5;
        final boolean fireMoveEvent = goodMovementPacket && packetInAccessor.accessor$hasPos() && ShouldFire.MOVE_ENTITY_EVENT;
        final boolean fireRotationEvent = goodMovementPacket && packetInAccessor.accessor$hasRot() && ShouldFire.ROTATE_ENTITY_EVENT;

        final ServerPlayer player = (ServerPlayer) this.player;
        final Vector3d fromRotation = new Vector3d(this.player.yRot, this.player.xRot, 0);

        // Use the position of the last movement with an event or the current player position if never called
        // We need this because we ignore very small position changes as to not spam as many move events.
        final Vector3d fromPosition = player.position();

        Vector3d toPosition = new Vector3d(packetIn.getX(this.player.getX()),
                packetIn.getY(this.player.getY()), packetIn.getZ(this.player.getZ()));
        Vector3d toRotation = new Vector3d(packetIn.getYRot(this.player.yRot),
                packetIn.getXRot(this.player.xRot), 0);

        final boolean significantRotation = fromRotation.distanceSquared(toRotation) > (.15f * .15f);

        final Vector3d originalToPosition = toPosition;
        boolean cancelMovement = false;
        boolean cancelRotation = false;
        // Call move & rotate event as needed...
        if (fireMoveEvent) {
            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(PhaseTracker.getCauseStackManager().currentCause(), (ServerPlayer) this.player, fromPosition,
                    toPosition, toPosition);
            if (SpongeCommon.postEvent(event)) {
                cancelMovement = true;
            } else {
                toPosition = event.destinationPosition();
            }
        }

        if (significantRotation && fireRotationEvent) {
            final RotateEntityEvent event = SpongeEventFactory.createRotateEntityEvent(PhaseTracker.getCauseStackManager().currentCause(), (ServerPlayer) this.player, fromRotation,
                    toRotation);
            if (SpongeCommon.postEvent(event)) {
                cancelRotation = true;
                toRotation = fromRotation;
            } else {
                toRotation = event.toRotation();
            }
        }

        // At this point, we cancel out and let the "confirmed teleport" code run through to update the
        // player position and update the player's relation in the chunk manager.
        if (cancelMovement) {
            if (fromPosition.distanceSquared(toPosition) > 0) {
                // Set the location, as if the player was teleporting
                this.awaitingTeleportTime = this.tickCount;
                this.shadow$teleport(fromPosition.x(), fromPosition.y(), fromPosition.z(), (float) toRotation.x(), (float) toRotation.y());
            } else {
                // If this is only rotation do not teleport back
                this.player.absMoveTo(fromPosition.x(), fromPosition.y(), fromPosition.z(), (float) toRotation.x(), (float) toRotation.y());
            }
            ci.cancel();
            return;
        }

        // TODO handle cancelRotation or rotation change

        // Handle event results
        if (!toPosition.equals(originalToPosition)) {
            // Check if we have to say it's a "teleport" vs a standard move
            final double d4 = packetIn.getX(this.player.getX());
            final double d5 = packetIn.getY(this.player.getY());
            final double d6 = packetIn.getZ(this.player.getZ());
            final double d7 = d4 - this.firstGoodX;
            final double d8 = d5 - this.firstGoodY;
            final double d9 = d6 - this.firstGoodZ;
            final double d10 = this.player.getDeltaMovement().lengthSqr();
            final double d11 = d7 * d7 + d8 * d8 + d9 * d9;
            final float f2 = this.player.isFallFlying() ? 300.0F : 100.0F;
            final int i = this.receivedMovePacketCount - this.knownMovePacketCount;
            if (d11 - d10 > (double)(f2 * (float)i) && !this.shadow$isSingleplayerOwner()) {
                // At this point, we need to set the target position so the teleport code forces it
                this.awaitingPositionFromClient = VecHelper.toVanillaVector3d(toPosition);
                ((EntityAccessor) this.player).invoker$setRot((float) toRotation.x(), (float) toRotation.y());
                // And reset the position update so the force set is done.
                this.awaitingTeleportTime = this.tickCount - Constants.Networking.MAGIC_TRIGGER_TELEPORT_CONFIRM_DIFF;
            } else {
                // otherwise, set the data back onto the packet
                packetInAccessor.accessor$hasPos(true);
                packetInAccessor.accessor$x(toPosition.x());
                packetInAccessor.accessor$y(toPosition.y());
                packetInAccessor.accessor$z(toPosition.z());
            }
        }
    }

    @Inject(
            method = "handleInteract",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;interactAt(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"
            )
    )
    public void impl$onRightClickAtEntity(final ServerboundInteractPacket p_147340_1, final CallbackInfo ci) {
        final Entity entity = p_147340_1.getTarget(this.player.getLevel());
        final ItemStack itemInHand = p_147340_1.getHand() == null ? ItemStack.EMPTY : this.player.getItemInHand(p_147340_1.getHand());
        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory
                .callInteractEntityEventSecondary(this.player, itemInHand, entity, p_147340_1.getHand(), VecHelper.toVector3d(p_147340_1.getLocation()));
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            this.impl$ignorePackets++;
        }
    }

    @Inject(
            method = "handleInteract",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V")
    )
    public void impl$onLeftClickEntity(final ServerboundInteractPacket p_147340_1_, final CallbackInfo ci) {
        final Entity entity = p_147340_1_.getTarget(this.player.getLevel());

        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player,
                this.player.getItemInHand(this.player.getUsedItemHand()), entity, this.player.getUsedItemHand());
        if (event.isCancelled()) {
            ci.cancel();
        } else {
            this.impl$ignorePackets++;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleAnimate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"),
            cancellable = true)
    private void impl$throwAnimationEvent(final ServerboundSwingPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = SpongeCommon.getServer().getTickCount();
        SpongeCommonEventFactory.lastAnimationPlayer = new WeakReference<>(this.player);

        if (!((ServerPlayerGameModeAccessor) this.player.gameMode).accessor$isDestroyingBlock()) {
            if (this.impl$ignorePackets > 0) {
                this.impl$ignorePackets--;
            } else {
                if (ShouldFire.INTERACT_ITEM_EVENT_PRIMARY) {
                    final Vec3 startPos = this.player.getEyePosition(1);
                    final Vec3 endPos = startPos.add(this.player.getLookAngle().scale(5d)); // TODO hook for blockReachDistance?
                    HitResult result = this.player.getLevel().clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this.player));
                    if (result.getType() == HitResult.Type.MISS) {
                        final ItemStack heldItem = this.player.getItemInHand(packetIn.getHand());
                        SpongeCommonEventFactory.callInteractItemEventPrimary(this.player, heldItem, packetIn.getHand());
                    }
                }
            }
        }

        if (ShouldFire.ANIMATE_HAND_EVENT) {
            final HandType handType = (HandType) (Object) packetIn.getHand();
            final ItemStack heldItem = this.player.getItemInHand(packetIn.getHand());

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
                frame.addContext(EventContextKeys.USED_HAND, handType);
                final AnimateHandEvent event =
                        SpongeEventFactory.createAnimateHandEvent(frame.currentCause(), handType, (Humanoid) this.player);
                if (SpongeCommon.postEvent(event)) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Z)Z"))
    public void impl$dropItem(final ServerboundPlayerActionPacket p_147345_1_, final CallbackInfo ci) {
        this.impl$ignorePackets++;
    }

    @Redirect(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;handleBlockBreakAction(Lnet/minecraft/core/BlockPos;Lnet/minecraft/network/protocol/game/ServerboundPlayerActionPacket$Action;Lnet/minecraft/core/Direction;I)V"))
    public void impl$callInteractBlockPrimaryEvent(final ServerPlayerGameMode playerInteractionManager, final BlockPos p_225416_1_,
            final ServerboundPlayerActionPacket.Action p_225416_2_, final Direction p_225416_3_, final int p_225416_4_) {
        final BlockSnapshot snapshot = ((org.spongepowered.api.world.server.ServerWorld) (playerInteractionManager.level)).createSnapshot(VecHelper.toVector3i(p_225416_1_));
        final InteractBlockEvent.Primary event = SpongeCommonEventFactory.callInteractBlockEventPrimary(p_225416_2_, this.player, this.player.getItemInHand(
                InteractionHand.MAIN_HAND), snapshot, InteractionHand.MAIN_HAND, p_225416_3_);
        if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            this.player.connection.send(new ClientboundBlockBreakAckPacket(p_225416_1_, playerInteractionManager.level.getBlockState(p_225416_1_), p_225416_2_, false, "block action restricted"));
            this.impl$ignorePackets++;
        } else {
            if (p_225416_2_ == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                if (!Objects.equals(((ServerPlayerGameModeAccessor) playerInteractionManager).accessor$destroyPos(), p_225416_1_)) {
                    return; // prevents Mismatch in destroy block pos warning
                }
            }
            playerInteractionManager.handleBlockBreakAction(p_225416_1_, p_225416_2_, p_225416_3_, p_225416_4_);
            if (p_225416_2_ == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                this.impl$ignorePackets++;
            }
        }
    }

    @Redirect(
            method = "handleClientCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;"
            )
    )
    private net.minecraft.server.level.ServerPlayer impl$usePlayerDimensionForRespawn(final PlayerList playerList, final net.minecraft.server.level.ServerPlayer player,
            final boolean keepAllPlayerData) {
        // A few changes to Vanilla logic here that, by default, still preserve game mechanics:
        // - If we have conquered The End then keep the dimension type we're headed to (which is Overworld as of 1.15)
        // - Otherwise, check the platform hooks for which dimension to respawn to. In Sponge, this is the Player's dimension they
        //   are already in if we can respawn there which is only true for Overworld dimensions
        final ResourceKey<Level> respawnDimension = player.getRespawnDimension();
        final @Nullable ServerLevel destinationWorld = this.server.getLevel(respawnDimension);
        final ServerLevel overworld = this.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Somehow the Overworld is not retrievable while trying to respawn player " + player.getGameProfile().getName());
        }
        final ServerLevel destination = destinationWorld == null ? overworld : destinationWorld;
        final RespawnPlayerEvent.SelectWorld event =
                SpongeEventFactory.createRespawnPlayerEventSelectWorld(PhaseTracker.getCauseStackManager().currentCause(),
                        (org.spongepowered.api.world.server.ServerWorld) destination,
                        (org.spongepowered.api.world.server.ServerWorld) player.getLevel(),
                        (org.spongepowered.api.world.server.ServerWorld) overworld,
                        (ServerPlayer) player);
        SpongeCommon.postEvent(event);
        ((PlayerListBridge) this.server.getPlayerList()).bridge$setOriginalDestinationDimension(((ServerLevel) event.originalDestinationWorld()).dimension());
        ((PlayerListBridge) this.server.getPlayerList()).bridge$setNewDestinationDimension(((ServerLevel) event.destinationWorld()).dimension());
        // The key is reset to null in the overwrite
        return playerList.respawn(player, keepAllPlayerData);
    }

    @Redirect(method = "onDisconnect", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    public void impl$handlePlayerDisconnect(final PlayerList playerList, final net.minecraft.network.chat.Component component, final ChatType chatType, UUID uuid) {
        // If this happens, the connection has not been fully established yet so we've kicked them during ClientConnectionEvent.Login,
        // but FML has created this handler earlier to send their handshake. No message should be sent, no disconnection event should
        // be fired either.
        if (this.player.connection == null) {
            return;
        }
        final ServerPlayer spongePlayer = (ServerPlayer) this.player;

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this.player);
            final Component message = SpongeAdventure.asAdventure(component);
            Audience audience = Sponge.server().broadcastAudience();
            final ServerSideConnectionEvent.Disconnect event = SpongeEventFactory.createServerSideConnectionEventDisconnect(
                    PhaseTracker.getCauseStackManager().currentCause(), audience, Optional.of(audience), message, message,
                    spongePlayer.connection(), spongePlayer);
            SpongeCommon.postEvent(event);
            event.audience().ifPresent(a -> a.sendMessage(spongePlayer, event.message()));
        }

        ((ServerPlayerBridge) this.player).bridge$getWorldBorderListener().onPlayerDisconnect();
    }

    @Redirect(method = "handleSignUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;filterTextPacket(Ljava/util/List;Ljava/util/function/Consumer;)V"))
    private void impl$switchToSignPhaseState(ServerGamePacketListenerImpl serverPlayNetHandler, List<String> p_244537_1_, Consumer<List<String>> p_244537_2_) {
        try (final BasicPacketContext context = PacketPhase.General.UPDATE_SIGN.createPhaseContext(PhaseTracker.getInstance())
                .packetPlayer(this.player)
                .buildAndSwitch()
        ) {

            this.shadow$filterTextPacket(p_244537_1_, p_244537_2_);
        }
    }

    @Redirect(method = "updateSignText", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
    private int impl$callChangeSignEvent(final List<String> list, ServerboundSignUpdatePacket p_244542_1_, List<String> p_244542_2_) {
        final SignBlockEntity blockEntity = (SignBlockEntity) this.player.level.getBlockEntity(p_244542_1_.getPos());
        final ListValue<Component> originalLinesValue = ((Sign) blockEntity).getValue(Keys.SIGN_LINES)
                .orElseGet(() -> new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.of()));

        final List<Component> newLines = new ArrayList<>();
        for (final String line : list) {
            // Sponge Start - While Vanilla does some strip formatting, it doesn't catch everything. This patches an exploit that allows color
            // signs to be created.
            newLines.add(Component.text(SharedConstants.filterText(line)));
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this.player);
            final ListValue.Mutable<Component> newLinesValue = ListValue.mutableOf(Keys.SIGN_LINES, newLines);
            final ChangeSignEvent event = SpongeEventFactory.createChangeSignEvent(PhaseTracker.getCauseStackManager().currentCause(),
                    originalLinesValue.asImmutable(), newLinesValue,
                    (Sign) blockEntity);
            final ListValue<Component> toApply = SpongeCommon.postEvent(event) ? originalLinesValue : newLinesValue;
            ((Sign) blockEntity).offer(toApply);
        }

        return 0;
    }

}
