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
package org.spongepowered.common.mixin.core.server.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Either;
import io.netty.channel.Channel;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.ConnectionAccessor;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.accessor.server.level.ChunkMap_TrackedEntityAccessor;
import org.spongepowered.common.accessor.server.level.ServerPlayerAccessor;
import org.spongepowered.common.accessor.server.network.ServerCommonPacketListenerImplAccessor;
import org.spongepowered.common.accessor.world.level.portal.TeleportTransitionAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.TransientBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;
import org.spongepowered.common.bridge.world.entity.player.BedLocationHolderBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.entity.player.ClientType;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.player.PlayerMixin;
import org.spongepowered.common.network.packet.SpongePacketHandler;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// See also: SubjectMixin_API and SubjectMixin
@SuppressWarnings("ConstantConditions")
@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements SubjectBridge, ServerPlayerBridge, BedLocationHolderBridge {

    // @formatter:off
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int lastRecordedExperience;
    @Shadow @javax.annotation.Nullable private net.minecraft.server.level.ServerPlayer.RespawnConfig respawnConfig;

    @Shadow public abstract ServerLevel shadow$level();
    @Shadow public abstract void shadow$doCloseContainer();
    @Shadow public abstract boolean shadow$setGameMode(GameType param0);
    @Shadow public abstract void shadow$setCamera(@org.jetbrains.annotations.Nullable final Entity $$0);
    @Shadow public abstract void shadow$setRespawnPosition(@javax.annotation.Nullable net.minecraft.server.level.ServerPlayer.RespawnConfig config, boolean sendMessage);
    // @formatter:on

    private net.minecraft.network.chat.@Nullable Component impl$connectionMessage;
    private Locale impl$language = Locales.DEFAULT;
    private Scoreboard impl$scoreboard = Sponge.game().server().serverScoreboard().get();
    // Note that this field cannot be reset until the player has been respawned because
    // after death, the server player will remain in the death state, and only when the player
    // is actually respawned, will their inventory be transferred to the new player instance.
    @Nullable
    private Boolean impl$keepInventory = null;
    // Used to restore original item received in a packet after canceling an event
    private int impl$viewDistance;
    private int impl$skinPartMask;
    private Set<SkinPart> impl$skinParts = ImmutableSet.of();
    private final PlayerOwnBorderListener impl$borderListener = new PlayerOwnBorderListener((net.minecraft.server.level.ServerPlayer) (Object) this);
    private boolean impl$sleepingIgnored;
    private boolean impl$noGameModeEvent;
    @Nullable
    private WorldBorder impl$worldBorder;
    private ServerLevel impl$respawnLevel;
    private final Map<org.spongepowered.api.ResourceKey, RespawnLocation> impl$bedLocations = new HashMap<>();
    private boolean impl$syncingRespawn;

    @Override
    public net.minecraft.network.chat.@Nullable Component bridge$getConnectionMessageToSend() {
        if (this.impl$connectionMessage == null) {
            return net.minecraft.network.chat.Component.literal("");
        }
        return this.impl$connectionMessage;
    }

    @Override
    public void bridge$setConnectionMessageToSend(final net.minecraft.network.chat.Component message) {
        this.impl$connectionMessage = message;
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.FALSE;
    }


    @WrapOperation(
        method = "handleShoulderEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;playShoulderEntityAmbientSound(Lnet/minecraft/nbt/CompoundTag;)V"
        )
    )
    private void impl$ignoreShoulderSoundsWhileVanished(
        final net.minecraft.server.level.ServerPlayer thisPlayer, final CompoundTag tag, final Operation<Void> original
    ) {
        if (!this.bridge$vanishState().createsSounds()) {
            return;
        }
        original.call(thisPlayer, tag);
    }

    @Override
    public void bridge$refreshExp() {
        this.lastRecordedExperience = -1;
    }

    @Override
    public boolean bridge$kick(final Component message) {
        final Component messageToSend;
        if (ShouldFire.KICK_PLAYER_EVENT) {
            final KickPlayerEvent kickEvent = SpongeEventFactory.createKickPlayerEvent(PhaseTracker.getInstance().currentCause(),
                message,
                message,
                (ServerPlayer) this
            );
            if (Sponge.eventManager().post(kickEvent)) {
                return false;
            }
            messageToSend = kickEvent.message();
        } else {
            messageToSend = message;
        }
        final net.minecraft.network.chat.Component component = SpongeAdventure.asVanilla(messageToSend);
        this.connection.disconnect(component);
        return true;
    }

    @Override
    public Locale bridge$getLanguage() {
        return this.impl$language;
    }

    @Override
    public void bridge$setLanguage(final Locale language) {
        this.impl$language = language;

        // Update locale on Channel, used for sending localized messages
        if (this.connection != null) {
            final Channel channel = ((ConnectionAccessor) ((ServerCommonPacketListenerImplAccessor) this.connection).accessor$connection()).accessor$channel();
            channel.attr(SpongeAdventure.CHANNEL_LOCALE).set(language);

            this.containerMenu.broadcastFullState();
        }
    }

    @Override
    public void bridge$initScoreboard() {
        ((ServerScoreboardBridge) this.shadow$level().getScoreboard()).bridge$addPlayer((net.minecraft.server.level.ServerPlayer) (Object) this, true);
    }

    @Override
    public void bridge$removeScoreboardOnRespawn() {
        ((ServerScoreboardBridge) ((ServerPlayer) this).scoreboard()).bridge$removePlayer((net.minecraft.server.level.ServerPlayer) (Object) this, false);
    }

    @Override
    public void bridge$setScoreboardOnRespawn(final Scoreboard scoreboard) {
        this.impl$scoreboard = scoreboard;
        ((ServerScoreboardBridge) ((ServerPlayer) this).scoreboard()).bridge$addPlayer((net.minecraft.server.level.ServerPlayer) (Object) this, false);
    }

    @Override
    public Scoreboard bridge$getScoreboard() {
        return this.impl$scoreboard;
    }

    @Override
    public void bridge$replaceScoreboard(@org.checkerframework.checker.nullness.qual.Nullable Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.game().server().serverScoreboard()
                .orElseThrow(() -> new IllegalStateException("Server does not have a valid scoreboard"));
        }
        this.impl$scoreboard = scoreboard;
    }

    @Override
    public boolean bridge$keepInventory() {
        return Objects.requireNonNullElseGet(this.impl$keepInventory, () -> this.shadow$level().getGameRules().get(GameRules.KEEP_INVENTORY));
    }

    @Override
    protected void impl$dropInventoryWrapForPlayerOverride(
        final LivingEntity instance, final ServerLevel level, final Operation<Void> original
    ) {
        if (this.impl$keepInventory == null || !this.impl$keepInventory) {
            original.call(instance, level);
        }
    }

    @Override
    public int bridge$getExperiencePointsOnDeath(final LivingEntity entity, final ServerLevel $$0, final Entity $$1) {
        if (this.impl$keepInventory != null && this.impl$keepInventory) {
            return 0;
        }
        return super.bridge$getExperiencePointsOnDeath(entity, $$0, $$1);
    }

    @Override
    public int bridge$getViewDistance() {
        return this.impl$viewDistance;
    }

    @Override
    public Set<SkinPart> bridge$getSkinParts() {
        final int mask = this.shadow$getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        if (this.impl$skinPartMask != mask) {
            this.impl$skinParts = Sponge.game().registry(RegistryTypes.SKIN_PART).stream()
                .map(part -> (SpongeSkinPart) part)
                .filter(part -> part.test(mask))
                .collect(ImmutableSet.toImmutableSet());
            this.impl$skinPartMask = mask;
        }

        return this.impl$skinParts;
    }

    @Override
    public void bridge$setSkinParts(final Set<SkinPart> skinParts) {
        int mask = 0;
        for (final SkinPart part : skinParts) {
            mask |= ((SpongeSkinPart) part).getMask();
        }

        this.shadow$getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) mask);
        this.impl$skinParts = ImmutableSet.copyOf(skinParts);
        this.impl$skinPartMask = mask;
    }

    @Override
    public boolean bridge$sleepingIgnored() {
        return this.impl$sleepingIgnored;
    }

    @Override
    public void bridge$setSleepingIgnored(final boolean sleepingIgnored) {
        this.impl$sleepingIgnored = sleepingIgnored;
    }

    @Override
    public void bridge$sendSpongePacketToViewer(final org.spongepowered.api.network.channel.packet.Packet packet) {
        if (this.impl$isFake) {
            return;
        }
        final ClientType clientType = this.bridge$getClientType();
        if (clientType == ClientType.SPONGE_VANILLA || clientType == ClientType.SPONGE_FORGE) {
            SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, packet);
        }
    }

    @Override
    public void bridge$sendToViewer(final Packet<ClientGamePacketListener> packet) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(packet);
    }

    @WrapOperation(method = "setCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z"))
    private boolean impl$onSpectatorTeleport(
        final net.minecraft.server.level.ServerPlayer instance, final ServerLevel level, final double x, final double y, final double z,
        final Set<Relative> relatives, final float xRot, final float yRot, final boolean setCamera, final Operation<Boolean> original
    ) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getWorldInstance(level).pushCauseFrame()) {
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.SPECTATOR);
            return original.call(instance, level, x, y, z, relatives, xRot, yRot, setCamera);
        }
    }

    @WrapMethod(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;")
    private net.minecraft.server.level.ServerPlayer impl$wrapTeleport(final TeleportTransition transition, Operation<net.minecraft.server.level.ServerPlayer> original) {
        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance(this.shadow$level());
        try (final CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame()) {
            frame.pushCause(this);
            return original.call(transition);
        }
    }

    @ModifyVariable(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", argsOnly = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/portal/TeleportTransition;missingRespawnBlock()Z"))
    private TeleportTransition impl$beforeTeleport(TeleportTransition transition, @Share("original-new-level") final LocalRef<ServerLevel> originalNewLevelRef,
                                                   @Cancellable final CallbackInfoReturnable<net.minecraft.server.level.ServerPlayer> cir) {
        final ServerLevel originalNewLevel = transition.newLevel();
        originalNewLevelRef.set(originalNewLevel);
        transition = originalNewLevel == this.shadow$level() ? this.impl$fireTeleportSameDimension(transition) : this.impl$fireTeleportCrossDimensionBefore(transition);
        if (transition == null) {
            cir.setReturnValue(null);
        }
        return transition;
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void impl$afterTeleportCrossDimension(
        final TeleportTransition transition, final CallbackInfoReturnable<net.minecraft.server.level.ServerPlayer> cir,
        @Local(ordinal = 1) final ServerLevel oldLevel, @Share("original-new-level") LocalRef<ServerLevel> originalNewLevel
    ) {
        this.impl$fireTeleportCrossDimensionAfter(oldLevel, transition.newLevel(), originalNewLevel.get());
    }

    @Redirect(
        method = {"openMenu", "openHorseInventory"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"
        )
    )
    private void impl$closePreviousContainer(final net.minecraft.server.level.ServerPlayer self) {
        this.shadow$doCloseContainer();
    }

    @Inject(
        method = "die",
        at = @At("HEAD"),
        cancellable = true
    )
    private void impl$fireDestructEntityEvent(
        final DamageSource cause, final CallbackInfo ci,
        @Share("sponge-event") LocalRef<DestructEntityEvent.@Nullable Death> event
    ) {
        event.set(SpongeCommonEventFactory.callDestructEntityEventDeath(
            (net.minecraft.server.level.ServerPlayer) (Object) this,
            cause,
            Audiences.server()
        ));
        if (event.get().isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
        method = "die",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;"
        ),
        slice = @Slice(
            from = @At("HEAD"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;")
        )
    )
    private Object impl$onlySendMessageIfEventCallsForIt(
        Object gameRules,
        @Share("sponge-event") LocalRef<DestructEntityEvent.@Nullable Death> event
    ) {
        final var spongeEvent = event.get();
        return (Boolean) gameRules && (spongeEvent == null || !spongeEvent.isMessageCancelled());
    }

    @ModifyExpressionValue(
        method = "die",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;")
    )
    private net.minecraft.network.chat.Component impl$useEventMessageIfUnset(
        final net.minecraft.network.chat.Component original,
        @Share("sponge-event") LocalRef<DestructEntityEvent.@Nullable Death> event
    ) {
        final var spongeEvent = event.get();
        if (spongeEvent == null) {
            return original;
        }
        if (Component.IS_NOT_EMPTY.test(spongeEvent.message())) {
            return SpongeAdventure.asVanilla(spongeEvent.message());
        }

        return original;
    }

    @WrapOperation(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    private void impl$useEventAudienceToSendServerMessage(
        final PlayerList serverList, final net.minecraft.network.chat.Component message,
        final boolean isSystem, final Operation<Void> original,
        final @Share("sponge-event") LocalRef<DestructEntityEvent.@Nullable Death> event
    ) {
        final var spongeEvent = event.get();
        if (spongeEvent == null) {
            // If we don't have a sponge event, just call the original
            original.call(serverList, message, isSystem);
            return;
        }
        // Otherwise, we will use our event's prescribed audience to send the message that has
        // been modified by the event listeners.
        final Component eventMessage = spongeEvent.message();
        if (eventMessage != Component.empty()) {
            spongeEvent.audience().ifPresent(eventChannel -> eventChannel.sendMessage(eventMessage));
        }
    }

    @Inject(
        method = "die",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z")
    )
    private void impl$setKeepInventoryFromEvent(
        final DamageSource source, final CallbackInfo ci,
        final @Share("sponge-event") LocalRef<DestructEntityEvent.@Nullable Death> event
    ) {
        if (event.get() == null) {
            return;
        }
        this.impl$keepInventory = event.get().keepInventory();
    }

    @ModifyExpressionValue(
        method = "restoreFrom",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    )
    private Object tracker$useKeepFromBridge(
        Object original,
        net.minecraft.server.level.ServerPlayer corpse,
        boolean wonGame
    ) {
        final boolean keep = ((PlayerBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.shadow$getInventory().replaceWith(corpse.getInventory());
            // Clear corpse so that mods do not copy from it again
            corpse.getInventory().clearContent();
        }
        return keep;
    }

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("HEAD"))
    private void impl$copyDataOnRespawn(final net.minecraft.server.level.ServerPlayer oldPlayer, final boolean respawnFromEnd, final CallbackInfo ci) {
        // Copy Sponge data
        if (oldPlayer instanceof DataCompoundHolder oldEntity) {
            DataUtil.syncDataToTag(oldEntity);
            final CompoundTag compound = oldEntity.data$getCompound();
            this.data$setCompound(compound);
            DataUtil.syncTagToData(this);
        }

        this.impl$language = ((ServerPlayerBridge) oldPlayer).bridge$getLanguage();
        this.impl$viewDistance = ((ServerPlayerBridge) oldPlayer).bridge$getViewDistance();

        // Update boss bars
        SpongeAdventure.forEachBossBar(bar -> ((BossEventBridge) bar).bridge$replacePlayer(oldPlayer, (net.minecraft.server.level.ServerPlayer) (Object) this));

        ((ServerPlayerBridge) oldPlayer).bridge$removeScoreboardOnRespawn();
        ((ServerPlayerBridge) this).bridge$setScoreboardOnRespawn(((ServerPlayer) oldPlayer).scoreboard());
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void impl$handleClientSettings(final ClientInformation info, final CallbackInfo ci) {
        if (!ShouldFire.PLAYER_CHANGE_CLIENT_SETTINGS_EVENT) {
            return;
        }

        final Locale newLocale = LocaleCache.getLocale(info.language());

        final ImmutableSet<SkinPart> skinParts = Sponge.game().registry(RegistryTypes.SKIN_PART).stream()
            .map(part -> (SpongeSkinPart) part)
            .filter(part -> part.test(info.modelCustomisation()))
            .collect(ImmutableSet.toImmutableSet());
        final int viewDistance = info.viewDistance();

        // Post before the player values are updated
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            final ChatVisibility visibility = (ChatVisibility) (Object) info.chatVisibility();
            final PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(
                frame.currentCause(),
                visibility,
                skinParts,
                newLocale,
                (ServerPlayer) this,
                info.chatColors(),
                viewDistance);
            SpongeCommon.post(event);
        }
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void impl$updateTrackedClientSettings(final ClientInformation info, final CallbackInfo ci) {
        final Locale newLocale = LocaleCache.getLocale(info.language());

        // Update the fields we track ourselves
        this.impl$viewDistance = info.viewDistance();
        this.bridge$setLanguage(newLocale);
        this.impl$language = newLocale;
    }

    @Override
    public PlayerOwnBorderListener bridge$getWorldBorderListener() {
        return this.impl$borderListener;
    }

    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
        cancellable = true, at = @At("HEAD"))
    public void sendMessage(final net.minecraft.network.chat.Component $$0, final boolean $$1, final CallbackInfo ci) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            ci.cancel();
        }
    }

    @Inject(method = "sendChatMessage", cancellable = true, at = @At("HEAD"))
    public void sendMessage(final OutgoingChatMessage $$0, final boolean $$1, final ChatType.Bound $$2, final CallbackInfo ci) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            ci.cancel();
        }
    }

    @WrapOperation(method = {
        "updateScoreForCriteria",
        "die",
        "awardKillScore",
        "handleTeamKill",
        "awardStat",
        "resetStat",
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getScoreboard()Lnet/minecraft/server/ServerScoreboard;"))
    private ServerScoreboard impl$usePerPlayerScoreboard(
        final ServerLevel instance, final Operation<ServerScoreboard> original
    ) {
        return (ServerScoreboard) this.impl$scoreboard;
    }

    @Override
    protected void impl$onRightClickEntity(
        final Entity entityToInteractOn, final InteractionHand hand,
        final Vec3 location, final CallbackInfoReturnable<InteractionResult> cir
    ) {
        final ItemStack itemInHand = this.shadow$getItemInHand(hand);
        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory.callInteractEntityEventSecondary((net.minecraft.server.level.ServerPlayer) (Object) this,
            itemInHand, entityToInteractOn, hand, null);
        if (event.isCancelled()) {
            this.containerMenu.sendAllDataToRemote();
            if (itemInHand.getItem() == Items.LEAD && entityToInteractOn instanceof Mob) {
                this.connection.send(new ClientboundSetEntityLinkPacket(entityToInteractOn, ((Mob) entityToInteractOn).getLeashHolder()));
            } else if (itemInHand.getItem() == Items.WATER_BUCKET && entityToInteractOn instanceof AbstractFish) {
                final ChunkMap_TrackedEntityAccessor trackerAccessor = ((ChunkMapAccessor) ((ServerWorld) this.shadow$level()).chunkManager()).accessor$entityMap().get(entityToInteractOn.getId());
                if (trackerAccessor != null) {
                    trackerAccessor.accessor$getServerEntity().sendPairingData((net.minecraft.server.level.ServerPlayer) (Object) this, this.connection::send);
                }
            }
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Override
    public PlayerTeam shadow$getTeam() {
        return ((net.minecraft.world.scores.Scoreboard) this.impl$scoreboard).getPlayersTeam(this.shadow$getScoreboardName());
    }

    @Inject(method = "startSleepInBed", at = @At(value = "RETURN"), cancellable = true)
    private void impl$onReturnSleep(final AbstractBedBlock param0, final BlockState param1, final BedRule param2, final BlockPos param3,
            final CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        final Either<Player.BedSleepingProblem, Unit> returnValue = cir.getReturnValue();
        returnValue.ifLeft(problem -> {
            if (problem.message() == null) {
                return;
            }
            final Cause currentCause = PhaseTracker.getInstance().currentCause();
            final BlockSnapshot snapshot = ((ServerWorld) this.shadow$level()).createSnapshot(param3.getX(), param3.getY(), param3.getZ());
            if (Sponge.eventManager().post(SpongeEventFactory.createSleepingEventFailed(currentCause, snapshot, (Living) this))) {
                final Either<Player.BedSleepingProblem, Unit> var5 = super.shadow$startSleepInBed(param0, param1, param2, param3).ifRight((param0x) -> {
                    this.shadow$awardStat(param0.getSleptInBedStatType());
                    if (param2.canSetSpawn(this.shadow$level())) {
                        CriteriaTriggers.SLEPT_IN_BED.trigger((net.minecraft.server.level.ServerPlayer) (Object) this);
                    }
                });
                ((ServerLevel) this.shadow$level()).updateSleepingPlayerList();
                cir.setReturnValue(var5);
            }
        });
    }

    @Override
    protected void impl$updateHealthForUseFinish(final CallbackInfo ci) {
        this.bridge$refreshScaledHealth();
    }

    @ModifyVariable(method = "setGameMode", at = @At(value = "HEAD"), argsOnly = true)
    private GameType impl$setGameMode(final GameType value) {
        if (!ShouldFire.CHANGE_DATA_HOLDER_EVENT_VALUE_CHANGE || Objects.equals(this.gameMode.getGameModeForPlayer(), value) || this.impl$noGameModeEvent) {
            return value;
        }

        final DataTransactionResult transaction = DataTransactionResult.builder()
            .replace(Value.immutableOf(Keys.GAME_MODE, (GameMode) (Object) this.gameMode.getGameModeForPlayer()))
            .success(Value.immutableOf(Keys.GAME_MODE, (GameMode) (Object) value))
            .result(DataTransactionResult.Type.SUCCESS)
            .build();

        final ChangeDataHolderEvent.ValueChange
            event =
            SpongeEventFactory.createChangeDataHolderEventValueChange(PhaseTracker.getInstance().currentCause(), transaction, (DataHolder.Mutable) this);

        Sponge.eventManager().post(event);

        if (event.isCancelled()) {
            return this.gameMode.getGameModeForPlayer();
        }

        return (GameType) (Object) event.endResult().successfulValue(Keys.GAME_MODE)
            .map(Value::get)
            .orElse((GameMode) (Object) value);
    }

    @Override
    public void bridge$setGameModeNoEvent(final GameType gameType) {
        try {
            this.impl$noGameModeEvent = true;
            this.shadow$setGameMode(gameType);
        } finally {
            this.impl$noGameModeEvent = false;
        }
    }

    @Override
    public @Nullable WorldBorder bridge$getWorldBorder() {
        return this.impl$worldBorder;
    }

    @Override
    public void bridge$replaceWorldBorder(final @Nullable WorldBorder border) {
        this.impl$worldBorder = border;
    }

    @Override
    public boolean bridge$isTransient() {
        return this.impl$transient;
    }

    /**
     * @return True if PVP allowed
     * @author Zidane
     * @reason Have PVP check if the world allows it or not
     */
    @Overwrite
    private boolean isPvpAllowed() {
        return ((ServerWorld) this.shadow$level()).properties().pvp();
    }

    @WrapOperation(method = "findRespawnPositionAndUseSpawnBlock",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getRespawnConfig()Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;"
        ))
    private net.minecraft.server.level.ServerPlayer.RespawnConfig impl$callRespawnPlayerSelectWorld(
        final net.minecraft.server.level.ServerPlayer player,
        final Operation<net.minecraft.server.level.ServerPlayer.RespawnConfig> original,
        @Share("sponge:overridden-respawn") final LocalRef<ResourceKey<Level>> dimension
    ) {
        // Prefer a forced Keys.RESPAWN_LOCATIONS entry for the world the player died in, even if vanilla's single
        // slot points elsewhere. Mirror it into vanilla's respawnConfig up front so findRespawnPositionAndUseSpawnBlock
        // reads the correct position when locating the bed/anchor.
        final org.spongepowered.api.ResourceKey dyingWorldKey = (org.spongepowered.api.ResourceKey) (Object) player.level().dimension().identifier();
        final RespawnLocation forcedInDyingWorld = this.impl$bedLocations.get(dyingWorldKey);
        if (forcedInDyingWorld != null && forcedInDyingWorld.isForced()) {
            this.impl$syncingRespawn = true;
            try {
                this.shadow$setRespawnPosition(this.impl$toRespawnConfig(forcedInDyingWorld), false);
            } finally {
                this.impl$syncingRespawn = false;
            }
        }

        final var config = original.call(player);
        final var defaulted = config == null ? Level.OVERWORLD : config.respawnData().dimension();

        var playerRespawnDestination = this.server.getLevel(defaulted);
        if (playerRespawnDestination == null) {
            SpongeCommon.logger().warn("The player '{}' respawn location was located in a world that isn't loaded or doesn't exist. This is not safe so "
                                       + "the player will be moved to the spawn of the default world.", player.getGameProfile().name());
            playerRespawnDestination = ((ServerPlayerAccessor) player).accessor$server().overworld();
        }

        final RespawnPlayerEvent.SelectWorld event = SpongeEventFactory.createRespawnPlayerEventSelectWorld(PhaseTracker.getInstance().currentCause(),
            (ServerWorld) playerRespawnDestination, (ServerWorld) player.level(), (ServerWorld) playerRespawnDestination, (ServerPlayer) player);
        SpongeCommon.post(event);

        this.impl$respawnLevel = (ServerLevel) event.destinationWorld();
        dimension.set(((ServerLevel) event.destinationWorld()).dimension());
        return config;
    }

    @WrapOperation(
        method = "findRespawnPositionAndUseSpawnBlock",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLevel(Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/server/level/ServerLevel;")
    )
    private ServerLevel impl$useOverriddenLevel(
        final MinecraftServer instance, final ResourceKey<Level> key, final Operation<ServerLevel> original,
        @Share("sponge:overridden-respawn") final LocalRef<ResourceKey<Level>> dimension
    ) {
        if (dimension.get() != null) {
            return original.call(instance, dimension.get());
        }

        return original.call(instance, key);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void impl$onFindRespawnPositionAndUseSpawnBlock(final CallbackInfoReturnable<TeleportTransition> cir) {
        ((TeleportTransitionAccessor) (Object) cir.getReturnValue()).accessor$newLevel(this.impl$respawnLevel);
        this.impl$respawnLevel = null;
    }

    @Redirect(method = "saveParentVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hasExactlyOnePlayerPassenger()Z"))
    private boolean impl$skipUnserializableRootVehicle(final Entity instance) {
        return instance.hasExactlyOnePlayerPassenger() && !((TransientBridge) instance).bridge$isTransient();
    }

    /**
     * Fixes <a href="https://bugs.mojang.com/browse/MC/issues/MC-302672">MC-302672</a>
     */
    @WrapOperation(method = "onEffectAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void impl$onEffectAdded(ServerGamePacketListenerImpl instance, Packet packet, Operation<Void> original) {
        if (instance == null) {
            return;
        }
        original.call(instance, packet);
    }

    @Override
    public Map<org.spongepowered.api.ResourceKey, RespawnLocation> bridge$getBedlocations() {
        return new HashMap<>(this.impl$bedLocations);
    }

    @Override
    public boolean bridge$setBedLocations(final Map<org.spongepowered.api.ResourceKey, RespawnLocation> value) {
        for (final Map.Entry<org.spongepowered.api.ResourceKey, RespawnLocation> entry : value.entrySet()) {
            if (!Objects.equals(entry.getKey(), entry.getValue().worldKey())) {
                throw new IllegalArgumentException("RespawnLocation world key " + entry.getValue().worldKey()
                        + " does not match map key " + entry.getKey());
            }
        }
        this.impl$bedLocations.clear();
        this.impl$bedLocations.putAll(value);
        this.impl$syncingRespawn = true;
        try {
            final RespawnLocation active = this.impl$pickActiveRespawn(value);
            this.shadow$setRespawnPosition(active == null ? null : this.impl$toRespawnConfig(active), false);
        } finally {
            this.impl$syncingRespawn = false;
        }
        if (!((SpongeDataHolderBridge) this).brigde$isDeserializing()) {
            if (value.isEmpty()) {
                ((SpongeDataHolderBridge) this).bridge$remove(Keys.RESPAWN_LOCATIONS);
            } else {
                ((SpongeDataHolderBridge) this).bridge$offer(Keys.RESPAWN_LOCATIONS, new HashMap<>(value));
            }
        }
        return true;
    }

    @Override
    public ImmutableMap<org.spongepowered.api.ResourceKey, RespawnLocation> bridge$removeAllBeds() {
        final ImmutableMap<org.spongepowered.api.ResourceKey, RespawnLocation> snapshot = ImmutableMap.copyOf(this.impl$bedLocations);
        this.impl$bedLocations.clear();
        this.impl$syncingRespawn = true;
        try {
            this.shadow$setRespawnPosition(null, false);
        } finally {
            this.impl$syncingRespawn = false;
        }
        ((SpongeDataHolderBridge) this).bridge$remove(Keys.RESPAWN_LOCATIONS);
        return snapshot;
    }

    @Unique
    private @Nullable RespawnLocation impl$pickActiveRespawn(final Map<org.spongepowered.api.ResourceKey, RespawnLocation> map) {
        if (map.isEmpty()) {
            return null;
        }
        if (this.respawnConfig != null) {
            final org.spongepowered.api.ResourceKey vanillaKey = (org.spongepowered.api.ResourceKey) (Object) this.respawnConfig.respawnData().dimension().identifier();
            final RespawnLocation forVanilla = map.get(vanillaKey);
            if (forVanilla != null) {
                return forVanilla;
            }
        }
        final org.spongepowered.api.ResourceKey currentLevelKey = (org.spongepowered.api.ResourceKey) (Object) this.shadow$level().dimension().identifier();
        final RespawnLocation forCurrent = map.get(currentLevelKey);
        if (forCurrent != null) {
            return forCurrent;
        }
        return map.values().iterator().next();
    }

    @Unique
    private net.minecraft.server.level.ServerPlayer.RespawnConfig impl$toRespawnConfig(final RespawnLocation location) {
        final ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION,
            (Identifier) (Object) location.worldKey());
        final Vector3d pos = location.position();
        // Vector3d -> BlockPos via floor (BlockPos.containing uses floor semantics).
        // RespawnLocation has no yaw/pitch — keep the player's current orientation so sleep angle isn't clobbered.
        final LevelData.RespawnData data = LevelData.RespawnData.of(
            dim, BlockPos.containing(pos.x(), pos.y(), pos.z()), this.shadow$getYRot(), this.shadow$getXRot());
        return new net.minecraft.server.level.ServerPlayer.RespawnConfig(data, location.isForced());
    }

    @Inject(method = "setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V", at = @At("RETURN"))
    private void impl$mirrorVanillaRespawnToBedLocations(
            final @javax.annotation.Nullable net.minecraft.server.level.ServerPlayer.RespawnConfig config,
            final boolean sendMessage, final CallbackInfo ci) {
        if (this.impl$syncingRespawn) {
            return;
        }
        if (config == null) {
            // Vanilla cleared respawn — drop every entry (vanilla only tracks one slot anyway)
            this.impl$bedLocations.clear();
        } else {
            final GlobalPos globalPos = config.respawnData().globalPos();
            final BlockPos pos = globalPos.pos();
            final org.spongepowered.api.ResourceKey apiKey = (org.spongepowered.api.ResourceKey) (Object) globalPos.dimension().identifier();
            this.impl$bedLocations.put(apiKey, RespawnLocation.builder()
                .world(apiKey)
                .position(new Vector3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))
                .forceSpawn(config.forced())
                .build());
        }
        if (this.impl$bedLocations.isEmpty()) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.RESPAWN_LOCATIONS);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.RESPAWN_LOCATIONS, new HashMap<>(this.impl$bedLocations));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void impl$seedBedLocationsFromVanilla(final ValueInput input, final CallbackInfo ci) {
        if (this.respawnConfig == null || !this.impl$bedLocations.isEmpty()) {
            return;
        }
        final GlobalPos globalPos = this.respawnConfig.respawnData().globalPos();
        final BlockPos pos = globalPos.pos();
        final org.spongepowered.api.ResourceKey apiKey = (org.spongepowered.api.ResourceKey) (Object) globalPos.dimension().identifier();
        this.impl$bedLocations.put(apiKey, RespawnLocation.builder()
            .world(apiKey)
            .position(new Vector3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))
            .forceSpawn(this.respawnConfig.forced())
            .build());
    }
}
