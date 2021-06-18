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
package org.spongepowered.common.mixin.api.minecraft.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Mth;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.map.MapStorage;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.teleport.TeleportHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.commands.CommandsBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.map.SpongeMapStorage;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.ServerScheduler;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.UsernameCache;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.storage.SpongePlayerDataManager;
import org.spongepowered.common.world.teleport.SpongeTeleportHelper;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
@Implements(value = @Interface(iface = Server.class, prefix = "server$"))
public abstract class MinecraftServerMixin_API extends ReentrantBlockableEventLoop<TickTask> implements SpongeServer {

    // @formatter:off
    @Shadow @Final public long[] tickTimes;
    @Shadow @Final protected WorldData worldData;
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract boolean shadow$usesAuthentication();
    @Shadow public abstract String shadow$getMotd();
    @Shadow public abstract int shadow$getTickCount();
    @Shadow public abstract void shadow$halt(boolean p_71263_1_);
    @Shadow public abstract int shadow$getPlayerIdleTimeout();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int p_143006_1_);
    @Shadow public abstract boolean shadow$isHardcore();
    @Shadow public abstract boolean shadow$getForceGameType();
    @Shadow public abstract boolean shadow$isPvpAllowed();
    @Shadow public abstract boolean shadow$isCommandBlockEnabled();
    @Shadow protected abstract boolean shadow$isSpawningMonsters();
    @Shadow public abstract boolean shadow$isSpawningAnimals();
    @Shadow public abstract boolean shadow$isNetherEnabled();
    @Shadow public abstract Commands shadow$getCommands();
    // @formatter:on

    private Iterable<? extends Audience> audiences;
    private ServerScheduler api$scheduler;
    private SpongeTeleportHelper api$teleportHelper;
    private SpongePlayerDataManager api$playerDataHandler;
    private UsernameCache api$usernameCache;
    private Audience api$broadcastAudience;
    private ServerScoreboard api$scoreboard;
    private GameProfileManager api$profileManager;
    private SpongeUserManager api$userManager;
    private MapStorage api$mapStorage;
    private RegistryHolder api$registryHolder;

    public MinecraftServerMixin_API(final String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void api$initializeSpongeFields(final Thread p_i232576_1_, final RegistryAccess.RegistryHolder p_i232576_2_,
            final LevelStorageSource.LevelStorageAccess p_i232576_3_, final WorldData p_i232576_4_, final PackRepository p_i232576_5_,
            final Proxy p_i232576_6_, final DataFixer p_i232576_7_, final ServerResources p_i232576_8_, final MinecraftSessionService p_i232576_9_,
            final GameProfileRepository p_i232576_10_, final GameProfileCache p_i232576_11_, final ChunkProgressListenerFactory p_i232576_12_,
            final CallbackInfo ci) {

        this.api$scheduler = new ServerScheduler();
        this.api$playerDataHandler = new SpongePlayerDataManager(this);
        this.api$teleportHelper = new SpongeTeleportHelper();
        this.api$userManager = new SpongeUserManager(this);
        this.api$mapStorage = new SpongeMapStorage();
        this.api$registryHolder = new SpongeRegistryHolder(p_i232576_2_);
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        if (this.audiences == null) {
            this.audiences = Iterables.concat((List) this.shadow$getPlayerList().getPlayers(), Collections.singleton(Sponge.game().systemSubject()));
        }
        return this.audiences;
    }

    @Override
    public ChunkLayout chunkLayout() {
        return SpongeChunkLayout.INSTANCE;
    }

    @Override
    public Audience broadcastAudience() {
        if (this.api$broadcastAudience == null) {
            this.api$broadcastAudience = this;
        }

        return this.api$broadcastAudience;
    }

    @Override
    public void setBroadcastAudience(final Audience channel) {
        this.api$broadcastAudience = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<InetSocketAddress> boundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean isWhitelistEnabled() {
        return this.shadow$getPlayerList().isUsingWhitelist();
    }

    @Override
    public void setHasWhitelist(final boolean enabled) {
        this.shadow$getPlayerList().setUsingWhiteList(enabled);
    }

    @Override
    public boolean isOnlineModeEnabled() {
        return this.shadow$usesAuthentication();
    }

    @Override
    public boolean isHardcoreModeEnabled() {
        return this.shadow$isHardcore();
    }

    @Override
    public Difficulty difficulty() {
        return (Difficulty) (Object) this.worldData.getDifficulty();
    }

    @Override
    public GameMode gameMode() {
        return (GameMode) (Object) this.worldData.getGameType();
    }

    @Override
    public boolean isGameModeEnforced() {
        return this.shadow$getForceGameType();
    }

    @Override
    public boolean isPVPEnabled() {
        return this.shadow$isPvpAllowed();
    }

    @Override
    public boolean areCommandBlocksEnabled() {
        return this.shadow$isCommandBlockEnabled();
    }

    @Override
    public boolean isMonsterSpawnsEnabled() {
        return this.shadow$isSpawningMonsters();
    }

    @Override
    public boolean isAnimalSpawnsEnabled() {
        return this.shadow$isSpawningAnimals();
    }

    @Override
    public boolean isMultiWorldEnabled() {
        return this.shadow$isNetherEnabled();
    }

    @Override
    public UserManager userManager() {
        return this.api$userManager;
    }

    @Override public TeleportHelper teleportHelper() {
        return this.api$teleportHelper;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<ServerPlayer> onlinePlayers() {
        if (this.shadow$getPlayerList() == null || this.shadow$getPlayerList().getPlayers() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) this.shadow$getPlayerList().getPlayers());
    }

    @Override
    public Optional<ServerPlayer> player(final UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId);
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ServerPlayer) this.shadow$getPlayerList().getPlayer(uniqueId));
    }

    @Override
    public Optional<ServerPlayer> player(final String name) {
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ServerPlayer) this.shadow$getPlayerList().getPlayerByName(name));
    }

    @Override
    public Component motd() {
        return LegacyComponentSerializer.legacySection().deserialize(this.shadow$getMotd());
    }

    @Override
    public int maxPlayers() {
        if (this.shadow$getPlayerList() == null) {
            return 0;
        }
        return this.shadow$getPlayerList().getMaxPlayers();
    }

    @Override
    public int runningTimeTicks() {
        return this.shadow$getTickCount();
    }

    @Override
    public double ticksPerSecond() {
        // Cap at 20 TPS
        return 1000 / Math.max(50, this.averageTickTime());
    }

    @Override
    public double averageTickTime() {
        return Mth.average(this.tickTimes) / 1000000;
    }

    @Override
    public int targetTicksPerSecond() {
        return 20;
    }

    @Override
    public void shutdown() {
        this.shadow$halt(false);
    }

    @Override
    public void shutdown(final Component kickMessage) {
        Preconditions.checkNotNull(kickMessage);
        for (final ServerPlayer player : this.onlinePlayers()) {
            player.kick(kickMessage);
        }

        this.shadow$halt(false);
    }

    @Override
    public GameProfileManager gameProfileManager() {
        if (this.api$profileManager == null) {
            this.api$profileManager = new SpongeGameProfileManager(this);
        }

        return this.api$profileManager;
    }

    @Override
    public SpongeCommandManager commandManager() {
        return ((CommandsBridge) this.shadow$getCommands()).bridge$commandManager();
    }

    public Optional<ResourcePack> server$resourcePack() {
        return Optional.ofNullable(((MinecraftServerBridge) this).bridge$getResourcePack());
    }

    @Override
    public Optional<Scoreboard> serverScoreboard() {
        if (this.api$scoreboard == null) {
            final ServerLevel world = SpongeCommon.getServer().overworld();
            if (world == null) {
                return Optional.empty();
            }
            this.api$scoreboard = world.getScoreboard();
        }

        return Optional.of((Scoreboard) this.api$scoreboard);
    }

    @Intrinsic
    public int server$playerIdleTimeout() {
        return this.shadow$getPlayerIdleTimeout();
    }

    @Intrinsic
    public void server$setPlayerIdleTimeout(final int timeout) {
        this.shadow$setPlayerIdleTimeout(timeout);
    }

    @Override
    public Game game() {
        return Sponge.game();
    }

    @Override
    public CauseStackManager causeStackManager() {
        return PhaseTracker.getCauseStackManager();
    }

    @Override
    public ServerScheduler scheduler() {
        return this.api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return this.isSameThread();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Override
    public SpongePlayerDataManager getPlayerDataManager() {
        return this.api$playerDataHandler;
    }

    @Override
    public UsernameCache getUsernameCache() {
        if (this.api$usernameCache == null) {
            this.api$usernameCache = new UsernameCache(this);
        }

        return this.api$usernameCache;
    }

    @Override
    public void sendMessage(final Identity identity, final Component message, final MessageType type) {
        this.shadow$getPlayerList().broadcastMessage(SpongeAdventure.asVanilla(message), SpongeAdventure.asVanilla(type), identity.uuid());
    }

    @Override
    public ServiceProvider.ServerScoped serviceProvider() {
        return ((MinecraftServerBridge) this).bridge$getServiceProvider();
    }

    @Override
    public RegistryScope registryScope() {
        return RegistryScope.ENGINE;
    }

    @Override
    public RegistryHolder registries() {
        return this.api$registryHolder;
    }
    @Override
    public MapStorage mapStorage() {
        return this.api$mapStorage;
    }
}
