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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.GameType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public final class GameModeRegistryModule implements CatalogRegistryModule<GameMode> {

    @RegisterCatalog(GameModes.class)
    public final BiMap<String, GameMode> gameModeMappings = HashBiMap.create();

    @Override
    public Optional<GameMode> getById(String id) {
        return Optional.ofNullable(this.gameModeMappings.get(checkNotNull(id, "id").toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<GameMode> getAll() {
        return ImmutableList.copyOf((GameMode[]) (Object[]) GameType.values());
    }

    @Override
    public void registerDefaults() {
        this.gameModeMappings.put("survival", (GameMode) (Object) GameType.SURVIVAL);
        this.gameModeMappings.put("creative", (GameMode) (Object) GameType.CREATIVE);
        this.gameModeMappings.put("adventure", (GameMode) (Object) GameType.ADVENTURE);
        this.gameModeMappings.put("spectator", (GameMode) (Object) GameType.SPECTATOR);
        this.gameModeMappings.put("not_set", (GameMode) (Object) GameType.NOT_SET);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (GameType gameType : GameType.values()) {
            if (!this.gameModeMappings.inverse().containsKey((GameMode) (Object) gameType)) {
                this.gameModeMappings.put(gameType.getName().toLowerCase(Locale.ENGLISH), (GameMode) (Object) gameType);
            }
        }
    }

    public static GameType toGameType(GameMode gamemode) {
        for (GameType gameType : GameType.values()) {
            if (gameType.getName().equalsIgnoreCase(gamemode.getId())) {
                return gameType;
            }
        }
        return GameType.SURVIVAL;
    }

}
