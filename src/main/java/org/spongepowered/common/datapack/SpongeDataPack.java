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
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.generation.biome.BiomeTemplate;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.advancement.SpongeAdvancementTemplate;
import org.spongepowered.common.datapack.recipe.RecipeDataPackSerializer;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.tag.SpongeTagTemplate;
import org.spongepowered.common.world.biome.SpongeBiomeTemplate;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

public record SpongeDataPack<T extends DataPackEntry<T>>(
        TypeToken<T> entryType,
        String name,
        String dir, String description,
        DataPackSerializer<T> packSerializer,

        boolean persistent,
        boolean reloadable) implements DataPack<T> {

    public static <T extends DataPackEntry<T>> SpongeDataPack<T> basic(final Class<T> clazz, final String desc, final String dir, final DataPackEncoder<T> encoder, final DataPackDecoder<T> decoder, final boolean persistent, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        final String baseName = "plugin_" + dir.replace("/", "_");
        final String description = "Sponge plugin provided " + desc;
        return new SpongeDataPack<>(tt, baseName, dir, description, new DataPackSerializer<>(encoder, decoder), persistent, reloadable);
    }

    public static <T extends DataPackEntry<T>> SpongeDataPack<T> custom(final Class<T> clazz,  final String desc, final String dir, final DataPackSerializer<T> serializer, final boolean persistent, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        final String baseName = "plugin_" + dir.replace("/", "_");
        final String description = "Sponge plugin provided " + desc;
        return new SpongeDataPack<>(tt, baseName, dir, description, serializer, persistent, reloadable);
    }

    public static <T extends DataPackEntry<T>> SpongeDataPack<T> custom(final TypeToken<T> typeToken, final String baseName, final String desc, final String dir, final DataPackSerializer<T> serializer, final boolean persistent, final boolean reloadable) {
        final String description = "Sponge plugin provided " + desc;
        return new SpongeDataPack<>(typeToken, baseName, dir, description, serializer, persistent, reloadable);
    }

    public static final class FactoryImpl implements DataPack.Factory {

        private final SpongeDataPack<@NonNull AdvancementTemplate> advancement = SpongeDataPack.basic(AdvancementTemplate.class,
                "Advancements", "advancements",
                      SpongeAdvancementTemplate::encode, null, // TODO decoder
                      false, true);

        private final SpongeDataPack<@NonNull RecipeRegistration> recipe = SpongeDataPack.custom(RecipeRegistration.class,
                "Recipes", "recipes",
                      new RecipeDataPackSerializer(SpongeRecipeRegistration::encode, null), // TODO decoder
                      false, true);

        private final SpongeDataPack<@NonNull WorldTypeTemplate> worldType = SpongeDataPack.basic(WorldTypeTemplate.class,
                "Dimension Types", "dimension_type",
                      SpongeWorldTypeTemplate::encode, null, // TODO decoder
                      true, false);

        private final SpongeDataPack<@NonNull WorldTemplate> world = SpongeDataPack.basic(WorldTemplate.class,
                "Dimensions", "dimension",
                      SpongeWorldTemplate::serialize, SpongeWorldTemplate::decode,
                      true, false);

        private final SpongeDataPack<@NonNull BiomeTemplate> biome = SpongeDataPack.basic(BiomeTemplate.class,
                "Biome", "worldgen/biome",
                      SpongeBiomeTemplate::encode, SpongeBiomeTemplate::decode,
                      true, false);

        @Override
        public DataPack<RecipeRegistration> recipe() {
            return this.recipe;
        }

        @Override
        public DataPack<AdvancementTemplate> advancement() {
            return this.advancement;
        }

        @Override
        public DataPack<WorldTypeTemplate> worldType() {
            return this.worldType;
        }

        @Override
        public DataPack<WorldTemplate> world() {
            return world;
        }

        @Override
        public DataPack<BiomeTemplate> biome() {
            return this.biome;
        }

        @Override
        public <T extends Taggable<T>> DataPack<TagTemplate<T>> tag(RegistryType<T> registry) {
            return SpongeDataPack.custom(new TypeToken<>() {}, "plugin_tags", "Tags", "tags/" + registry.location(),
                            new TagDataPackSerializer<>(SpongeTagTemplate::encode, null), // TODO decoder
                            false, true);
        }

    }

    public DataPack<T> with(final String name, final String description) {
        return new SpongeDataPack<>(this.entryType, name, this.dir, description, this.packSerializer, this.persistent, this.reloadable);
    }
}
