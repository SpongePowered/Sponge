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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = CommandSource.class, prefix = "command$"))
public abstract class MinecraftServerMixin_API implements Server, ConsoleSource {

    @Shadow @Final public Profiler profiler;
    @Shadow @Final public long[] tickTimeArray;
    @Shadow private int tickCounter;
    @Shadow private String motd;
    @Shadow public WorldServer[] worlds;
    @Shadow private Thread serverThread;

    @Shadow public abstract void sendMessage(ITextComponent message);
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract GameType getGameType();
    @Shadow public abstract int getMaxPlayerIdleMinutes();

    @Shadow public abstract String shadow$getName();

    private GameProfileManager profileManager;
    private MessageChannel broadcastChannel = MessageChannel.TO_ALL;

    @Override
    public ConsoleSource getConsole() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<World> getWorld(final String worldName) {
        return (Optional<World>) (Object) WorldManager.getWorld(worldName);
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(final String worldName) {
        return WorldManager.getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return WorldManager.getAllWorldProperties();
    }

    @Override
    public MessageChannel getBroadcastChannel() {
        return this.broadcastChannel;
    }

    @Override
    public void setBroadcastChannel(final MessageChannel channel) {
        this.broadcastChannel = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhitelist() {
        return this.getPlayerList().func_72383_n();
    }

    @Override
    public void setHasWhitelist(final boolean enabled) {
        this.getPlayerList().func_72371_a(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return isServerInOnlineMode();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Player> getOnlinePlayers() {
        if (getPlayerList() == null || getPlayerList().func_181057_v() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) getPlayerList().func_181057_v());
    }

    @Override
    public Optional<Player> getPlayer(final UUID uniqueId) {
        if (getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getPlayerList().func_177451_a(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(final String name) {
        if (getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getPlayerList().func_152612_a(name));
    }

    @Override
    public Text getMotd() {
        return SpongeTexts.fromLegacy(this.motd);
    }

    @Override
    public int getMaxPlayers() {
        if (getPlayerList() == null) {
            return 0;
        }
        return getPlayerList().func_72352_l();
    }

    @Override
    public int getRunningTimeTicks() {
        return this.tickCounter;
    }

    @Override
    public double getTicksPerSecond() {
        final double nanoSPerTick = MathHelper.func_76127_a(this.tickTimeArray);
        // Cap at 20 TPS
        return 1000 / Math.max(50, nanoSPerTick / 1000000);
    }

    @Override
    public void shutdown() {
        initiateShutdown();
    }

    @Override
    public void shutdown(final Text kickMessage) {
        for (final Player player : getOnlinePlayers()) {
            player.kick(kickMessage);
        }

        initiateShutdown();
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<World> loadWorld(final UUID uuid) {
        return (Optional) WorldManager.loadWorld(uuid);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<World> loadWorld(final WorldProperties properties) {
        return (Optional) WorldManager.loadWorld(properties);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<World> loadWorld(final String worldName) {
        return (Optional) WorldManager.loadWorld(worldName);
    }

    @Override
    public WorldProperties createWorldProperties(final String folderName, final WorldArchetype archetype) {
        return WorldManager.createWorldProperties(folderName, archetype);
    }

    @Override
    public boolean unloadWorld(final World world) {
        // API is not allowed to unload overworld
        return ((WorldServerBridge) world).bridge$getDimensionId() != 0 && WorldManager.unloadWorld((WorldServer) world, false, false);

    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<World> getWorlds() {
        return (Collection<World>) (Object) Collections.unmodifiableCollection(WorldManager.getWorlds());
    }

    @Override
    public Optional<World> getWorld(final UUID uniqueId) {
        for (final WorldServer worldserver : WorldManager.getWorlds()) {
            if (((World) worldserver).getUniqueId().equals(uniqueId)) {
                return Optional.of((World) worldserver);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        return WorldManager.getWorldByDimensionId(0).map(worldServer -> ((World) worldServer).getProperties());
    }

    @Override
    public String getDefaultWorldName() {
        checkState(getFolderName() != null, "Attempt made to grab default world name too early!");
        return getFolderName();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        return WorldManager.getAllWorldProperties().stream()
                .filter(props -> !this.getWorld(props.getUniqueId()).isPresent())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(final UUID uniqueId) {
        return WorldManager.getWorldProperties(uniqueId);
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(final WorldProperties worldProperties, final String copyName) {
        return WorldManager.copyWorld(worldProperties, copyName);
    }

    @Override
    public Optional<WorldProperties> renameWorld(final WorldProperties worldProperties, final String newName) {
        return WorldManager.renameWorld(worldProperties, newName);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final WorldProperties worldProperties) {
        return WorldManager.deleteWorld(worldProperties);
    }

    @Override
    public boolean saveWorldProperties(final WorldProperties properties) {
        return WorldManager.saveWorldProperties(properties);
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        if (this.profileManager == null) {
            this.profileManager = new SpongeProfileManager();
        }
        return this.profileManager;
    }

    @Override
    public Optional<ResourcePack> getDefaultResourcePack() {
        return Optional.ofNullable(((MinecraftServerBridge) this).bridge$getResourcePack());
    }

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        return WorldManager.getWorldByDimensionId(0).map(worldServer -> (Scoreboard) worldServer.func_96441_U());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }




    @Override
    public int getPlayerIdleTimeout() {
        return this.getMaxPlayerIdleMinutes();
    }


    @Override
    public boolean isMainThread() {
        return this.serverThread == Thread.currentThread();
    }

    @Override
    public String getIdentifier() {
        return ((CommandSourceBridge) this).bridge$getIdentifier();
    }

    @Intrinsic
    public String command$getName() {
        return shadow$getName();
    }
}
