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

import com.google.common.base.Preconditions;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.title.Title;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import net.minecraft.network.play.server.SStopSoundPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerBossInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.PlayerChatRouter;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.network.ServerPlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.network.play.server.SChangeBlockPacketAccessor;
import org.spongepowered.common.accessor.world.border.WorldBorderAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;
import org.spongepowered.common.bridge.advancements.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.network.play.server.SSendResourcePackPacketBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.BookUtil;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.world.dimension.SpongeDimensionType;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(ServerPlayerEntity.class)
@Implements(@Interface(iface = Player.class, prefix = "player$"))
public abstract class ServerPlayerEntityMixin_API extends PlayerEntityMixin_API implements ServerPlayer {

    @Shadow @Final public MinecraftServer server;
    @Shadow @Final private PlayerAdvancements advancements;
    @Shadow private net.minecraft.entity.player.ChatVisibility chatVisibility;
    @Shadow private String language;
    @Shadow public ServerPlayNetHandler connection;
    @Shadow private boolean chatColours;

    private PlayerChatRouter api$chatRouter;
    private final TabList api$tabList = new SpongeTabList((ServerPlayerEntity) (Object) this);
    @Nullable private WorldBorder api$worldBorder;

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        if (this.impl$isFake) {
            // Don't bother sending messages to fake players
            return;
        }
        Preconditions.checkNotNull(particleEffect, "The particle effect cannot be null!");
        Preconditions.checkNotNull(position, "The position cannot be null");
        Preconditions.checkArgument(radius > 0, "The radius has to be greater then zero!");

        final List<IPacket<?>> packets = SpongeParticleHelper.toPackets(particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.shadow$getPosX(), this.shadow$getPosY(), this.shadow$getPosZ()).lengthSquared() < (long) radius * (long) radius) {
                for (final IPacket<?> packet : packets) {
                    this.connection.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public User getUser() {
        return ((ServerPlayerEntityBridge) this).bridge$getUser();
    }

    @Override
    public GameProfile getProfile() {
        return ((ServerPlayerEntityBridge) this).bridge$getUser().getProfile();
    }

    @Override
    public Locale getLocale() {
        return LocaleCache.getLocale(this.language);
    }

    @Override
    public int getViewDistance() {
        return ((ServerPlayerEntityBridge) this).bridge$getViewDistance();
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
        return ((ServerPlayerEntityBridge) this).bridge$getSkinParts();
    }

    @Override
    public void sendEnvironment(final DimensionType dimensionType) {
        ((ServerPlayerEntityBridge) this).bridge$sendViewerEnvironment((SpongeDimensionType) dimensionType);
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
    public ServerPlayerConnection getConnection() {
        return (ServerPlayerConnection) this.connection;
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
        return ((ServerPlayerEntityBridge) this).bridge$getUser().getIdentifier();
    }

    @Override
    public void setScoreboard(final Scoreboard scoreboard) {
        if (((ServerPlayerEntityBridge) this).bridge$hasDelegate()) {
            ((ServerPlayer) ((ServerPlayerEntityBridge) this).bridge$getDelegate()).setScoreboard(scoreboard);
        }
        ((ServerScoreboardBridge) ((ServerPlayerEntityBridge) this).bridge$getScoreboard()).bridge$removePlayer((ServerPlayerEntity) (Object) this, true);
        ((ServerPlayerEntityBridge) this).bridge$replaceScoreboard(scoreboard);
        ((ServerScoreboardBridge) ((ServerPlayerEntityBridge) this).bridge$getScoreboard()).bridge$addPlayer((ServerPlayerEntity) (Object) this, true);
    }

    @Override
    public Component getTeamRepresentation() {
        return SpongeAdventure.asAdventure(this.shadow$getName());
    }

    @Override
    public Scoreboard getScoreboard() {
        return ((ServerPlayerEntityBridge) this).bridge$getScoreboard();
    }

    @Override
    public void kick() {
        this.kick(TranslatableComponent.of("disconnect.disconnected"));
    }

    @Override
    public void kick(final Component message) {
        final ITextComponent component = SpongeAdventure.asVanilla(message);
        this.connection.disconnect(component);
    }

    @Override
    public void playMusicDisc(final Vector3i position, final MusicDisc recordType) {
        this.playRecord0(position, Preconditions.checkNotNull(recordType, "recordType"));
    }

    @Override
    public void stopMusicDisc(final Vector3i position) {
        this.playRecord0(position, null);
    }

    private void playRecord0(final Vector3i position, @Nullable final MusicDisc recordType) {
        this.connection.sendPacket(SpongeRecordType.createPacket(position, recordType));
    }

    @Override
    public void sendResourcePack(final ResourcePack pack) {
        final SSendResourcePackPacket packet = new SSendResourcePackPacket();
        ((SSendResourcePackPacketBridge) packet).bridge$setSpongePack(pack);
        this.connection.sendPacket(packet);
    }

    @Override
    public TabList getTabList() {
        return this.api$tabList;
    }

    @Override
    public boolean hasPlayedBefore() {
        final Instant instant = ((SpongeServer) this.shadow$getServer()).getPlayerDataManager().getFirstJoined(this.getUniqueId()).get();
        final Instant toTheMinute = instant.truncatedTo(ChronoUnit.MINUTES);
        final Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final Duration timeSinceFirstJoined = Duration.of(now.minusMillis(toTheMinute.toEpochMilli()).toEpochMilli(), ChronoUnit.MINUTES);
        return timeSinceFirstJoined.getSeconds() > 0;
    }

    public void sendBlockChange(final BlockPos pos, final net.minecraft.block.BlockState state) {
        final SChangeBlockPacket packet = new SChangeBlockPacket();
        final SChangeBlockPacketAccessor accessor = (SChangeBlockPacketAccessor) packet;
        accessor.accessor$setPos(pos);
        accessor.accessor$setState(state);
        this.connection.sendPacket(packet);
    }

    @Override
    public void sendBlockChange(final int x, final int y, final int z, final BlockState state) {
        Preconditions.checkNotNull(state, "state");
        this.sendBlockChange(new BlockPos(x, y, z), (net.minecraft.block.BlockState) state);
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        final SChangeBlockPacket packet = new SChangeBlockPacket(this.shadow$getEntityWorld(), new BlockPos(x, y, z));
        this.connection.sendPacket(packet);
    }

    @Override
    public boolean respawnPlayer() {
        if (this.shadow$getHealth() > 0.0F) {
            return false;
        }
        this.connection.player = this.server.getPlayerList().recreatePlayerEntity((ServerPlayerEntity) (Object) this, this.dimension, false);
        return true;
    }

    @Override
    public PlayerChatRouter getChatRouter() {
        if (this.api$chatRouter == null) {
            this.api$chatRouter = (player, message) -> ((Server) this.server).sendMessage(
                    TranslatableComponent.of("chat.type.text", ((EntityBridge) player).bridge$getDisplayNameText(), message));
        }
        return this.api$chatRouter;
    }

    @Override
    public void setChatRouter(final PlayerChatRouter router) {
        this.api$chatRouter = Objects.requireNonNull(router, "router");
    }

    @Override
    public PlayerChatEvent simulateChat(final Component message, final Cause cause) {
        Preconditions.checkNotNull(message, "message");

        final PlayerChatRouter originalRouter = this.getChatRouter();
        final PlayerChatEvent event = SpongeEventFactory.createPlayerChatEvent(
                cause, originalRouter, Optional.of(originalRouter), message, message);
        if (!SpongeCommon.postEvent(event)) {
            event.getChatRouter().ifPresent(channel -> channel.chat(this, event.getMessage()));
        }
        return event;
    }

    @Override
    public Optional<WorldBorder> getWorldBorder() {
        return Optional.ofNullable(this.api$worldBorder);
    }


    @Override
    public CooldownTracker getCooldownTracker() {
        return (CooldownTracker) this.shadow$getCooldownTracker();
    }

    @Override
    public AdvancementProgress getProgress(final Advancement advancement) {
        Preconditions.checkNotNull(advancement, "advancement");
        Preconditions.checkState(((AdvancementBridge) advancement).bridge$isRegistered(), "The advancement must be registered");
        return (AdvancementProgress) this.advancements.getProgress((net.minecraft.advancements.Advancement) advancement);
    }

    @Override
    public Collection<AdvancementTree> getUnlockedAdvancementTrees() {
        return ((PlayerAdvancementsBridge) this.advancements).bridge$getAdvancementTrees();
    }

    @Override
    public void setWorldBorder(@Nullable WorldBorder border) {
        if (this.api$worldBorder == border) {
            return; //do not fire an event since nothing would have changed
        }
        if (!SpongeCommon.postEvent(SpongeEventFactory.createChangeWorldBorderEventTargetPlayer(PhaseTracker.getCauseStackManager().getCurrentCause(),
                Optional.ofNullable(this.api$worldBorder), this, Optional.ofNullable(border)))) {
            if (this.api$worldBorder != null) { //is the world border about to be unset?
                ((WorldBorderAccessor) this.api$worldBorder).accessor$getListeners().remove(
                        ((ServerPlayerEntityBridge) this).bridge$getWorldBorderListener()); //remove the listener, if so
            }
            this.api$worldBorder = border;
            if (this.api$worldBorder != null) {
                ((net.minecraft.world.border.WorldBorder) this.api$worldBorder).addListener(
                        ((ServerPlayerEntityBridge) this).bridge$getWorldBorderListener());
                this.connection.sendPacket(
                        new SWorldBorderPacket((net.minecraft.world.border.WorldBorder) this.api$worldBorder,
                                SWorldBorderPacket.Action.INITIALIZE));
            } else { //unset the border if null
                this.connection.sendPacket(
                        new SWorldBorderPacket(this.shadow$getEntityWorld().getWorldBorder(), SWorldBorderPacket.Action.INITIALIZE));
            }
        }
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Humanoid
        values.add(this.foodLevel().asImmutable());
        values.add(this.exhaustion().asImmutable());
        values.add(this.saturation().asImmutable());
        values.add(this.gameMode().asImmutable());

        // Player
        values.add(this.firstJoined().asImmutable());
        values.add(this.lastPlayed().asImmutable());
        values.add(this.sleepingIgnored().asImmutable());
        values.add(this.hasViewedCredits().asImmutable());

        // If getSpectatingEntity returns this player, then we are not spectating any other entity, so spectatorTarget would be an Optional.empty()
        this.spectatorTarget().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

    // Audience

    @Override
    public void sendMessage(final Component message, final MessageType type) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(type, "type");
        this.connection.sendPacket(new SChatPacket(SpongeAdventure.asVanilla(message), SpongeAdventure.asVanilla(type)));
    }

    @Override
    public void sendActionBar(final Component message) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(message, "message");
        this.connection.sendPacket(new STitlePacket(STitlePacket.Type.ACTIONBAR, SpongeAdventure.asVanilla(message)));
    }

    @Override
    public void showTitle(final Title title) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(title, "title");
        final Title.Times times = title.times();
        if (times != null) {
            this.connection.sendPacket(new STitlePacket(ticks(times.fadeIn()), ticks(times.stay()), ticks(times.fadeOut())));
        }
        this.connection.sendPacket(new STitlePacket(STitlePacket.Type.SUBTITLE, SpongeAdventure.asVanilla(title.subtitle())));
        this.connection.sendPacket(new STitlePacket(STitlePacket.Type.TITLE, SpongeAdventure.asVanilla(title.title())));
    }

    private static int ticks(final Duration duration) {
        if (duration == null) {
            return -1;
        }
        return (int) (duration.toMillis() / 50L);
    }

    @Override
    public void clearTitle() {
        if (this.impl$isFake) {
            return;
        }
        this.connection.sendPacket(new STitlePacket(STitlePacket.Type.CLEAR, null));
    }

    @Override
    public void resetTitle() {
        if (this.impl$isFake) {
            return;
        }
        this.connection.sendPacket(new STitlePacket(STitlePacket.Type.RESET, null));
    }

    @Override
    public void showBossBar(final BossBar bar) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(bar, "bar");
        final ServerBossInfo vanilla = SpongeAdventure.asVanillaServer(bar);
        vanilla.addPlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void hideBossBar(final BossBar bar) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(bar, "bar");
        final ServerBossInfo vanilla = SpongeAdventure.asVanillaServer(bar);
        vanilla.removePlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void playSound(final Sound sound) {
        this.playSound(sound, this.shadow$getPosX(), this.shadow$getPosY(), this.shadow$getPosZ());
    }

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(sound, "sound");
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getValue(SpongeAdventure.asVanilla(sound.name()));
        if (event.isPresent()) {
            // Check if the event is registered
            this.connection.sendPacket(new SPlaySoundEffectPacket(event.get(), SpongeAdventure.asVanilla(sound.source()),
                    x, y, z, sound.volume(), sound.pitch()));
        } else {
            // Otherwise send it as a custom sound
            this.connection.sendPacket(new SPlaySoundPacket(SpongeAdventure.asVanilla(sound.name()), SpongeAdventure.asVanilla(sound.source()),
                    new Vec3d(x, y, z), sound.volume(), sound.pitch()));
        }
    }

    @Override
    public void stopSound(final SoundStop stop) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(stop, "stop");
        this.connection.sendPacket(new SStopSoundPacket(SpongeAdventure.asVanillaNullable(stop.sound()), SpongeAdventure.asVanillaNullable(stop.source())));
    }

    @Override
    public void openBook(@NonNull final Book book) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(book, "book");
        BookUtil.fakeBookView(book, Collections.singletonList(this));
    }
}
