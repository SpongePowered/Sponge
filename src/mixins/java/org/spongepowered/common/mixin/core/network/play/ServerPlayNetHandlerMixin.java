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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.data.value.Value;
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
import org.spongepowered.api.util.Transform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.play.client.CPlayerPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.network.NetworkManagerHolderBridge;
import org.spongepowered.common.bridge.server.management.PlayerListBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierBasedRegistrar;
import org.spongepowered.common.command.registrar.BrigadierCommandRegistrar;
import org.spongepowered.common.data.value.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.MutableSpongeListValue;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Mixin(ServerPlayNetHandler.class)
public abstract class ServerPlayNetHandlerMixin implements NetworkManagerHolderBridge {

    private static final String[] IMPL$ZERO_LENGTH_STRING_ARRAY = new String[0];
    private static final String[] IMPL$EMPTY_COMMAND_ARRAY = new String[] { "" };

    @Shadow @Final public NetworkManager netManager;
    @Shadow public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;
    @Nullable private Vector3d impl$lastMovePosition = null;
    @Nullable private Entity impl$targetedEntity = null;

    private boolean impl$justTeleported = false;

    @Override
    public NetworkManager bridge$getNetworkManager() {
        return this.netManager;
    }

    @Inject(method = "processTabComplete", at = @At(value = "NEW", target = "com/mojang/brigadier/StringReader", remap = false),
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
                this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), builder.build()));
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
                            this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), builder.build()));
                        } catch (final CommandException e) {
                            cause.sendMessage(Identity.nil(), Component.text("Unable to create suggestions for your tab completion"));
                            this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), Suggestions.empty().join()));
                        }
                    } else {
                        this.netManager.sendPacket(new STabCompletePacket(p_195518_1_.getTransactionId(), Suggestions.empty().join()));
                    }
                    ci.cancel();
                }
            }
        }
    }

    @Redirect(method = "processTabComplete",
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
     * A workaround to resolve <a href="https://bugs.mojang.com/browse/MC-107103">MC-107103</a>
     * since the server will expect the client is trying to interact with the "eyes" of the entity.
     * If the check is desired, {@link #impl$getPlatformReach(double)}
     * is where the seen check should be done.
     */
    @Redirect(
        method = "processUseEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ServerPlayerEntity;canEntityBeSeen(Lnet/minecraft/entity/Entity;)Z"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/player/ServerPlayerEntity;markPlayerActive()V"
            ),
            to = @At(
                value = "CONSTANT",
                args = "doubleValue=36.0D"
            )
        )
    )
    private boolean impl$preventMC_107103(final ServerPlayerEntity serverPlayerEntity, final Entity entityIn) {
        this.impl$targetedEntity = entityIn;
        return true;
    }

    /**
     * Specifically hooks the reach distance to use the forge hook.
     */
    @ModifyConstant(
        method = "processUseEntity",
        constant = @Constant(doubleValue = 36.0D)
    )
    private double impl$getPlatformReach(final double thirtySix) {
        final Entity targeted = this.impl$targetedEntity;
        this.impl$targetedEntity = null;
        return PlatformHooks.getInstance().getGeneralHooks().getEntityReachDistanceSq(this.player, targeted);
    }

    @Redirect(method = "processPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;queuedEndExit:Z"))
    private boolean impl$callMoveEvents(final ServerPlayerEntity playerMP, final CPlayerPacket packetIn) {
        if (playerMP.queuedEndExit) {
            return true;
        }
        // If the movement is modified we pretend that the player has queuedEndExit = true
        // so that vanilla wont process that packet further

        final CPlayerPacketAccessor packetInAccessor = (CPlayerPacketAccessor) packetIn;
        // During login, minecraft sends a packet containing neither the 'moving' or 'rotating' flag set - but only once.
        // We don't fire an event to avoid confusing plugins.
        if (!packetInAccessor.accessor$getMoving() && !packetInAccessor.accessor$getRotating()) {
            return false;
        }

        final ServerPlayer player = (ServerPlayer) this.player;
        final ServerPlayerEntityBridge mixinPlayer = (ServerPlayerEntityBridge) this.player;
        final Vector3d fromRotation = player.getRotation();
        // Use the position of the last movement with an event or the current player position if never called
        // We need this because we ignore very small position changes as to not spam as many move events.
        final Vector3d fromPosition = this.impl$lastMovePosition != null ? this.impl$lastMovePosition : player.getPosition();

        Vector3d toPosition = new Vector3d(packetInAccessor.accessor$getX(), packetInAccessor.accessor$getY(), packetInAccessor.accessor$getZ());
        Vector3d toRotation = new Vector3d(packetInAccessor.accessor$getPitch(), packetInAccessor.accessor$getYaw(), 0);

        // If we have zero movement, we have rotation only, we might as well note that now.
        final boolean zeroMovement = !packetInAccessor.accessor$getMoving() || toPosition.equals(fromPosition);

        // Minecraft does the same with rotation when it's only a positional update
        // Branch executed for CPacketPlayer.Position
        boolean fireMoveEvent = packetInAccessor.accessor$getMoving() && !packetInAccessor.accessor$getRotating();
        if (fireMoveEvent) {
            // Correct the new rotation to match the old rotation
            toRotation = fromRotation;
            fireMoveEvent = !zeroMovement && ShouldFire.MOVE_ENTITY_EVENT;
        }

        // Minecraft sends a 0, 0, 0 position when rotation only update occurs, this needs to be recognized and corrected
        // Branch executed for CPacketPlayer.Rotation
        boolean fireRotationEvent = !packetInAccessor.accessor$getMoving() && packetInAccessor.accessor$getRotating();
        if (fireRotationEvent) {
            toPosition = fromPosition; // Rotation only update
            fireRotationEvent = ShouldFire.ROTATE_ENTITY_EVENT;
        }

        // Branch executed for CPacketPlayer.PositionRotation
        if (packetInAccessor.accessor$getMoving() && packetInAccessor.accessor$getRotating()) {
            fireMoveEvent = !zeroMovement && ShouldFire.MOVE_ENTITY_EVENT;
            fireRotationEvent = ShouldFire.ROTATE_ENTITY_EVENT;
        }

        mixinPlayer.bridge$setVelocityOverride(toPosition.sub(fromPosition));

        // These magic numbers are sad but help prevent excessive lag from this event.
        // eventually it would be nice to not have them
        final boolean significantMovement = !zeroMovement && toPosition.distanceSquared(fromPosition)  > ((1f / 16) * (1f / 16));
        final boolean significantRotation = fromRotation.distanceSquared(toRotation) > (.15f * .15f);

        final Transform originalToTransform = Transform.of(toPosition, toRotation);

        // Call move & rotate event as needed...
        if (significantMovement && fireMoveEvent) {
            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(PhaseTracker.SERVER.getCurrentCause(), player, fromPosition, toPosition, toPosition);
            if (SpongeCommon.postEvent(event)) {
                this.impl$lastMovePosition = event.getOriginalPosition();
                toPosition = fromPosition;
            } else {
                toPosition = event.getDestinationPosition();
            }
        }

        if (significantRotation && fireRotationEvent) {
            final RotateEntityEvent event = SpongeEventFactory.createRotateEntityEvent(PhaseTracker.SERVER.getCurrentCause(), player, fromRotation, toRotation);
            if (SpongeCommon.postEvent(event)) {
                toRotation = fromRotation;
            } else {
                toRotation = event.getToRotation();
            }
        }

        final Transform toTransform = Transform.of(toPosition, toRotation);

        // Handle event results
        if (!toTransform.equals(originalToTransform)) {
            // event changed position or rotation (includes cancel)
            ((EntityBridge) mixinPlayer).bridge$setLocationAndAngles(toTransform);
            this.impl$lastMovePosition = toPosition;
            mixinPlayer.bridge$setVelocityOverride(null);
            return true;
        } else if (!fromPosition.equals(player.getPosition()) && this.impl$justTeleported) {
            // Teleport during move event - prevent vanilla from causing odd behaviors after this
            this.impl$justTeleported = false;
            this.impl$lastMovePosition = player.getLocation().getPosition();
            mixinPlayer.bridge$setVelocityOverride(null);
            return true;
        } else { // Normal movement - Just update lastMovePosition
            this.impl$lastMovePosition = toPosition;
        }
        // TODO ??? this.bridge$resendLatestResourcePackRequest();
        return false; // Continue with vanilla movement processing
    }

    @Inject(method = "processUseEntity", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;applyPlayerInteraction(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void impl$onRightClickAtEntity(CUseEntityPacket packetIn, CallbackInfo ci, ServerWorld serverworld, Entity entity) {
        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory
                .callInteractEntityEventSecondary(this.player, this.player.getHeldItem(packetIn.getHand()), entity, packetIn.getHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "processUseEntity", cancellable = true,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void impl$onLeftClickEntity(CUseEntityPacket packetIn, CallbackInfo ci, ServerWorld serverworld, Entity entity) {
        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player,
                this.player.getHeldItem(this.player.getActiveHand()), entity, this.player.getActiveHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * In production, the ServerWorld is lost.
     */
    @SuppressWarnings("Duplicates")
    @Surrogate
    public void impl$onLeftClickEntity(CUseEntityPacket packetIn, CallbackInfo ci, Entity entity) {
        final InteractEntityEvent.Primary event = SpongeCommonEventFactory.callInteractEntityEventPrimary(this.player,
                this.player.getHeldItem(this.player.getActiveHand()), entity, this.player.getActiveHand(), null);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handleAnimation",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;markPlayerActive()V"),
            cancellable = true)
    private void impl$throwAnimationEvent(final CAnimateHandPacket packetIn, final CallbackInfo ci) {
        if (PhaseTracker.getInstance().getPhaseContext().isEmpty()) {
            return;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = SpongeCommon.getServer().getTickCounter();
        SpongeCommonEventFactory.lastAnimationPlayer = new WeakReference<>(this.player);
        if (ShouldFire.ANIMATE_HAND_EVENT) {
            final HandType handType = (HandType) (Object) packetIn.getHand();
            final ItemStack heldItem = this.player.getHeldItem(packetIn.getHand());

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(heldItem));
                frame.addContext(EventContextKeys.USED_HAND, handType);
                final AnimateHandEvent event =
                        SpongeEventFactory.createAnimateHandEvent(frame.getCurrentCause(), handType, (Humanoid) this.player);
                if (SpongeCommon.postEvent(event)) {
                    ci.cancel();
                }
            }
        }
    }

    @ModifyArg(method = "processClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;"
            + "recreatePlayerEntity(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/dimension/DimensionType;Z)"
            + "Lnet/minecraft/entity/player/ServerPlayerEntity;", ordinal = 1))
    private DimensionType impl$usePlayerDimensionForRespawn(final ServerPlayerEntity entity, final DimensionType dimensionType,
            boolean conqueredEnd) {
        // A few changes to Vanilla logic here that, by default, still preserve game mechanics:
        // - If we have conquered The End then keep the dimension type we're headed to (which is Overworld as of 1.15)
        // - Otherwise, check the platform hooks for which dimension to respawn to. In Sponge, this is the Player's dimension they
        //   are already in if we can respawn there which is only true for Overworld dimensions
        if (conqueredEnd) {
            return dimensionType;
        }

        final DimensionType respawnDimension = PlatformHooks.getInstance().getDimensionHooks().getRespawnDimension(entity, dimensionType, false);
        final ServerWorld destinationWorld = this.server.getWorld(respawnDimension);
        final RespawnPlayerEvent.SelectWorld event =
                SpongeEventFactory.createRespawnPlayerEventSelectWorld(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        (org.spongepowered.api.world.server.ServerWorld) (Object) destinationWorld,
                        (org.spongepowered.api.world.server.ServerWorld) (Object) this.server.getWorld(entity.dimension),
                        (org.spongepowered.api.world.server.ServerWorld) (Object) this.server.getWorld(DimensionType.OVERWORLD),
                        (ServerPlayer) entity);
        SpongeCommon.postEvent(event);
        return ((ServerWorld) event.getDestinationWorld()).getDimension().getType();
    }

    @Redirect(method = "processClientStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerList;recreatePlayerEntity(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/world/dimension/DimensionType;Z)Lnet/minecraft/entity/player/ServerPlayerEntity;"))
    private ServerPlayerEntity impl$setOriginalDestinationRef(PlayerList playerList, ServerPlayerEntity playerIn, DimensionType dimension, boolean conqueredEnd) {
        ((PlayerListBridge) playerList).bridge$setOriginalDestinationDimensionForRespawn(dimension);
        return playerList.recreatePlayerEntity(playerIn, dimension, conqueredEnd);
    }

    @Redirect(method = "processUpdateSign", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/client/CUpdateSignPacket;getLines()[Ljava/lang/String;"))
    private String[] impl$callChangeSignEvent(CUpdateSignPacket packet) {
        final ServerWorld world = this.server.getWorld(this.player.dimension);
        final BlockPos position = packet.getPosition();
        final SignTileEntity sign = (SignTileEntity) world.getTileEntity(position);

        final ListValue<Component> originalLinesValue = ((Sign) sign).getValue(Keys.SIGN_LINES).orElse(null);
        final List<Component> newLines = new ArrayList<>();
        for (final String line : packet.getLines()) {
            newLines.add(SpongeAdventure.legacySection(line));
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
