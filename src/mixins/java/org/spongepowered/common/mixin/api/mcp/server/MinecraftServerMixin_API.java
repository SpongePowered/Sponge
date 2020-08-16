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
package org.spongepowered.common.mixin.api.mcp.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import java.util.Collections;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.minecraft.command.Commands;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.api.world.teleport.TeleportHelper;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.scheduler.ServerScheduler;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.util.UsernameCache;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.storage.SpongePlayerDataManager;
import org.spongepowered.common.world.teleport.SpongeTeleportHelper;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
@Implements(value = @Interface(iface = Server.class, prefix = "server$"))
public abstract class MinecraftServerMixin_API extends RecursiveEventLoop<TickDelayedTask> implements SpongeServer {

    @Shadow @Final public long[] tickTimeArray;
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract boolean shadow$isServerInOnlineMode();
    @Shadow public abstract String shadow$getMOTD();
    @Shadow public abstract int shadow$getTickCounter();
    @Shadow public abstract void shadow$initiateShutdown(boolean p_71263_1_);
    @Shadow public abstract int shadow$getMaxPlayerIdleMinutes();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int p_143006_1_);
    @Shadow public abstract void shadow$sendMessage(ITextComponent p_145747_1_);

    private Iterable<? extends Audience> audiences;
    private ServerScheduler api$scheduler;
    private SpongeTeleportHelper api$teleportHelper;
    private SpongePlayerDataManager api$playerDataHandler;
    private UsernameCache api$usernameCache;
    private Audience api$broadcastAudience;
    private ServerScoreboard api$scoreboard;
    private GameProfileManager api$profileManager;
    private SpongeUserManager api$userManager;

    public MinecraftServerMixin_API(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void api$initializeSpongeFields(File p_i50590_1_, Proxy p_i50590_2_, DataFixer dataFixerIn, Commands p_i50590_4_,
        YggdrasilAuthenticationService p_i50590_5_, MinecraftSessionService p_i50590_6_, GameProfileRepository p_i50590_7_,
        PlayerProfileCache p_i50590_8_, IChunkStatusListenerFactory p_i50590_9_, String p_i50590_10_, CallbackInfo ci) {

        this.api$scheduler = new ServerScheduler();
        this.api$playerDataHandler = new SpongePlayerDataManager(this);
        this.api$teleportHelper = new SpongeTeleportHelper();
        this.api$userManager = new SpongeUserManager(this);
    }

    @Override
    public @NonNull Iterable<? extends Audience> audiences() {
        if (this.audiences == null) {
            this.audiences = Iterables.concat((List) this.shadow$getPlayerList().getPlayers(), Collections.singleton(Sponge.getGame().getSystemSubject()));
        }
        return this.audiences;
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public Audience getBroadcastAudience() {
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
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhitelist() {
        return this.shadow$getPlayerList().isWhiteListEnabled();
    }

    @Override
    public void setHasWhitelist(final boolean enabled) {
        this.shadow$getPlayerList().setWhiteListEnabled(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return this.shadow$isServerInOnlineMode();
    }

    @Override
    public UserManager getUserManager() {
        return this.api$userManager;
    }

    @Override public TeleportHelper getTeleportHelper() {
        return this.api$teleportHelper;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<ServerPlayer> getOnlinePlayers() {
        if (this.shadow$getPlayerList() == null || this.shadow$getPlayerList().getPlayers() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) this.shadow$getPlayerList().getPlayers());
    }

    @Override
    public Optional<ServerPlayer> getPlayer(UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId);
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ServerPlayer) this.shadow$getPlayerList().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<ServerPlayer> getPlayer(String name) {
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((ServerPlayer) this.shadow$getPlayerList().getPlayerByUsername(name));
    }

    @Override
    public Component getMotd() {
        return SpongeAdventure.legacySection(this.shadow$getMOTD());
    }

    @Override
    public int getMaxPlayers() {
        if (this.shadow$getPlayerList() == null) {
            return 0;
        }
        return this.shadow$getPlayerList().getMaxPlayers();
    }

    @Override
    public int getRunningTimeTicks() {
        return this.shadow$getTickCounter();
    }

    @Override
    public double getTicksPerSecond() {
        final double nanoSPerTick = MathHelper.average(this.tickTimeArray);
        // Cap at 20 TPS
        return 1000 / Math.max(50, nanoSPerTick / 1000000);
    }

    @Override
    public void shutdown() {
        this.shadow$initiateShutdown(false);
    }

    @Override
    public void shutdown(final Component kickMessage) {
        Preconditions.checkNotNull(kickMessage);
        for (final ServerPlayer player : this.getOnlinePlayers()) {
            player.kick(kickMessage);
        }

        this.shadow$initiateShutdown(false);
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        if (this.api$profileManager == null) {
            this.api$profileManager = new SpongeGameProfileManager(this);
        }

        return this.api$profileManager;
    }

    @Override
    public Optional<ResourcePack> getDefaultResourcePack() {
        return Optional.ofNullable(((MinecraftServerBridge) this).bridge$getResourcePack());
    }

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        if (this.api$scoreboard == null) {
            final ServerWorld world = ((SpongeWorldManager) this.getWorldManager()).getDefaultWorld();
            if (world == null) {
                return Optional.empty();
            }
            this.api$scoreboard = world.getScoreboard();
        }

        return Optional.of((Scoreboard) this.api$scoreboard);
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.shadow$getMaxPlayerIdleMinutes();
    }

    @Intrinsic
    public void server$setPlayerIdleTimeout(int timeout) {
        this.shadow$setPlayerIdleTimeout(timeout);
    }

    @Override
    public Game getGame() {
        return Sponge.getGame();
    }

    @Override
    public CauseStackManager getCauseStackManager() {
        return PhaseTracker.getCauseStackManager();
    }

    @Override
    public ServerScheduler getScheduler() {
        return this.api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return this.isOnExecutionThread();
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
    public void sendMessage(final Component message, final MessageType type) {
        this.shadow$sendMessage(SpongeAdventure.asVanilla(message));
    }

}
