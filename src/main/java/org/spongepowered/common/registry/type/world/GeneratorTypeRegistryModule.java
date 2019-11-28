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

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.biome.provider.SingleBiomeProvider;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorNether;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.common.bridge.world.WorldTypeBridge;
import org.spongepowered.common.mixin.core.world.WorldTypeAccessor;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RegisterCatalog(GeneratorTypes.class)
public final class GeneratorTypeRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<GeneratorType>
    implements SpongeAdditionalCatalogRegistryModule<GeneratorType>, AlternateCatalogRegistryModule<GeneratorType> {

    public static GeneratorTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void registerDefaults() {
        for (final WorldType worldType : WorldTypeAccessor.accessor$getWorldTypes()) {
            this.registerAdditionalCatalog((GeneratorType) worldType);
        }
        {
            final WorldType the_end = new WorldType(getNextID(), "the_end");
            ((WorldTypeBridge) the_end).bridge$setChunkGenerator((world, options)
                -> new ChunkGeneratorEnd(world, true, world.func_72905_C(), new BlockPos(100, 50, 0)));
            ((WorldTypeBridge) the_end).bridge$setBiomeProvider(world -> new SingleBiomeProvider(Biomes.field_76779_k));
            ((WorldTypeAccessor) the_end).accessor$setHasInfoNotice(true);

            this.registerAdditionalCatalog((GeneratorType) the_end);
        }
        {
            final WorldType nether = new WorldType(getNextID(), "nether");
            ((WorldTypeAccessor) nether).accessor$setHasInfoNotice(true);
            ((WorldTypeBridge) nether).bridge$setBiomeProvider(world -> new SingleBiomeProvider(Biomes.field_76778_j));
            ((WorldTypeBridge) nether).bridge$setChunkGenerator((world, s) -> new ChunkGeneratorNether(world, world.func_72912_H().func_76089_r(), world.func_72905_C()));
            this.registerAdditionalCatalog((GeneratorType) nether);
        }
        {
            final WorldType overworld = new WorldType(getNextID(), "overworld");
            ((WorldTypeAccessor) overworld).accessor$setCanBeCreated(false);
            ((WorldTypeBridge) overworld).bridge$setBiomeProvider(world -> new BiomeProvider(world.func_72912_H()));
            ((WorldTypeBridge) overworld).bridge$setChunkGenerator(((world, s) -> new ChunkGeneratorOverworld(world, world.func_72905_C(), world.func_72912_H().func_76089_r(), s)));
            this.registerAdditionalCatalog((GeneratorType) overworld);
        }

    }

    @AdditionalRegistration(RegistrationPhase.PRE_REGISTRY)
    public void registerAdditional() {
        for (final WorldType worldType : WorldTypeAccessor.accessor$getWorldTypes()) {
            if (worldType != null && !this.catalogTypeMap.values().contains(worldType)) {
                this.catalogTypeMap.put(worldType.func_77127_a().toLowerCase(Locale.ENGLISH), (GeneratorType) worldType);
            }
        }
        // Re-map fields in case mods have changed vanilla world types
        RegistryHelper.mapFields(GeneratorTypes.class, this.provideCatalogMap());
    }

    @Override
    public Map<String, GeneratorType> provideCatalogMap() {
        final HashMap<String, GeneratorType> map = new HashMap<>();
        for (final Map.Entry<String, GeneratorType> entry : this.catalogTypeMap.entrySet()) {
            final String replace = entry.getKey().replace("minecraft:", "").replace("sponge:", "")
                    .replace("debug_all_block_states", "debug");
            map.put(replace, entry.getValue());
        }
        return map;
    }

    private GeneratorTypeRegistryModule() {
        super("minecraft",
            new String[] {"minecraft", "sponge"},
            id -> id.replace("debug_all_block_states", "debug")
            );
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(final GeneratorType extraCatalog) {
        if (extraCatalog != null) {
            this.catalogTypeMap.put(extraCatalog.getId(), extraCatalog);
        }
    }

    private static final class Holder {
        static final GeneratorTypeRegistryModule INSTANCE = new GeneratorTypeRegistryModule();
        static {
            try {
                Class.forName("net.minecraft.world.WorldType");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getNextID() {
        for (int x = 0; x < WorldTypeAccessor.accessor$getWorldTypes().length; x++) {
            if (WorldTypeAccessor.accessor$getWorldTypes()[x] == null) {
                return x;
            }
        }

        final int oldLen = WorldTypeAccessor.accessor$getWorldTypes().length;
        WorldTypeAccessor.accessor$setWorldTypes(Arrays.copyOf(WorldTypeAccessor.accessor$getWorldTypes(), oldLen + 16));
        return oldLen;
    }
}
