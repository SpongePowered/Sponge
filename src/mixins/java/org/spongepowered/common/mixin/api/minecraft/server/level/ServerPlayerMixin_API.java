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

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.permission.PermissionChecker;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.advancement.AdvancementProgress;
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.CooldownTracker;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.event.world.ChangeWorldBorderEvent;
import org.spongepowered.api.network.ServerSideConnection;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.network.ServerCommonPacketListenerImplAccessor;
import org.spongepowered.common.accessor.server.network.ServerGamePacketListenerImplAccessor;
import org.spongepowered.common.accessor.world.level.border.WorldBorderAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.network.ConnectionBridge;
import org.spongepowered.common.bridge.server.PlayerAdvancementsBridge;
import org.spongepowered.common.bridge.server.ServerScoreboardBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.server.network.ServerCommonPacketListenerImplBridge;
import org.spongepowered.common.bridge.world.level.border.WorldBorderBridge;
import org.spongepowered.common.entity.player.SpongeUserView;
import org.spongepowered.common.entity.player.tab.SpongeTabList;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.api.minecraft.world.entity.player.PlayerMixin_API;
import org.spongepowered.common.profile.SpongeGameProfile;
import org.spongepowered.common.util.NetworkUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin_API extends PlayerMixin_API implements ServerPlayer {

    // @formatter:off
    @Shadow @Final public MinecraftServer server;
    @Shadow @Final private PlayerAdvancements advancements;
    @Shadow public ServerGamePacketListenerImpl connection;

    @Shadow public abstract net.minecraft.server.level.ServerLevel shadow$serverLevel();
    @Shadow public abstract void shadow$sendSystemMessage(final net.minecraft.network.chat.Component $$0);

    // @formatter:on


    @Shadow
    @Nullable
    private Vec3 enteredLavaOnVehiclePosition;
    private volatile Pointers api$pointers;

    private final TabList api$tabList = new SpongeTabList((net.minecraft.server.level.ServerPlayer) (Object) this);

    @Override
    public ServerWorld world() {
        return (ServerWorld) this.shadow$serverLevel();
    }

    @Override
    public User user() {
        return SpongeUserView.create(this.uuid);
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
        return SpongeGameProfile.of(this.shadow$getGameProfile());
    }

    @Override
    public ServerSideConnection connection() {
        final Connection connection = ((ServerCommonPacketListenerImplAccessor) this.connection).accessor$connection();
        return (ServerSideConnection) ((ConnectionBridge) connection).bridge$getEngineConnection();
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     * inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Overwrite
    public String getIpAddress() {
        return NetworkUtil.getHostString(((ServerCommonPacketListenerImplAccessor) this.connection).accessor$connection().getRemoteAddress());
    }

    @Override
    public String identifier() {
        return this.uuid.toString();
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
    public boolean respawn() {
        if (this.impl$isFake) {
            return false;
        }
        if (this.shadow$getHealth() > 0.0F) {
            return false;
        }
        this.connection.player = this.server.getPlayerList().respawn((net.minecraft.server.level.ServerPlayer) (Object) this, false, Entity.RemovalReason.DISCARDED);
        return true;
    }

    @Override
    public void simulateChat(final Component message, final Cause cause) {
        // TODO maybe deprecate & remove this as we cannot fake player messages anymore
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(cause, "cause");
        final PlayerChatEvent.Decorate event = SpongeEventFactory.createPlayerChatEventDecorate(cause, message, message, Optional.of(this));
        if (!SpongeCommon.post(event)) {
            final net.minecraft.network.chat.Component decoratedMessage = SpongeAdventure.asVanilla(event.message());
            final ChatType.Bound boundType = ChatType.bind(ChatType.CHAT, this.server.registryAccess(), this.getName());
            final var thisPlayer = (net.minecraft.server.level.ServerPlayer) (Object) this;
            this.server.getPlayerList().broadcastChatMessage(PlayerChatMessage.system(decoratedMessage.getString()), thisPlayer, boundType);
        }
    }

    @Override
    @NonNull
    public Optional<WorldBorder> worldBorder() {
        final net.minecraft.world.level.border.@Nullable WorldBorder border = ((ServerPlayerBridge) this).bridge$getWorldBorder();
        if (border == null) {
            return Optional.empty();
        }
        return Optional.of(((WorldBorderBridge) border).bridge$asImmutable());
    }

    @Override
    public CooldownTracker cooldownTracker() {
        return (CooldownTracker) this.shadow$getCooldowns();
    }

    @Override
    public AdvancementProgress progress(final AdvancementTemplate advancement) {
        Objects.requireNonNull(advancement, "advancement");
        final AdvancementHolder holder = new AdvancementHolder((ResourceLocation) (Object) advancement.key(), (Advancement) (Object) advancement.advancement());
        return (AdvancementProgress) this.advancements.getOrStartProgress(holder);
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

        final net.minecraft.world.level.border.@Nullable WorldBorder oldWorldBorder = ((ServerPlayerBridge) this).bridge$getWorldBorder();
        if (oldWorldBorder != null) { // is the world border about to be unset?
            ((WorldBorderAccessor) oldWorldBorder).accessor$listeners().remove(
                ((ServerPlayerBridge) this).bridge$getWorldBorderListener()); // remove the listener, if so
        }
        final Optional<WorldBorder> toSet = event.newBorder();
        if (toSet.isPresent()) {
            final net.minecraft.world.level.border.WorldBorder mutableWorldBorder =
                new net.minecraft.world.level.border.WorldBorder();
            ((WorldBorderBridge) mutableWorldBorder).bridge$applyFrom(toSet.get());
            ((ServerPlayerBridge) this).bridge$replaceWorldBorder(mutableWorldBorder);
            mutableWorldBorder.addListener(((ServerPlayerBridge) this).bridge$getWorldBorderListener());
            this.connection.send(new ClientboundInitializeBorderPacket(mutableWorldBorder));
        } else { // unset the border if null
            ((ServerPlayerBridge) this).bridge$replaceWorldBorder(null);
            this.connection.send(new ClientboundInitializeBorderPacket(this.shadow$getCommandSenderWorld().getWorldBorder()));
        }
        return toSet;

    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.requireValue(Keys.CHAT_COLORS_ENABLED).asImmutable());
        values.add(this.requireValue(Keys.CHAT_VISIBILITY).asImmutable());
        values.add(this.requireValue(Keys.GAME_MODE).asImmutable());
        values.add(this.requireValue(Keys.HAS_VIEWED_CREDITS).asImmutable());
        values.add(this.requireValue(Keys.LOCALE).asImmutable());
        values.add(this.requireValue(Keys.PREVIOUS_GAME_MODE).asImmutable());
        values.add(this.requireValue(Keys.SKIN_PARTS).asImmutable());
        values.add(this.requireValue(Keys.SPECTATOR_TARGET).asImmutable());
        // TODO ClassCastException: ServerStatsCounter -> StatsCounterBridge
        // values.add(this.requireValue(Keys.STATISTICS).asImmutable());
        values.add(this.requireValue(Keys.VIEW_DISTANCE).asImmutable());

        this.getValue(Keys.HEALTH_SCALE).map(Value::asImmutable).ifPresent(values::add);
        this.getValue(Keys.SKIN_PROFILE_PROPERTY).map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

    // Audience

    @Override
    public @NonNull Pointers pointers() {
        Pointers pointers = this.api$pointers;
        if (pointers == null) {
            synchronized (this) {
                if (this.api$pointers == null) {
                    this.api$pointers = pointers = Pointers.builder()
                        .withDynamic(Identity.NAME, () -> ((net.minecraft.server.level.ServerPlayer) (Object) this).getGameProfile().getName())
                        .withDynamic(Identity.DISPLAY_NAME, () -> this.displayName().get())
                        .withDynamic(Identity.UUID, ((Entity) (Object) this)::getUUID)
                        .withDynamic(Identity.LOCALE, this::locale)
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
    @Deprecated
    public void sendMessage(final Identity identity, final Component message, final MessageType type) {
        if (this.impl$isFake) {
            return;
        }
        this.shadow$sendSystemMessage(SpongeAdventure.asVanilla(Objects.requireNonNull(message, "message")));
        // TODO chatMessage
        // this.shadow$sendChatMessage(PlayerChatMessage.unsigned(mcMessage), new ChatSender(mcIdentity, name, teamName));
    }

    @Override
    public void sendMessage(final @NonNull Component message) {
        if (this.impl$isFake) {
            return;
        }
        this.shadow$sendSystemMessage(SpongeAdventure.asVanilla(message));
    }

    @Override
    public void sendMessage(final @NonNull Component message, final net.kyori.adventure.chat.ChatType.@NonNull Bound boundChatType) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.sendDisguisedChatMessage(SpongeAdventure.asVanilla(message), SpongeAdventure.asVanilla(this.shadow$level().registryAccess(), boundChatType));
    }

    @Override
    public void sendMessage(final @NonNull SignedMessage signedMessage, final net.kyori.adventure.chat.ChatType.@NonNull Bound boundChatType) {
        if (this.impl$isFake) {
            return;
        }
        // TODO: implement once we actually expose a way to get signed messages in-api
        this.connection.sendDisguisedChatMessage(
            SpongeAdventure.asVanilla(Objects.requireNonNullElse(signedMessage.unsignedContent(), Component.text(signedMessage.message()))),
            SpongeAdventure.asVanilla(this.shadow$level().registryAccess(), boundChatType)
        );
    }

    @Override
    public void deleteMessage(final SignedMessage.@NonNull Signature signature) {
        if (this.impl$isFake) {
            return;
        }
        this.connection.send(new ClientboundDeleteChatPacket(((MessageSignature) (Object) signature)
            .pack(((ServerGamePacketListenerImplAccessor) this.connection).accessor$messageSignatureCache())));
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
    public @NonNull Locale locale() {
        return ((ServerPlayerBridge) this).bridge$getLanguage();
    }

    @Override
    public void sendResourcePacks(final @NonNull ResourcePackRequest request) {
        ((ServerCommonPacketListenerImplBridge) this.connection).bridge$sendResourcePacks(request);
    }

    @Override
    public void removeResourcePacks(final @NonNull UUID id, final @NonNull UUID @NonNull ... others) {
        ((ServerCommonPacketListenerImplBridge) this.connection).bridge$removeResourcePacks(id, others);
    }

    @Override
    public void clearResourcePacks() {
        ((ServerCommonPacketListenerImplBridge) this.connection).bridge$clearResourcePacks();
    }
}
