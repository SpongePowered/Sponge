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
package org.spongepowered.common.mixin.api.mcp.entity.player;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.AffectsSpawningData;
import org.spongepowered.api.data.manipulator.mutable.entity.GameModeData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthScalingData;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.inventory.ContainerBridge;
import org.spongepowered.common.bridge.network.NetHandlerPlayServerBridge;
import org.spongepowered.common.bridge.packet.SPacketResourcePackSendBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.bridge.text.TitleBridge;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGameModeData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJoinData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.effect.sound.SoundEffectHelper;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.network.play.server.SPacketBlockChangeAccessor;
import org.spongepowered.common.mixin.core.util.SoundEventsAccessor;
import org.spongepowered.common.mixin.core.world.border.WorldBorderAccessor;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.BookFaker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Mixin(EntityPlayerMP.class)
@Implements(@Interface(iface = Player.class, prefix = "api$"))
public abstract class EntityPlayerMPMixin_API extends EntityPlayerMixin_API implements Player {

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow @Final private PlayerAdvancements advancements;
    @Shadow private String language;
    @Shadow public NetHandlerPlayServer connection;
    @Shadow private EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    @Shadow private boolean chatColours;

    @Shadow public abstract Entity getSpectatingEntity();
    @Shadow public abstract void setSpectatingEntity(Entity entity);

    private boolean api$sleepingIgnored;
    private TabList api$tabList = new SpongeTabList((EntityPlayerMP) (Object) this);
    @Nullable private WorldBorder api$worldBorder;

    @Override
    public GameProfile getProfile() {
        return ((EntityPlayerMPBridge) this).bridge$getUser().getProfile();
    }

    @Override
    public boolean isOnline() {
        return ((EntityPlayerMPBridge) this).bridge$getUser().isOnline();
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.of(this);
    }

    @Override
    public Locale getLocale() {
        return LocaleCache.getLocale(this.language);
    }

    @Override
    public int getViewDistance() {
        return ((EntityPlayerMPBridge) this).bridge$getViewDistance();
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
        return ((EntityPlayerMPBridge) this).bridge$getSkinParts();
    }

    @Override
    public void sendMessage(final ChatType type, final Text message) {
        if (this.impl$isFake) {
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

    @Override
    public void sendBookView(final BookView bookView) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        BookFaker.fakeBookView(bookView, Collections.singletonList(this));
    }

    @Override
    public void sendTitle(final Title title) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        ((TitleBridge) (Object) title).bridge$send((EntityPlayerMP) (Object) this);
    }

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        final List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.posX, this.posY, this.posZ).lengthSquared() < (long) radius * (long) radius) {
                for (final Packet<?> packet : packets) {
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
    public String getIdentifier() {
        return ((EntityPlayerMPBridge) this).bridge$getUser().getIdentifier();
    }

    @Override
    public Optional<Container> getOpenInventory() {
        return Optional.ofNullable((Container) this.openContainer);
    }

    @Override
    public Optional<Container> openInventory(final Inventory inventory) throws IllegalArgumentException {
        return this.openInventory(inventory, null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    @Override
    public Optional<Container> openInventory(final Inventory inventory, final Text displayName) {
        if (((ContainerBridge) this.openContainer).bridge$isInUse()) {
            final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
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
        if (((ContainerBridge) this.openContainer).bridge$isInUse()) {
            final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
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
        try (final PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext()
                .source(this)
                .packetPlayer(((EntityPlayerMP)(Object) this))
                .openContainer(this.openContainer)
             // intentionally missing the lastCursor to not double throw close event
                ) {
            ctx.buildAndSwitch();
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.inventory.getItemStack());
            return !SpongeCommonEventFactory.callInteractInventoryCloseEvent(this.openContainer, (EntityPlayerMP) (Object) this, cursor, cursor, false).isCancelled();
        }
    }

    @Override
    public void setScoreboard(final Scoreboard scoreboard) {
        if (((EntityPlayerMPBridge) this).bridge$hasDelegate()) {
            ((Player) ((EntityPlayerMPBridge) this).bridge$getDelegate()).setScoreboard(scoreboard);
        }
        ((ServerScoreboardBridge) ((EntityPlayerMPBridge) this).bridge$getScoreboard()).bridge$removePlayer((EntityPlayerMP) (Object) this, true);
        ((EntityPlayerMPBridge) this).bridge$replaceScoreboard(scoreboard);
        ((ServerScoreboardBridge) ((EntityPlayerMPBridge) this).bridge$getScoreboard()).bridge$addPlayer((EntityPlayerMP) (Object) this, true);
    }

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.shadow$getName());
    }

    @Override
    public Scoreboard getScoreboard() {
        return ((EntityPlayerMPBridge) this).bridge$getScoreboard();
    }

    @Override
    public void kick() {
        this.kick(Text.of(SpongeImpl.getGame().getRegistry().getTranslationById("disconnect.disconnected").get()));
    }

    @Override
    public void kick(final Text message) {
        ((EntityPlayerMPBridge) this).bridge$kick(message);
    }

    @Override
    public void playSound(final SoundType sound, final SoundCategory category, final Vector3d position, final double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(final SoundType sound, final SoundCategory category, final Vector3d position, final double volume, final double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @Override
    public void playSound(
        final SoundType sound, final SoundCategory category, final Vector3d position, final double volume, final double pitch, final double minVolume) {
        final SoundEvent event;
        try {
            // Check if the event is registered (ie has an integer ID)
            event = SoundEventsAccessor.accessor$getRegisteredSoundEvent(sound.getId());
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
    public void stopSounds(final SoundType sound) {
        stopSounds0(checkNotNull(sound, "sound"), null);
    }

    @Override
    public void stopSounds(final SoundCategory category) {
        stopSounds0(null, checkNotNull(category, "category"));
    }

    @Override
    public void stopSounds(final SoundType sound, final SoundCategory category) {
        stopSounds0(checkNotNull(sound, "sound"), checkNotNull(category, "category"));
    }

    private void stopSounds0(@Nullable final SoundType sound, @Nullable final SoundCategory category) {
        this.connection.sendPacket(SoundEffectHelper.createStopSoundPacket(sound, category));
    }

    @Override
    public void playRecord(final Vector3i position, final RecordType recordType) {
        playRecord0(position, checkNotNull(recordType, "recordType"));
    }

    @Override
    public void stopRecord(final Vector3i position) {
        playRecord0(position, null);
    }

    private void playRecord0(final Vector3i position, @Nullable final RecordType recordType) {
        this.connection.sendPacket(SpongeRecordType.createPacket(position, recordType));
    }

    @Override
    public void sendResourcePack(final ResourcePack pack) {
        final SPacketResourcePackSend packet = new SPacketResourcePackSend();
        ((SPacketResourcePackSendBridge) packet).bridge$setSpongePack(pack);
        this.connection.sendPacket(packet);
    }

    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void onPlayerActive(final CallbackInfo ci) {
        ((NetHandlerPlayServerBridge) this.connection).bridge$resendLatestResourcePackRequest();
    }

    @Override
    public boolean isSleepingIgnored() {
        return this.api$sleepingIgnored;
    }

    @Override
    public void setSleepingIgnored(final boolean sleepingIgnored) {
        this.api$sleepingIgnored = sleepingIgnored;
    }

    @Override
    public Vector3d getVelocity() {
        if (((EntityPlayerMPBridge) this).bridge$getVelocityOverride() != null) {
            return ((EntityPlayerMPBridge) this).bridge$getVelocityOverride();
        }
        return super.getVelocity();
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return (CarriedInventory<? extends Carrier>) this.inventory;
    }

    @Override
    public TabList getTabList() {
        return this.api$tabList;
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

    @Override
    public GameModeData getGameModeData() {
        return new SpongeGameModeData((GameMode) (Object) this.interactionManager.getGameType());
    }

    @Override
    public Value<GameMode> gameMode() {
        return new SpongeValue<>(Keys.GAME_MODE, Constants.Catalog.DEFAULT_GAMEMODE,
                (GameMode) (Object) this.interactionManager.getGameType());
    }

    @Override
    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getGameModeData());
        manipulators.add(this.getJoinData());
        manipulators.add(this.getStatisticData());
        this.get(AffectsSpawningData.class).ifPresent(manipulators::add);
        this.get(HealthScalingData.class).ifPresent(manipulators::add);
    }

    public void sendBlockChange(final BlockPos pos, final IBlockState state) {
        final SPacketBlockChange packet = new SPacketBlockChange();
        ((SPacketBlockChangeAccessor) packet).accessor$setBlockPosition(pos);
        ((SPacketBlockChangeAccessor) packet).accessor$setBlockState(state);
        this.connection.sendPacket(packet);
    }

    @Override
    public void sendBlockChange(final int x, final int y, final int z, final BlockState state) {
        checkNotNull(state, "state");
        this.sendBlockChange(new BlockPos(x, y, z), (IBlockState) state);
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        final SPacketBlockChange packet = new SPacketBlockChange(this.world, new BlockPos(x, y, z));
        this.connection.sendPacket(packet);
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
        this.connection.player = this.server.getPlayerList().recreatePlayerEntity((EntityPlayerMP) (Object) this, this.dimension, false);
        return true;
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getSpectatorTarget() {
        // For the API, return empty if we're spectating ourself.
        @Nonnull final Entity entity = this.getSpectatingEntity();
        return entity == (Object) this ? Optional.empty() : Optional.of((org.spongepowered.api.entity.Entity) entity);
    }

    @Override
    public void setSpectatorTarget(@Nullable final org.spongepowered.api.entity.Entity entity) {
        this.setSpectatingEntity((Entity) entity);
    }

    @Override
    public MessageChannelEvent.Chat simulateChat(final Text message, final Cause cause) {
        checkNotNull(message, "message");

        final TextComponentTranslation component = new TextComponentTranslation("chat.type.text", SpongeTexts.toComponent(((EntityBridge) this).bridge$getDisplayNameText()),
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
        return Optional.ofNullable(this.api$worldBorder);
    }

    @Override
    public void setWorldBorder(@Nullable final WorldBorder border, final Cause cause) {
        if (this.api$worldBorder == border) {
            return; //do not fire an event since nothing would have changed
        }
        if (!SpongeImpl.postEvent(SpongeEventFactory.createChangeWorldBorderEventTargetPlayer(cause, Optional.ofNullable(this.api$worldBorder), Optional.ofNullable(border), this))) {
            if (this.api$worldBorder != null) { //is the world border about to be unset?
                ((WorldBorderAccessor) this.api$worldBorder).accessor$getListeners().remove(((EntityPlayerMPBridge) this).bridge$getWorldBorderListener()); //remove the listener, if so
            }
            this.api$worldBorder = border;
            if (this.api$worldBorder != null) {
                ((net.minecraft.world.border.WorldBorder) this.api$worldBorder).addListener(((EntityPlayerMPBridge) this).bridge$getWorldBorderListener());
                this.connection.sendPacket(new SPacketWorldBorder((net.minecraft.world.border.WorldBorder) this.api$worldBorder, SPacketWorldBorder.Action.INITIALIZE));
            } else { //unset the border if null
                this.connection.sendPacket(new SPacketWorldBorder(this.world.getWorldBorder(), SPacketWorldBorder.Action.INITIALIZE));
            }
        }
    }

    @Override
    public CooldownTracker getCooldownTracker() {
        return (CooldownTracker) shadow$getCooldownTracker();
    }

    @Override
    public AdvancementProgress getProgress(final Advancement advancement) {
        checkNotNull(advancement, "advancement");
        checkState(((AdvancementBridge) advancement).bridge$isRegistered(), "The advancement must be registered");
        return (AdvancementProgress) this.advancements.getProgress((net.minecraft.advancements.Advancement) advancement);
    }

    @Override
    public Collection<AdvancementTree> getUnlockedAdvancementTrees() {
        return ((PlayerAdvancementsBridge) this.advancements).bridge$getAdvancementTrees();
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
    public boolean setLocation(final Vector3d position, final UUID world) {
        final WorldProperties prop = Sponge.getServer().getWorldProperties(world).orElseThrow(() -> new IllegalArgumentException("Invalid World: No world found for UUID"));
        final World loaded = Sponge.getServer().loadWorld(prop).orElseThrow(() -> new IllegalArgumentException("Invalid World: Could not load world for UUID"));
        return this.setLocation(new Location<>(loaded, position));
    }
}
