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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;
import org.spongepowered.common.registry.Registration;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.world.SpongeDimensionType;

import java.util.Collection;
import java.util.Optional;

@Registration(Registration.Phase.INIT)
@RegisterCatalog(GameMode.class)
public class DimensionTypesRegistryModule implements CatalogRegistryModule<DimensionType> {

    public final BiMap<String, GameMode> gameModeMappings = HashBiMap.create();

    @Override
    public Optional<DimensionType> getById(String id) {
        return Optional.empty();
    }

    @Override
    public Collection<DimensionType> getAll() {
        return ImmutableList.of();
    }

    @Override
    public void registerDefaults() {
        try {
            DimensionTypes.class.getDeclaredField("NETHER").set(null, new SpongeDimensionType("nether", true, WorldProviderHell.class, -1));
            DimensionTypes.class.getDeclaredField("OVERWORLD").set(null, new SpongeDimensionType("overworld", true, WorldProviderSurface.class, 0));
            DimensionTypes.class.getDeclaredField("END").set(null, new SpongeDimensionType("end", false, WorldProviderEnd.class, 1));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        RegistryHelper.mapFields(GameModes.class, this.gameModeMappings);
    }

    @AdditionalRegistration
    public void registerAdditional() {

    }
}
