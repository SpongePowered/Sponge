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
package org.spongepowered.common.test;

import com.google.inject.Singleton;
import org.spongepowered.api.Server;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class TestServer implements Server {

    @Override
    public Collection<Player> getOnlinePlayers() {
        return Collections.emptyList();
    }

    @Override
    public int getMaxPlayers() {
        return 0;
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        return Optional.empty();
    }

    @Override
    public Collection<World> getWorlds() {
        return Collections.emptyList();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        return Collections.emptyList();
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return Collections.emptyList();
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public Optional<World> getWorld(String worldName) {
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        return Optional.empty();
    }

    @Override
    public String getDefaultWorldName() {
        return "test";
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        return Optional.empty();
    }

    @Override
    public Optional<World> loadWorld(UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public boolean unloadWorld(World world) {
        return false;
    }

    @Override
    public WorldProperties createWorldProperties(String folderName, WorldArchetype archetype) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        return Optional.empty();
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        return false;
    }

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        return Optional.empty();
    }

    @Override
    public ChunkLayout getChunkLayout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRunningTimeTicks() {
        return 0;
    }

    @Override
    public MessageChannel getBroadcastChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBroadcastChannel(MessageChannel channel) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        throw new NoSuchElementException();
    }

    @Override
    public boolean getOnlineMode() {
        return false;
    }

    @Override
    public Text getMotd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown(Text kickMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsoleSource getConsole() {
        throw new NoSuchElementException();
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        throw new NoSuchElementException();
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        throw new NoSuchElementException();
    }

    @Override
    public double getTicksPerSecond() {
        return 0;
    }

    @Override
    public Optional<ResourcePack> getDefaultResourcePack() {
        return Optional.empty();
    }

    @Override
    public int getPlayerIdleTimeout() {
        return 0;
    }

    @Override
    public void setPlayerIdleTimeout(int timeout) {

    }

    @Override
    public boolean isMainThread() {
        return true;
    }
}
