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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.STabCompletePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.AnimateHandEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.network.play.client.CPlayerPacketAccessor;
import org.spongepowered.common.bridge.network.NetworkManagerHolderBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierBasedRegistrar;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.data.value.ImmutableSpongeListValue;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin implements NetworkManagerHolderBridge {

    private static final String[] IMPL$ZERO_LENGTH_STRING_ARRAY = new String[0];
    private static final String[] IMPL$EMPTY_COMMAND_ARRAY = new String[] { "" };

    // @formatter:off
    @Shadow @Final public NetworkManager connection;
    @Shadow public ServerPlayerEntity player;
    @Shadow @Final private MinecraftServer server;
    @Shadow private Vector3d awaitingPositionFromClient;
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow private int receivedMovePacketCount;
    @Shadow private int knownMovePacketCount;
    @Shadow private int tickCount;
    @Shadow private int awaitingTeleportTime;

    @Shadow protected abstract boolean shadow$isSingleplayerOwner();
    @Shadow public abstract void shadow$teleport(double x, double y, double z, float yaw, float pitch);
    // @formatter:on

    @Nullable private Entity impl$targetedEntity = null;

    @Override
    public NetworkManager bridge$getConnection() {
        return this.connection;
    }

    @Inject(method = "handleCustomCommandSuggestions", at = @At(value = "NEW", target = "com/mojang/brigadier/StringReader", remap = false),
            cancellable = true)
    private void impl$getSuggestionsFromNonBrigCommand(final CTabCompletePacket p_195518_1_, final CallbackInfo ci) {
        final String rawCommand = p_195518_1_.getCommand();
        final String[] command = this.impl$extractCommandString(rawCommand);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this.player);
            final CommandCause cause = CommandCause.create();
            final SpongeCommandManager manager = ((SpongeCommandManager) Sponge.getCommandManager());
            if (!rawCommand.contains(" ")) {
                final SuggestionsBuilder builder = new SuggestionsBuilder(command[0], 0);
                if (command[0].isEmpty()) {
                    manager.getAliasesForCause(cause).forEach(builder::suggest);
                } else {
                    manager.getAliasesThatStartWithForCause(cause, command[0]).forEach(builder::suggest);
                }
                this.connection.send(new STabCompletePacket(p_195518_1_.getId(), builder.build()));
                ci.cancel();
            } else {
                final Optional<CommandMapping> mappingOptional =
                        manager.getCommandMapping(command[0].toLowerCase(Locale.ROOT)).filter(x -> !(x.getRegistrar() instanceof BrigadierBasedRegistrar));
                if (mappingOptional.isPresent()) {
                    final CommandMapping mapping = mappingOptional.get();
                    if (mapping.getRegistrar().canExecute(cause, mapping)) {
                        try {
                            final SuggestionsBuilder builder = new SuggestionsBuilder(rawCommand, rawCommand.lastIndexOf(" ") + 1);
                            mapping.getRegistrar().suggestions(cause, mapping, command[0], command[1]).forEach(builder::suggest);
                            this.connection.send(new STabCompletePacket(p_195518_1_.getId(), builder.build()));
                        } catch (final CommandException e) {
                            cause.sendMessage(Identity.nil(), Component.text("Unable to create suggestions for your tab completion"));
                            this.connection.send(new STabCompletePacket(p_195518_1_.getId(), Suggestions.empty().join()));
                        }
                    } else {
                        this.connection.send(new STabCompletePacket(p_195518_1_.getId(), Suggestions.empty().join()));
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "handleCustomCommandSuggestions",
        at = @At(value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;",
            remap = false
        )
    )
    private ParseResults<CommandSource> impl$informParserThisIsASuggestionCheck(final CommandDispatcher<CommandSource> commandDispatcher,
            final StringReader command,
            final Object source) {
        return BrigadierCommandRegistrar.INSTANCE.getDispatcher().parse(command, (CommandSource) source, true);
    }

    /**
     * Specifically hooks the reach distance to use the forge hook.
     */
    @ModifyConstant(
        method = "handleInteract",
        constant = @Constant(doubleValue = 36.0D)
    )
    private double impl$getPlatformReach(final double thirtySix) {
        final Entity targeted = this.impl$targetedEntity;
        this.impl$targetedEntity = null;
        return PlatformHooks.INSTANCE.getGeneralHooks().getEntityReachDistanceSq(this.player, targeted);
    }

    /**
     * Effectively, hooking into the following code block:
     * <pre>
     *       if (isMovePlayerPacketInvalid(packetIn)) {
     *          this.disconnect(new TranslationTextComponent("multiplayer.disconnect.invalid_player_movement"));
     *       } else {
     *          ServerWorld serverworld = this.server.getWorld(this.player.dimension);
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
            target = "Lnet/minecraft/network/play/ServerPlayNetHandler;awaitingPositionFromClient:Lnet/minecraft/util/math/vector/Vector3d;"
        ),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/entity/player/ServerPlayerEntity;wonGame:Z"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/network/play/ServerPlayNetHandler;tickCount:I",
                ordinal = 1
            )
        ),
        cancellable = true
    )
    private void impl$callMoveEntityEvent(final CPlayerPacket packetIn, final CallbackInfo ci) {

        // If the movement is modified we pretend that the player has queuedEndExit = true
        // so that vanilla wont process that packet further

        final CPlayerPacketAccessor packetInAccessor = (CPlayerPacketAccessor) packetIn;

        // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
        // We don't fire an event to avoid confusing plugins.
        if (!packetInAccessor.accessor$hasPos() && !packetInAccessor.accessor$hasRot()) {
            return;
        }

        final boolean goodMovementPacket = this.receivedMovePacketCount - this.knownMovePacketCount <= 5;
        final boolean fireMoveEvent = goodMovementPacket && packetInAccessor.accessor$hasPos() && ShouldFire.MOVE_ENTITY_EVENT;
        final boolean fireRotationEvent = goodMovementPacket && packetInAccessor.accessor$hasRot() && ShouldFire.ROTATE_ENTITY_EVENT;

        final ServerPlayer player = (ServerPlayer) this.player;
        final org.spongepowered.math.vector.Vector3d fromRotation = new org.spongepowered.math.vector.Vector3d(packetIn.getYRot(this.player
                .yRot), packetIn.getXRot(this.player.xRot), 0);

        // Use the position of the last movement with an event or the current player position if never called
        // We need this because we ignore very small position changes as to not spam as many move events.
        final org.spongepowered.math.vector.Vector3d fromPosition = player.getPosition();

        org.spongepowered.math.vector.Vector3d toPosition = new org.spongepowered.math.vector.Vector3d(packetIn.getX(this.player.getX()),
                packetIn.getY(this.player.getY()), packetIn.getZ(this.player.getZ()));
        org.spongepowered.math.vector.Vector3d toRotation = new org.spongepowered.math.vector.Vector3d(packetIn.getYRot(this.player.yRot),
                packetIn.getXRot(this.player.xRot), 0);

        final boolean significantRotation = fromRotation.distanceSquared(toRotation) > (.15f * .15f);

        final org.spongepowered.math.vector.Vector3d originalToPosition = toPosition;
        boolean cancelMovement = false;
        boolean cancelRotation = false;
        // Call move & rotate event as needed...
        if (fireMoveEvent) {
            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (ServerPlayer) this.player, fromPosition,
                    toPosition, toPosition);
            if (SpongeCommon.postEvent(event)) {
                cancelMovement = true;
            } else {
                toPosition = event.getDestinationPosition();
            }
        }

        if (significantRotation && fireRotationEvent) {
            final RotateEntityEvent event = SpongeEventFactory.createRotateEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), (ServerPlayer) this.player, fromRotation,
                    toRotation);
            if (SpongeCommon.postEvent(event)) {
                cancelRotation = true;
            } else {
                toRotation = event.getToRotation();
            }
        }

        // At this point, we cancel out and let the "confirmed teleport" code run through to update the
        // player position and update the player's relation in the chunk manager.
        if (cancelMovement) {
            if (packetInAccessor.accessor$hasRot() && !cancelRotation) {
                // Rest the rotation here
                ((EntityAccessor) this.player).invoker$setRot((float) toRotation.getX(), (float) toRotation.getY());
            }
            final float yaw = packetInAccessor.accessor$hasRot() && !cancelRotation ? (float) toRotation.getX() : this.player.yRot;
            final float pitch = packetInAccessor.accessor$hasRot() && !cancelRotation ? (float) toRotation.getY() : this.player.xRot;
            this.awaitingTeleportTime = this.tickCount;
            // Then, we set the location, as if the player was teleporting
            this.shadow$teleport(fromPosition.getX(), fromPosition.getY(), fromPosition.getZ(), yaw, pitch);
            ci.cancel();
            return;
        }

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
                ((EntityAccessor) this.player).invoker$setRot((float) toRotation.getX(), (float) toRotation.getY());
                // And reset the position update so the force set is done.
                this.awaitingTeleportTime = this.tickCount - Constants.Networking.MAGIC_TRIGGER_TELEPORT_CONFIRM_DIFF;
            } else {
                // otherwise, set the data back onto the packet
                packetInAccessor.accessor$hasPos(true);
                packetInAccessor.accessor$x(toPosition.getX());
                packetInAccessor.accessor$y(toPosition.getY());
                packetInAccessor.accessor$z(toPosition.getZ());
            }
        }
    }

    @Inject(method = "handleInteract", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/vector/Vector3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void impl$onRightClickAtEntity(final CUseEntityPacket packetIn, final CallbackInfo ci, final ServerWorld serverworld, final Entity entity) {
        final ItemStack itemInHand = packetIn.getHand() == null ? ItemStack.EMPTY : this.player.getItemInHand(packetIn.getHand());
        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory
                .callInteractEntityEventSecondary(this.player, itemInHand, entity, packetIn.getHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void impl$onLeftClickEntity(final CUseEntityPacket packetIn, final CallbackInfo ci, final ServerWorld serverworld, final Entity entity) {
        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player,
                this.player.getItemInHand(this.player.getUsedItemHand()), entity, this.player.getUsedItemHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * In production, the ServerWorld is lost.
     */
    @SuppressWarnings("Duplicates")
    @Surrogate
    public void impl$onLeftClickEntity(final CUseEntityPacket packetIn, final CallbackInfo ci, final Entity entity) {
        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player,
                this.player.getItemInHand(this.player.getUsedItemHand()), entity, this.player.getUsedItemHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleAnimate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;resetLastActionTime()V"),
            cancellable = true)
    private void impl$throwAnimationEvent(final CAnimateHandPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = SpongeCommon.getServer().getTickCount();
        SpongeCommonEventFactory.lastAnimationPlayer = new WeakReference<>(this.player);
        if (ShouldFire.ANIMATE_HAND_EVENT) {
            final HandType handType = (HandType) (Object) packetIn.getHand();
            final ItemStack heldItem = this.player.getItemInHand(packetIn.getHand());

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM.get(), ItemStackUtil.snapshotOf(heldItem));
                frame.addContext(EventContextKeys.USED_HAND.get(), handType);
                final AnimateHandEvent event =
                        SpongeEventFactory.createAnimateHandEvent(frame.getCurrentCause(), handType, (Humanoid) this.player);
                if (SpongeCommon.postEvent(event)) {
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(
        method = "handleClientCommand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/management/PlayerList;respawn(Lnet/minecraft/entity/player/ServerPlayerEntity;Z)Lnet/minecraft/entity/player/ServerPlayerEntity;"
        )
    )
    private ServerPlayerEntity impl$usePlayerDimensionForRespawn(final PlayerList playerList, final ServerPlayerEntity player,
            final boolean keepAllPlayerData) {
        // A few changes to Vanilla logic here that, by default, still preserve game mechanics:
        // - If we have conquered The End then keep the dimension type we're headed to (which is Overworld as of 1.15)
        // - Otherwise, check the platform hooks for which dimension to respawn to. In Sponge, this is the Player's dimension they
        //   are already in if we can respawn there which is only true for Overworld dimensions
        final RegistryKey<World> respawnDimension = player.getRespawnDimension();
        final @Nullable ServerWorld destinationWorld = this.server.getLevel(respawnDimension);
        final ServerWorld overworld = this.server.getLevel(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Somehow the Overworld is not retrievable while trying to respawn player " + player.getGameProfile().getName());
        }
        final ServerWorld destination = destinationWorld == null ? overworld : destinationWorld;
        final RespawnPlayerEvent.SelectWorld event =
                SpongeEventFactory.createRespawnPlayerEventSelectWorld(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (org.spongepowered.api.world.server.ServerWorld) destination,
                        (org.spongepowered.api.world.server.ServerWorld) player.getLevel(),
                        (org.spongepowered.api.world.server.ServerWorld) overworld,
                        (ServerPlayer) player);
        SpongeCommon.postEvent(event);
        ((PlayerListBridge) this.server.getPlayerList()).bridge$setNewDestinationDimensionKey(((ServerWorld) event.getDestinationWorld()).dimension());
        // The key is reset to null in the overwrite
        return playerList.respawn(player, keepAllPlayerData);
    }

    @SuppressWarnings("deprecation")
    @Redirect(method = "handleSignUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/CUpdateSignPacket;getLines()[Ljava/lang/String;"))
    private String[] impl$callChangeSignEvent(final CUpdateSignPacket packet) {
        final @Nullable ServerWorld world = this.server.getLevel(this.player.getLevel().dimension());
        if (world == null) {
            return new String[] {};
        }
        final BlockPos position = packet.getPos();
        if (!world.hasChunkAt(position)) {
            return new String[] {};
        }
        final @Nullable SignTileEntity sign = (SignTileEntity) world.getBlockEntity(position);
        if (sign == null) {
            return new String[] {};
        }
        final ListValue<Component> originalLinesValue = ((Sign) sign).getValue(Keys.SIGN_LINES)
            .orElseGet(() -> new ImmutableSpongeListValue<>(Keys.SIGN_LINES.get(), ImmutableList.of()));

        final List<Component> newLines = new ArrayList<>();
        for (final String line : packet.getLines()) {
            newLines.add(Component.text(SharedConstants.filterText(line)));
        }

        final ListValue.Mutable<Component> newLinesValue = ListValue.mutableOf(Keys.SIGN_LINES.get(), newLines);
        final ChangeSignEvent event = SpongeEventFactory.createChangeSignEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                originalLinesValue.asImmutable(), newLinesValue,
                (Sign) sign);
        final ListValue<Component> toApply = SpongeCommon.postEvent(event) ? originalLinesValue : newLinesValue;
        ((Sign) sign).offer(toApply);

        return ServerPlayNetHandlerMixin.IMPL$ZERO_LENGTH_STRING_ARRAY;
    }

    private String[] impl$extractCommandString(final String commandString) {
        if (commandString.isEmpty()) {
            return ServerPlayNetHandlerMixin.IMPL$EMPTY_COMMAND_ARRAY;
        }
        if (commandString.startsWith("/")) {
            return commandString.substring(1).split(" ", 2);
        }
        return commandString.split(" ", 2);
    }
}
