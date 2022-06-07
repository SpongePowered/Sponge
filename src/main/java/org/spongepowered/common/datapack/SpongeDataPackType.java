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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.datapack.DataPackType;
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

public record SpongeDataPackType<T extends DataPackEntry<T>>(String dir, boolean persistent, boolean reloadable, TypeToken<T> entryType, DataPackSerializer<T> packSerializer) implements DataPackType<T> {

    public static <T extends DataPackEntry<T>> SpongeDataPackType<T> basic(final Class<T> clazz, final String dir, final DataPackEncoder<T> encoder, final DataPackDecoder<T> decoder, final boolean persistent, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        return new SpongeDataPackType<>(dir, persistent, reloadable, tt, new DataPackSerializer<>(encoder, decoder));
    }

    public static <T extends DataPackEntry<T>> SpongeDataPackType<T> custom(final Class<T> clazz, final String dir, final DataPackSerializer<T> serializer, final boolean persistent, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        return new SpongeDataPackType<>(dir, persistent, reloadable, tt, serializer);
    }

    public static <T extends DataPackEntry<T>> SpongeDataPackType<T> custom(final TypeToken<T> typeToken, final String dir, final DataPackSerializer<T> serializer, final boolean persistent, final boolean reloadable) {
        return new SpongeDataPackType<>(dir, persistent, reloadable, typeToken, serializer);
    }

    public static final class FactoryImpl implements DataPackType.Factory {

        private final SpongeDataPackType<@NonNull AdvancementTemplate> advancement = SpongeDataPackType.basic(AdvancementTemplate.class,
                "advancements",
                      SpongeAdvancementTemplate::encode, null, // TODO decoder
                      false, true);

        // TODO accept DataPack in RecipeRegistration
        private final SpongeDataPackType<@NonNull RecipeRegistration> recipe = SpongeDataPackType.custom(RecipeRegistration.class,
                "recipes",
                      new RecipeDataPackSerializer(SpongeRecipeRegistration::encode, null), // TODO decoder
                      false, true);

        private final SpongeDataPackType<@NonNull WorldTypeTemplate> worldType = SpongeDataPackType.basic(WorldTypeTemplate.class,
                "dimension_type",
                      SpongeWorldTypeTemplate::encode, null, // TODO decoder
                      true, false);

        private final SpongeDataPackType<@NonNull WorldTemplate> world = SpongeDataPackType.basic(WorldTemplate.class,
                "dimension",
                      SpongeWorldTemplate::serialize, SpongeWorldTemplate::decode,
                      true, false);

        private final SpongeDataPackType<@NonNull BiomeTemplate> biome = SpongeDataPackType.basic(BiomeTemplate.class,
                "worldgen/biome",
                      SpongeBiomeTemplate::encode, SpongeBiomeTemplate::decode,
                      true, false);

        @Override
        public DataPackType<RecipeRegistration> recipe() {
            return this.recipe;
        }

        @Override
        public DataPackType<AdvancementTemplate> advancement() {
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
        public DataPackType<BiomeTemplate> biome() {
            return this.biome;
        }

        @Override
        public <T extends Taggable<T>> DataPackType<TagTemplate<T>> tag(RegistryType<T> registry) {
            final String tagDir = TagManager.getTagDir(ResourceKey.createRegistryKey((ResourceLocation) (Object) registry.location()));
            return SpongeDataPackType.custom(new TypeToken<>() {}, tagDir,
                            new TagDataPackSerializer<>(SpongeTagTemplate::encode, null), // TODO decoder
                            false, true);
        }

    }

    public DataPack<T> pack(final String name, final String description) {
        return new SpongeDataPack<>(name, description, this);
    }
}
