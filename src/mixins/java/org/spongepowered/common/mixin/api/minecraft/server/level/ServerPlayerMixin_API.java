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
package org.spongepowered.common.mixin.api.minecraft.server.level;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.PlayerChatFormatter;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.world.ChangeWorldBorderEvent;
import org.spongepowered.api.network.ServerPlayerConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.world.level.border.WorldBorderAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.api.minecraft.world.entity.player.PlayerMixin_API;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.util.BookUtil;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
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

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin_API extends PlayerMixin_API implements ServerPlayer {

    // @formatter:off
    @Shadow @Final public MinecraftServer server;
    @Shadow @Final private PlayerAdvancements advancements;
    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow public abstract net.minecraft.server.level.ServerLevel shadow$getLevel();
    // @formatter:on

    private volatile Pointers api$pointers;

    private final TabList api$tabList = new SpongeTabList((net.minecraft.server.level.ServerPlayer) (Object) this);
    @Nullable private PlayerChatFormatter api$chatRouter;
    @Nullable private WorldBorderBridge api$worldBorder;

    @Override
    public ServerWorld world() {
        return (ServerWorld) this.shadow$getLevel();
    }

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        if (this.impl$isFake) {
            return;
        }
        Objects.requireNonNull(particleEffect, "particleEffect");
        Objects.requireNonNull(position, "position");
        if (radius <= 0) {
            throw new IllegalArgumentException("The radius has to be greater then zero!");
        }
        final List<Packet<?>> packets = SpongeParticleHelper.toPackets(particleEffect, position);

        if (!packets.isEmpty()) {
            if (position.sub(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ()).lengthSquared() < (long) radius * (long) radius) {
                for (final Packet<?> packet : packets) {
                    this.connection.send(packet);
                }
            }
        }
    }

    @Override
    public User user() {
        return ((ServerPlayerBridge) this).bridge$getUser();
    }

    @Override
    public boolean isOnline() {
        if (this.impl$isFake) {
            return true;
        }
        return this.server.getPlayerList().getPlayer(this.uuid) == (net.minecraft.server.level.ServerPlayer) (Object) this;
    }

    @Override
    public GameProfile profile() {
        return ((ServerPlayerBridge) this).bridge$getUser().profile();
    }

    @Override
    public void sendWorldType(final WorldType worldType) {
        if (this.impl$isFake) {
            return;
        }
        ((ServerPlayerBridge) this).bridge$sendViewerEnvironment((net.minecraft.world.level.dimension.DimensionType) Objects.requireNonNull(worldType,
                "worldType"));
    }

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position) {
        if (this.impl$isFake) {
            return;
        }
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public ServerPlayerConnection connection() {
        return (ServerPlayerConnection) this.connection;
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     *     inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Overwrite
    public String getIpAddress() {
        return NetworkUtil.getHostString(this.connection.connection.getRemoteAddress());
    }

    @Override
    public String identifier() {
        return ((ServerPlayerBridge) this).bridge$getUser().identifier();
    }

    @Override
    public void setScoreboard(final Scoreboard scoreboard) {
        Objects.requireNonNull(scoreboard, "scoreboard");

        ((ServerScoreboardBridge) ((ServerPlayerBridge) this).bridge$getScoreboard()).bridge$removePlayer((net.minecraft.server.level.ServerPlayer) (Object) this, true);
        ((ServerPlayerBridge) this).bridge$replaceScoreboard(scoreboard);
        ((ServerScoreboardBridge) ((ServerPlayerBridge) this).bridge$getScoreboard()).bridge$addPlayer((net.minecraft.server.level.ServerPlayer) (Object) this, true);
    }

    @Override
    public Component teamRepresentation() {
        return SpongeAdventure.asAdventure(this.shadow$getName());
    }

    @Override
    public Scoreboard scoreboard() {
        return ((ServerPlayerBridge) this).bridge$getScoreboard();
    }

    @Override
    public boolean kick() {
        return this.kick(Component.translatable("disconnect.disconnected"));
    }

    @Override
    public boolean kick(final Component message) {
        return ((ServerPlayerBridge) this).bridge$kick(Objects.requireNonNull(message, "message"));
    }

    @Override
    public void playMusicDisc(final Vector3i position, final MusicDisc recordType) {
        this.connection.send(SpongeMusicDisc.createPacket(Objects.requireNonNull(position, "position"), Objects.requireNonNull(recordType, "recordType")));
    }

    @Override
    public void stopMusicDisc(final Vector3i position) {
        this.connection.send(SpongeMusicDisc.createPacket(position, null));
    }

    @Override
    public void sendResourcePack(final ResourcePack pack) {
        this.connection.send(new ClientboundResourcePackPacket(((SpongeResourcePack) Objects.requireNonNull(pack, "pack")).getUrlString(), pack.hash().orElse("")));
    }

    @Override
    public TabList tabList() {
        return this.api$tabList;
    }

    @Override
    public boolean hasPlayedBefore() {
        final Instant instant = ((SpongeServer) this.shadow$getServer()).getPlayerDataManager().getFirstJoined(this.uniqueId()).get();
        final Instant toTheMinute = instant.truncatedTo(ChronoUnit.MINUTES);
        final Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        final Duration timeSinceFirstJoined = Duration.of(now.minusMillis(toTheMinute.toEpochMilli()).toEpochMilli(), ChronoUnit.MINUTES);
        return timeSinceFirstJoined.getSeconds() > 0;
    }

    @Override
    public void sendBlockChange(final int x, final int y, final int z, final BlockState state) {
        this.connection.send(new ClientboundBlockUpdatePacket(new BlockPos(x, y, z), (net.minecraft.world.level.block.state.BlockState) state));
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        this.connection.send(new ClientboundBlockUpdatePacket(this.shadow$getCommandSenderWorld(), new BlockPos(x, y, z)));
    }

    @Override
    public boolean respawn() {
        if (this.impl$isFake) {
            return false;
        }
        if (this.shadow$getHealth() > 0.0F) {
            return false;
        }
        this.connection.player = this.server.getPlayerList().respawn((net.minecraft.server.level.ServerPlayer) (Object) this, false);
        return true;
    }

    @Override
    public PlayerChatFormatter chatFormatter() {
        if (this.api$chatRouter == null) {
            this.api$chatRouter = (player, audience, message, originalMessage) ->
                    Optional.of(Component.translatable("chat.type.text", SpongeAdventure.asAdventure(this.shadow$getDisplayName()), message));
        }
        return this.api$chatRouter;
    }

    @Override
    public void setChatFormatter(final PlayerChatFormatter router) {
        this.api$chatRouter = Objects.requireNonNull(router, "router");
    }

    @Override
    public PlayerChatEvent simulateChat(final Component message, final Cause cause) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(cause, "cause");

        final PlayerChatFormatter originalRouter = this.chatFormatter();
        final Audience audience = (Audience) this.server;
        final PlayerChatEvent event = SpongeEventFactory.createPlayerChatEvent(cause, audience, Optional.of(audience), originalRouter, Optional.of(originalRouter), message, message);
        if (!SpongeCommon.post(event)) {
            event.chatFormatter().ifPresent(formatter ->
                event.audience().map(SpongeAdventure::unpackAudiences).ifPresent(targets -> {
                    for (Audience target : targets) {
                        formatter.format(this, target, event.message(), event.originalMessage()).ifPresent(formattedMessage ->
                            target.sendMessage(this, formattedMessage));
                    }
                })
            );
        }
        return event;
    }

    @Override
    @NonNull
    public Optional<WorldBorder> worldBorder() {
        if (this.api$worldBorder == null) {
            return Optional.empty();
        }
        return Optional.of(this.api$worldBorder.bridge$asImmutable());
    }

    @Override
    public CooldownTracker cooldownTracker() {
        return (CooldownTracker) this.shadow$getCooldowns();
    }

    @Override
    public AdvancementProgress progress(final Advancement advancement) {
        return (AdvancementProgress) this.advancements.getOrStartProgress((net.minecraft.advancements.Advancement) Objects.requireNonNull(advancement, "advancement"));
    }

    @Override
    public Collection<AdvancementTree> unlockedAdvancementTrees() {
        if (this.impl$isFake) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(((PlayerAdvancementsBridge) this.advancements).bridge$getAdvancementTrees());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @NonNull
    public Optional<WorldBorder> setWorldBorder(final @Nullable WorldBorder border) {
        if (this.impl$isFake) {
            return Optional.empty();
        }
        final Optional<WorldBorder> currentBorder = this.worldBorder();
        if (Objects.equals(currentBorder.orElse(null), border)) {
            return currentBorder; // do not fire an event since nothing would have changed
        }
        final ChangeWorldBorderEvent.Player event =
                SpongeEventFactory.createChangeWorldBorderEventPlayer(PhaseTracker.getCauseStackManager().currentCause(),
                        Optional.ofNullable(border), Optional.ofNullable(border), this, Optional.ofNullable(border));
        if (SpongeCommon.post(event)) {
            return currentBorder;
        }

        if (this.api$worldBorder != null) { // is the world border about to be unset?
            ((WorldBorderAccessor) this.api$worldBorder).accessor$listeners().remove(
                    ((ServerPlayerBridge) this).bridge$getWorldBorderListener()); // remove the listener, if so
        }
        final Optional<WorldBorder> toSet = event.newBorder();
        if (toSet.isPresent()) {
            final net.minecraft.world.level.border.WorldBorder mutableWorldBorder =
                    new net.minecraft.world.level.border.WorldBorder();
            this.api$worldBorder = ((WorldBorderBridge) mutableWorldBorder);
            this.api$worldBorder.bridge$applyFrom(toSet.get());
            mutableWorldBorder.addListener(((ServerPlayerBridge) this).bridge$getWorldBorderListener());
            this.connection.send(
                    new ClientboundSetBorderPacket((net.minecraft.world.level.border.WorldBorder) this.api$worldBorder,
                            ClientboundSetBorderPacket.Type.INITIALIZE));
        } else { // unset the border if null
            this.api$worldBorder = null;
            this.connection.send(
                    new ClientboundSetBorderPacket(this.shadow$getCommandSenderWorld().getWorldBorder(), ClientboundSetBorderPacket.Type.INITIALIZE));
        }
        return toSet;

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
    public @NotNull Pointers pointers() {
        Pointers pointers = this.api$pointers;
        if (pointers == null) {
            synchronized (this) {
                if (this.api$pointers == null) {
                    this.api$pointers = pointers = Pointers.builder()
                        .withDynamic(Identity.NAME, () -> ((net.minecraft.server.level.ServerPlayer) (Object) this).getGameProfile().getName())
                        .withDynamic(Identity.DISPLAY_NAME, () -> this.displayName().get())
                        .withDynamic(Identity.UUID, ((Entity) (Object) this)::getUUID)
                        .withStatic(PermissionChecker.POINTER, permission -> SpongeAdventure.asAdventure(this.permissionValue(permission)))
                        .build();
                } else {
                    return this.api$pointers;
                }
            }
        }
        return pointers;
    }

    @Override
    public void sendMessage(final Identity identity, final Component message, final MessageType type) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundChatPacket(SpongeAdventure.asVanilla(Objects.requireNonNull(message, "message")),
                SpongeAdventure.asVanilla(Objects.requireNonNull(type, "type")), Objects.requireNonNull(identity, "identity").uuid()));
    }

    @Override
    public void sendActionBar(final Component message) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.ACTIONBAR, SpongeAdventure.asVanilla(Objects.requireNonNull(message, "message"))));
    }

    @Override
    public void sendPlayerListHeader(final Component header) {
        this.api$tabList.setHeader(Objects.requireNonNull(header, "header"));
    }

    @Override
    public void sendPlayerListFooter(final Component footer) {
        this.api$tabList.setFooter(Objects.requireNonNull(footer, "footer"));
    }

    @Override
    public void sendPlayerListHeaderAndFooter(final Component header, final Component footer) {
        this.api$tabList.setHeaderAndFooter(Objects.requireNonNull(header, "header"), Objects.requireNonNull(footer, "footer"));
    }

    @Override
    public void showTitle(final Title title) {
        if (this.impl$isFake) {
            return;
        }
        final Title.Times times = Objects.requireNonNull(title, "title").times();
        if (times != null) {
            this.connection.send(new ClientboundSetTitlesPacket(this.api$durationToTicks(times.fadeIn()), this.api$durationToTicks(times.stay()), this.api$durationToTicks(times.fadeOut())));
        }
        this.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.SUBTITLE, SpongeAdventure.asVanilla(title.subtitle())));
        this.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.TITLE, SpongeAdventure.asVanilla(title.title())));
    }

    @Override
    public void clearTitle() {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.CLEAR, null));
    }

    @Override
    public void resetTitle() {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundSetTitlesPacket(ClientboundSetTitlesPacket.Type.RESET, null));
    }

    @Override
    public void showBossBar(final BossBar bar) {
        if (this.impl$isFake) {
            return;
        }
        final ServerBossEvent vanilla = SpongeAdventure.asVanillaServer(Objects.requireNonNull(bar, "bar"));
        vanilla.addPlayer((net.minecraft.server.level.ServerPlayer) (Object) this);
    }

    @Override
    public void hideBossBar(final BossBar bar) {
        if (this.impl$isFake) {
            return;
        }
        final ServerBossEvent vanilla = SpongeAdventure.asVanillaServer(Objects.requireNonNull(bar, "bar"));
        vanilla.removePlayer((net.minecraft.server.level.ServerPlayer) (Object) this);
    }

    @Override
    public void playSound(final Sound sound) {
        this.playSound(Objects.requireNonNull(sound, "sound"), this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
    }

    @Override
    public void playSound(final @NonNull Sound sound, final Sound.@NotNull Emitter emitter) {
        Objects.requireNonNull(sound, "sound");
        Objects.requireNonNull(emitter, "emitter");
        if (this.impl$isFake) {
            return;
        }
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getOptional(SpongeAdventure.asVanilla(Objects.requireNonNull(sound, "sound").name()));
        if (event.isPresent()) { // The SoundEntityPacket does not support custom sounds
            final Entity tracked;
            if (emitter == Sound.Emitter.self()) {
                tracked = (Entity) (Object) this;
            } else if (emitter instanceof org.spongepowered.api.entity.Entity) {
                tracked = (Entity) emitter;
            } else {
                throw new IllegalArgumentException("Specified emitter '" + emitter + "' is not a Sponge Entity or Emitter.self(), was of type '" + emitter.getClass() + "'");
            }
            this.connection.send(new ClientboundSoundEntityPacket(event.get(), SpongeAdventure.asVanilla(sound.source()), tracked, sound.volume(), sound.pitch()));
        }
    }

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        if (this.impl$isFake) {
            return;
        }
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getOptional(SpongeAdventure.asVanilla(Objects.requireNonNull(sound, "sound").name()));
        if (event.isPresent()) {
            // Check if the event is registered
            this.connection.send(new ClientboundSoundPacket(event.get(), SpongeAdventure.asVanilla(sound.source()), x, y, z, sound.volume(), sound.pitch()));
        } else {
            // Otherwise send it as a custom sound
            this.connection.send(new ClientboundCustomSoundPacket(SpongeAdventure.asVanilla(sound.name()), SpongeAdventure.asVanilla(sound.source()),
                    new net.minecraft.world.phys.Vec3(x, y, z), sound.volume(), sound.pitch()));
        }
    }

    @Override
    public void stopSound(final SoundStop stop) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundStopSoundPacket(SpongeAdventure.asVanillaNullable(Objects.requireNonNull(stop, "stop").sound()), SpongeAdventure.asVanillaNullable(stop.source())));
    }

    @Override
    public void openBook(@NonNull final Book book) {
        if (this.impl$isFake) {
            return;
        }
        BookUtil.fakeBookView(Objects.requireNonNull(book, "book"), Collections.singletonList(this));
    }

    @Override
    public @NonNull Locale locale() {
        return ((ServerPlayerBridge) this).bridge$getLanguage();
    }

    private int api$durationToTicks(final Duration duration) {
        return (int) (duration.toMillis() / 50L);
    }
}
