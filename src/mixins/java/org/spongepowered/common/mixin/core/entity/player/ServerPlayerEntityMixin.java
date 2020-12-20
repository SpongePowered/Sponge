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
package org.spongepowered.common.mixin.core.entity.player;

import com.google.common.collect.ImmutableSet;
import io.netty.channel.Channel;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.server.SCombatPacket;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.network.NetworkManagerAccessor;
import org.spongepowered.common.accessor.network.play.client.CClientSettingsPacketAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.world.BossInfoBridge;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.WrappedITeleporterPortalType;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

// See also: SubjectMixin_API and SubjectMixin
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements SubjectBridge, ServerPlayerEntityBridge {

    // @formatter:off
    @Shadow public ServerPlayNetHandler connection;
    @Shadow @Final public PlayerInteractionManager gameMode;
    @Shadow @Final public MinecraftServer server;
    @Shadow private int lastRecordedExperience;

    @Shadow public abstract net.minecraft.world.server.ServerWorld shadow$getLevel();
    @Shadow public abstract void shadow$setCamera(final Entity entity);
    @Shadow public abstract void shadow$stopRiding();
    @Shadow public abstract void shadow$closeContainer();
    @Shadow public abstract void shadow$resetStat(final Stat<?> statistic);
    @Shadow protected abstract void shadow$tellNeutralMobsThatIDied();
    // @formatter:on

    private final User impl$user = this.impl$getUserObjectOnConstruction();
    private @Nullable ITextComponent impl$connectionMessage;
    private Locale impl$language = Locales.EN_US;
    private Scoreboard impl$scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
    @Nullable private Boolean impl$keepInventory = null;
    // Used to restore original item received in a packet after canceling an event
    private ItemStack impl$packetItem = ItemStack.EMPTY;
    private int impl$viewDistance;
    private int impl$skinPartMask;
    private Set<SkinPart> impl$skinParts = ImmutableSet.of();

    @Nullable
    @Override
    public ITextComponent bridge$getConnectionMessageToSend() {
        if (this.impl$connectionMessage == null) {
            return new StringTextComponent("");
        }
        return this.impl$connectionMessage;
    }

    @Override
    public void bridge$setConnectionMessageToSend(final ITextComponent message) {
        this.impl$connectionMessage = message;
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public User bridge$getUserObject() {
        return this.impl$user;
    }

    @Override
    public User bridge$getUser() {
        return this.impl$user;
    }

    @Override
    public boolean bridge$isVanished() {
        return false;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.FALSE;
    }

    @Override
    public void bridge$setPacketItem(final ItemStack itemstack) {
        this.impl$packetItem = itemstack;
    }

    @Override
    public boolean bridge$setLocation(final ServerLocation location) {
        if (this.removed) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.getActivePlugin());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

            ServerWorld destinationWorld = (net.minecraft.world.server.ServerWorld) location.getWorld();

            Vector3d toPosition;

            if (this.shadow$getLevel() != destinationWorld) {
                final ChangeEntityWorldEvent.Pre event = SpongeEventFactory.createChangeEntityWorldEventPre(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, (org.spongepowered.api.world.server.ServerWorld) this.shadow$getLevel(),
                        location.getWorld(), location.getWorld());
                if (SpongeCommon.postEvent(event)) {
                    return false;
                }

                final ChangeEntityWorldEvent.Reposition repositionEvent =
                        SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                                (org.spongepowered.api.entity.Entity) this, (org.spongepowered.api.world.server.ServerWorld) this.shadow$getLevel(),
                                VecHelper.toVector3d(this.shadow$position()), location.getPosition(), event.getOriginalDestinationWorld(),
                                location.getPosition(), event.getDestinationWorld());

                if (SpongeCommon.postEvent(repositionEvent)) {
                    return false;
                }

                destinationWorld = (net.minecraft.world.server.ServerWorld) event.getDestinationWorld();

                toPosition = repositionEvent.getDestinationPosition();
            } else {
                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()),
                        location.getPosition(), location.getPosition());
                if (SpongeCommon.postEvent(event)) {
                    return false;
                }

                toPosition = event.getDestinationPosition();
            }

            ((ServerPlayerEntity) (Object) this).stopRiding();

            if (((ServerPlayerEntity) (Object) this).isSleeping()) {
                ((ServerPlayerEntity) (Object) this).stopSleepInBed(true, true);
            }

            final ChunkPos chunkPos = new ChunkPos((int) toPosition.getX() >> 4, (int) toPosition.getZ() >> 4);
            destinationWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, ((ServerPlayerEntity) (Object) this).getId());

            if (this.shadow$getLevel() != destinationWorld) {
                this.shadow$absMoveTo(toPosition.getX(), toPosition.getY(), toPosition.getZ(), this.yRot, this.xRot);

                EntityUtil.performPostChangePlayerWorldLogic((ServerPlayerEntity) (Object) this, this.shadow$getLevel(),
                        (net.minecraft.world.server.ServerWorld) location.getWorld(), destinationWorld, false);
            } else {
                this.connection.teleport(toPosition.getX(), toPosition.getY(), toPosition.getZ(), this.yRot, this.xRot,
                        new HashSet<>());
                this.connection.resetPosition();
            }
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
            final KickPlayerEvent kickEvent = SpongeEventFactory.createKickPlayerEvent(PhaseTracker.getCauseStackManager().getCurrentCause(),
                message,
                message,
                (ServerPlayer) this
                );
            if (Sponge.getEventManager().post(kickEvent)) {
                return false;
            }
            messageToSend = kickEvent.getMessage();
        } else {
            messageToSend = message;
        }
        final ITextComponent component = SpongeAdventure.asVanilla(messageToSend);
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
    }

    @Override
    public void bridge$initScoreboard() {
        ((ServerScoreboardBridge) this.shadow$getScoreboard()).bridge$addPlayer((ServerPlayerEntity) (Object) this, true);
    }

    @Override
    public void bridge$removeScoreboardOnRespawn() {
        ((ServerScoreboardBridge) ((ServerPlayer) this).getScoreboard()).bridge$removePlayer((ServerPlayerEntity) (Object) this, false);
    }

    @Override
    public void bridge$setScoreboardOnRespawn(final Scoreboard scoreboard) {
        this.impl$scoreboard = scoreboard;
        ((ServerScoreboardBridge) ((ServerPlayer) this).getScoreboard()).bridge$addPlayer((ServerPlayerEntity) (Object) this, false);
    }

    @Override
    public Scoreboard bridge$getScoreboard() {
        return this.impl$scoreboard;
    }

    @Override
    public void bridge$replaceScoreboard(@org.checkerframework.checker.nullness.qual.Nullable Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.getGame().getServer().getServerScoreboard()
                    .orElseThrow(() -> new IllegalStateException("Server does not have a valid scoreboard"));
        }
        this.impl$scoreboard = scoreboard;
    }

    @Override
    public boolean bridge$keepInventory() {
        if (this.impl$keepInventory == null) {
            return this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
        }
        return this.impl$keepInventory;
    }

    @Override
    public int bridge$getExperiencePointsOnDeath(final LivingEntity entity, final PlayerEntity attackingPlayer) {
        if (this.impl$keepInventory != null && this.impl$keepInventory) {
            return 0;
        }
        return super.bridge$getExperiencePointsOnDeath(entity, attackingPlayer);
    }

    @Override
    public int bridge$getViewDistance() {
        return this.impl$viewDistance;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public Set<SkinPart> bridge$getSkinParts() {
        final int mask = this.shadow$getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
        if (this.impl$skinPartMask != mask) {
            this.impl$skinParts = Sponge.getGame().registries().registry(RegistryTypes.SKIN_PART).streamValues()
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
    public void teleportTo(net.minecraft.world.server.ServerWorld world, double x, double y, double z, float yaw, float pitch) {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        double actualX;
        double actualY;
        double actualZ;
        double actualYaw = yaw;
        double actualPitch = pitch;

        boolean hasMovementContext = PhaseTracker.getCauseStackManager().getCurrentContext().containsKey(EventContextKeys.MOVEMENT_TYPE);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            if (!hasMovementContext) {
                frame.pushCause(SpongeCommon.getActivePlugin());
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
            }

            if (world == player.level) {
                final MoveEntityEvent posEvent = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, VecHelper.toVector3d(player.position()),
                        new Vector3d(x, y, z), new Vector3d(x, y, z));

                if (SpongeCommon.postEvent(posEvent)) {
                    return;
                }

                final RotateEntityEvent rotateEvent = SpongeEventFactory.createRotateEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, new Vector3d(actualPitch, actualYaw, 0),
                        new Vector3d(pitch, yaw, 0));

                SpongeCommon.postEvent(rotateEvent);

                actualX = posEvent.getDestinationPosition().getX();
                actualY = posEvent.getDestinationPosition().getY();
                actualZ = posEvent.getDestinationPosition().getZ();
                actualYaw = rotateEvent.isCancelled() ? player.yRot : rotateEvent.getToRotation().getY();
                actualPitch = rotateEvent.isCancelled() ? player.xRot : rotateEvent.getToRotation().getX();

                this.shadow$setCamera(player);
                this.shadow$stopRiding();

                if (player.isSleeping()) {
                    player.stopSleepInBed(true, true);
                }

                player.connection.teleport(actualX, actualY, actualZ, (float) actualYaw, (float) actualPitch);

                player.setYHeadRot((float) actualYaw);

                ChunkPos chunkpos = new ChunkPos(new BlockPos(actualX, actualY, actualZ));
                world.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getId());
            } else {
                final ChangeEntityWorldEvent.Pre preEvent = PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPre(player, world);
                if (SpongeCommon.postEvent(preEvent)) {
                    return;
                }

                final MoveEntityEvent posEvent = SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, preEvent.getOriginalWorld(), VecHelper.toVector3d(player.position()),
                        new Vector3d(x, y, z), preEvent.getOriginalDestinationWorld(), new Vector3d(x, y, z), preEvent.getDestinationWorld());

                final RotateEntityEvent rotateEvent = SpongeEventFactory.createRotateEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, new Vector3d(actualYaw, actualPitch, 0),
                        new Vector3d(yaw, pitch, 0));

                if (SpongeCommon.postEvent(posEvent)) {
                    return;
                }

                this.shadow$setPos(posEvent.getDestinationPosition().getX(), posEvent.getDestinationPosition().getY(),
                        posEvent.getDestinationPosition().getZ());

                if (!SpongeCommon.postEvent(rotateEvent)) {
                    this.yRot = (float) rotateEvent.getToRotation().getX();
                    this.xRot = (float) rotateEvent.getToRotation().getY();
                }

                EntityUtil.performPostChangePlayerWorldLogic(player, (net.minecraft.world.server.ServerWorld) preEvent.getOriginalWorld(),
                        (net.minecraft.world.server.ServerWorld) preEvent.getOriginalDestinationWorld(),
                        (net.minecraft.world.server.ServerWorld) preEvent.getDestinationWorld(), false);
            }
        }
    }

    /**
     * @author zidane - November 21st, 2020 - Minecraft 1.15.2
     * @reason Call to EntityUtil to handle dimension changes
     */
    @javax.annotation.Nullable
    @Overwrite
    public Entity changeDimension(ServerWorld destination) {
        if (this.shadow$getCommandSenderWorld().isClientSide || this.removed) {
            return (ServerPlayerEntity) (Object) this;
        }

        final WrappedITeleporterPortalType portalType = new WrappedITeleporterPortalType((PlatformITeleporterBridge) destination.getPortalForcer(), null);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.pushCause(portalType);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PORTAL);

            EntityUtil.invokePortalTo((ServerPlayerEntity) (Object) this, portalType, destination);
            return (ServerPlayerEntity) (Object) this;
        }
    }

    @Inject(method = "sendRemoveEntity", at = @At("RETURN"))
    private void impl$removeHumanFromPlayerClient(final Entity entityIn, final CallbackInfo ci) {
        if (entityIn instanceof HumanEntity) {
            ((HumanEntity) entityIn).untrackFrom((ServerPlayerEntity) (Object) this);
        }
    }

    @Redirect(
            method = {"openMenu", "openHorseInventory"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;closeContainer()V"
            )
    )
    private void impl$closePreviousContainer(final ServerPlayerEntity self) {
        this.shadow$closeContainer();
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
        final DestructEntityEvent.Death event = SpongeCommonEventFactory.callDestructEntityEventDeath((ServerPlayerEntity) (Object) this, cause,
                Audiences.server());
        if (event.isCancelled()) {
            return;
        }
        // Sponge end

        final boolean flag = this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && !event.isMessageCancelled();
        if (flag) {
            final ITextComponent itextcomponent = this.shadow$getCombatTracker().getDeathMessage();
            this.connection.send(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent), (p_212356_2_) -> {
                if (!p_212356_2_.isSuccess()) {
                    int i = 256;
                    String s = itextcomponent.getString(256);
                    final ITextComponent itextcomponent1 = new TranslationTextComponent("death.attack.message_too_long", (new StringTextComponent(s)).withStyle(TextFormatting.YELLOW));
                    final ITextComponent itextcomponent2 = new TranslationTextComponent("death.attack.even_more_magic", this.shadow$getDisplayName())
                                    .withStyle((p_212357_1_) -> p_212357_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1)));
                    this.connection.send(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent2));
                }

            });
            final Team team = this.shadow$getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().broadcastToTeam(
                            (ServerPlayerEntity) (Object) this, itextcomponent);
                } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().broadcastToAllExceptTeam(
                            (ServerPlayerEntity) (Object) this, itextcomponent);
                }
            } else {
                final Component message = event.getMessage();
                // Sponge start - use the event audience
                if (message != Component.empty()) {
                    event.getAudience().ifPresent(eventChannel -> eventChannel.sendMessage(Identity.nil(), message));
                }
                // Sponge end
                // this.server.getPlayerList().sendMessage(itextcomponent);
            }
        } else {
            this.connection.send(
                    new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED));
        }

        this.shadow$removeEntitiesOnShoulder();
        if (this.level.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
            this.shadow$tellNeutralMobsThatIDied();
        }

        // Sponge Start - update the keep inventory flag for dropping inventory
        // during the death update ticks
        this.impl$keepInventory = event.getKeepInventory();

        if (!this.shadow$isSpectator()) {
            this.shadow$dropAllDeathLoot(cause);
        }
        // Sponge End

        this.shadow$getScoreboard().forAllObjectives(
                ScoreCriteria.DEATH_COUNT, this.shadow$getScoreboardName(), Score::increment);
        final LivingEntity livingentity = this.shadow$getKillCredit();
        if (livingentity != null) {
            this.shadow$awardStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
            livingentity.awardKillScore((ServerPlayerEntity) (Object) this, this.deathScore, cause);
            this.shadow$createWitherRose(livingentity);
        }

        this.level.broadcastEntityEvent((ServerPlayerEntity) (Object) this, (byte) 3);
        this.shadow$awardStat(Stats.DEATHS);
        this.shadow$resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.shadow$resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.shadow$clearFire();
        this.shadow$setSharedFlag(0, false);
        this.shadow$getCombatTracker().recheckStatus();
    }

    @Redirect(method = "restoreFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    private boolean tracker$useKeepFromBridge(final GameRules gameRules, final GameRules.RuleKey<?> key,
            final ServerPlayerEntity corpse, final boolean keepEverything) {
        final boolean keep = ((PlayerEntityBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.replaceWith(corpse.inventory);
            // Clear corpse so that mods do not copy from it again
            corpse.inventory.clearContent();
        }
        return keep;
    }

    private User impl$getUserObjectOnConstruction() {
        if (this.impl$isFake) {
            return this.bridge$getUserObject();
        }
        // Ensure that the game profile is up to date.
        return ((SpongeUserManager) SpongeCommon.getGame().getServer().getUserManager()).forceRecreateUser(SpongeGameProfile.of(this.shadow$getGameProfile()));
    }

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    private void impl$copyDataOnRespawn(final ServerPlayerEntity oldPlayer, final boolean respawnFromEnd, final CallbackInfo ci) {
        // Copy Sponge data
        if (oldPlayer instanceof DataCompoundHolder) {
            final DataCompoundHolder oldEntity = (DataCompoundHolder) oldPlayer;
            if (oldEntity.data$hasSpongeData()) {
                final CompoundNBT compound = oldEntity.data$getCompound();
                ((DataCompoundHolder) this).data$setCompound(compound);
            }
        }

        // Update boss bars
        SpongeAdventure.forEachBossBar(bar -> ((BossInfoBridge) bar).bridge$replacePlayer(oldPlayer, (ServerPlayerEntity) (Object) this));
    }

    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    @Inject(method = "updateOptions", at = @At("HEAD"))
    private void impl$handleClientSettings(final CClientSettingsPacket packet, final CallbackInfo ci) {
        final CClientSettingsPacketAccessor $packet = (CClientSettingsPacketAccessor) packet;
        final Locale newLocale = LocaleCache.getLocale($packet.accessor$language());

        final ImmutableSet<SkinPart> skinParts = Sponge.getGame().registries().registry(RegistryTypes.SKIN_PART).streamValues()
                .map(part -> (SpongeSkinPart) part)
                .filter(part -> part.test(packet.getModelCustomisation()))
                .collect(ImmutableSet.toImmutableSet());
        final int viewDistance = $packet.accessor$viewDistance();

        // Post before the player values are updated
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ChatVisibility visibility = (ChatVisibility) (Object) packet.getChatVisibility();
            final PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(
                    frame.getCurrentCause(),
                    visibility,
                    skinParts,
                    newLocale,
                    (ServerPlayer) this,
                    packet.getChatColors(),
                    viewDistance);
            SpongeCommon.postEvent(event);
        }
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void impl$updateTrackedClientSettings(final CClientSettingsPacket packet, final CallbackInfo ci) {
        final CClientSettingsPacketAccessor $packet = (CClientSettingsPacketAccessor) packet;
        final Locale newLocale = LocaleCache.getLocale($packet.accessor$language());
        final int viewDistance = $packet.accessor$viewDistance();

        // Update locale on Channel, used for sending localized messages
        final Channel channel = ((NetworkManagerAccessor) this.connection.connection).accessor$channel();
        channel.attr(SpongeAdventure.CHANNEL_LOCALE).set(newLocale);
        SpongeAdventure.forEachBossBar(bar -> this.connection.send(new SUpdateBossInfoPacket(SUpdateBossInfoPacket.Operation.UPDATE_NAME, bar)));

        // Update the fields we track ourselves
        this.impl$viewDistance = viewDistance;
        this.impl$language = newLocale;
    }
}
