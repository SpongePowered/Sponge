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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.registry.Registration;

import java.util.Collection;
import java.util.Optional;

@Registration
public class GameModeRegistryModule implements CatalogRegistryModule<GameMode> {

    @RegisterCatalog(GameModes.class)
    public final BiMap<String, GameMode> gameModeMappings = HashBiMap.create();

    @Override
    public Optional<GameMode> getById(String id) {
        return Optional.of(this.gameModeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<GameMode> getAll() {
        return ImmutableList.copyOf((GameMode[]) (Object[]) WorldSettings.GameType.values());
    }

    @Override
    public void registerDefaults() {
        this.gameModeMappings.put("survival", (GameMode) (Object) WorldSettings.GameType.SURVIVAL);
        this.gameModeMappings.put("creative", (GameMode) (Object) WorldSettings.GameType.CREATIVE);
        this.gameModeMappings.put("adventure", (GameMode) (Object) WorldSettings.GameType.ADVENTURE);
        this.gameModeMappings.put("spectator", (GameMode) (Object) WorldSettings.GameType.SPECTATOR);
        this.gameModeMappings.put("not_set", (GameMode) (Object) WorldSettings.GameType.NOT_SET);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (WorldSettings.GameType gameType : WorldSettings.GameType.values()) {
            if (!this.gameModeMappings.inverse().containsKey((GameMode) (Object) gameType)) {
                this.gameModeMappings.put(gameType.getName().toLowerCase(), (GameMode) (Object) gameType);
            }
        }
    }
}
