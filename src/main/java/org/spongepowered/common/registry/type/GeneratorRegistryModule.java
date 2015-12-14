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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.world.WorldType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.AdditionalRegistration;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.world.type.SpongeWorldTypeEnd;
import org.spongepowered.common.world.type.SpongeWorldTypeNether;
import org.spongepowered.common.world.type.SpongeWorldTypeOverworld;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class GeneratorRegistryModule implements CatalogRegistryModule<GeneratorType> {

    @RegisterCatalog(GeneratorTypes.class)
    private final Map<String, GeneratorType> generatorTypeMappings = Maps.newHashMap();

    @Override
    public Optional<GeneratorType> getById(String id) {
        return Optional.ofNullable(this.generatorTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<GeneratorType> getAll() {
        return ImmutableList.copyOf(this.generatorTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.generatorTypeMappings.put("amplified", (GeneratorType) WorldType.AMPLIFIED);
        this.generatorTypeMappings.put("debug", (GeneratorType) WorldType.DEBUG_WORLD);
        this.generatorTypeMappings.put("default", (GeneratorType) WorldType.DEFAULT);
        this.generatorTypeMappings.put("the_end", (GeneratorType) new SpongeWorldTypeEnd());
        this.generatorTypeMappings.put("flat", (GeneratorType) WorldType.FLAT);
        this.generatorTypeMappings.put("large_biomes", (GeneratorType) WorldType.LARGE_BIOMES);
        this.generatorTypeMappings.put("nether", (GeneratorType) new SpongeWorldTypeNether());
        this.generatorTypeMappings.put("overworld", (GeneratorType) new SpongeWorldTypeOverworld());
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (WorldType worldType : WorldType.worldTypes) {
            if (worldType != null && !this.generatorTypeMappings.values().contains(worldType)) {
                this.generatorTypeMappings.put(worldType.getWorldTypeName().toLowerCase(), (GeneratorType) worldType);
            }
        }
    }
}
