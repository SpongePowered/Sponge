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
import com.google.common.collect.ImmutableSet;
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
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.network.NetHandlerPlayServerBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.BasicEntityContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.entity.SkinPartRegistryModule;
import org.spongepowered.common.service.user.SpongeUserStorageService;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayerMixin implements SubjectBridge, EntityPlayerMPBridge, CommandSenderBridge,
    CommandSourceBridge {

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow public NetHandlerPlayServer connection;
    @Shadow private int lastExperience;
    @Shadow private float lastHealth;
    @Shadow private int lastFoodLevel;
    @Shadow public boolean isChangingQuantityOnly;

    @Shadow public abstract WorldServer getServerWorld();

    // Used to restore original item received in a packet after canceling an event
    private ItemStack impl$packetItem = ItemStack.field_190927_a;
    private final User impl$user = bridge$getUserObject();
    private ImmutableSet<SkinPart> impl$skinParts = ImmutableSet.of();
    private int impl$viewDistance;
    @Nullable private GameType impl$pendingGameType;
    private Scoreboard impl$spongeScoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
    @Nullable private EntityPlayerMP impl$delegate;
    @Nullable private Vector3d impl$velocityOverride = null;
    private double impl$healthScale = Constants.Entity.Player.DEFAULT_HEALTH_SCALE;
    private float impl$cachedModifiedHealth = -1;
    private final PlayerOwnBorderListener impl$borderListener = new PlayerOwnBorderListener((EntityPlayerMP) (Object) this);
    private boolean impl$keepInventory = false;
    @Nullable private Text impl$displayName = null;

    @Override
    public void spongeImpl$writeToSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        if (bridge$isHealthScaled()) {
            compound.func_74780_a(Constants.Sponge.Entity.Player.HEALTH_SCALE, this.impl$healthScale);
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_150297_b(Constants.Sponge.Entity.Player.HEALTH_SCALE, Constants.NBT.TAG_DOUBLE)) {
            this.impl$healthScale = compound.func_74769_h(Constants.Sponge.Entity.Player.HEALTH_SCALE);
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void impl$removeHumanHook(final Entity entityIn, final CallbackInfo ci) {
        if (entityIn instanceof EntityHuman) {
            ((EntityHuman) entityIn).onRemovedFrom((EntityPlayerMP) (Object) this);
        }
    }


    @Override
    public boolean bridge$keepInventory() {
        return this.impl$keepInventory;
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
        if (isMainThread && !this.world.field_72995_K) {
            tracksEntityDeaths = PhaseTracker.getInstance().getCurrentState().tracksEntityDeaths();
        } else {
            tracksEntityDeaths = false;
        }
        try (final PhaseContext<?> context = impl$createDeathContext(cause, tracksEntityDeaths)) {
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge end

            final boolean flag = this.world.func_82736_K().func_82766_b(Constants.GameRule.SHOW_DEATH_MESSAGES);
            this.connection.func_147359_a(new SPacketCombatEvent(this.getCombatTracker(), SPacketCombatEvent.Event.ENTITY_DIED, flag));

            if (flag) {
                final Team team = this.getTeam();

                if (team != null && team.func_178771_j() != Team.EnumVisible.ALWAYS) {
                    if (team.func_178771_j() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                        this.server.func_184103_al()
                            .func_177453_a((EntityPlayerMP) (Object) this, this.getCombatTracker().func_151521_b());
                    } else if (team.func_178771_j() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                        this.server.func_184103_al()
                            .func_177452_b((EntityPlayerMP) (Object) this, this.getCombatTracker().func_151521_b());
                    }
                } else {
                    this.server.func_184103_al().func_148539_a(this.getCombatTracker().func_151521_b());
                }
            }

            this.spawnShoulderEntities();

            // Ignore keepInventory GameRule instead use keepInventory from Event
            if (!event.getKeepInventory() && !this.isSpectator()) {
                this.destroyVanishingCursedItems();
                this.inventory.func_70436_m();
            }

            for (final ScoreObjective scoreobjective : this.getWorldScoreboard().func_96520_a(IScoreCriteria.field_96642_c)) {
                final Score score = this.getWorldScoreboard().func_96529_a(this.shadow$getName(), scoreobjective);
                score.func_96648_a();
            }

            final EntityLivingBase entitylivingbase = this.getAttackingEntity();

            if (entitylivingbase != null) {
                final EntityList.EntityEggInfo entitylist$entityegginfo = EntityList.field_75627_a.get(EntityList.func_191301_a(entitylivingbase));

                if (entitylist$entityegginfo != null) {
                    this.addStat(entitylist$entityegginfo.field_151513_e);
                }

                entitylivingbase.func_191956_a((EntityPlayerMP) (Object) this, this.scoreValue, cause);
            }

            this.addStat(StatList.field_188069_A);
            this.takeStat(StatList.field_188098_h);
            this.extinguish();
            this.setFlag(0, false);
            this.getCombatTracker().func_94549_h();

            this.impl$keepInventory = event.getKeepInventory();
        } // Sponge - brackets
    }

    @Nullable
    private PhaseContext<?> impl$createDeathContext(final DamageSource cause, final boolean tracksEntityDeaths) {
        return !tracksEntityDeaths
               ? EntityPhase.State.DEATH.createPhaseContext()
                   .source(this)
                   .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) cause)
               : null;
    }

    @Inject(method = "copyFrom", at = @At("HEAD"))
    private void impl$copySpongeDataOnRespawn(final EntityPlayerMP oldPlayer, final boolean respawnFromEnd, final CallbackInfo ci) {
        // Copy over sponge data from the old player.
        // Allows plugins to specify data that persists after players respawn.
        if (!(oldPlayer instanceof DataCompoundHolder)) {
            return;
        }
        final DataCompoundHolder oldEntity = (DataCompoundHolder) oldPlayer;
        if (oldEntity.data$hasSpongeCompound()) {
            ((DataCompoundHolder) this).data$getRootCompound().func_74782_a(Constants.Sponge.SPONGE_DATA, oldEntity.data$getSpongeCompound());
            this.spongeImpl$readFromSpongeCompound(((DataCompoundHolder) this).data$getSpongeCompound());
        }
    }

    @Redirect(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private boolean impl$useKeepFromBridge(final GameRules gameRules, final String key, final EntityPlayerMP corpse, final boolean keepEverything) {
        final boolean keep = ((EntityPlayerBridge) corpse).bridge$keepInventory(); // Override Keep Inventory GameRule?
        if (!keep) {
            // Copy corpse inventory to respawned player
            this.inventory.func_70455_b(corpse.field_71071_by);
            // Clear corpse so that mods do not copy from it again
            corpse.field_71071_by.func_174888_l();
        }
        return keep;
    }

    /**
     * @author Aaron1101 August 11th, 2018
     * @reason Wrap the method in a try-with-resources for a EntityPhase.State.PLAYER_WAKE_UP
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public void wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {
        // Sponge start - enter phase
        try (final BasicEntityContext basicEntityContext = EntityPhase.State.PLAYER_WAKE_UP.createPhaseContext()
                .source(this)) {
            basicEntityContext.buildAndSwitch();
            // Sponge end

            if (this.isPlayerSleeping()) {
                this.getServerWorld().func_73039_n()
                        .func_151248_b((Entity) (Object) this, new SPacketAnimation((Entity) (Object) this, 2)); // Sponge - cast to Entity
            }

            super.wakeUpPlayer(immediately, updateWorldFlag, setSpawn);

            if (this.connection != null) {
                this.connection.func_147364_a(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            }
        } // Sponge - add bracket to close 'try' block
    }

    @Override
    public Optional<User> bridge$getBackingUser() {
        // may be null during initialization, mainly used to avoid potential stack overflow with #bridge$getUserObject
        return Optional.of(this.impl$user);
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
    @SuppressWarnings({"ConstantConditions", "UnstableApiUsage"})
    @Inject(method = "handleClientSettings", at = @At("HEAD"))
    private void impl$throwClientSettingsEvent(final CPacketClientSettings packet, final CallbackInfo ci) {
        if (ShouldFire.PLAYER_CHANGE_CLIENT_SETTINGS_EVENT) {
            final CauseStackManager csm = Sponge.getCauseStackManager();
            csm.pushCause(this);
            try {
                final Cause cause = csm.getCurrentCause();
                final ImmutableSet<SkinPart> skinParts = SkinPartRegistryModule.getInstance().getAll().stream()
                    .map(part -> (SpongeSkinPart) part)
                    .filter(part -> part.test(packet.func_149521_d()))
                    .collect(ImmutableSet.toImmutableSet());
                final Locale locale = LocaleCache.getLocale(packet.func_149524_c());
                final ChatVisibility visibility = (ChatVisibility) (Object) packet.func_149523_e();
                final PlayerChangeClientSettingsEvent event = SpongeEventFactory.createPlayerChangeClientSettingsEvent(cause, visibility, skinParts,
                    locale, (Player) this, packet.func_149520_f(), packet.field_149528_b);
                SpongeImpl.postEvent(event);
            } finally {
                csm.popCause();
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "handleClientSettings", at = @At("RETURN"))
    private void impl$updateSkinFromPacket(final CPacketClientSettings packet, final CallbackInfo ci) {
        this.impl$skinParts = SkinPartRegistryModule.getInstance().getAll().stream()
            .map(part -> (SpongeSkinPart) part)
            .filter(part -> part.test(packet.func_149521_d()))
            .collect(ImmutableSet.toImmutableSet()); // Returned set is immutable
        this.impl$viewDistance = packet.field_149528_b;
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
        return NetworkUtil.getHostString(this.connection.field_147371_a.func_74430_c());
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
    public void bridge$setPacketItem(final ItemStack itemstack) {
        this.impl$packetItem = itemstack;
    }

    @Override
    public void bridge$refreshExp() {
        this.lastExperience = -1;
    }

    @Override
    public void bridge$restorePacketItem(final EnumHand hand) {
        if (this.impl$packetItem.func_190926_b()) {
            return;
        }

        this.isChangingQuantityOnly = true;
        this.setHeldItem(hand, this.impl$packetItem);
        final Slot slot = this.openContainer.func_75147_a(this.inventory, this.inventory.field_70461_c);
        this.openContainer.func_75142_b();
        this.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        if (slot != null) {
            this.connection.func_147359_a(new SPacketSetSlot(this.openContainer.field_75152_c, slot.field_75222_d, this.impl$packetItem));
        }
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
            scoreboard = Sponge.getGame().getServer().getServerScoreboard()
                .orElseThrow(() -> new IllegalStateException("Server does not have a valid scoreboard"));
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
        if (player.field_70170_p.func_82736_K().func_82766_b(Constants.GameRule.SHOW_DEATH_MESSAGES)) {
            @Nullable final Team team = player.func_96124_cp();

            if (team != null && team.func_178771_j() != Team.EnumVisible.ALWAYS) {
                if (team.func_178771_j() == Team.EnumVisible.HIDE_FOR_OTHER_TEAMS) {
                    return ((ScorePlayerTeamBridge) team).bridge$getTeamChannel(player);
                } else if (team.func_178771_j() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                    return ((ScorePlayerTeamBridge) team).bridge$getNonTeamChannel();
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

    @Nullable
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
                        (GameMode) (Object) this.interactionManager.func_73081_b(), (GameMode) (Object) gameType, (Player) this);
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
        this.connection.func_147359_a(new SPacketSpawnPosition(VecHelper.toBlockPos(this.bridge$getTargetedLocation())));
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
        packet.field_179828_a = pos;
        packet.field_148883_d = state;
        this.connection.func_147359_a(packet);
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
        final ItemStack currentItem = this.inventory.func_70448_g();
        if (currentItem.func_190926_b()) {
            return null;
        }

        // Add SlotTransaction to PlayerContainer
        final org.spongepowered.api.item.inventory.Slot slot = ((Inventory) this.inventoryContainer)
                .query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class))
                .query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotIndex.of(this.inventory.field_70461_c)));
        final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(currentItem);
        final int count = dropAll && !currentItem.func_190926_b() ? currentItem.func_190916_E() : 1;
        final ItemStack itemToDrop = this.inventory.func_70298_a(this.inventory.field_70461_c, count);
        final SlotTransaction transaction = new SlotTransaction(slot, originalItem, ItemStackUtil.snapshotOf(this.inventory.func_70448_g()));
        ((TrackedInventoryBridge) this.inventoryContainer).bridge$getCapturedSlotTransactions().add(transaction);

        return this.dropItem(itemToDrop, false, true);
    }

    @Override
    public void stopActiveHand() { // stopActiveHand
        // Our using item state is probably desynced from the client (e.g. from the initial air interaction of a bow being cancelled).
        // We need to re-send the player's inventory to overwrite any client-side inventory changes that may have occured as a result
        // of the client (but not the server) calling Item#onPlayerStoppedUsing (which in the case of a bow, removes one arrow from the inventory).
        if (this.activeItemStack.func_190926_b()) {
            ((EntityPlayerMP) (Object) this).func_71120_a(((EntityPlayerMP) (Object) this).field_71069_bz);
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

    @Inject(method = "displayGUIChest",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/player/EntityPlayerMP;openContainer:Lnet/minecraft/inventory/Container;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1,
            shift = At.Shift.AFTER))
    private void onSetContainer(final IInventory chestInventory, final CallbackInfo ci) {
        if (!(chestInventory instanceof IInteractionObject) && this.openContainer instanceof ContainerChest && this.isSpectator()) {
            SpongeImpl.getLogger().warn("Opening fallback ContainerChest for inventory '{}'. Most API inventory methods will not be supported", chestInventory);
            ((ContainerBridge) this.openContainer).bridge$setSpectatorChest(true);
        }
    }

    @Override
    public PlayerOwnBorderListener bridge$getWorldBorderListener() {
        return this.impl$borderListener;
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
        if (containerToSend.func_75139_a(slotInd) instanceof SlotCrafting) {
            this.connection.func_147359_a(new SPacketSetSlot(containerToSend.field_75152_c, slotInd, stack));
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

    @Inject(method = "onUpdateEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getTotalArmorValue()I", ordinal = 1))
    private void updateHealthPriorToArmor(final CallbackInfo ci) {
        bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$setHealthScale(final double scale) {
        checkArgument(scale > 0, "Health scale must be greater than 0!");
        checkArgument(scale < Float.MAX_VALUE, "Health scale cannot exceed Float.MAX_VALUE!");
        this.impl$healthScale = scale;
        this.impl$cachedModifiedHealth = -1;
        this.lastHealth = -1.0F;
        if (scale != Constants.Entity.Player.DEFAULT_HEALTH_SCALE) {
            final NBTTagCompound spongeData = ((DataCompoundHolder) this).data$getSpongeCompound();
            spongeData.func_74780_a(Constants.Sponge.Entity.Player.HEALTH_SCALE, scale);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeCompound()) {
                ((DataCompoundHolder) this).data$getSpongeCompound().func_82580_o(Constants.Sponge.Entity.Player.HEALTH_SCALE);
            }
        }
        bridge$refreshScaledHealth();
    }

    @Override
    public void bridge$refreshScaledHealth() {
        // We need to use the dirty instances to signify that the player needs to ahve it updated, instead
        // of modifying the attribute instances themselves, we bypass other potentially detrimental logi
        // that would otherwise break the actual health scaling.
        final Set<IAttributeInstance> dirtyInstances = ((AttributeMap) this.getAttributeMap()).func_111161_b();
        bridge$injectScaledHealth(dirtyInstances);

        // Send the new information to the client.
        this.connection.func_147359_a(new SPacketUpdateHealth(bridge$getInternalScaledHealth(), getFoodStats().func_75116_a(), getFoodStats().func_75115_e()));
        this.connection.func_147359_a(new SPacketEntityProperties(this.getEntityId(), dirtyInstances));
        // Reset the dirty instances since they've now been manually updated on the client.
        dirtyInstances.clear();

    }

    @Override
    public void bridge$injectScaledHealth(final Collection<IAttributeInstance> set) {
        // We need to remove the existing attribute instance for max health, since it's not always going to be the
        // same as SharedMonsterAttributes.MAX_HEALTH
        @Nullable Collection<AttributeModifier> modifiers = null;
        boolean foundMax = false; // Sometimes the max health isn't modified and no longer dirty
        for (final Iterator<IAttributeInstance> iter = set.iterator(); iter.hasNext(); ) {
            final IAttributeInstance dirtyInstance = iter.next();
            if ("generic.maxHealth".equals(dirtyInstance.func_111123_a().func_111108_a())) {
                foundMax = true;
                modifiers = dirtyInstance.func_111122_c();
                iter.remove();
                break;
            }
        }
        if (!foundMax) {
            // Means we didn't find the max health attribute and need to fetch the modifiers from
            // the cached map because it wasn't marked dirty for some reason
            modifiers = this.getEntityAttribute(SharedMonsterAttributes.field_111267_a).func_111122_c();
        }

        // We now re-create a new ranged attribute for our desired max health
        final double defaultt = bridge$isHealthScaled() ? this.impl$healthScale : this.getEntityAttribute(SharedMonsterAttributes.field_111267_a).func_111125_b();

        final RangedAttribute maxHealth =
            new RangedAttribute(null, "generic.maxHealth", defaultt, 0.0D, Float.MAX_VALUE);
        maxHealth.func_111117_a("Max Health");
        maxHealth.func_111112_a(true); // needs to be watched

        final ModifiableAttributeInstance attribute = new ModifiableAttributeInstance(this.getAttributeMap(), maxHealth);

        if (!modifiers.isEmpty()) {
            modifiers.forEach(attribute::func_111121_a);
        }
        set.add(attribute);
    }

    @Override
    public double bridge$getHealthScale() {
        return this.impl$healthScale;
    }

    @Override
    public float bridge$getInternalScaledHealth() {
        if (!bridge$isHealthScaled()) {
            return getHealth();
        }
        if (this.impl$cachedModifiedHealth == -1) {
            // Because attribute modifiers from mods can add onto health and multiply health, we
            // need to replicate what the mod may be trying to represent, regardless whether the health scale
            // says to show only x hearts.
            final IAttributeInstance maxAttribute = this.getEntityAttribute(SharedMonsterAttributes.field_111267_a);
            double modifiedScale = this.impl$healthScale;
            // Apply additive modifiers
            for (final AttributeModifier attributemodifier : maxAttribute.func_111130_a(0)) {
                modifiedScale += attributemodifier.func_111164_d();
            }

            for (final AttributeModifier attributemodifier1 : maxAttribute.func_111130_a(1)) {
                modifiedScale += modifiedScale * attributemodifier1.func_111164_d();
            }

            for (final AttributeModifier attributemodifier2 : maxAttribute.func_111130_a(2)) {
                modifiedScale *= 1.0D + attributemodifier2.func_111164_d();
            }

            this.impl$cachedModifiedHealth = (float) modifiedScale;
        }
        return (getHealth() / getMaxHealth()) * this.impl$cachedModifiedHealth;
    }

    @Override
    public boolean bridge$isHealthScaled() {
        return this.impl$healthScale != Constants.Entity.Player.DEFAULT_HEALTH_SCALE;
    }

    @Redirect(method = "readEntityFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForceGamemode()Z"))
    private boolean onCheckForcedGameMode(final MinecraftServer minecraftServer) {
        return minecraftServer.func_104056_am() && !bridge$hasForcedGamemodeOverridePermission();
    }

    @Override
    public boolean bridge$hasForcedGamemodeOverridePermission() {
        final Player player = (Player) this;
        return player.hasPermission(player.getActiveContexts(), Constants.Permissions.FORCE_GAMEMODE_OVERRIDE);
    }

    @Override
    public void bridge$setDelegateAfterRespawn(final EntityPlayerMP delegate) {
        this.impl$delegate = delegate;
    }

    @Override
    public void bridge$setContainerDisplay(final Text displayName) {
        this.impl$displayName = displayName;
    }

    @Redirect(method = "displayGUIChest", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$updateDisplayName(final IInventory chestInventory) {
        if (this.impl$displayName == null) {
            return chestInventory.func_145748_c_();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.impl$displayName));
    }

    @Redirect(method = "displayGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IInteractionObject;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$updateDisplayName(final IInteractionObject guiOwner) {
        if (this.impl$displayName == null) {
            return guiOwner.func_145748_c_();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.impl$displayName));
    }

    @Redirect(method = "openGuiHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/IInventory;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$updateDisplayNameForHorseInventory(final IInventory inventoryIn) {
        if (this.impl$displayName == null) {
            return inventoryIn.func_145748_c_();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.impl$displayName));
    }

    @Redirect(method = "displayVillagerTradeGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/IMerchant;getDisplayName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$updateDisplayNameForVillagerTrading(final IMerchant villager) {
        if (this.impl$displayName == null) {
            return villager.func_145748_c_();
        }
        return new TextComponentString(SpongeTexts.toLegacy(this.impl$displayName));
    }

    @SuppressWarnings("BoundedWildcard")
    @Inject(method = "canAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void impl$useWorldBasedAttackRules(final EntityPlayer other, final CallbackInfoReturnable<Boolean> cir) {
        final boolean worldPVP = ((WorldProperties) other.field_70170_p.func_72912_H()).isPVPEnabled();

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
