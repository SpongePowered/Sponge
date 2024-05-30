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

import com.google.gson.JsonElement;
import io.leangen.geantyref.TypeToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.advancement.AdvancementTemplate;
import org.spongepowered.api.adventure.ChatTypeTemplate;
import org.spongepowered.api.datapack.DataPack;
import org.spongepowered.api.datapack.DataPackEntry;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypeTemplate;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.biome.BiomeTemplate;
import org.spongepowered.api.world.generation.carver.CarverTemplate;
import org.spongepowered.api.world.generation.config.noise.DensityFunctionTemplate;
import org.spongepowered.api.world.generation.config.noise.NoiseGeneratorConfigTemplate;
import org.spongepowered.api.world.generation.config.noise.NoiseTemplate;
import org.spongepowered.api.world.generation.feature.FeatureTemplate;
import org.spongepowered.api.world.generation.feature.PlacedFeatureTemplate;
import org.spongepowered.api.world.generation.structure.SchematicTemplate;
import org.spongepowered.api.world.generation.structure.StructureSetTemplate;
import org.spongepowered.api.world.generation.structure.StructureTemplate;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPoolTemplate;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorListTemplate;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.advancement.SpongeAdvancementTemplate;
import org.spongepowered.common.adventure.SpongeChatTypeTemplate;
import org.spongepowered.common.datapack.recipe.RecipeDataPackSerializer;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageTypeTemplate;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;
import org.spongepowered.common.tag.SpongeTagTemplate;
import org.spongepowered.common.world.biome.SpongeBiomeTemplate;
import org.spongepowered.common.world.generation.carver.SpongeCarverTemplate;
import org.spongepowered.common.world.generation.config.noise.SpongeDensityFunctionTemplate;
import org.spongepowered.common.world.generation.config.noise.SpongeNoiseGeneratorConfigTemplate;
import org.spongepowered.common.world.generation.config.noise.SpongeNoiseTemplate;
import org.spongepowered.common.world.generation.feature.SpongeFeatureTemplate;
import org.spongepowered.common.world.generation.feature.SpongePlacedFeatureTemplate;
import org.spongepowered.common.world.generation.structure.SpongeSchematicTemplate;
import org.spongepowered.common.world.generation.structure.SpongeStructureSetTemplate;
import org.spongepowered.common.world.generation.structure.SpongeStructureTemplate;
import org.spongepowered.common.world.generation.structure.jigsaw.SpongeJigsawPoolTemplate;
import org.spongepowered.common.world.generation.structure.jigsaw.SpongeProcessorListTemplate;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

public record SpongeDataPackType<E, T extends DataPackEntry<T>>(String dir, boolean reloadable, TypeToken<T> entryType, DataPackSerializer<E, T> packSerializer) implements DataPackType<T> {

    public static <T extends DataPackEntry<T>> SpongeDataPackType<JsonElement, T> basic(final Class<T> clazz, final String dir, final DataPackEncoder<JsonElement, T> encoder, final DataPackDecoder<JsonElement, T> decoder, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        return new SpongeDataPackType<>(dir, reloadable, tt, new JsonDataPackSerializer<>(encoder, decoder));
    }

    public static <E, T extends DataPackEntry<T>> SpongeDataPackType<E, T> custom(final Class<T> clazz, final String dir, final DataPackSerializer<E, T> serializer, final boolean reloadable) {
        final TypeToken<T> tt = TypeToken.get(clazz);
        return new SpongeDataPackType<>(dir, reloadable, tt, serializer);
    }

    public static <E, T extends DataPackEntry<T>> SpongeDataPackType<E, T> custom(final TypeToken<T> typeToken, final String dir, final DataPackSerializer<E, T> serializer, final boolean reloadable) {
        return new SpongeDataPackType<>(dir, reloadable, typeToken, serializer);
    }

    public static final class FactoryImpl implements DataPackType.Factory {

        private final SpongeDataPackType<JsonElement, @NonNull AdvancementTemplate> advancement = SpongeDataPackType.basic(AdvancementTemplate.class,
                "advancements", SpongeAdvancementTemplate::encode, null, // TODO decoder
                true);

        private final SpongeDataPackType<JsonElement, @NonNull RecipeRegistration> recipe = SpongeDataPackType.custom(RecipeRegistration.class,
                "recipes", new RecipeDataPackSerializer(SpongeRecipeRegistration::encode, null), // TODO decoder
                true);

        private final SpongeDataPackType<JsonElement, @NonNull WorldTypeTemplate> worldType = SpongeDataPackType.basic(WorldTypeTemplate.class,
                "dimension_type", SpongeWorldTypeTemplate::encode, SpongeWorldTypeTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull WorldTemplate> world = SpongeDataPackType.basic(WorldTemplate.class,
                "dimension", SpongeWorldTemplate::encode, SpongeWorldTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull BiomeTemplate> biome = SpongeDataPackType.basic(BiomeTemplate.class,
                "worldgen/biome", SpongeBiomeTemplate::encode, SpongeBiomeTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull CarverTemplate> carver = SpongeDataPackType.basic(CarverTemplate.class,
                "worldgen/configured_carver", SpongeCarverTemplate::encode, SpongeCarverTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull FeatureTemplate> feature = SpongeDataPackType.basic(FeatureTemplate.class,
                "worldgen/configured_feature", SpongeFeatureTemplate::encode, SpongeFeatureTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull PlacedFeatureTemplate> placedFeature = SpongeDataPackType.basic(PlacedFeatureTemplate.class,
                "worldgen/placed_feature", SpongePlacedFeatureTemplate::encode, SpongePlacedFeatureTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull NoiseGeneratorConfigTemplate> noiseGeneratorConfig = SpongeDataPackType.basic(NoiseGeneratorConfigTemplate.class,
                "worldgen/noise_settings", SpongeNoiseGeneratorConfigTemplate::encode, SpongeNoiseGeneratorConfigTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull NoiseTemplate> noise = SpongeDataPackType.basic(NoiseTemplate.class,
                "worldgen/noise", SpongeNoiseTemplate::encode, SpongeNoiseTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull DensityFunctionTemplate> densityFunction = SpongeDataPackType.basic(DensityFunctionTemplate.class,
                "worldgen/density_function", SpongeDensityFunctionTemplate::encode, SpongeDensityFunctionTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull StructureTemplate> structure = SpongeDataPackType.basic(StructureTemplate.class,
                "worldgen/structure", SpongeStructureTemplate::encode, SpongeStructureTemplate::decode,
                false);

        private final SpongeDataPackType<CompoundTag, @NonNull SchematicTemplate> schematic = SpongeDataPackType.custom(SchematicTemplate.class,
                "structures", new NbtDataPackSerializer<>(SpongeSchematicTemplate::encode, SpongeSchematicTemplate::decode),
                false);

        private final SpongeDataPackType<JsonElement, @NonNull ProcessorListTemplate> processorList = SpongeDataPackType.basic(ProcessorListTemplate.class,
                "worldgen/processor_list", SpongeProcessorListTemplate::encode, SpongeProcessorListTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull StructureSetTemplate> structureSet = SpongeDataPackType.basic(StructureSetTemplate.class,
                "worldgen/structure_set", SpongeStructureSetTemplate::encode, SpongeStructureSetTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull JigsawPoolTemplate> jigsawPool = SpongeDataPackType.basic(JigsawPoolTemplate.class,
                "worldgen/template_pool", SpongeJigsawPoolTemplate::encode, SpongeJigsawPoolTemplate::decode,
                false);

        // TODO should be reloadable https://bugs.mojang.com/browse/MC-251318
        private final SpongeDataPackType<JsonElement, @NonNull ChatTypeTemplate> chatType = SpongeDataPackType.basic(ChatTypeTemplate.class,
                "chat_type", SpongeChatTypeTemplate::encode, SpongeChatTypeTemplate::decode,
                false);

        private final SpongeDataPackType<JsonElement, @NonNull DamageTypeTemplate> damageType = SpongeDataPackType.basic(DamageTypeTemplate.class,
                "damage_type", SpongeDamageTypeTemplate::encode, SpongeDamageTypeTemplate::decode,
                false);


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
        public DataPackType<CarverTemplate> carver() {
            return this.carver;
        }

        @Override
        public DataPackType<FeatureTemplate> feature() {
            return this.feature;
        }

        @Override
        public DataPackType<PlacedFeatureTemplate> placedFeature() {
            return this.placedFeature;
        }

        @Override
        public DataPackType<NoiseGeneratorConfigTemplate> noiseGeneratorConfig() {
            return this.noiseGeneratorConfig;
        }

        @Override
        public DataPackType<NoiseTemplate> noise() {
            return this.noise;
        }

        @Override
        public DataPackType<DensityFunctionTemplate> densityFunction() {
            return this.densityFunction;
        }

        @Override
        public DataPackType<StructureTemplate> structure() {
            return this.structure;
        }

        @Override
        public DataPackType<SchematicTemplate> schematic() {
            return this.schematic;
        }

        @Override
        public DataPackType<ProcessorListTemplate> processorList() {
            return this.processorList;
        }

        @Override
        public DataPackType<StructureSetTemplate> structureSet() {
            return this.structureSet;
        }

        @Override
        public DataPackType<JigsawPoolTemplate> jigsawPool() {
            return this.jigsawPool;
        }

        @Override
        public DataPackType<ChatTypeTemplate> chatType() {
            return this.chatType;
        }

        @Override
        public DataPackType<DamageTypeTemplate> damageType() {
            return this.damageType;
        }

        @Override
        public <T extends Taggable<T>> DataPackType<TagTemplate<T>> tag(RegistryType<T> registry) {
            final String tagDir = TagManager.getTagDir(ResourceKey.createRegistryKey((ResourceLocation) (Object) registry.location()));
            return SpongeDataPackType.custom(new TypeToken<>() {}, tagDir,
                            new TagDataPackSerializer<>(SpongeTagTemplate::encode, null), // TODO decoder
                    true);
        }

    }

    public DataPack<T> pack(final String name, final String description) {
        return new SpongeDataPack<>(name, description, this);
    }
}
