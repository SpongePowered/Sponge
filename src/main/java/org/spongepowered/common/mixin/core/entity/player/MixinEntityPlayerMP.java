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
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.server.management.PlayerList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.WorldServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.gamerule.DefaultGameRules;
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
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.processor.data.entity.SkinDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.effect.sound.SoundEffectHelper;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.PlayerKickHelper;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.interfaces.IMixinInteractable;
import org.spongepowered.common.interfaces.IMixinPacketResourcePackSend;
import org.spongepowered.common.interfaces.IMixinServerScoreboard;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinTeam;
import org.spongepowered.common.interfaces.advancement.IMixinAdvancement;
import org.spongepowered.common.interfaces.advancement.IMixinPlayerAdvancements;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.text.IMixinTitle;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.network.keepalive.SpongeClientWaiter;
import org.spongepowered.common.service.user.SpongeUserStorageService;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.BookFaker;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.SkinUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends MixinEntityPlayer implements Player, IMixinSubject, IMixinEntityPlayerMP, IMixinCommandSender,
        IMixinCommandSource {

    @Shadow @Final public MinecraftServer mcServer;
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
    private boolean respawning;


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
    private GameProfile originalProfile = this.copyCurrentGameProfile();
    private boolean updateGameProfile = false;
    private ProfileProperty customSkin;

    @Nullable private Vector3d velocityOverride = null;
    private boolean healthScaling = false;
    private double healthScale = 20;

    private Map<UUID, TabListEntry> savedTabEntries = new HashMap<>();

    private ProfileProperty getTextures() {
        return Iterables.getFirst(this.getProfile().getPropertyMap().get(ProfileProperty.TEXTURES), SkinDataProcessor.EMPTY_SKIN);
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        if (this.healthScaling) {
            compound.setDouble(NbtDataUtil.HEALTH_SCALE, this.healthScale);
        }
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
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
            final PhaseData peek = PhaseTracker.getInstance().getCurrentPhaseData();
            final IPhaseState<?> state = peek.state;
            tracksEntityDeaths = state.tracksEntityDeaths();
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
                        this.mcServer.getPlayerList()
                            .sendMessageToAllTeamMembers((EntityPlayerMP) (Object) this, this.getCombatTracker().getDeathMessage());
                    } else if (team.getDeathMessageVisibility() == Team.EnumVisible.HIDE_FOR_OWN_TEAM) {
                        this.mcServer.getPlayerList()
                            .sendMessageToTeamOrAllPlayers((EntityPlayerMP) (Object) this, this.getCombatTracker().getDeathMessage());
                    }
                } else {
                    this.mcServer.getPlayerList().sendMessage(this.getCombatTracker().getDeathMessage());
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
            this.readFromNbt(this.getSpongeData());
        }
        // Copy overworld spawn pos
        ((IMixinEntityPlayer) this).setOverworldSpawnPoint(SpongeImplHooks.getBedLocation(oldPlayer, 0));

        // Get old tab list.
        // We remove the tab list from the old player in MixinPlayerList,
        SpongeTabList tabList = (SpongeTabList) ((Player) oldPlayer).getTabList();
        tabList.setPlayer((EntityPlayerMP) (Object) this);
        this.tabList = tabList;
        this.updateGameProfile = ((IMixinEntityPlayerMP) oldPlayer).shouldUpdateGameProfile();
        this.customSkin = ((IMixinEntityPlayerMP) oldPlayer).getSkin();

        if (this.updateGameProfile) {
            this.updateGameProfileWithSkin();
        }
    }

    @Override
    public SpongeTabList removeTabList() {
        SpongeTabList tabList = (SpongeTabList) this.tabList;
        this.tabList = null;
        return tabList;
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

    @Override
    public GameProfile getProfile() {
        return this.user.getProfile();
    }

    @Override
    public boolean isOnline() {
        return this.user.isOnline();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.of(this);
    }

    @Override
    public void forceRecreateUser() {
        UserStorageService service = SpongeImpl.getGame().getServiceManager().provideUnchecked(UserStorageService.class);
        if (!(service instanceof SpongeUserStorageService)) {
            SpongeImpl.getLogger().error("Not re-creating User object for player {}, as UserStorageServer has been replaced with {}", this.getName(), service);
        } else {
            this.user = ((SpongeUserStorageService) service).forceRecreateUser((GameProfile) this.getGameProfile());
        }
        this.customSkin = this.getTextures();
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

    @Override
    public Locale getLocale() {
        return LocaleCache.getLocale(this.language);
    }

    @Override
    public int getViewDistance() {
        return this.viewDistance;
    }

    @Override
    public ChatVisibility getChatVisibility() {
        return (ChatVisibility) (Object) this.chatVisibility;
    }

    @Override
    public boolean isChatColorsEnabled() {
        return this.chatColours;
    }

    @Override
    public Set<SkinPart> getDisplayedSkinParts() {
        return this.skinParts;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        checkNotNull(type, "type");
        checkNotNull(message, "message");

        ITextComponent component = SpongeTexts.toComponent(message);
        if (type == ChatTypes.ACTION_BAR) {
            component = SpongeTexts.fixActionBarFormatting(component);
        }

        this.connection.sendPacket(new SPacketChat(component, (net.minecraft.util.text.ChatType) (Object) type));
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
        ChatUtil.sendMessage(component, MessageChannel.fixed(this), (CommandSource) this.mcServer, false);
    }

    @Override
    public void sendBookView(BookView bookView) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        BookFaker.fakeBookView(bookView, this);
    }

    @Override
    public void sendTitle(Title title) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        ((IMixinTitle) (Object) title).send((EntityPlayerMP) (Object) this);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        if (this.isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.posX, this.posY, this.posZ).lengthSquared() < (long) radius * (long) radius) {
                for (Packet<?> packet : packets) {
                    this.connection.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public PlayerConnection getConnection() {
        return (PlayerConnection) this.connection;
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
    public Optional<Container> getOpenInventory() {
        return Optional.ofNullable((Container) this.openContainer);
    }

    @Override
    public Optional<Container> openInventory(Inventory inventory) throws IllegalArgumentException {
        return this.openInventory(inventory, null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    @Override
    public Optional<Container> openInventory(Inventory inventory, Text displayName) {
        if (((IMixinContainer) this.openContainer).isInUse()) {
            Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            SpongeImpl.getLogger().warn("This player is currently modifying an open container. This action will be delayed.");
            Sponge.getScheduler().createTaskBuilder().delayTicks(0).execute(() -> {
                try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.getContext().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    this.closeInventory(); // Cause close event first. So cursor item is not lost.
                    this.openInventory(inventory); // Then open the inventory
                }
            }).submit(SpongeImpl.getPlugin());
            return this.getOpenInventory();
        }
        return Optional.ofNullable((Container) SpongeCommonEventFactory.displayContainer((EntityPlayerMP) (Object) this, inventory, displayName));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean closeInventory() throws IllegalArgumentException {
        if (((IMixinContainer) this.openContainer).isInUse()) {
            Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            SpongeImpl.getLogger().warn("This player is currently modifying an open container. This action will be delayed.");
            Sponge.getScheduler().createTaskBuilder().delayTicks(0).execute(() -> {
                try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.getContext().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    closeInventory();
                }
            }).submit(SpongeImpl.getPlugin());
            return false;
        }
        // Create Close_Window to capture item drops
        try (PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext()
                .source(this)
                .packetPlayer(((EntityPlayerMP)(Object) this))
                .openContainer(this.openContainer)
                // intentionally missing the lastCursor to not double throw close event
                ) {
            ctx.buildAndSwitch();
            ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.inventory.getItemStack());
            return !SpongeCommonEventFactory.callInteractInventoryCloseEvent(this.openContainer, (EntityPlayerMP) (Object) this, cursor, cursor, false).isCancelled();
        }
    }

    @Override
    public void setScoreboard(Scoreboard scoreboard) {
        if (scoreboard == null) {
            scoreboard = Sponge.getGame().getServer().getServerScoreboard().get();
        }
        ((IMixinServerScoreboard) this.spongeScoreboard).removePlayer((EntityPlayerMP) (Object) this, true);
        this.spongeScoreboard = scoreboard;
        ((IMixinServerScoreboard) this.spongeScoreboard).addPlayer((EntityPlayerMP) (Object) this, true);
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
    public Text getTeamRepresentation() {
        return Text.of(this.getName());
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
                return this.getMessageChannel();
            }
        }

        return MessageChannel.TO_NONE;
    }

    @Override
    public net.minecraft.scoreboard.Scoreboard getWorldScoreboard() {
        return (net.minecraft.scoreboard.Scoreboard) this.spongeScoreboard;
    }

    @Override
    public Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }

    @Override
    public void kick() {
        kick(Text.of(SpongeImpl.getGame().getRegistry().getTranslationById("disconnect.disconnected").get()));
    }

    @Override
    public void kick(Text message) {
        final ITextComponent component = SpongeTexts.toComponent(message);
        PlayerKickHelper.kickPlayer((EntityPlayerMP) (Object) this, component);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        SoundEvent event;
        try {
            // Check if the event is registered (ie has an integer ID)
            event = SoundEvents.getRegisteredSoundEvent(sound.getId());
        } catch (IllegalStateException e) {
            // Otherwise send it as a custom sound
            this.connection.sendPacket(new SPacketCustomSound(sound.getId(), (net.minecraft.util.SoundCategory) (Object) category,
                    position.getX(), position.getY(), position.getZ(), (float) Math.max(minVolume, volume), (float) pitch));
            return;
        }

        this.connection.sendPacket(new SPacketSoundEffect(event, (net.minecraft.util.SoundCategory) (Object) category, position.getX(),
                position.getY(), position.getZ(), (float) Math.max(minVolume, volume), (float) pitch));
    }

    @Override
    public void stopSounds() {
        stopSounds0(null, null);
    }

    @Override
    public void stopSounds(SoundType sound) {
        stopSounds0(checkNotNull(sound, "sound"), null);
    }

    @Override
    public void stopSounds(SoundCategory category) {
        stopSounds0(null, checkNotNull(category, "category"));
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
        stopSounds0(checkNotNull(sound, "sound"), checkNotNull(category, "category"));
    }

    private void stopSounds0(@Nullable SoundType sound, @Nullable SoundCategory category) {
        this.connection.sendPacket(SoundEffectHelper.createStopSoundPacket(sound, category));
    }

    @Override
    public void playRecord(Vector3i position, RecordType recordType) {
        playRecord0(position, checkNotNull(recordType, "recordType"));
    }

    @Override
    public void stopRecord(Vector3i position) {
        playRecord0(position, null);
    }

    private void playRecord0(Vector3i position, @Nullable RecordType recordType) {
        this.connection.sendPacket(SpongeRecordType.createPacket(position, recordType));
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
        SPacketResourcePackSend packet = new SPacketResourcePackSend();
        ((IMixinPacketResourcePackSend) packet).setResourcePack(pack);
        this.connection.sendPacket(packet);
    }

    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void onPlayerActive(CallbackInfo ci) {
        ((IMixinNetHandlerPlayServer) this.connection).resendLatestResourcePackRequest();
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (ICommandSender) this;
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.sleepingIgnored;
    }

    @Override
    public void setSleepingIgnored(boolean sleepingIgnored) {
        this.sleepingIgnored = sleepingIgnored;
    }

    @Override
    public Vector3d getVelocity() {
        if (this.velocityOverride != null) {
            return this.velocityOverride;
        }
        return super.getVelocity();
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

    @SuppressWarnings("unchecked")
    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory<? extends Carrier>) this.inventory;
    }

    @Inject(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void onSetGameType(GameType gameType, CallbackInfo ci) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            ChangeGameModeEvent.TargetPlayer event =
                    SpongeEventFactory.createChangeGameModeEventTargetPlayer(frame.getCurrentCause(),
                            (GameMode) (Object) this.interactionManager.getGameType(), (GameMode) (Object) gameType, this);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
            this.pendingGameType = (GameType) (Object) event.getGameMode();
        }
    }

    /**
     * This injector must appear <b>after</b> {@link #onSetGameType} since it
     * assigns the {@link #pendingGameType} returned by the event to the actual
     * local variable in the method.
     */
    @ModifyVariable(method = "Lnet/minecraft/entity/player/EntityPlayerMP;setGameType(Lnet/minecraft/world/GameType;)V", at = @At(value = "HEAD", remap = false), argsOnly = true)
    private GameType assignPendingGameType(GameType gameType) {
        return this.pendingGameType;
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z", ordinal = 0))
    public boolean onGetGameRules(GameRules gameRules, String gameRule) {
        return false; // Suppress death messages since this is handled together with the event calling
    }

    @Override
    public TabList getTabList() {
        return this.tabList;
    }


    @Override
    public void setTargetedLocation(@Nullable Vector3d vec) {
        super.setTargetedLocation(vec);
        this.connection.sendPacket(new SPacketSpawnPosition(VecHelper.toBlockPos(this.getTargetedLocation())));
    }

    @Override
    public JoinData getJoinData() {
        return new SpongeJoinData(SpongePlayerDataHandler.getFirstJoined(this.getUniqueID()).get(), Instant.now());
    }

    @Override
    public Value<Instant> firstPlayed() {
        return new SpongeValue<>(Keys.FIRST_DATE_PLAYED, Instant.EPOCH, SpongePlayerDataHandler.getFirstJoined(this.getUniqueID()).get());
    }

    @Override
    public Value<Instant> lastPlayed() {
        return new SpongeValue<>(Keys.LAST_DATE_PLAYED, Instant.EPOCH, Instant.now());
    }

    @Override
    public boolean hasPlayedBefore() {
        final Instant instant = SpongePlayerDataHandler.getFirstJoined(this.getUniqueId()).get();
        final Instant toTheMinute = instant.truncatedTo(ChronoUnit.MINUTES);
        final Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final Duration timeSinceFirstJoined = Duration.of(now.minusMillis(toTheMinute.toEpochMilli()).toEpochMilli(), ChronoUnit.MINUTES);
        return timeSinceFirstJoined.getSeconds() > 0;
    }

    // TODO implement with contextual data
//    @Override
//    public DisplayNameData getDisplayNameData() {
//        return null;
//    }

    @Override
    public GameModeData getGameModeData() {
        return new SpongeGameModeData((GameMode) (Object) this.interactionManager.getGameType());
    }

    @Override
    public Value<GameMode> gameMode() {
        return new SpongeValue<>(Keys.GAME_MODE, DataConstants.Catalog.DEFAULT_GAMEMODE,
                (GameMode) (Object) this.interactionManager.getGameType());
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
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getJoinData());
        manipulators.add(getGameModeData());
    }

    @Override
    public void sendBlockChange(BlockPos pos, IBlockState state) {
        final SPacketBlockChange packet = new SPacketBlockChange();
        packet.blockPosition = pos;
        packet.blockState = state;
        this.connection.sendPacket(packet);
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        checkNotNull(state, "state");
        this.sendBlockChange(new BlockPos(x, y, z), (IBlockState) state);
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        SPacketBlockChange packet = new SPacketBlockChange(this.world, new BlockPos(x, y, z));
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
        // TODO direct access instead of queries
        org.spongepowered.api.item.inventory.Slot slot = (org.spongepowered.api.item.inventory.Slot)((Inventory) this.inventoryContainer)
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

    @Override
    public Inventory getEnderChestInventory() {
        return (Inventory) this.enderChest;
    }

    @Override
    public boolean respawnPlayer() {
        if (this.getHealth() > 0.0F) {
            return false;
        }
        this.connection.player = this.mcServer.getPlayerList().recreatePlayerEntity((EntityPlayerMP) (Object) this, this.dimension, false);
        return true;
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

    @Inject(method = "displayGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;addListener(Lnet/minecraft/inventory/IContainerListener;)V"))
    private void onDisplayGuiAddListener(IInteractionObject guiOwner, CallbackInfo ci) {
        this.trackInteractable(guiOwner);
    }

    @Inject(method = "displayGUIChest", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;addListener(Lnet/minecraft/inventory/IContainerListener;)V"))
    private void onDisplayGuiChestAddListener(IInventory inventory, CallbackInfo ci) {
        this.trackInteractable(inventory);
    }

    @Inject(method = "displayVillagerTradeGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;addListener(Lnet/minecraft/inventory/IContainerListener;)V"))
    private void onDisplayVillagerTradeGuiAddListener(IMerchant villager, CallbackInfo ci) {
        this.trackInteractable(villager);
    }

    @Inject(method = "openGuiHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;addListener(Lnet/minecraft/inventory/IContainerListener;)V"))
    private void onOpenGuiHorseInventoryAddListener(AbstractHorse horse, IInventory inventoryIn, CallbackInfo ci) {
        this.trackInteractable(inventoryIn);
    }

    private void trackInteractable(Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).getInventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((IMixinInteractable) i).addContainer(this.openContainer));
        }
        ((IMixinContainer) this.openContainer).setViewed(inventory);
        // TODO else unknown inventory - try to provide wrapper Interactable
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getSpectatorTarget() {
        // For the API, return empty if we're spectating ourself.
        @Nonnull final Entity entity = this.getSpectatingEntity();
        return entity == (Object) this ? Optional.empty() : Optional.of((org.spongepowered.api.entity.Entity) entity);
    }

    @Override
    public void setSpectatorTarget(@Nullable org.spongepowered.api.entity.Entity entity) {
        this.setSpectatingEntity((Entity) entity);
    }

    @Override
    public MessageChannelEvent.Chat simulateChat(Text message, Cause cause) {
        checkNotNull(message, "message");

        TextComponentTranslation component = new TextComponentTranslation("chat.type.text", SpongeTexts.toComponent(this.getDisplayNameText()),
                SpongeTexts.toComponent(message));
        final Text[] messages = SpongeTexts.splitChatMessage(component);

        final MessageChannel originalChannel = this.getMessageChannel();
        final MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
                cause, originalChannel, Optional.of(originalChannel),
                new MessageEvent.MessageFormatter(messages[0], messages[1]), message, false
        );
        if (!SpongeImpl.postEvent(event) && !event.isMessageCancelled()) {
            event.getChannel().ifPresent(channel -> channel.send(this, event.getMessage(), ChatTypes.CHAT));
        }
        return event;
    }

    @Override
    public Optional<WorldBorder> getWorldBorder() {
        return Optional.ofNullable(this.worldBorder);
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder border, Cause cause) {
        if (this.worldBorder == border) {
            return; //do not fire an event since nothing would have changed
        }
        if (!SpongeImpl.postEvent(SpongeEventFactory.createChangeWorldBorderEventTargetPlayer(cause, Optional.ofNullable(this.worldBorder), Optional.ofNullable(border), this))) {
            if (this.worldBorder != null) { //is the world border about to be unset?
                ((net.minecraft.world.border.WorldBorder) this.worldBorder).listeners.remove(this.borderListener); //remove the listener, if so
            }
            this.worldBorder = border;
            if (this.worldBorder != null) {
                ((net.minecraft.world.border.WorldBorder) this.worldBorder).addListener(this.borderListener);
                this.connection.sendPacket(new SPacketWorldBorder((net.minecraft.world.border.WorldBorder) this.worldBorder, SPacketWorldBorder.Action.INITIALIZE));
            } else { //unset the border if null
                this.connection.sendPacket(new SPacketWorldBorder(this.world.getWorldBorder(), SPacketWorldBorder.Action.INITIALIZE));
            }
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
        for (final Iterator<IAttributeInstance> iter = set.iterator(); iter.hasNext(); ) {
            final IAttributeInstance dirtyInstance = iter.next();
            if (dirtyInstance.getAttribute().getName().equals("generic.maxHealth")) {
                iter.remove();
                break;
            }
        }

        // We now re-create a new ranged attribute for our desired health
        final RangedAttribute maxHealth =
            new RangedAttribute(null, "generic.maxHealth", this.healthScaling ? this.healthScale : getMaxHealth(), 0.0D, Float.MAX_VALUE);
        maxHealth.setDescription("Max Health");
        maxHealth.setShouldWatch(true); // needs to be watched

        set.add(new ModifiableAttributeInstance(this.getAttributeMap(), maxHealth));

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
            this.cachedScaledHealth = (float) ((this.cachedHealth / getMaxHealth()) * this.healthScale);
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

    @Override
    public CooldownTracker getCooldownTracker() {
        return (CooldownTracker) shadow$getCooldownTracker();
    }

    @Redirect(method = "readEntityFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForceGamemode()Z"))
    private boolean onCheckForcedGameMode(MinecraftServer minecraftServer) {
        return minecraftServer.getForceGamemode() && !hasForcedGamemodeOverridePermission();
    }

    @Override
    public boolean hasForcedGamemodeOverridePermission() {
        return this.hasPermission(getActiveContexts(), "minecraft.force-gamemode.override");
    }

    @Override
    public AdvancementProgress getProgress(Advancement advancement) {
        checkNotNull(advancement, "advancement");
        checkState(((IMixinAdvancement) advancement).isRegistered(), "The advancement must be registered");
        return (AdvancementProgress) this.advancements.getProgress((net.minecraft.advancements.Advancement) advancement);
    }

    @Override
    public Collection<AdvancementTree> getUnlockedAdvancementTrees() {
        return ((IMixinPlayerAdvancements) this.advancements).getAdvancementTrees();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This is an internal method not intended for use with Players " +
                "as it causes the player to be placed into an undefined state. " +
                "Consider putting them through the normal death process instead.");
    }

    @Override
    public Optional<UUID> getWorldUniqueId() {
        return Optional.of(this.getWorld().getUniqueId());
    }

    @Override
    public boolean setLocation(Vector3d position, UUID world) {
        WorldProperties prop = Sponge.getServer().getWorldProperties(world).orElseThrow(() -> new IllegalArgumentException("Invalid World: No world found for UUID"));
        World loaded = Sponge.getServer().loadWorld(prop).orElseThrow(() -> new IllegalArgumentException("Invalid World: Could not load world for UUID"));
        return this.setLocation(new Location<>(loaded, position));
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

    @Override
    @Nullable
    public ProfileProperty getSkin() {
        return this.customSkin;
    }

    /*@Override
    public com.mojang.authlib.GameProfile getTabListProfilea() {
        if (this.updateGameProfile) {
            return (com.mojang.authlib.GameProfile) this.getProfile();
        } else {
            return this.oldP

        }
    }*/

    @Override
    public com.mojang.authlib.GameProfile getCustomSkinProfile() {
        if (this.updateGameProfile) {
            return (com.mojang.authlib.GameProfile) this.getProfile();
        } else {
            // Re-create it each time, so that we stay up-to-date
            // with any property changes made by plugins or mods
            GameProfile newProfile = GameProfile.of(this.getUniqueID(), this.getName());
            newProfile.getPropertyMap().putAll(this.getProfile().getPropertyMap());

            Collection<ProfileProperty> textures = newProfile.getPropertyMap().get(ProfileProperty.TEXTURES);
            textures.clear();
            textures.add(this.customSkin);
            return (com.mojang.authlib.GameProfile) newProfile;
        }
    }

    @Override
    public boolean setSkin(ProfileProperty skin) {
        this.customSkin = skin;
        if (this.updateGameProfile) {
            this.updateGameProfileWithSkin();
        }

        this.updateSkin();
        return true;
    }

    private void updateGameProfileWithSkin() {
        Collection<ProfileProperty> props = this.getProfile().getPropertyMap().get(ProfileProperty.TEXTURES);
        props.clear();
        props.add(this.customSkin);
    }

    @Override
    public boolean shouldUpdateGameProfile() {
        return this.updateGameProfile;
    }

    @Override
    public void setUpdateGameProfile(boolean updateGameProfile) {
        if (!this.updateGameProfile && this.updateGameProfile) {
            this.originalProfile = this.copyCurrentGameProfile();
            this.updateGameProfileWithSkin();
            this.updateTabListNoSkinChange();
        } else if (this.updateGameProfile && !updateGameProfile) {
            this.restoreOriginalProfile();
            this.updateTabListNoSkinChange();
        }
        this.updateGameProfile = updateGameProfile;
    }

    @Override
    public boolean setSkin(ProfileProperty skin, boolean updateGameProfile) {
        this.setUpdateGameProfile(updateGameProfile);
        this.setSkin(skin);

        return true;
    }

    private void restoreOriginalProfile() {
        Collection<ProfileProperty> textures = this.getProfile().getPropertyMap().get(ProfileProperty.TEXTURES);
        textures.clear();
        textures.addAll(this.originalProfile.getPropertyMap().get(ProfileProperty.TEXTURES));
    }

    private void updateTabListNoSkinChange() {
        for (Player player: Sponge.getServer().getOnlinePlayers()) {
            player.getTabList().removeEntry(this.getUniqueID()).ifPresent(oldEntry -> {
                TabListEntry newEntry = TabListEntry.builder().from(oldEntry).profile(this.getProfile()).build();
                player.getTabList().addEntry(newEntry);
            });
        }
    }

    @Override
    public DataTransactionResult removeSkin() {
        if (!this.getProfile().getPropertyMap().containsKey(ProfileProperty.TEXTURES)) {
            return DataTransactionResult.successNoData();
        }
        Collection<ProfileProperty> skin = this.getProfile().getPropertyMap().removeAll("textures");
        ImmutableValue<?> oldValue = new ImmutableSpongeValue<>(Keys.SKIN, skin.iterator().next());
        this.updateSkin();

        return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).replace(oldValue).build();
    }

    @Override
    public void refreshSkinOnRespawn() {
        this.updateSkinSelf();
    }

    private void updateSkin() {
        this.updateSkinSelf();
        this.updateSkinOthers();
    }

    private GameProfile copyCurrentGameProfile() {
        GameProfile newProfile = GameProfile.of(this.getUniqueID(), this.getName());
        newProfile.getPropertyMap().putAll(this.getProfile().getPropertyMap());

        return newProfile;
    }

    private void updateSkinSelf() {
        // Remove any existing entry
        Optional<TabListEntry> oldEntry = this.getTabList().removeEntry(this.getUniqueID());

        // Add an entry with the new GameProfile
        this.getTabList().addEntry(TabListEntry.builder()
                .displayName(this.getDisplayNameData().displayName().get())
                .latency(this.getConnection().getLatency())
                .list(this.getTabList())
                .gameMode(this.getGameModeData().type().get())
                .profile((GameProfile) this.getCustomSkinProfile())
                .build());

        Runnable restoreOldEntry = () -> {
            // Don't restore the previous entry if a plugin
            // wants the new skin to show up in the tab list
            if (this.updateGameProfile) {
                return;
            }
            SpongeClientWaiter.INSTANCE.waitForRenderTick(() -> {
                // Remove the entry we added
                this.getTabList().removeEntry(this.getUniqueID());
                // Restore any existing entry
                oldEntry.ifPresent(e -> this.getTabList().addEntry(e));

            }, (EntityPlayerMP) (Object) this);
        };

        if (this.isDead) {
            // If we're dead, we don't want to force a respawn now.
            // Instead, we wait for a respawn to occur before restoring the tab list.
            // This might appear odd to plugins, but unfortunately, it can't be helped.
            Sponge.getEventManager().registerListener(SpongeImpl.getPlugin(), RespawnPlayerEvent.class, Order.PRE, (event) -> {
                if (event.getOriginalPlayer().getUniqueId().equals(this.getUniqueID())) {
                    // Delay restoring the skin by one tick, to ensure
                    // that the respawn packet is sent to the client before we change the skin back
                    Sponge.getScheduler().createTaskBuilder().execute(restoreOldEntry).delayTicks(1).submit(SpongeImpl.getPlugin());
                    Sponge.getEventManager().unregisterListeners(this);
                }
            });
        } else {
            // If we're alive, we perform a fake respawn, and immediately
            // restore the tab list.
            if (!this.isRespawning()) {
                this.fakeRespawn();
            }
            restoreOldEntry.run();
        }
    }

    @Override
    public void onVanish(EntityTrackerEntry entry) {
        super.onVanish(entry);
        this.savedTabEntries.clear();
        for (EntityPlayerMP entityPlayerMP : entry.trackingPlayers) {
            ((Player) entityPlayerMP).getTabList().getEntry(this.getUniqueID()).ifPresent(e -> this.savedTabEntries.put(entityPlayerMP.getUniqueID(), e));
            //this.savedTabEntries.put(entityPlayerMP.getUniqueID(), .orElse(null));
        }
    }

    @Override
    public void onUnVanish() {
        super.onUnVanish();
        this.onSpawnToPlayers(Sponge.getServer().getOnlinePlayers(), ImmutableMap.copyOf(this.savedTabEntries));
        this.savedTabEntries.clear();
    }

    @Override
    public void onSpawnToPlayers(Collection<Player> players) {
        this.onSpawnToPlayers(players, new HashMap<>());
    }

    public void onSpawnToPlayers(Collection<Player> players, Map<UUID, TabListEntry> previousEntries) {
        for (Player player : players) {
            if (player == (EntityPlayerMP) (Object) this) {
                continue;
            }

                TabListEntry.Builder baseBuilder = TabListEntry.builder()
                    .displayName(this.getDisplayNameData().displayName().get())
                    .latency(this.getConnection().getLatency())
                    .list(player.getTabList())
                    .gameMode(this.getGameModeData().type().get());

            // If we haven't modified the player's GameProfile, we remove their old tab list entry (which contains their original skin)
            if (!this.updateGameProfile && !previousEntries.containsKey(player.getUniqueId())) {
                // If we don't have an old entry (e.g. due to vanishing), we create a new one
                // using our normal profile.
                TabListEntry oldEntry = player.getTabList().removeEntry(this.getUniqueID()).orElse(baseBuilder.profile(this.getProfile()).build());
                previousEntries.put(player.getUniqueId(), oldEntry);
            }
            if (!player.getTabList().getEntry(this.getUniqueID()).isPresent()) {
                player.getTabList().addEntry(baseBuilder.profile((GameProfile) this.getCustomSkinProfile())
                        .build());
            }

        }

        if (!this.updateGameProfile) {
            this.runDelay(2, () -> {

                // Restore the tab list
                for (Player player : players) {
                    if (player == this) {
                        continue;
                    }
                    SpongeClientWaiter.INSTANCE.waitForRenderTick(() -> {
                        player.getTabList().removeEntry(this.getUniqueID());
                        TabListEntry entry = previousEntries.get(player.getUniqueId());
                        if (entry != null) {
                            player.getTabList().addEntry(entry);
                        }
                    }, (EntityPlayerMP) player);
                }
            });
        }
    }

    private void updateSkinOthers() {
        // If we're already vanished, do nothing. When a plugin unvanishes us,
        // the skin will be applied
        if (!this.isDead && !this.get(Keys.VANISH).get()) {
            // MixinEntity#spongeOnUpdate takes care of setting the new skin when we unvanish.
            this.offer(Keys.VANISH, true);
            this.runDelay(2, () -> this.offer(Keys.VANISH, false));
        }
    }

    // Adapted from MixinPlayerList#recreatePlayerEntity
    // We want to respawn the player *only* on tts own client. Therefore,
    // we skip most of the logic that's performed for a normal respawn.
    private void fakeRespawn() {
        WorldServer worldServer = this.getServerWorld();
        final int dimensionId = WorldManager.getClientDimensionId((EntityPlayerMP) (Object) this, worldServer);
        PlayerList playerList = SpongeImpl.getServer().getPlayerList();
        Transform<World> transform = this.getTransform();

        this.connection.sendPacket(new SPacketRespawn(dimensionId, worldServer.getDifficulty(), worldServer
                .getWorldInfo().getTerrainType(), this.interactionManager.getGameType()));
        this.connection.sendPacket(new SPacketServerDifficulty(worldServer.getDifficulty(), worldServer.getWorldInfo().isDifficultyLocked()));
        this.connection.setPlayerLocation(transform.getLocation().getX(), transform.getLocation().getY(), transform.getLocation().getZ(),
                (float) transform.getYaw(), (float) transform.getPitch());

        final BlockPos spawnLocation = worldServer.getSpawnPoint();
        this.connection.sendPacket(new SPacketSpawnPosition(spawnLocation));
        this.connection.sendPacket(new SPacketSetExperience(this.experience, this.experienceTotal,
                this.experienceLevel));
        playerList.updateTimeAndWeatherForPlayer((EntityPlayerMP) (Object) this, worldServer);
        playerList.updatePermissionLevel((EntityPlayerMP) (Object) this);
        playerList.syncPlayerInventory((EntityPlayerMP) (Object) this);
        //worldServer.getPlayerChunkMap().addPlayer(this);
        //org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) this;
        //((org.spongepowered.api.world.World) worldServer).spawnEntity(spongeEntity);
        //this.playerEntityList.add(this);
        //this.uuidToPlayerMap.put(this.getUniqueID(), this);
        //this.addSelfToInternalCraftingInventory();

        // Update reducedDebugInfo game rule
        this.connection.sendPacket(new SPacketEntityStatus((EntityPlayerMP) (Object) this,
                worldServer.getGameRules().getBoolean(DefaultGameRules.REDUCED_DEBUG_INFO) ? DataConstants.REDUCED_DEBUG_INFO_ENABLE : DataConstants.REDUCED_DEBUG_INFO_DISABLE));

        for (Object potioneffect : this.getActivePotionEffects()) {
            this.connection.sendPacket(new SPacketEntityEffect(this.getEntityId(), (PotionEffect) potioneffect));
        }
        this.refreshScaledHealth();

    }

    @Override
    public void setRespawning(boolean respawning) {
        this.respawning = respawning;
    }

    @Override
    public boolean isRespawning() {
        return this.respawning;
    }
}
