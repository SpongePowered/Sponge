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
package org.spongepowered.common.world.server;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

import java.util.Optional;
import java.util.UUID;

public final class SpongeServerLevelData {

    private @Nullable ResourceKey key;
    private UUID uniqueId = UUID.randomUUID();
    private @Nullable InheritableConfigHandle<WorldConfig> configAdapter;
    private WorldOptions worldGenOptions;

    public SpongeServerLevelData() {
    }

    public @Nullable ResourceKey key() {
        return this.key;
    }

    public Optional<net.minecraft.resources.ResourceKey<Level>> resourceKey() {
        return Optional.ofNullable((net.minecraft.resources.ResourceKey<Level>) (Object) this.key);
    }

    public void setKey(final ResourceKey key) {
        this.key = key;
    }

    public UUID uniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(final UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setWorldGenOptions(final WorldGenSettings worldGenSettings) {
        this.worldGenOptions = worldGenSettings.options();
    }

    public void setWorldOptions(final WorldOptions options) {
        this.worldGenOptions = options;
    }

    public WorldOptions worldGenOptions() {
        return this.worldGenOptions;
    }

    public @Nullable InheritableConfigHandle<WorldConfig> configAdapter() {
        return this.configAdapter;
    }

    public void setConfigAdapter(final InheritableConfigHandle<WorldConfig> adapter) {
        this.configAdapter = adapter;
    }

}
