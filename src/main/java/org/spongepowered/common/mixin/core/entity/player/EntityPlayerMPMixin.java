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

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.storage.WorldProperties;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.scoreboard.TeamBridge;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.BasicEntityContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.network.NetHandlerPlayServerBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.service.user.SpongeUserStorageService;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.SkinUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayerMixin implements SubjectBridge, ServerPlayerEntityBridge, CommandSenderBridge,
    CommandSourceBridge {

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow public NetHandlerPlayServer connection;
    @Shadow private int lastExperience;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;
    @Shadow public boolean isChangingQuantityOnly;

    @Shadow @Override public abstract void takeStat(StatBase stat);
    @Shadow public abstract WorldServer getServerWorld();

    // Used to restore original item received in a packet after canceling an event
    private ItemStack impl$packetItem = ItemStack.EMPTY;
    private User impl$user = bridge$getUserObject();
    private Set<SkinPart> impl$skinParts = Sets.newHashSet();
    private int impl$viewDistance;
    @Nullable private GameType impl$pendingGameType;
    private Scoreboard impl$spongeScoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
    @Nullable private EntityPlayerMP impl$delegate;
    @Nullable private Vector3d impl$velocityOverride = null;
    private boolean impl$healthScaling = false;
    private double impl$healthScale = Constants.Entity.Player.DEFAULT_HEALTH_SCALE;
    private final PlayerOwnBorderListener borderListener = new PlayerOwnBorderListener((EntityPlayerMP) (Object) this);
    private boolean keepInventory = false;
    private float cachedHealth = -1;
    private float cachedScaledHealth = -1;
    @Nullable private Text displayName = null;

    @Override
    public void spongeImpl$writeToSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        if (this.impl$healthScaling) {
            compound.setDouble(Constants.Sponge.Entity.Player.HEALTH_SCALE, this.impl$healthScale);
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.hasKey(Constants.Sponge.Entity.Player.HEALTH_SCALE, Constants.NBT.TAG_DOUBLE)) {
            this.impl$healthScaling = true;
            this.impl$healthScale = compound.getDouble(Constants.Sponge.Entity.Player.HEALTH_SCALE);
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onRemoveEntity(final Entity entityIn, final CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }


    @Override
    public boolean bridge$keepInventory() {
        return this.keepInventory;
    }

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 3rd, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Override
    @Overwrite
    public void onDeath(final DamageSource cause) {
        // Sponge start
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        final Optional<DestructEntityEvent.Death> optEvent = SpongeCommonEventFactory.callDestructEntityEventDeath((EntityPlayerMP) (Object) this, cause, isMainThread);
        if (optEvent.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        final DestructEntityEvent.Death event = optEvent.get();

        // Double check that the PhaseTracker is already capturing the Death phase
        final boolean tracksEntityDeaths;
        if (isMainThread && !this.world.isRemote) {
            tracksEntityDeaths = PhaseTracker.getInstance().getCurrentState().tracksEntityDeaths();
        } else {
            tracksEntityDeaths = false;
        }
        try (final PhaseContext<?> context = createContextForDeath(cause, tracksEntityDeaths)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge end

            final boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
            this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

            if (flag) {
                final Team team = this.getTeam();

                if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                    if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                        this.server.getPlayerList()
                            .sendMessageToAllTeamMembers((EntityPlayerMP) (Object) this, this.getCombatTracker().getDeathMessage());
                    } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                        this.server.getPlayerList()
                            .sendMessageToTeamOrAllPlayers((EntityPlayerMP) (Object) this, this.getCombatTracker().getDeathMessage());
                    }
                } else {
                    this.server.getPlayerList().sendMessage(this.getCombatTracker().getDeathMessage());
                }
            }

            this.spawnShoulderEntities();

            // Ignore keepInventory GameRule instead use keepInventory from Event
            if (!event.getKeepInventory() && !this.isSpectator()) {
                this.destroyVanishingCursedItems();
                this.inventory.dropAllItems();
            }

            for (final ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
                final Score score = this.getWorldScoreboard().getOrCreateScore(this.shadow$getName(), scoreobjective);
                score.incrementScore();
            }

            final EntityLivingBase entitylivingbase = this.getAttackingEntity();

            if (entitylivingbase != null) {
                final EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

                if (entitylist$entityegginfo != null) {
                    this.addStat(entitylist$entityegginfo.entityKilledByStat);
                }

                entitylivingbase.awardKillScore((EntityPlayerMP) (Object) this, this.scoreValue, cause);
            }

            this.addStat(StatList.DEATHS);
            this.takeStat(StatList.TIME_SINCE_DEATH);
            this.extinguish();
            this.setFlag(0, false);
            this.getCombatTracker().reset();

            this.keepInventory = event.getKeepInventory();
        } // Sponge - brackets
    }

    @Nullable
    private PhaseContext<?> createContextForDeath(final DamageSource cause, final boolean tracksEntityDeaths) {
        return !tracksEntityDeaths
               ? EntityPhase.State.DEATH.createPhaseContext()
                   .source(this)
                   .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) cause)
               : null;
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void onClonePlayer(final EntityPlayerMP oldPlayer, final boolean respawnFromEnd, final CallbackInfo ci) {
        // Copy over sponge data from the old player.
        // Allows plugins to specify data that persists after players respawn.
        final EntityBridge oldEntity = (EntityBridge) oldPlayer;
        if (((DataCompoundHolder) oldEntity).data$hasRootCompound()) {
            final NBTTagCompound old = ((DataCompoundHolder) oldEntity).data$getRootCompound();
            if (old.hasKey(Constants.Sponge.SPONGE_DATA)) {
                ((DataCompoundHolder) this).data$getRootCompound().setTag(Constants.Sponge.SPONGE_DATA, old.getCompoundTag(Constants.Sponge.SPONGE_DATA));
                this.spongeImpl$readFromSpongeCompound(((DataCompoundHolder) this).data$getSpongeCompound());
            }
        }
    }

    @Redirect(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean keepInventory(final GameRules gameRules, final String key, final EntityPlayerMP corpse, final boolean keepEverything) {
        final boolean keep = ((PlayerEntityBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.copyInventory(corpse.inventory);
            // Clear corpse so that mods do not copy from it again
            corpse.inventory.clear();
        }
        return keep;
    }

    /* // gabizou comment - Due to forge changes, this is now required to be injected/overwritten
       // in either SpongeForge or SpongeVanilla respectively due to the signature change from Forge.
       // The logic is still being processed as normal in vanilla, just the actual method calls are
       // per project, and not in common.
     * @author blood - May 30th, 2016
     * @author gabizou - May 31st, 2016 - Update for 1.9.4 changes
     *
     * @reason - adjusted to support {@link MoveEntityEvent.Teleport}
     *
     * @param dimensionId The id of target dimension.
     *
    @Nullable
    @Override
    @Overwrite
    public Entity changeDimension(int dimensionId) {
        return EntityUtil.teleportPlayerToDimension((EntityPlayerMP)(Object) this, dimensionId);
    }
    */

    /**
     * @author Aaron1101 August 11th, 2018
     * @reason Wrap the method in a try-with-resources for a EntityPhase.State.PLAYER_WAKE_UP
     */
    @Override
    @Overwrite
    public void wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {
        // Sponge start - enter phase
        try (final BasicEntityContext basicEntityContext = EntityPhase.State.PLAYER_WAKE_UP.createPhaseContext()
                .source(this)
                .addCaptures()) {
            basicEntityContext.buildAndSwitch();
            // Sponge end

            if (this.isPlayerSleeping()) {
                this.getServerWorld().getEntityTracker()
                        .sendToTrackingAndSelf((Entity) (Object) this, new SPacketAnimation((Entity) (Object) this, 2)); // Sponge - cast to Entity
            }

            super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

            if (this.connection != null) {
                this.connection.setPlayerLocation(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            }
        } // Sponge - add bracket to close 'try' block
    }

    @Override
    public void bridge$forceRecreateUser() {
        final UserStorageService service = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);
        if (!(service instanceof SpongeUserStorageService)) {
            SpongeImpl.getLogger().error("Not re-creating User object for player {}, as UserStorageServer has been replaced with {}", this.shadow$getName(), service);
        } else {
            this.impl$user = ((SpongeUserStorageService) service).forceRecreateUser((GameProfile) this.getGameProfile());
        }
    }

    @Override
    public Optional<User> bridge$getBackingUser() {
        // may be null during initialization, mainly used to avoid potential stack overflow with #bridge$getUserObject
        return Optional.ofNullable(this.impl$user);
    }

    @Override
    public User bridge$getUserObject() {
        final UserStorageService service = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);
        if (this.isFake) { // Fake players are recogizeable through the field set up with bridge$isFake.
            return service.getOrCreate(SpongeUserStorageService.FAKEPLAYER_PROFILE);
        }
        return service.getOrCreate((GameProfile) this.getGameProfile());
    }

    @Override
    public User bridge$getUser() {
        return this.impl$user;
    }

    // Post before the player values are updated
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleClientSettings", at = @At("HEAD"))
    private void postClientSettingsEvent(final CPacketClientSettings packet, final CallbackInfo ci) {
        if (ShouldFire.PLAYER_CHANGE_CLIENT_SETTINGS_EVENT) {
            final CauseStackManager csm = Sponge.getCauseStackManager();
            csm.pushCause(this);
            try {
                final Cause cause = csm.getCurrentCause();
                final Set<SkinPart> skinParts = SkinUtil.fromFlags(packet.getModelPartFlags());
                final Locale locale = LocaleCache.getLocale(packet.getLang());
                final ChatVisibility visibility = (ChatVisibility) (Object) packet.getChatVisibility();
                final PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(cause, visibility, skinParts,
                    locale, (Player) this, packet.isColorsEnabled(), packet.view);
                SpongeImpl.postEvent(event);
            } finally {
                csm.popCause();
            }
        }
    }

    @Inject(method = "handleClientSettings", at = @At("RETURN"))
    private void impl$updateSkinFromPacket(final CPacketClientSettings packet, final CallbackInfo ci) {
        this.impl$skinParts = SkinUtil.fromFlags(packet.getModelPartFlags()); // Returned set is immutable
        this.impl$viewDistance = packet.view;
    }

    /**
     * @author simon816 - 14th November, 2016
     *
     * @reason Redirect messages sent to the player to fire a message event. Once the
     * event is handled, it will send the message to
     * {@link Player#sendMessage(ChatType, Text)}.
     *
     * @param component The message
     */
    @Overwrite
    public void sendMessage(final ITextComponent component) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        ChatUtil.sendMessage(component, MessageChannel.fixed((Player) this), (CommandSource) this.server, false);
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     *     inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Overwrite
    public String getPlayerIP() {
        return NetworkUtil.getHostString(this.connection.netManager.getRemoteAddress());
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return ((SubjectBridge) this.impl$user).bridge$getSubjectCollectionIdentifier();
    }

    @Override
    public String bridge$getIdentifier() {
        return this.impl$user.getIdentifier();
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return ((SubjectBridge) this.impl$user).bridge$permDefault(permission);
    }

    @Override
    public void refreshXpHealthAndFood() {
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
        bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$setPacketItem(final ItemStack itemstack) {
        this.impl$packetItem = itemstack;
    }

    @Override
    public void bridge$refreshExp() {
        this.lastExperience = -1;
    }

    @Override
    public void bridge$restorePacketItem(final EnumHand hand) {
        if (this.impl$packetItem.isEmpty()) {
            return;
        }

        this.isChangingQuantityOnly = true;
        this.setHeldItem(hand, this.impl$packetItem);
        final Slot slot = this.openContainer.getSlotFromInventory(this.inventory, this.inventory.currentItem);
        this.openContainer.detectAndSendChanges();
        this.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        this.connection.sendPacket(new SPacketSetSlot(this.openContainer.windowId, slot.slotNumber, this.impl$packetItem));
    }

    @Override
    public void bridge$initScoreboard() {
        ((ServerScoreboardBridge) this.getWorldScoreboard()).bridge$addPlayer((EntityPlayerMP) (Object) this, true);
    }

    @Override
    public Scoreboard bridge$getScoreboard() {
        return this.impl$spongeScoreboard;
    }

    @Override
    public void bridge$replaceScoreboard(@Nullable Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
        }
        this.impl$spongeScoreboard = scoreboard;
    }

    @Override
    public void bridge$setScoreboardOnRespawn(final Scoreboard scoreboard) {
        this.impl$spongeScoreboard = scoreboard;
        ((ServerScoreboardBridge) ((Player) this).getScoreboard()).bridge$addPlayer((EntityPlayerMP) (Object) this, false);
    }

    @Override
    public void bridge$removeScoreboardOnRespawn() {
        ((ServerScoreboardBridge) ((Player) this).getScoreboard()).bridge$removePlayer((EntityPlayerMP) (Object) this, false);
    }

    @Override
    public MessageChannel bridge$getDeathMessageChannel() {
        final EntityPlayerMP player = (EntityPlayerMP) (Object) this;
        if (player.world.getGameRules().getBoolean("showDeathMessages")) {
            @Nullable final Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    return ((TeamBridge) team).bridge$getTeamChannel(player);
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    return ((TeamBridge) team).bridge$getNonTeamChannel();
                }
            } else {
                return ((Player) this).getMessageChannel();
            }
        }

        return MessageChannel.TO_NONE;
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return (net.minecraft.scoreboard.Scoreboard) ((Player) this).getScoreboard();
    }


    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void onPlayerActive(final CallbackInfo ci) {
        ((NetHandlerPlayServerBridge) this.connection).bridge$resendLatestResourcePackRequest();
    }

    @Override
    public CommandSource bridge$asCommandSource() {
        return (CommandSource) this;
    }

    @Override
    public ICommandSender bridge$asICommandSender() {
        return (ICommandSender) this;
    }

    @Override
    public void bridge$setImplVelocity(final Vector3d velocity) {
        super.bridge$setImplVelocity(velocity);
        this.impl$velocityOverride = null;
    }

    @Override
    public void bridge$setVelocityOverride(@Nullable final Vector3d velocity) {
        this.impl$velocityOverride = velocity;
    }

    @Override
    public Vector3d bridge$getVelocityOverride() {
        return this.impl$velocityOverride;
    }

    @Override
    public Set<SkinPart> bridge$getSkinParts() {
        return this.impl$skinParts;
    }

    @Override
    public boolean bridge$hasDelegate() {
        return this.impl$delegate != null;
    }

    @Nullable
    @Override
    public EntityPlayerMP bridge$getDelegate() {
        return this.impl$delegate;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At("HEAD"), cancellable = true)
    private void spongeImpl$onSetGameTypeThrowEvent(final GameType gameType, final CallbackInfo ci) {
        if (ShouldFire.CHANGE_GAME_MODE_EVENT_TARGET_PLAYER) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                final ChangeGameModeEvent.TargetPlayer event =
                    SpongeEventFactory.createChangeGameModeEventTargetPlayer(frame.getCurrentCause(),
                        (GameMode) (Object) this.interactionManager.getGameType(), (GameMode) (Object) gameType, (Player) this);
                SpongeImpl.postEvent(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
                this.impl$pendingGameType = (GameType) (Object) event.getGameMode();
            }
        } else {
            this.impl$pendingGameType = gameType;
        }
    }

    /**
     * This injector must appear <b>after</b> {@link #spongeImpl$onSetGameTypeThrowEvent} since it
     * assigns the {@link #impl$pendingGameType} returned by the event to the actual
     * local variable in the method.
     */
    @ModifyVariable(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At(value = "HEAD", remap = false), argsOnly = true)
    private GameType spongeImpl$assignPendingGameType(final GameType gameType) {
        return this.impl$pendingGameType;
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z", ordinal = 0))
    private boolean spongeImpl$SuppressDeathMessageDueToPriorEvent(final GameRules gameRules, final String gameRule) {
        return false; // Suppress death messages since this is handled together with the event calling
    }

    @Override
    public void bridge$setTargetedLocation(@Nullable final Vector3d vec) {
        super.bridge$setTargetedLocation(vec);
        this.connection.sendPacket(new SPacketSpawnPosition(VecHelper.toBlockPos(this.bridge$getTargetedLocation())));
    }

    @Override
    @Nullable
    public Text bridge$getDisplayNameText() {
        return Text.of(shadow$getName());
    }

    @Override
    public void bridge$setDisplayName(@Nullable final Text displayName) {
        // Do nothing
    }

    @Override
    public void bridge$sendBlockChange(final BlockPos pos, final IBlockState state) {
        final SPacketBlockChange packet = new SPacketBlockChange();
        packet.blockPosition = pos;
        packet.blockState = state;
        this.connection.sendPacket(packet);
    }

    /**
     * @author gabizou, April 7th, 2016
     *
     * Technically an overwrite of {@link EntityPlayer#dropItem(boolean)}
     * @param dropAll
     * @return
     */
    @Override
    @Nullable
    public EntityItem dropItem(final boolean dropAll) {
        final ItemStack currentItem = this.inventory.getCurrentItem();
        if (currentItem.isEmpty()) {
            return null;
        }

        // Add SlotTransaction to PlayerContainer
        final org.spongepowered.api.item.inventory.Slot slot = ((Inventory) this.inventoryContainer)
                .query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(this.inventory.currentItem)));
        final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(currentItem);
        final ItemStack itemToDrop = this.inventory.decrStackSize(this.inventory.currentItem, dropAll && !currentItem.isEmpty() ? currentItem.getCount() : 1);
        ((TrackedInventoryBridge) this.inventoryContainer).bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, originalItem, ItemStackUtil.snapshotOf(this.inventory.getCurrentItem())));

        return this.dropItem(itemToDrop, false, true);
    }

    @Override
    public void stopActiveHand() { // stopActiveHand
        // Our using item state is probably desynced from the client (e.g. from the initial air interaction of a bow being cancelled).
        // We need to re-send the player's inventory to overwrite any client-side inventory changes that may have occured as a result
        // of the client (but not the server) calling Item#onPlayerStoppedUsing (which in the case of a bow, removes one arrow from the inventory).
        if (this.activeItemStack.isEmpty()) {
            ((EntityPlayerMP) (Object) this).sendContainerToPlayer(((EntityPlayerMP) (Object) this).inventoryContainer);
        }
        super.stopActiveHand();
    }

    @Inject(method = "closeContainer", at = @At("RETURN"))
    private void onCloseContainer(final CallbackInfo ci) {
        final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) this.openContainer;
        // Safety measure to avoid memory leaks as mods may call this directly
        if (mixinContainer.bridge$capturingInventory()) {
            mixinContainer.bridge$setCaptureInventory(false);
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
        }
    }

    @Inject(method = "displayGUIChest", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;openContainer:Lnet/minecraft/inventory/Container;", opcode = Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    private void onSetContainer(final IInventory chestInventory, final CallbackInfo ci) {
        if (!(chestInventory instanceof IInteractionObject) && this.openContainer instanceof ContainerChest && this.isSpectator()) {
            SpongeImpl.getLogger().warn("Opening fallback ContainerChest for inventory '{}'. Most API inventory methods will not be supported", chestInventory);
            ((ContainerBridge) this.openContainer).bridge$setSpectatorChest(true);
        }
    }

    @Override
    public PlayerOwnBorderListener bridge$getWorldBorderListener() {
        return this.borderListener;
    }

    /**
     * Send SlotCrafting updates to client for custom recipes.
     *
     * @author Faithcaio - 31.12.2016
     * @reason Vanilla is not updating the Client when Slot is SlotCrafting - this is an issue when plugins register new recipes
     */
    @Inject(method = "sendSlotContents", at = @At("HEAD"))
    private void sendSlotContents(
        final net.minecraft.inventory.Container containerToSend, final int slotInd, final ItemStack stack, final CallbackInfo ci) {
        if (containerToSend.getSlot(slotInd) instanceof SlotCrafting) {
            this.connection.sendPacket(new SPacketSetSlot(containerToSend.windowId, slotInd, stack));
        }
    }

    @Redirect(method = "onUpdateEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayerMP;getHealth()F"
            ),
            slice =  @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/entity/player/EntityPlayerMP;lastHealth:F"
                    ),
                    to = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/network/play/server/SPacketUpdateHealth;<init>(FIF)V"
                    )
            )
    )
    private float spongeGetScaledHealthForPacket(final EntityPlayerMP entityPlayerMP) {
        return bridge$getInternalScaledHealth();
    }

    @Inject(method = "onUpdateEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getTotalArmorValue()I", ordinal = 0))
    private void updateHealthPriorToArmor(final CallbackInfo ci) {
        bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$setHealthScale(final double scale) {
        checkArgument(scale > 0, "Health scale must be greater than 0!");
        checkArgument(scale < Float.MAX_VALUE, "Health scale cannot exceed Float.MAX_VALUE!");
        this.impl$healthScale = scale;
        this.impl$healthScaling = true;
        bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$refreshScaledHealth() {
        // We need to use the dirty instances to signify that the player needs to ahve it updated, instead
        // of modifying the attribute instances themselves, we bypass other potentially detrimental logi
        // that would otherwise break the actual health scaling.
        final Set<IAttributeInstance> dirtyInstances = ((AttributeMap) this.getAttributeMap()).getDirtyInstances();
        bridge$injectScaledHealth(dirtyInstances, true);

        // Send the new information to the client.
        sendHealthUpdate();
        this.connection.sendPacket(new SPacketEntityProperties(this.getEntityId(), dirtyInstances));
        // Reset the dirty instances since they've now been manually updated on the client.
        dirtyInstances.clear();

    }

    private void sendHealthUpdate() {
        this.connection.sendPacket(new SPacketUpdateHealth(bridge$getInternalScaledHealth(), getFoodStats().getFoodLevel(), getFoodStats().getSaturationLevel()));
    }

    @Override
    public void bridge$injectScaledHealth(final Collection<IAttributeInstance> set, final boolean force) {
        if (!this.impl$healthScaling && !force) {
            return;
        }
        // We need to remove the existing attribute instance for max health, since it's not always going to be the
        // same as SharedMonsterAttributes.MAX_HEALTH
        @Nullable Collection<AttributeModifier> modifiers = null;
        boolean foundMax = false; // Sometimes the max health isn't modified and no longer dirty
        for (final Iterator<IAttributeInstance> iter = set.iterator(); iter.hasNext(); ) {
            final IAttributeInstance dirtyInstance = iter.next();
            if ("generic.maxHealth".equals(dirtyInstance.getAttribute().getName())) {
                foundMax = true;
                modifiers = dirtyInstance.getModifiers();
                iter.remove();
                break;
            }
        }
        if (!foundMax) {
            // Means we didn't find the max health attribute and need to fetch the modifiers from
            // the cached map because it wasn't marked dirty for some reason
            modifiers = this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifiers();
        }

        // We now re-create a new ranged attribute for our desired max health
        final RangedAttribute maxHealth =
            new RangedAttribute(null, "generic.maxHealth", this.impl$healthScaling ? this.impl$healthScale : getMaxHealth(), 0.0D, Float.MAX_VALUE);
        maxHealth.setDescription("Max Health");
        maxHealth.setShouldWatch(true); // needs to be watched

        final ModifiableAttributeInstance attribute = new ModifiableAttributeInstance(this.getAttributeMap(), maxHealth);

        if (!modifiers.isEmpty()) {
            modifiers.forEach(attribute::applyModifier);
        }
        set.add(attribute);

    }

    @Override
    public double bridge$getHealthScale() {
        return this.impl$healthScale;
    }

    @Override
    public float bridge$getInternalScaledHealth() {
        if (this.impl$healthScaling) {
            // Micro-optimization so we don't have to recalculate it all the time
            if (this.cachedHealth != -1 && this.getHealth() == this.cachedHealth) {
                if (this.cachedScaledHealth != -1) {
                    return this.cachedScaledHealth;
                }
            }
            this.cachedHealth = this.getHealth();
            // Because attribute modifiers from mods can add onto health and multiply health, we
            // need to replicate what the mod may be trying to represent, regardless whether the health scale
            // says to show only x hearts.
            final IAttributeInstance maxAttribute = this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
            double modifiedScale = (float) this.impl$healthScale;
            // Apply additive modifiers
            for (final AttributeModifier attributemodifier : maxAttribute.getModifiersByOperation(0)) {
                modifiedScale += attributemodifier.getAmount();
            }


            // Apply
            for (final AttributeModifier attributemodifier1 : maxAttribute.getModifiersByOperation(1)) {
                modifiedScale += modifiedScale * attributemodifier1.getAmount();
            }

            for (final AttributeModifier attributemodifier2 : maxAttribute.getModifiersByOperation(2)) {
                modifiedScale *= 1.0D + attributemodifier2.getAmount();
            }

            final float maxHealth = getMaxHealth();
            this.cachedScaledHealth = (float) ((this.cachedHealth / maxHealth) * modifiedScale);
            return this.cachedScaledHealth;
        }
        return getHealth();
    }

    @Override
    public boolean bridge$isHealthScaled() {
        return this.impl$healthScaling;
    }

    @Override
    public void bridge$setHealthScaled(final boolean scaled) {
        this.impl$healthScaling = scaled;
    }

    @Override
    public void updateDataManagerForScaledHealth() {
        this.dataManager.set(EntityLivingBase.HEALTH, bridge$getInternalScaledHealth());
    }

    @Redirect(method = "readEntityFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForceGamemode()Z"))
    private boolean onCheckForcedGameMode(final MinecraftServer minecraftServer) {
        return minecraftServer.getForceGamemode() && !bridge$hasForcedGamemodeOverridePermission();
    }

    @Override
    public boolean bridge$hasForcedGamemodeOverridePermission() {
        final Player player = (Player) this;
        return player.hasPermission(player.getActiveContexts(), Constants.Permissions.FORCE_GAMEMODE_OVERRIDE);
    }

    @Override
    public void bridge$setDelegateAfterRespawn(EntityPlayerMP delegate) {
        this.impl$delegate = delegate;
    }

    @Override
    public void bridge$setContainerDisplay(final Text displayName) {
        this.displayName = displayName;
    }

    @Redirect(method = "displayGUIChest", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName(final IInventory chestInventory) {
        if (this.displayName == null) {
            return chestInventory.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "displayGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IInteractionObject;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName2(final IInteractionObject guiOwner) {
        if (this.displayName == null) {
            return guiOwner.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "openGuiHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName3(final IInventory inventoryIn) {
        if (this.displayName == null) {
            return inventoryIn.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "displayVillagerTradeGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/IMerchant;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName4(final IMerchant villager) {
        if (this.displayName == null) {
            return villager.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Inject(method = "canAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void onCanAttackPlayer(final EntityPlayer other, final CallbackInfoReturnable<Boolean> cir) {
        final boolean worldPVP = ((WorldProperties) other.world.getWorldInfo()).isPVPEnabled();

        if (!worldPVP) {
            cir.setReturnValue(false);
            return;
        }

        final boolean teamPVP = super.canAttackPlayer(other);
        cir.setReturnValue(teamPVP);
    }

    @Override
    public int bridge$getViewDistance() {
        return this.impl$viewDistance;
    }
}
