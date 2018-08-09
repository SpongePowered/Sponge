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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.gen.WorldGeneratorModifiers;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.type.data.DataFormatRegistryModule;
import org.spongepowered.common.registry.type.entity.GameModeRegistryModule;

import java.util.HashMap;
import java.util.Map;

@RegistrationDependency({GameModeRegistryModule.class, GeneratorTypeRegistryModule.class, DifficultyRegistryModule.class,
                         DimensionTypeRegistryModule.class, SerializationBehaviorRegistryModule.class, WorldGeneratorModifierRegistryModule.class,
                         DataFormatRegistryModule.class})
public class WorldArchetypeRegistryModule extends AbstractCatalogRegistryModule<WorldArchetype>
    implements AdditionalCatalogRegistryModule<WorldArchetype>, AlternateCatalogRegistryModule<WorldArchetype> {

    public static WorldArchetypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerDefaults() {
        // Note that we don't have to register them because when we call build, they're registered.
        final WorldArchetype overworld = WorldArchetype.builder()
            .enabled(true)
            .loadsOnStartup(true)
            .keepsSpawnLoaded(true)
            .generateSpawnOnLoad(true)
            .commandsAllowed(true)
            .gameMode(GameModes.SURVIVAL)
            .generator(GeneratorTypes.DEFAULT)
            .dimension(DimensionTypes.OVERWORLD)
            .difficulty(Difficulties.NORMAL)
            .usesMapFeatures(true)
            .hardcore(false)
            .pvp(true)
            .generateBonusChest(false)
            .serializationBehavior(SerializationBehaviors.AUTOMATIC)
            .build("minecraft:overworld", "Overworld");
        WorldArchetype.builder()
            .from(overworld)
            .generator(GeneratorTypes.NETHER)
            .dimension(DimensionTypes.NETHER)
            .build("minecraft:the_nether", "The Nether");
        WorldArchetype.builder()
            .from(overworld)
            .generator(GeneratorTypes.THE_END)
            .dimension(DimensionTypes.THE_END)
            .build("minecraft:the_end", "The End");
        WorldArchetype.builder()
            .from(overworld)
            .generatorModifiers(WorldGeneratorModifiers.VOID)
            .build("sponge:the_void", "The Void");
    }

    @Override
    public void registerAdditionalCatalog(WorldArchetype extraCatalog) {
        checkNotNull(extraCatalog, "WorldArchetype cannot be null!");
        //checkArgument(!id.startsWith("minecraft:"), "Plugin trying to register a fake minecraft generation settings!");
        //checkArgument(!id.startsWith("sponge:"), "Plugin trying to register a fake sponge generation settings!");
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    @Override
    public Map<String, WorldArchetype> provideCatalogMap() {
        Map<String, WorldArchetype> provided = new HashMap<>();
        for (Map.Entry<CatalogKey, WorldArchetype> entry : this.map.entrySet()) {
            provided.put(entry.getKey().getValue(), entry.getValue());
        }
        return provided;
    }

    WorldArchetypeRegistryModule() {
    }

    private static final class Holder {

        static final WorldArchetypeRegistryModule INSTANCE = new WorldArchetypeRegistryModule();
    }
}
