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

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SCombatPacket;
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
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.Audiences;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.RotateEntityEvent;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
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
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.WrappedITeleporterPortalType;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashSet;

import javax.annotation.Nullable;

// See also: SubjectMixin_API and SubjectMixin
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements SubjectBridge, ServerPlayerEntityBridge {

    // @formatter:off
    @Shadow public ServerPlayNetHandler connection;
    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow @Final public MinecraftServer server;
    @Shadow private int lastExperience;

    @Shadow public abstract net.minecraft.world.server.ServerWorld shadow$getServerWorld();
    @Shadow public abstract void shadow$setSpectatingEntity(Entity p_175399_1_);
    @Shadow public abstract void shadow$stopRiding();
    @Shadow public abstract void shadow$closeContainer();
    @Shadow public abstract void shadow$takeStat(Stat<?> stat);
    // @formatter:on

    private final User impl$user = this.impl$getUserObjectOnConstruction();
    private @Nullable ITextComponent impl$connectionMessage;
    @Nullable private Vector3d impl$velocityOverride = null;
    private Scoreboard impl$scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
    @Nullable private Boolean impl$keepInventory = null;
    // Used to restore original item received in a packet after canceling an event
    private ItemStack impl$packetItem = ItemStack.EMPTY;

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
        if (this.removed || ((WorldBridge) location.getWorld()).bridge$isFake()) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.getActivePlugin());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

            ServerWorld destinationWorld = (net.minecraft.world.server.ServerWorld) location.getWorld();

            Vector3d toPosition;

            if (this.shadow$getServerWorld() != destinationWorld) {
                final ChangeEntityWorldEvent.Pre event = SpongeEventFactory.createChangeEntityWorldEventPre(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, (org.spongepowered.api.world.server.ServerWorld) this.shadow$getServerWorld(),
                        location.getWorld(), location.getWorld());
                if (SpongeCommon.postEvent(event)) {
                    return false;
                }

                final ChangeEntityWorldEvent.Reposition repositionEvent =
                        SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                                (org.spongepowered.api.entity.Entity) this, (org.spongepowered.api.world.server.ServerWorld) this.shadow$getServerWorld(),
                                VecHelper.toVector3d(this.shadow$getPositionVector()), location.getPosition(), event.getOriginalDestinationWorld(),
                                location.getPosition(), event.getDestinationWorld());

                if (SpongeCommon.postEvent(repositionEvent)) {
                    return false;
                }

                destinationWorld = (net.minecraft.world.server.ServerWorld) event.getDestinationWorld();

                toPosition = repositionEvent.getDestinationPosition();
            } else {
                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$getPositionVector()),
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
            destinationWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1, ((ServerPlayerEntity) (Object) this).getEntityId());

            if (this.shadow$getServerWorld() != destinationWorld) {
                this.shadow$setLocationAndAngles(toPosition.getX(), toPosition.getY(), toPosition.getZ(), this.rotationYaw, this.rotationPitch);

                EntityUtil.performPostChangePlayerWorldLogic((ServerPlayerEntity) (Object) this, this.shadow$getServerWorld(),
                        (net.minecraft.world.server.ServerWorld) location.getWorld(), destinationWorld, false);
            } else {
                this.connection.setPlayerLocation(toPosition.getX(), toPosition.getY(), toPosition.getZ(), this.rotationYaw, this.rotationPitch,
                        new HashSet<>());
            }
        }

        return true;
    }

    @Override
    public void bridge$refreshExp() {
        this.lastExperience = -1;
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
    public void bridge$setVelocityOverride(@Nullable final Vector3d velocity) {
        this.impl$velocityOverride = velocity;
    }

    @Override
    @Nullable
    public Vector3d bridge$getVelocityOverride() {
        return this.impl$velocityOverride;
    }

    @Override
    public void bridge$initScoreboard() {
        ((ServerScoreboardBridge) this.shadow$getWorldScoreboard()).bridge$addPlayer((ServerPlayerEntity) (Object) this, true);
    }

    @Override
    public void bridge$removeScoreboardOnRespawn() {
        ((ServerScoreboardBridge) ((ServerPlayer) this).getScoreboard()).bridge$removePlayer((ServerPlayerEntity) (Object) this, false);
    }

    @Override
    public void bridge$setScoreboardOnRespawn(Scoreboard scoreboard) {
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
            return this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
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
    public void teleport(net.minecraft.world.server.ServerWorld world, double x, double y, double z, float yaw, float pitch) {
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

            if (world == player.world) {
                final MoveEntityEvent posEvent = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, VecHelper.toVector3d(player.getPositionVector()),
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
                actualYaw = rotateEvent.isCancelled() ? player.rotationYaw : rotateEvent.getToRotation().getY();
                actualPitch = rotateEvent.isCancelled() ? player.rotationPitch : rotateEvent.getToRotation().getX();

                this.shadow$setSpectatingEntity(player);
                this.shadow$stopRiding();

                if (player.isSleeping()) {
                    player.stopSleepInBed(true, true);
                }

                player.connection.setPlayerLocation(actualX, actualY, actualZ, (float) actualYaw, (float) actualPitch);

                player.setRotationYawHead((float) actualYaw);

                ChunkPos chunkpos = new ChunkPos(new BlockPos(actualX, actualY, actualZ));
                world.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, player.getEntityId());
            } else {
                final ChangeEntityWorldEvent.Pre preEvent = PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPre(player, world);
                if (SpongeCommon.postEvent(preEvent)) {
                    return;
                }

                final MoveEntityEvent posEvent = SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, preEvent.getOriginalWorld(), VecHelper.toVector3d(player.getPositionVector()),
                        new Vector3d(x, y, z), preEvent.getOriginalDestinationWorld(), new Vector3d(x, y, z), preEvent.getDestinationWorld());

                final RotateEntityEvent rotateEvent = SpongeEventFactory.createRotateEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) player, new Vector3d(actualYaw, actualPitch, 0),
                        new Vector3d(yaw, pitch, 0));

                if (SpongeCommon.postEvent(posEvent)) {
                    return;
                }

                this.shadow$setPosition(posEvent.getDestinationPosition().getX(), posEvent.getDestinationPosition().getY(),
                        posEvent.getDestinationPosition().getZ());

                if (!SpongeCommon.postEvent(rotateEvent)) {
                    this.rotationYaw = (float) rotateEvent.getToRotation().getX();
                    this.rotationPitch = (float) rotateEvent.getToRotation().getY();
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
    @Nullable
    @Overwrite
    public Entity changeDimension(DimensionType destination) {
        if (this.shadow$getEntityWorld().isRemote || this.removed) {
            return (ServerPlayerEntity) (Object) this;
        }

        final WrappedITeleporterPortalType portalType = new WrappedITeleporterPortalType((PlatformITeleporterBridge) this.shadow$getServer()
                .getWorld(destination).getDefaultTeleporter(), null);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.pushCause(portalType);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PORTAL);

            EntityUtil.invokePortalTo((ServerPlayerEntity) (Object) this, portalType, destination);
            return (ServerPlayerEntity) (Object) this;
        }
    }

    @Inject(method = "removeEntity", at = @At("RETURN"))
    private void impl$removeHumanFromPlayerClient(final Entity entityIn, final CallbackInfo ci) {
        if (entityIn instanceof HumanEntity) {
            ((HumanEntity) entityIn).untrackFrom((ServerPlayerEntity) (Object) this);
        }
    }

    @Redirect(
            method = {"openContainer", "openHorseInventory"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;closeScreen()V"
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
    public void onDeath(final DamageSource cause) {
        // Sponge start - Call Destruct Death Event
        final DestructEntityEvent.Death event = SpongeCommonEventFactory.callDestructEntityEventDeath((ServerPlayerEntity) (Object) this, cause,
                Audiences.server());
        if (event.isCancelled()) {
            return;
        }
        // Sponge end

        final boolean flag = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES) && !event.isMessageCancelled();
        if (flag) {
            final ITextComponent itextcomponent = this.shadow$getCombatTracker().getDeathMessage();
            this.connection.sendPacket(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent), (p_212356_2_) -> {
                if (!p_212356_2_.isSuccess()) {
                    int i = 256;
                    String s = itextcomponent.getStringTruncated(256);
                    final ITextComponent itextcomponent1 = new TranslationTextComponent("death.attack.message_too_long", (new StringTextComponent(s)).applyTextStyle(
                            TextFormatting.YELLOW));
                    final ITextComponent itextcomponent2 =
                            (new TranslationTextComponent("death.attack.even_more_magic", this.shadow$getDisplayName())).applyTextStyle((p_212357_1_) -> {
                        p_212357_1_.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent1));
                    });
                    this.connection.sendPacket(new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED, itextcomponent2));
                }

            });
            final Team team = this.shadow$getTeam();
            if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().sendMessageToAllTeamMembers(
                            (ServerPlayerEntity) (Object) this, itextcomponent);
                } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().sendMessageToTeamOrAllPlayers(
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
            this.connection.sendPacket(
                    new SCombatPacket(this.shadow$getCombatTracker(), SCombatPacket.Event.ENTITY_DIED));
        }

        this.shadow$spawnShoulderEntities();

        // Sponge Start - update the keep inventory flag for dropping inventory
        // during the death update ticks
        this.impl$keepInventory = event.getKeepInventory();

        if (!this.shadow$isSpectator()) {
            this.shadow$spawnDrops(cause);
        }
        // Sponge End

        this.shadow$getWorldScoreboard().forAllObjectives(
                ScoreCriteria.DEATH_COUNT, this.shadow$getScoreboardName(), Score::incrementScore);
        final LivingEntity livingentity = this.shadow$getAttackingEntity();
        if (livingentity != null) {
            this.shadow$addStat(Stats.ENTITY_KILLED_BY.get(livingentity.getType()));
            livingentity.awardKillScore((ServerPlayerEntity) (Object) this, this.scoreValue, cause);
            this.shadow$createWitherRose(livingentity);
        }

        this.world.setEntityState((ServerPlayerEntity) (Object) this, (byte) 3);
        this.shadow$addStat(Stats.DEATHS);
        this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.shadow$extinguish();
        this.shadow$setFlag(0, false);
        this.shadow$getCombatTracker().reset();
    }

    @Redirect(method = "copyFrom",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z"))
    private boolean tracker$useKeepFromBridge(final GameRules gameRules, final GameRules.RuleKey<?> key,
            final ServerPlayerEntity corpse, final boolean keepEverything) {
        final boolean keep = ((PlayerEntityBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.copyInventory(corpse.inventory);
            // Clear corpse so that mods do not copy from it again
            corpse.inventory.clear();
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

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void impl$copySpongeDataOnRespawn(final ServerPlayerEntity oldPlayer, final boolean respawnFromEnd, final CallbackInfo ci) {
        if (oldPlayer instanceof DataCompoundHolder) {
            final DataCompoundHolder oldEntity = (DataCompoundHolder) oldPlayer;
            if (oldEntity.data$hasSpongeData()) {
                final CompoundNBT compound = oldEntity.data$getCompound();
                ((DataCompoundHolder) this).data$setCompound(compound);
            }
        }
    }

}
