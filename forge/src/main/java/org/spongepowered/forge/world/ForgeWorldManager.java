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
package org.spongepowered.forge.world;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ForgeWorldManager implements SpongeWorldManager {

    private final MinecraftServer server;

    public ForgeWorldManager (final MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Server server() {
        return (Server) this.server;
    }

    @Override
    public Optional<ServerWorld> world(final ResourceKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<Path> worldDirectory(final ResourceKey key) {
        return Optional.empty();
    }

    @Override
    public ServerWorld defaultWorld() {
        return null;
    }

    @Override
    public Collection<ServerWorld> worlds() {
        return null;
    }

    @Override
    public List<ResourceKey> worldKeys() {
        return null;
    }

    @Override
    public List<ResourceKey> templateKeys() {
        return null;
    }

    @Override
    public boolean worldExists(final ResourceKey key) {
        return false;
    }

    @Override
    public Optional<ResourceKey> worldKey(final UUID uniqueId) {
        return Optional.empty();
    }

    @Override
    public CompletableFuture<ServerWorld> loadWorld(final WorldTemplate template) {
        return null;
    }

    @Override
    public CompletableFuture<ServerWorld> loadWorld(final ResourceKey key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ResourceKey key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> unloadWorld(final ServerWorld world) {
        return null;
    }

    @Override
    public boolean templateExists(final ResourceKey key) {
        return false;
    }

    @Override
    public CompletableFuture<Optional<WorldTemplate>> loadTemplate(final ResourceKey key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> saveTemplate(final WorldTemplate template) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<ServerWorldProperties>> loadProperties(final ResourceKey key) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> saveProperties(final ServerWorldProperties properties) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> copyWorld(final ResourceKey key, final ResourceKey copyKey) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> moveWorld(final ResourceKey key, final ResourceKey moveKey) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(final ResourceKey key) {
        return null;
    }

    @Override
    public Path getDefaultWorldDirectory() {
        return null;
    }

    @Override
    public Path getDimensionDataPackDirectory() {
        return null;
    }

    @Override
    public void unloadWorld0(final ServerLevel world) throws IOException {

    }

    @Override
    public void loadLevel() {
        throw new RuntimeException("The WorldManager needs to be implemented!");
    }
}
