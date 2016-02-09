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

import net.minecraft.world.WorldType;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.type.MutableCatalogRegistryModule;
import org.spongepowered.common.world.type.SpongeWorldTypeEnd;
import org.spongepowered.common.world.type.SpongeWorldTypeNether;
import org.spongepowered.common.world.type.SpongeWorldTypeOverworld;

public final class GeneratorRegistryModule extends MutableCatalogRegistryModule<GeneratorType> {

    @Override
    public void registerDefaults() {
        registerUnsafe(WorldType.AMPLIFIED, "amplified");
        registerUnsafe(WorldType.DEBUG_WORLD, "debug", "debug_all_block_states");
        registerUnsafe(WorldType.DEFAULT, "default");
        registerUnsafe(new SpongeWorldTypeEnd(), "the_end");
        registerUnsafe(WorldType.FLAT, "flat");
        registerUnsafe(WorldType.LARGE_BIOMES, "large_biomes", "largebiomes");
        registerUnsafe(new SpongeWorldTypeNether(), "nether");
        registerUnsafe(new SpongeWorldTypeOverworld(), "overworld");
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (WorldType worldType : WorldType.worldTypes) {
            if (worldType != null) {
                registerUnsafe(worldType, worldType.getWorldTypeName());
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(GeneratorTypes.class, provideCatalogMap());
    }
}
