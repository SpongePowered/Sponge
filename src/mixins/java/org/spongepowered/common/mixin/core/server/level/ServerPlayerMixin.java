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

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import io.netty.channel.Channel;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
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
import org.spongepowered.common.accessor.server.network.ServerCommonPacketListenerImplAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.BossEventBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.mixin.core.world.entity.player.PlayerMixin;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;
import org.spongepowered.common.world.portal.PortalLogic;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

// See also: SubjectMixin_API and SubjectMixin
@SuppressWarnings("ConstantConditions")
@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements SubjectBridge, ServerPlayerBridge {

    // @formatter:off
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow @Final public MinecraftServer server;
    @Shadow private int lastRecordedExperience;
    @Shadow private boolean isChangingDimension;
    @Shadow public boolean wonGame;
    @Shadow private boolean seenCredits;
    @Shadow private net.minecraft.world.phys.Vec3 enteredNetherPosition;
    @Shadow private int lastSentExp;
    @Shadow private float lastSentHealth;
    @Shadow private int lastSentFood;

    @Shadow public abstract ServerLevel shadow$serverLevel();
    @Shadow public abstract void shadow$setCamera(final Entity entity);
    @Shadow public abstract void shadow$closeContainer();
    @Shadow public abstract void shadow$resetStat(final Stat<?> statistic);
    @Shadow protected abstract void shadow$tellNeutralMobsThatIDied();
    @Shadow protected abstract void shadow$createEndPlatform(ServerLevel p_241206_1_, BlockPos blockPos);
    @Shadow protected abstract void shadow$triggerDimensionChangeTriggers(ServerLevel serverworld);
    @Shadow public abstract void shadow$doCloseContainer();
    @Shadow public abstract void shadow$setServerLevel(ServerLevel serverLevel);
    @Shadow public abstract boolean shadow$setGameMode(GameType param0);
    // @formatter:on

    private net.minecraft.network.chat.@Nullable Component impl$connectionMessage;
    private Locale impl$language = Locales.DEFAULT;
    private Scoreboard impl$scoreboard = Sponge.game().server().serverScoreboard().get();
    @Nullable private Boolean impl$keepInventory = null;
    // Used to restore original item received in a packet after canceling an event
    private int impl$viewDistance;
    private int impl$skinPartMask;
    private Set<SkinPart> impl$skinParts = ImmutableSet.of();
    private final PlayerOwnBorderListener impl$borderListener = new PlayerOwnBorderListener((net.minecraft.server.level.ServerPlayer) (Object) this);
    private boolean impl$sleepingIgnored;
    private boolean impl$noGameModeEvent;
    @Nullable private WorldBorder impl$worldBorder;

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

    @Override
    protected final boolean impl$setLocation(final boolean isChangeOfWorld, final ServerLevel originalDestination,
            final ServerLevel destinationWorld, final Vector3d destinationPosition) {
        if (this.shadow$isRemoved()) {
            return false;
        }

        final net.minecraft.server.level.ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) (Object) this);
        player.stopRiding();
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }

        final ChunkPos chunkPos = VecHelper.toChunkPos(Sponge.server().chunkLayout().forceToChunk(destinationPosition.toInt()));
        destinationWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, player.getId());

        if (isChangeOfWorld) {
            this.shadow$absMoveTo(destinationPosition.x(), destinationPosition.y(), destinationPosition.z(), this.shadow$getYRot(), this.shadow$getXRot());
            EntityUtil.performPostChangePlayerWorldLogic(player, this.shadow$serverLevel(), destinationWorld, destinationWorld, false);
        } else {
            this.connection.teleport(destinationPosition.x(), destinationPosition.y(), destinationPosition.z(), this.shadow$getYRot(), this.shadow$getXRot(),
                    new HashSet<>());
            this.connection.resetPosition();
        }
        return true;
    }

    @Override
    public void bridge$refreshExp() {
        this.lastRecordedExperience = -1;
    }

    @Override
    public boolean bridge$kick(final Component message) {
        final Component messageToSend;
        if (ShouldFire.KICK_PLAYER_EVENT) {
            final KickPlayerEvent kickEvent = SpongeEventFactory.createKickPlayerEvent(PhaseTracker.getCauseStackManager().currentCause(),
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

            SpongeAdventure.forEachBossBar(bar -> {
                if (bar.getPlayers().contains(this)) {
                    this.connection.send(ClientboundBossEventPacket.createUpdateNamePacket(bar));
                }
            });

            this.containerMenu.broadcastFullState();
        }
    }

    @Override
    public void bridge$initScoreboard() {
        ((ServerScoreboardBridge) this.shadow$getScoreboard()).bridge$addPlayer((net.minecraft.server.level.ServerPlayer) (Object) this, true);
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
        if (this.impl$keepInventory == null) {
            return this.shadow$level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        }
        return this.impl$keepInventory;
    }

    @Override
    public int bridge$getExperiencePointsOnDeath(final LivingEntity entity) {
        if (this.impl$keepInventory != null && this.impl$keepInventory) {
            return 0;
        }
        return super.bridge$getExperiencePointsOnDeath(entity);
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

    /*
    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void impl$onPlayerActive(final CallbackInfo ci) {
        ((ServerPlayNetHandlerBridge) this.connection).bridge$resendLatestResourcePackRequest();
    }
*/

    /**
     * @author zidane - November 21st, 2020 - Minecraft 1.15
     * @reason Ensure that the teleport hook honors our events
     */
    @Overwrite
    public void teleportTo(
            final ServerLevel world, final double x, final double y, final double z, final float yaw, final float pitch) {
        final net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) (Object) this;
        double actualX = x;
        double actualY = y;
        double actualZ = z;
        double actualYaw = yaw;
        double actualPitch = pitch;

        final boolean hasMovementContext = PhaseTracker.getCauseStackManager().currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (!hasMovementContext) {
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
            }

            if (world == player.level()) {
                final @Nullable Vector3d destination = this.impl$fireMoveEvent(PhaseTracker.SERVER, new Vector3d(x, y, z));
                if (destination == null) {
                    return;
                }
                actualX = destination.x();
                actualY = destination.y();
                actualZ = destination.z();

                if (ShouldFire.ROTATE_ENTITY_EVENT) {
                    final RotateEntityEvent rotateEvent = SpongeEventFactory.createRotateEntityEvent(frame.currentCause(),
                            (org.spongepowered.api.entity.Entity) player, new Vector3d(actualPitch, actualYaw, 0),
                            new Vector3d(pitch, yaw, 0));

                    SpongeCommon.post(rotateEvent);

                    actualYaw = rotateEvent.isCancelled() ? player.getYRot() : rotateEvent.toRotation().y();
                    actualPitch = rotateEvent.isCancelled() ? player.getXRot() : rotateEvent.toRotation().x();
                }

                this.shadow$setCamera(player);
                this.shadow$stopRiding();

                if (player.isSleeping()) {
                    player.stopSleepInBed(true, true);
                }

                player.connection.teleport(actualX, actualY, actualZ, (float) actualYaw, (float) actualPitch);

                player.setYHeadRot((float) actualYaw);

                final ChunkPos chunkpos = new ChunkPos(new BlockPos((int) actualX, (int) actualY, (int) actualZ));
                world.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
            } else {
                final ChangeEntityWorldEvent.Pre preEvent = PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPre(player, world);
                if (SpongeCommon.post(preEvent)) {
                    return;
                }

                if (ShouldFire.MOVE_ENTITY_EVENT) {
                    final MoveEntityEvent posEvent = SpongeEventFactory.createChangeEntityWorldEventReposition(frame.currentCause(),
                            (org.spongepowered.api.entity.Entity) player, preEvent.originalWorld(), VecHelper.toVector3d(player.position()),
                            new Vector3d(x, y, z), preEvent.originalDestinationWorld(), new Vector3d(x, y, z), preEvent.destinationWorld());

                    if (SpongeCommon.post(posEvent)) {
                        return;
                    }

                    actualX = posEvent.destinationPosition().x();
                    actualY = posEvent.destinationPosition().y();
                    actualZ = posEvent.destinationPosition().z();
                }
                this.shadow$setPos(actualX, actualY, actualZ);

                if (ShouldFire.ROTATE_ENTITY_EVENT) {
                    final RotateEntityEvent rotateEvent = SpongeEventFactory.createRotateEntityEvent(frame.currentCause(),
                            (org.spongepowered.api.entity.Entity) player, new Vector3d(actualYaw, actualPitch, 0),
                            new Vector3d(yaw, pitch, 0));

                    if (!SpongeCommon.post(rotateEvent)) {
                        actualYaw = (float) rotateEvent.toRotation().x();
                        actualPitch = (float) rotateEvent.toRotation().y();
                    }
                }
                this.shadow$setYRot((float) actualYaw);
                this.shadow$setXRot((float) actualPitch);

                EntityUtil.performPostChangePlayerWorldLogic(player, (ServerLevel) preEvent.originalWorld(),
                        (ServerLevel) preEvent.originalDestinationWorld(),
                        (ServerLevel) preEvent.destinationWorld(), false);
            }
        }
    }

    @Override
    protected final void impl$onChangingDimension(final ServerLevel target) {
        if (this.shadow$level() != target) {
            this.isChangingDimension = true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected final Entity impl$performGameWinLogic() {
        this.shadow$unRide();
        this.shadow$serverLevel().removePlayerImmediately((net.minecraft.server.level.ServerPlayer) (Object) this, Entity.RemovalReason.CHANGED_DIMENSION);
        if (!this.wonGame) {
            this.wonGame = true;
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, this.seenCredits ? 0.0F : 1.0F));
            this.seenCredits = true;
        }

        return (Entity) (Object) this;
    }

    @Override
    protected final void impl$prepareForPortalTeleport(final ServerLevel currentWorld, final ServerLevel targetWorld) {
        final LevelData levelData = targetWorld.getLevelData();
        this.connection.send(new ClientboundRespawnPacket(new CommonPlayerSpawnInfo(targetWorld.dimensionTypeRegistration(), targetWorld.dimension(),
                BiomeManager.obfuscateSeed(targetWorld.getSeed()), this.gameMode.getGameModeForPlayer(),
                this.gameMode.getPreviousGameModeForPlayer(), targetWorld.isDebug(), targetWorld.isFlat(), this.shadow$getLastDeathLocation(), this.shadow$getPortalCooldown()),
                ClientboundRespawnPacket.KEEP_ALL_DATA));
        this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
        final PlayerList playerlist = this.server.getPlayerList();
        playerlist.sendPlayerPermissionLevel((net.minecraft.server.level.ServerPlayer) (Object) this);
        currentWorld.removePlayerImmediately((net.minecraft.server.level.ServerPlayer) (Object) this,
            Entity.RemovalReason.CHANGED_DIMENSION);
        this.shadow$unsetRemoved();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected final void impl$validateEntityAfterTeleport(final Entity e, final PortalLogic portalLogic) {
        if (e != (Object) this) {
            throw new IllegalArgumentException(String.format("Teleporter %s "
                    + "did not return the expected player entity: got %s, expected PlayerEntity %s", portalLogic, e, this));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected final Entity impl$portalRepositioning(final boolean createEndPlatform,
            final ServerLevel serverworld,
            final ServerLevel targetWorld,
            final PortalInfo portalinfo) {
        serverworld.getProfiler().push("moving");
        if (serverworld.dimension() == Level.OVERWORLD && targetWorld.dimension() == Level.NETHER) {
            this.enteredNetherPosition = this.shadow$position();
            // Sponge: From Forge - only enter this branch if the teleporter indicated that we should
            // create end platforms and we're in the end (vanilla only has the second condition)
        } else if (createEndPlatform && targetWorld.dimension() == Level.END) {
            this.shadow$createEndPlatform(targetWorld, new BlockPos((int) portalinfo.pos.x, (int) portalinfo.pos.y, (int) portalinfo.pos.z));
        }

        // This is standard vanilla processing
        serverworld.getProfiler().pop();
        serverworld.getProfiler().push("placing");
        this.shadow$setServerLevel(targetWorld);
        targetWorld.addDuringPortalTeleport((net.minecraft.server.level.ServerPlayer) (Object) this);
        this.shadow$setRot(portalinfo.yRot, portalinfo.xRot);
        // Sponge Start: prevent sending the teleport packet here, we'll do so later.
        // this.shadow$moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z);
        this.shadow$absMoveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z);
        // Sponge End
        serverworld.getProfiler().pop();

        return (Entity) (Object) this;
    }

    @Override
    protected final void impl$postPortalForceChangeTasks(final Entity entity, final ServerLevel targetWorld,
            final boolean isNetherPortal) {
        // Standard vanilla processing
        this.gameMode.setLevel(targetWorld);
        this.connection.send(new ClientboundPlayerAbilitiesPacket(this.shadow$getAbilities()));
        final PlayerList playerlist = this.server.getPlayerList();
        playerlist.sendLevelInfo((net.minecraft.server.level.ServerPlayer) (Object) this, targetWorld);
        playerlist.sendAllPlayerInfo((net.minecraft.server.level.ServerPlayer) (Object) this);

        // Sponge Start: teleport here after all data is sent to avoid any potential "stuttering" due to slow packets.
        final net.minecraft.world.phys.Vec3 finalPos = this.shadow$position();
        this.shadow$teleportTo(finalPos.x, finalPos.y, finalPos.z);
        this.connection.resetPosition();
        // Sponge End

        for (final MobEffectInstance effectinstance : this.shadow$getActiveEffects()) {
            this.connection.send(new ClientboundUpdateMobEffectPacket(this.shadow$getId(), effectinstance, false));
        }

        if (isNetherPortal) { // Sponge: only play the sound if we've got a vanilla teleporter that reports a nether portal
            this.connection.send(new ClientboundLevelEventPacket(1032, BlockPos.ZERO, 0, false));
        } // Sponge: end if
        this.lastSentExp = -1;
        this.lastSentHealth = -1.0F;
        this.lastSentFood = -1;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Redirect(method = "getExitPortal",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Ljava/util/Optional;"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;level()Lnet/minecraft/world/level/Level;")
            ),
            at = @At(value = "INVOKE", remap = false, target = "Ljava/util/Optional;isPresent()Z"))
    private boolean impl$dontCreatePortalIfItsAlreadyBeenAttempted(final Optional<?> optional) {
        // This prevents a second attempt at a portal creation if the portal
        // creation attempt due to a reposition event failed (this would put it
        // in the original position, and we don't want that to happen!).
        //
        // In this case, we just force it to return the empty optional by
        // claiming the optional is "present".
        return this.impl$dontCreateExitPortal || optional.isPresent();
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

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 3rd, 2016
     * @author gabizou - February 22nd, 2020 - Minecraft 1.14.3
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Overwrite
    public void die(final DamageSource cause) {
        // Sponge start - Call Destruct Death Event
        final DestructEntityEvent.Death event = SpongeCommonEventFactory.callDestructEntityEventDeath((net.minecraft.server.level.ServerPlayer) (Object) this, cause,
                Audiences.server());
        if (event.isCancelled()) {
            return;
        }
        // Sponge end

        final boolean flag = this.shadow$level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && !event.isMessageCancelled();
        if (flag) {
            final net.minecraft.network.chat.Component component = this.shadow$getCombatTracker().getDeathMessage();
            final ClientboundPlayerCombatKillPacket packet = new ClientboundPlayerCombatKillPacket(this.shadow$getId(), component);
            this.connection.send(packet, PacketSendListener.exceptionallySend(() -> {
                final String s = component.getString(256);
                final net.minecraft.network.chat.Component itextcomponent1 = net.minecraft.network.chat.Component.translatable("death.attack.message_too_long", net.minecraft.network.chat.Component.literal(s).withStyle(ChatFormatting.YELLOW));
                final net.minecraft.network.chat.Component itextcomponent2 = net.minecraft.network.chat.Component.translatable("death.attack.even_more_magic", this.shadow$getDisplayName())
                        .withStyle((p_212357_1_) -> p_212357_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1)));
                return new ClientboundPlayerCombatKillPacket(this.shadow$getId(), itextcomponent2);
            }));
            final Team team = this.shadow$getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visibility.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().broadcastSystemToTeam(
                            (net.minecraft.server.level.ServerPlayer) (Object) this, component);
                } else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().broadcastSystemToAllExceptTeam(
                            (net.minecraft.server.level.ServerPlayer) (Object) this, component);
                }
            } else {
                final Component message = event.message();
                // Sponge start - use the event audience
                if (message != Component.empty()) {
                    event.audience().ifPresent(eventChannel -> eventChannel.sendMessage(Identity.nil(), message));
                }
                // Sponge end
                // this.server.getPlayerList().sendMessage(itextcomponent);
            }
        } else {
            this.connection.send(
                    new ClientboundPlayerCombatKillPacket(this.shadow$getId(), net.minecraft.network.chat.Component.empty()));
        }

        this.shadow$removeEntitiesOnShoulder();
        if (this.shadow$level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.shadow$tellNeutralMobsThatIDied();
        }

        // Sponge Start - update the keep inventory flag for dropping inventory
        // during the death update ticks
        this.impl$keepInventory = event.keepInventory();

        if (!this.shadow$isSpectator()) {
            this.shadow$dropAllDeathLoot(cause);
        }
        // Sponge End

        this.shadow$getScoreboard().forAllObjectives(
                ObjectiveCriteria.DEATH_COUNT, (net.minecraft.server.level.ServerPlayer) (Object) this, sa -> sa.set(sa.get() + 1));
        final LivingEntity livingentity = this.shadow$getKillCredit();
        if (livingentity != null) {
            this.shadow$awardStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
            livingentity.awardKillScore((net.minecraft.server.level.ServerPlayer) (Object) this, this.deathScore, cause);
            this.shadow$createWitherRose(livingentity);
        }

        this.shadow$level().broadcastEntityEvent((net.minecraft.server.level.ServerPlayer) (Object) this, (byte) 3);
        this.shadow$awardStat(Stats.DEATHS);
        this.shadow$resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.shadow$resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.shadow$clearFire();
        this.shadow$setSharedFlag(0, false);
        this.shadow$getCombatTracker().recheckStatus();
    }

    @Redirect(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean tracker$useKeepFromBridge(final GameRules gameRules, final GameRules.Key<?> key,
            final net.minecraft.server.level.ServerPlayer corpse, final boolean keepEverything) {
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
        if (oldPlayer instanceof DataCompoundHolder) {
            final DataCompoundHolder oldEntity = (DataCompoundHolder) oldPlayer;
            DataUtil.syncDataToTag(oldEntity);
            final CompoundTag compound = oldEntity.data$getCompound();
            ((DataCompoundHolder) this).data$setCompound(compound);
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
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
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

    @Override
    public net.minecraft.world.scores.Scoreboard shadow$getScoreboard() {
        return (net.minecraft.world.scores.Scoreboard) this.impl$scoreboard;
    }

    @Override
    protected void impl$onRightClickEntity(
        final Entity entityToInteractOn, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> cir
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
    private void impl$onReturnSleep(final BlockPos param0, final CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        final Either<Player.BedSleepingProblem, Unit> returnValue = cir.getReturnValue();
        if (returnValue.left().isPresent()) {
            switch (returnValue.left().get()) {

                case NOT_POSSIBLE_HERE:
                case TOO_FAR_AWAY:
                case NOT_POSSIBLE_NOW:
                case OBSTRUCTED:
                case NOT_SAFE:
                    final Cause currentCause = Sponge.server().causeStackManager().currentCause();
                    final BlockSnapshot snapshot = ((ServerWorld) this.shadow$level()).createSnapshot(param0.getX(), param0.getY(), param0.getZ());
                    if (Sponge.eventManager().post(SpongeEventFactory.createSleepingEventFailed(currentCause, snapshot, (Living) this))) {
                        final Either<Player.BedSleepingProblem, Unit> var5 = super.shadow$startSleepInBed(param0).ifRight((param0x) -> {
                            this.shadow$awardStat(Stats.SLEEP_IN_BED);
                            CriteriaTriggers.SLEPT_IN_BED.trigger((net.minecraft.server.level.ServerPlayer) (Object) this);
                        });
                        ((ServerLevel) this.shadow$level()).updateSleepingPlayerList();
                        cir.setReturnValue(var5);
                    }
                    break;
                case OTHER_PROBLEM: // ignore
                    break;
            }
        }
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
                SpongeEventFactory.createChangeDataHolderEventValueChange(PhaseTracker.getCauseStackManager().currentCause(), transaction, (DataHolder.Mutable) this);

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
     * @author Zidane
     * @reason Have PVP check if the world allows it or not
     * @return True if PVP allowed
     */
    @Overwrite
    private boolean isPvpAllowed() {
        return ((ServerWorld) this.shadow$serverLevel()).properties().pvp();
    }
}
