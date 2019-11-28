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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.difficulty.Difficulty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class DifficultyRegistryModule implements AlternateCatalogRegistryModule<Difficulty> {

    public static DifficultyRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(Difficulties.class)
    private final Map<String, Difficulty> difficultyMappings = new HashMap<>();

    @Override
    public Optional<Difficulty> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":") && !id.equals("none")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.difficultyMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<Difficulty> getAll() {
        return ImmutableList.copyOf(this.difficultyMappings.values());
    }

    @Override
    public Map<String, Difficulty> provideCatalogMap() {
        Map<String, Difficulty> newMap = new HashMap<>();
        for (Map.Entry<String, Difficulty> entry : this.difficultyMappings.entrySet()) {
            newMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return newMap;
    }

    @Override
    public void registerDefaults() {
        this.difficultyMappings.put("minecraft:peaceful", (Difficulty) (Object) net.minecraft.world.Difficulty.PEACEFUL);
        this.difficultyMappings.put("minecraft:easy", (Difficulty) (Object) net.minecraft.world.Difficulty.EASY);
        this.difficultyMappings.put("minecraft:normal", (Difficulty) (Object) net.minecraft.world.Difficulty.NORMAL);
        this.difficultyMappings.put("minecraft:hard", (Difficulty) (Object) net.minecraft.world.Difficulty.HARD);
    }

    @AdditionalRegistration
    public void additional() {
        for (net.minecraft.world.Difficulty difficulty : net.minecraft.world.Difficulty.values()) {
            if (!this.difficultyMappings.containsValue(difficulty)) {
                // TODO This doesn't have the modid...
                this.difficultyMappings.put(difficulty.name(), (Difficulty) (Object) difficulty);
            }
        }
    }

    private static final class Holder {
        static final DifficultyRegistryModule INSTANCE = new DifficultyRegistryModule();
    }
}
