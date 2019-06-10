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
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import net.minecraft.advancements.PlayerAdvancements;
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
import net.minecraft.entity.passive.AbstractHorse;
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
import net.minecraft.stats.StatisticsManagerServer;
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
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.WorldBorder;
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
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.BasicEntityContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.bridge.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.service.user.SpongeUserStorageService;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.SkinUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements IMixinSubject, IMixinEntityPlayerMP, IMixinCommandSender,
        IMixinCommandSource {

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow @Final private PlayerAdvancements advancements;
    @Shadow private String language;
    @Shadow public NetHandlerPlayServer connection;
    @Shadow public int lastExperience;
    @Shadow private EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    @Shadow private boolean chatColours;
    @Shadow public boolean queuedEndExit;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;
    @Shadow public boolean isChangingQuantityOnly;

    @Shadow public abstract Entity getSpectatingEntity();
    @Shadow public abstract void setSpectatingEntity(Entity entity);
    @Shadow public abstract void sendPlayerAbilities();
    @Shadow @Override public abstract void takeStat(StatBase stat);
    @Shadow public abstract StatisticsManagerServer getStatFile();
    @Shadow public abstract void displayGUIChest(IInventory chestInventory);
    @Shadow public abstract void displayGui(IInteractionObject guiOwner);
    @Shadow public abstract void openGuiHorseInventory(AbstractHorse horse, IInventory horseInventory);

    // Inventory
    @Shadow public abstract void closeScreen();
    @Shadow public int currentWindowId;
    @Shadow private void getNextWindowId() { }

    @Shadow public abstract void closeContainer();

    @Shadow public abstract WorldServer getServerWorld();

    @Shadow protected abstract boolean canPlayersAttack();

    public int newExperience = 0;
    public int newLevel = 0;
    public int newTotalExperience = 0;
    public boolean keepsLevel = false;
    private boolean sleepingIgnored;
    // Used to restore original item received in a packet after canceling an event
    private ItemStack packetItem;

    private User user = getUserObject();

    private Set<SkinPart> skinParts = Sets.newHashSet();
    private int viewDistance;
    private TabList tabList = new SpongeTabList((EntityPlayerMP) (Object) this);

    private GameType pendingGameType;

    private Scoreboard spongeScoreboard = Sponge.getGame().getServer().getServerScoreboard().get();

    @Nullable private Vector3d velocityOverride = null;
    private boolean healthScaling = false;
    private double healthScale = 20;

    @Override
    public void spongeImpl$writeToSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        if (this.healthScaling) {
            compound.setDouble(NbtDataUtil.HEALTH_SCALE, this.healthScale);
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.hasKey(NbtDataUtil.HEALTH_SCALE, NbtDataUtil.TAG_DOUBLE)) {
            this.healthScaling = true;
            this.healthScale = compound.getDouble(NbtDataUtil.HEALTH_SCALE);
        }
    }

    @Nullable private WorldBorder worldBorder;
    private final PlayerOwnBorderListener borderListener = new PlayerOwnBorderListener((EntityPlayerMP) (Object) this);

    @Inject(method = "removeEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void onRemoveEntity(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }

    private boolean keepInventory = false;

    @Override
    public boolean keepInventory() {
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
    public void onDeath(DamageSource cause) {
        // Sponge start
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        Optional<DestructEntityEvent.Death> optEvent = SpongeCommonEventFactory.callDestructEntityEventDeath((EntityPlayerMP) (Object) this, cause, isMainThread);
        if (optEvent.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        DestructEntityEvent.Death event = optEvent.get();

        // Double check that the PhaseTracker is already capturing the Death phase
        final boolean tracksEntityDeaths;
        if (isMainThread && !this.world.isRemote) {
            tracksEntityDeaths = PhaseTracker.getInstance().getCurrentState().tracksEntityDeaths();
        } else {
            tracksEntityDeaths = false;
        }
        try (PhaseContext<?> context = createContextForDeath(cause, tracksEntityDeaths)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge end

            boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
            this.connection.sendPacket(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

            if (flag) {
                Team team = this.getTeam();

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

            for (ScoreObjective scoreobjective : this.getWorldScoreboard().getObjectivesFromCriteria(IScoreCriteria.DEATH_COUNT)) {
                Score score = this.getWorldScoreboard().getOrCreateScore(this.getName(), scoreobjective);
                score.incrementScore();
            }

            EntityLivingBase entitylivingbase = this.getAttackingEntity();

            if (entitylivingbase != null) {
                EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.ENTITY_EGGS.get(EntityList.getKey(entitylivingbase));

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
    private PhaseContext<?> createContextForDeath(DamageSource cause, boolean tracksEntityDeaths) {
        return !tracksEntityDeaths
               ? EntityPhase.State.DEATH.createPhaseContext()
                   .source(this)
                   .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) cause)
               : null;
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    public void onClonePlayer(EntityPlayerMP oldPlayer, boolean respawnFromEnd, CallbackInfo ci) {
        // Copy over sponge data from the old player.
        // Allows plugins to specify data that persists after players respawn.
        IMixinEntity oldEntity = (IMixinEntity) oldPlayer;
        NBTTagCompound old = oldEntity.getEntityData();
        if (old.hasKey(NbtDataUtil.SPONGE_DATA)) {
            this.getEntityData().setTag(NbtDataUtil.SPONGE_DATA, old.getCompoundTag(NbtDataUtil.SPONGE_DATA));
            this.spongeImpl$readFromSpongeCompound(this.getSpongeData());
        }
    }

    @Redirect(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean keepInventory(GameRules gameRules, String key, EntityPlayerMP corpse, boolean keepEverything) {
        boolean keep = ((IMixinEntityPlayer) corpse).keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.copyInventory(corpse.inventory);
            // Clear corpse so that mods do not copy from it again
            corpse.inventory.clear();
        }
        return keep;
    }

    @Override
    public IMixinWorldServer getMixinWorld() {
        return ((IMixinWorldServer) this.world);
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
    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
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
    public void forceRecreateUser() {
        UserStorageService service = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);
        if (!(service instanceof SpongeUserStorageService)) {
            SpongeImpl.getLogger().error("Not re-creating User object for player {}, as UserStorageServer has been replaced with {}", this.getName(), service);
        } else {
            this.user = ((SpongeUserStorageService) service).forceRecreateUser((GameProfile) this.getGameProfile());
        }
    }

    @Override
    public Optional<User> getBackingUser() {
        // may be null during initialization, mainly used to avoid potential stack overflow with #getUserObject
        return Optional.ofNullable(this.user);
    }

    @Override
    public User getUserObject() {
        final UserStorageService service = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);
        if (this.isFake) { // Fake players are recogizeable through the field set up with isFake.
            return service.getOrCreate(SpongeUserStorageService.FAKEPLAYER_PROFILE);
        }
        return service.getOrCreate((GameProfile) this.getGameProfile());
    }

    // Post before the player values are updated
    @Inject(method = "handleClientSettings", at = @At("HEAD"))
    public void postClientSettingsEvent(CPacketClientSettings packet, CallbackInfo ci) {
        // TODO: add HandPreference to PlayerChangeClientSettingsEvent once DominantHandData is implemented
        Sponge.getCauseStackManager().pushCause(this);
        final PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(Sponge.getCauseStackManager().getCurrentCause(),
                (ChatVisibility) (Object) packet.getChatVisibility(), SkinUtil.fromFlags(packet.getModelPartFlags()),
                LocaleCache.getLocale(packet.getLang()), this, packet.isColorsEnabled(), packet.view);
        SpongeImpl.postEvent(event);
        Sponge.getCauseStackManager().popCause();
    }

    @Inject(method = "handleClientSettings", at = @At("RETURN"))
    public void captureClientSettings(CPacketClientSettings packet, CallbackInfo ci) {
        this.skinParts = SkinUtil.fromFlags(packet.getModelPartFlags()); // Returned set is immutable
        this.viewDistance = packet.view;
    }

    /**
     * @author simon816 - 14th November, 2016
     *
     * @reason Redirect messages sent to the player to fire a message event. Once the
     * event is handled, it will send the message to
     * {@link #sendMessage(ChatType, Text)}.
     *
     * @param component The message
     */
    @Overwrite
    public void sendMessage(ITextComponent component) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        ChatUtil.sendMessage(component, MessageChannel.fixed(this), (CommandSource) this.server, false);
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
    public String getSubjectCollectionIdentifier() {
        return ((IMixinSubject) this.user).getSubjectCollectionIdentifier();
    }

    @Override
    public String getIdentifier() {
        return this.user.getIdentifier();
    }

    @Override
    public Tristate permDefault(String permission) {
        return ((IMixinSubject) this.user).permDefault(permission);
    }

    @Override
    public void refreshXpHealthAndFood() {
        this.lastExperience = -1;
        this.lastHealth = -1.0F;
        this.lastFoodLevel = -1;
        refreshScaledHealth();
    }

    @Override
    public void setPacketItem(ItemStack itemstack) {
        this.packetItem = itemstack;
    }

    @Override
    public void refreshExp() {
        this.lastExperience = -1;
    }

    @Override
    public void restorePacketItem(EnumHand hand) {
        if (this.packetItem.isEmpty()) {
            return;
        }

        this.isChangingQuantityOnly = true;
        this.setHeldItem(hand, this.packetItem);
        Slot slot = this.openContainer.getSlotFromInventory(this.inventory, this.inventory.currentItem);
        this.openContainer.detectAndSendChanges();
        this.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        this.connection.sendPacket(new SPacketSetSlot(this.openContainer.windowId, slot.slotNumber, this.packetItem));
    }

    @Override
    public void initScoreboard() {
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this, true);
    }

    @Override
    public void setScoreboardOnRespawn(Scoreboard scoreboard) {
        this.spongeScoreboard = scoreboard;
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this, false);
    }

    @Override
    public void removeScoreboardOnRespawn() {
        ((IMixinServerScoreboard) this.spongeScoreboard).removePlayer((EntityPlayerMP) (Object) this, false);
        // This player is being removed, so this is fine
        this.spongeScoreboard = null;
    }

    @Override
    public MessageChannel getDeathMessageChannel() {
        EntityPlayerMP player = (EntityPlayerMP) (Object) this;
        if (player.world.getGameRules().getBoolean("showDeathMessages")) {
            @Nullable Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.EnumVisible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    return ((IMixinTeam) team).getTeamChannel(player);
                } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    return ((IMixinTeam) team).getNonTeamChannel();
                }
            } else {
                return ((Player) this).getMessageChannel();
            }
        }

        return MessageChannel.TO_NONE;
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return (net.minecraft.scoreboard.Scoreboard) this.spongeScoreboard;
    }

    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void onPlayerActive(CallbackInfo ci) {
        ((IMixinNetHandlerPlayServer) this.connection).resendLatestResourcePackRequest();
    }

    @Override
    public CommandSource asCommandSource() {
        return (CommandSource) this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (ICommandSender) this;
    }

    @Override
    public void setImplVelocity(Vector3d velocity) {
        super.setImplVelocity(velocity);
        this.velocityOverride = null;
    }

    @Override
    public void setVelocityOverride(@Nullable Vector3d velocity) {
        this.velocityOverride = velocity;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At("HEAD"), cancellable = true)
    private void spongeImpl$onSetGameTypeThrowEvent(GameType gameType, CallbackInfo ci) {
        if (ShouldFire.CHANGE_GAME_MODE_EVENT_TARGET_PLAYER) {
            try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                ChangeGameModeEvent.TargetPlayer event =
                    SpongeEventFactory.createChangeGameModeEventTargetPlayer(frame.getCurrentCause(),
                        (GameMode) (Object) this.interactionManager.getGameType(), (GameMode) (Object) gameType, (Player) this);
                SpongeImpl.postEvent(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
                this.pendingGameType = (GameType) (Object) event.getGameMode();
            }

        }
    }

    /**
     * This injector must appear <b>after</b> {@link #spongeImpl$onSetGameTypeThrowEvent} since it
     * assigns the {@link #pendingGameType} returned by the event to the actual
     * local variable in the method.
     */
    @ModifyVariable(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At(value = "HEAD", remap = false), argsOnly = true)
    private GameType spongeImpl$assignPendingGameType(GameType gameType) {
        return this.pendingGameType;
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z", ordinal = 0))
    private boolean spongeImpl$SuppressDeathMessageDueToPriorEvent(GameRules gameRules, String gameRule) {
        return false; // Suppress death messages since this is handled together with the event calling
    }

    @Override
    public void setTargetedLocation(@Nullable Vector3d vec) {
        super.setTargetedLocation(vec);
        this.connection.sendPacket(new SPacketSpawnPosition(VecHelper.toBlockPos(this.getTargetedLocation())));
    }

    @Override
    @Nullable
    public Text getDisplayNameText() {
        return Text.of(getName());
    }

    @Override
    public void setDisplayName(@Nullable Text displayName) {
        // Do nothing
    }

    @Override
    public void sendBlockChange(BlockPos pos, IBlockState state) {
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
    public EntityItem dropItem(boolean dropAll) {
        final ItemStack currentItem = this.inventory.getCurrentItem();
        if (currentItem.isEmpty()) {
            return null;
        }

        // Add SlotTransaction to PlayerContainer
        org.spongepowered.api.item.inventory.Slot slot = ((Inventory) this.inventoryContainer)
                .query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(this.inventory.currentItem)));
        final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(currentItem);
        ItemStack itemToDrop = this.inventory.decrStackSize(this.inventory.currentItem, dropAll && !currentItem.isEmpty() ? currentItem.getCount() : 1);
        ((IMixinContainer) this.inventoryContainer).getCapturedTransactions().add(new SlotTransaction(slot, originalItem, ItemStackUtil.snapshotOf(this.inventory.getCurrentItem())));

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
    public void onCloseContainer(CallbackInfo ci) {
        IMixinContainer mixinContainer = (IMixinContainer) this.openContainer;
        // Safety measure to avoid memory leaks as mods may call this directly
        if (mixinContainer.capturingInventory()) {
            mixinContainer.setCaptureInventory(false);
            mixinContainer.getCapturedTransactions().clear();
        }
    }

    @Inject(method = "displayGUIChest", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/EntityPlayerMP;openContainer:Lnet/minecraft/inventory/Container;", opcode = Opcodes.PUTFIELD, ordinal = 1, shift = At.Shift.AFTER))
    public void onSetContainer(IInventory chestInventory, CallbackInfo ci) {
        if (!(chestInventory instanceof IInteractionObject) && this.openContainer instanceof ContainerChest && this.isSpectator()) {
            SpongeImpl.getLogger().warn("Opening fallback ContainerChest for inventory '{}'. Most API inventory methods will not be supported", chestInventory);
            ((IMixinContainer) this.openContainer).setSpectatorChest(true);
        }
    }

    @Override
    public PlayerOwnBorderListener getWorldBorderListener() {
        return this.borderListener;
    }


    /**
     * Send SlotCrafting updates to client for custom recipes.
     *
     * @author Faithcaio - 31.12.2016
     * @reason Vanilla is not updating the Client when Slot is SlotCrafting - this is an issue when plugins register new recipes
     */
    @Inject(method = "sendSlotContents", at = @At("HEAD"))
    private void sendSlotContents(net.minecraft.inventory.Container containerToSend, int slotInd, ItemStack stack, CallbackInfo ci) {
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
    private float spongeGetScaledHealthForPacket(EntityPlayerMP entityPlayerMP) {
        return getInternalScaledHealth();
    }

    @Inject(method = "onUpdateEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getTotalArmorValue()I", ordinal = 0))
    private void updateHealthPriorToArmor(CallbackInfo ci) {
        refreshScaledHealth();
    }

    @Override
    public void setHealthScale(double scale) {
        checkArgument(scale > 0, "Health scale must be greater than 0!");
        checkArgument(scale < Float.MAX_VALUE, "Health scale cannot exceed Float.MAX_VALUE!");
        this.healthScale = scale;
        this.healthScaling = true;
        refreshScaledHealth();
    }

    @Override
    public void refreshScaledHealth() {
        // We need to use the dirty instances to signify that the player needs to ahve it updated, instead
        // of modifying the attribute instances themselves, we bypass other potentially detrimental logi
        // that would otherwise break the actual health scaling.
        final Set<IAttributeInstance> dirtyInstances = ((AttributeMap) this.getAttributeMap()).getDirtyInstances();
        injectScaledHealth(dirtyInstances, true);

        // Send the new information to the client.
        sendHealthUpdate();
        this.connection.sendPacket(new SPacketEntityProperties(this.getEntityId(), dirtyInstances));
        // Reset the dirty instances since they've now been manually updated on the client.
        dirtyInstances.clear();

    }

    public void sendHealthUpdate() {
        this.connection.sendPacket(new SPacketUpdateHealth(getInternalScaledHealth(), getFoodStats().getFoodLevel(), getFoodStats().getSaturationLevel()));
    }

    @Override
    public void injectScaledHealth(Collection<IAttributeInstance> set, boolean force) {
        if (!this.healthScaling && !force) {
            return;
        }
        // We need to remove the existing attribute instance for max health, since it's not always going to be the
        // same as SharedMonsterAttributes.MAX_HEALTH
        @Nullable Collection<AttributeModifier> modifiers = null;
        boolean foundMax = false; // Sometimes the max health isn't modified and no longer dirty
        for (final Iterator<IAttributeInstance> iter = set.iterator(); iter.hasNext(); ) {
            final IAttributeInstance dirtyInstance = iter.next();
            if (dirtyInstance.getAttribute().getName().equals("generic.maxHealth")) {
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
            new RangedAttribute(null, "generic.maxHealth", this.healthScaling ? this.healthScale : getMaxHealth(), 0.0D, Float.MAX_VALUE);
        maxHealth.setDescription("Max Health");
        maxHealth.setShouldWatch(true); // needs to be watched

        final ModifiableAttributeInstance attribute = new ModifiableAttributeInstance(this.getAttributeMap(), maxHealth);

        if (!modifiers.isEmpty()) {
            modifiers.forEach(attribute::applyModifier);
        }
        set.add(attribute);

    }

    @Override
    public double getHealthScale() {
        return this.healthScale;
    }

    float cachedHealth = -1;
    float cachedScaledHealth = -1;

    @Override
    public float getInternalScaledHealth() {
        if (this.healthScaling) {
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
            double modifiedScale = (float) this.healthScale;
            // Apply additive modifiers
            for (AttributeModifier attributemodifier : maxAttribute.getModifiersByOperation(0)) {
                modifiedScale += attributemodifier.getAmount();
            }


            // Apply
            for (AttributeModifier attributemodifier1 : maxAttribute.getModifiersByOperation(1)) {
                modifiedScale += modifiedScale * attributemodifier1.getAmount();
            }

            for (AttributeModifier attributemodifier2 : maxAttribute.getModifiersByOperation(2)) {
                modifiedScale *= 1.0D + attributemodifier2.getAmount();
            }

            final float maxHealth = getMaxHealth();
            this.cachedScaledHealth = (float) ((this.cachedHealth / maxHealth) * modifiedScale);
            return this.cachedScaledHealth;
        }
        return getHealth();
    }

    @Override
    public boolean isHealthScaled() {
        return this.healthScaling;
    }

    @Override
    public void setHealthScaled(boolean scaled) {
        this.healthScaling = scaled;
    }

    @Override
    public void updateDataManagerForScaledHealth() {
        this.dataManager.set(EntityLivingBase.HEALTH, getInternalScaledHealth());
    }

    @Redirect(method = "readEntityFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForceGamemode()Z"))
    private boolean onCheckForcedGameMode(MinecraftServer minecraftServer) {
        return minecraftServer.getForceGamemode() && !hasForcedGamemodeOverridePermission();
    }

    @Override
    public boolean hasForcedGamemodeOverridePermission() {
        final Player player = (Player) this;
        return player.hasPermission(player.getActiveContexts(), DataConstants.FORCE_GAMEMODE_OVERRIDE);
    }

    @Nullable private Text displayName = null;

    @Override
    public void setContainerDisplay(Text displayName) {
        this.displayName = displayName;
    }

    @Redirect(method = "displayGUIChest", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName(IInventory chestInventory) {
        if (this.displayName == null) {
            return chestInventory.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "displayGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IInteractionObject;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName2(IInteractionObject guiOwner) {
        if (this.displayName == null) {
            return guiOwner.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "openGuiHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName3(IInventory inventoryIn) {
        if (this.displayName == null) {
            return inventoryIn.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Redirect(method = "displayVillagerTradeGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/IMerchant;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent onGetDisplayName4(IMerchant villager) {
        if (this.displayName == null) {
            return villager.getDisplayName();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.displayName));
    }

    @Inject(method = "canAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void onCanAttackPlayer(EntityPlayer other, CallbackInfoReturnable<Boolean> cir) {
        final boolean worldPVP = ((WorldProperties) other.world.getWorldInfo()).isPVPEnabled();

        if (!worldPVP) {
            cir.setReturnValue(false);
            return;
        }

        final boolean teamPVP = super.canAttackPlayer(other);
        cir.setReturnValue(teamPVP);
    }
}
