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

import com.google.common.collect.ImmutableList;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.chunk.ChunkTicketManager;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
@Implements(value = @Interface(iface = Server.class, prefix = "server$"))
public abstract class MinecraftServerMixin_API implements Server {

    @Shadow @Final public long[] tickTimeArray;
    @Shadow @Final protected Thread serverThread;

    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract boolean shadow$isServerInOnlineMode();
    @Shadow public abstract String shadow$getMOTD();
    @Shadow public abstract int shadow$getTickCounter();
    @Shadow public abstract void shadow$initiateShutdown(boolean p_71263_1_);
    @Shadow public abstract int shadow$getMaxPlayerIdleMinutes();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int p_143006_1_);

    // TODO 1.14 - Scheduler
    private SpongeScheduler api$scheduler;
    @Nullable private ServerScoreboard api$scoreboard;
    private GameProfileManager api$profileManager;
    private MessageChannel api$broadcastChannel = MessageChannel.toPlayersAndServer();

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public MessageChannel getBroadcastChannel() {
        return this.api$broadcastChannel;
    }

    @Override
    public void setBroadcastChannel(final MessageChannel channel) {
        this.api$broadcastChannel = checkNotNull(channel, "channel");
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
    public WorldManager getWorldManager() {
        return SpongeImpl.getWorldManager();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Player> getOnlinePlayers() {
        if (this.shadow$getPlayerList() == null || this.shadow$getPlayerList().getPlayers() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) this.shadow$getPlayerList().getPlayers());
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) this.shadow$getPlayerList().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        if (this.shadow$getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) this.shadow$getPlayerList().getPlayerByUsername(name));
    }

    @Override
    public Text getMotd() {
        return SpongeTexts.fromLegacy(this.shadow$getMOTD());
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
    public void shutdown(final Text kickMessage) {
        for (final Player player : this.getOnlinePlayers()) {
            player.kick(kickMessage);
        }

        this.shadow$initiateShutdown(false);
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        if (this.api$profileManager == null) {
            this.api$profileManager = new SpongeProfileManager();
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
            final ServerWorld world = SpongeImpl.getWorldManager().getDefaultWorld();
            if (world == null) {
                return Optional.empty();
            }
            this.api$scoreboard = world.getScoreboard();
        }

        return Optional.ofNullable((Scoreboard) this.api$scoreboard);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
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
    public Scheduler getScheduler() {
        return this.api$scheduler;
    }

    @Override
    public boolean onMainThread() {
        return this.serverThread == Thread.currentThread();
    }
}
