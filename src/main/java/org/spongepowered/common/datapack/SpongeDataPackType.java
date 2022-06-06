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
package org.spongepowered.common.datapack;

import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.generation.biome.BiomeTemplate;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.datapack.recipe.RecipeDataPackSerializer;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.tag.SpongeTagTemplate;
import org.spongepowered.common.world.biome.SpongeBiomeTemplate;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

public record SpongeDataPackType<T extends DataPackEntry<T>>(
         TypeToken<T> type,
         String name,
         String dir,
         DataPackSerializer<T> packSerializer,
         DataPackEncoder<T> encoder,
         DataPackDecoder<T> decoder,
         boolean persistent,
         boolean reloadable) implements DataPackType<T> {

    public SpongeDataPackType(final Class<T> clazz, final String name, final String dir, final DataPackSerializer<T> packSerializer,
            final DataPackEncoder<T> encoder, final DataPackDecoder<T> decoder, final boolean persistent,
            final boolean reloadable) {
        this(TypeToken.get(clazz), name, dir, packSerializer, encoder, decoder, persistent, reloadable);
    }

    public SpongeDataPackType(final Class<T> clazz, final String name, final String dir,
            final DataPackEncoder<T> encoder, final DataPackDecoder<T> decoder, final boolean persistent,
            final boolean reloadable) {
        this(clazz, name, dir, new DataPackSerializer<>(), encoder, decoder, persistent, reloadable);
    }

    public static final class FactoryImpl implements DataPackType.Factory {

        private final SpongeDataPackType<@NonNull Advancement> advancement =
                new SpongeDataPackType<>(Advancement.class, "Advancements", "advancements",
                        (s, registryAccess) -> ((net.minecraft.advancements.Advancement) s).deconstruct().serializeToJson(),
                        null, // TODO decoder
                        false, true
                );

        private final SpongeDataPackType<@NonNull RecipeRegistration> recipe =
                new SpongeDataPackType<>(RecipeRegistration.class, "Recipes", "recipes",
                        new RecipeDataPackSerializer(),
                        SpongeRecipeRegistration::encode,
                        null, // TODO decoder
                        false, true
                );

        private final SpongeDataPackType<@NonNull WorldTypeTemplate> worldType =
                new SpongeDataPackType<>(WorldTypeTemplate.class, "Dimension Types", "dimension_type",
                        SpongeWorldTypeTemplate::encode,
                        null, // TODO decoder
                        true, false
                );

        private final SpongeDataPackType<@NonNull WorldTemplate> world =
                new SpongeDataPackType<>(WorldTemplate.class, "Dimensions", "dimension",
                        SpongeWorldTemplate::serialize,
                        SpongeWorldTemplate::decode,
                        true, false
                );

        private final SpongeDataPackType<@NonNull TagTemplate> tag =
                new SpongeDataPackType<>(TagTemplate.class,"Tag", "tags",
                        new TagDataPackSerializer(),
                        SpongeTagTemplate::encode,
                        null, // TODO decoder
                        false, true
                );

        private final SpongeDataPackType<@NonNull BiomeTemplate> biome =
                new SpongeDataPackType<>(BiomeTemplate.class, "Biome", "worldgen/biome",
                        SpongeBiomeTemplate::encode,
                        SpongeBiomeTemplate::decode,
                        true, false
                );

        @Override
        public DataPackType<RecipeRegistration> recipe() {
            return this.recipe;
        }

        @Override
        public DataPackType<Advancement> advancement() {
            return this.advancement;
        }

        @Override
        public DataPackType<WorldTypeTemplate> worldType() {
            return this.worldType;
        }

        @Override
        public DataPackType<WorldTemplate> world() {
            return world;
        }

        @Override
        public DataPackType<TagTemplate> tag() {
            return this.tag;
        }

        @Override
        public DataPackType<BiomeTemplate> biome() {
            return this.biome;
        }
    }
}
