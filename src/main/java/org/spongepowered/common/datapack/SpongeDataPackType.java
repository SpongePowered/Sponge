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

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.common.accessor.world.level.dimension.DimensionTypeAccessor;
import org.spongepowered.common.bridge.world.level.dimension.LevelStemBridge;
import org.spongepowered.common.datapack.recipe.RecipeDataPackSerializer;
import org.spongepowered.common.datapack.recipe.RecipeSerializedObject;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.common.world.server.SpongeWorldTemplate;
import org.spongepowered.common.world.server.SpongeWorldTypeTemplate;

import java.util.OptionalLong;
import java.util.function.BiFunction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeZoomer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class SpongeDataPackType<T extends DataPackSerializable, U extends DataPackSerializedObject> implements DataPackType<T> {

    private final TypeToken<T> token;
    private final DataPackSerializer<U> packSerializer;
    private final DataPackSerializableSerializer<T> objectSerializer;
    private final BiFunction<T, JsonObject, U> objectFunction;
    private final boolean persistent;

    public SpongeDataPackType(final TypeToken<T> token, final DataPackSerializer<U> packSerializer, final DataPackSerializableSerializer<T>
            objectSerializer, final BiFunction<T, JsonObject, U> objectFunction, final boolean persistent) {
        this.token = token;
        this.packSerializer = packSerializer;
        this.objectSerializer = objectSerializer;
        this.objectFunction = objectFunction;
        this.persistent = persistent;
    }

    @Override
    public TypeToken<T> type() {
        return this.token;
    }

    @Override
    public boolean persistent() {
        return this.persistent;
    }

    public DataPackSerializer<U> getPackSerializer() {
        return this.packSerializer;
    }

    public DataPackSerializableSerializer<T> getObjectSerializer() {
        return this.objectSerializer;
    }

    public BiFunction<T, JsonObject, U> getObjectFunction() {
        return this.objectFunction;
    }

    public static final class FactoryImpl implements DataPackType.Factory {

        private final SpongeDataPackType<@NonNull Advancement, DataPackSerializedObject> advancement = new SpongeDataPackType<>(TypeToken.get(Advancement.class),
                new DataPackSerializer<>("Advancements", "advancements"),
                s -> ((net.minecraft.advancements.Advancement) s).deconstruct().serializeToJson(),
                (i1, i2) -> new DataPackSerializedObject(i1.key(), i2),
                false
        );

        private final SpongeDataPackType<@NonNull RecipeRegistration, RecipeSerializedObject> recipe = new SpongeDataPackType<>(TypeToken.get(RecipeRegistration.class),
                new RecipeDataPackSerializer(),
                s -> ((FinishedRecipe) s).serializeRecipe(),
                (i1, i2) -> new RecipeSerializedObject(i1.key(), i2, new DataPackSerializedObject(i1.key(), ((FinishedRecipe) i1).serializeAdvancement())),
                false
        );

        private final SpongeDataPackType<@NonNull WorldTypeTemplate, DataPackSerializedObject> worldType = new SpongeDataPackType<>(TypeToken.get(WorldTypeTemplate.class),
                new DataPackSerializer<>("Dimension Types", "dimension_type"),
                s -> {
                    final OptionalLong fixedTime = !s.fixedTime().isPresent() ? OptionalLong.empty() : OptionalLong.of(s.fixedTime().get().asTicks().ticks());
                    final DimensionType type =
                            DimensionTypeAccessor.invoker$new(fixedTime, s.hasSkylight(), s.hasCeiling(), s.scorching(), s.natural(),
                                    s.coordinateMultiplier(),
                                    s.createDragonFight(), s.piglinSafe(), s.bedsUsable(), s.respawnAnchorsUsable(), s.hasRaids(), s.logicalHeight(),
                                    (BiomeZoomer) s.biomeSampler(), (ResourceLocation) (Object) ((SpongeWorldTypeTemplate) s).infiniburn,
                                    (ResourceLocation) (Object) s.effect().key(), s.ambientLighting());
                    return SpongeWorldTypeTemplate.DIRECT_CODEC.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, BootstrapProperties.registries), type).getOrThrow(false, e -> {});
                },
                (i1, i2) -> new DataPackSerializedObject(i1.key(), i2),
                true
        );

        private final SpongeDataPackType<@NonNull WorldTemplate, DataPackSerializedObject> world = new SpongeDataPackType<>(TypeToken.get(WorldTemplate.class),
                new DataPackSerializer<>("Dimensions", "dimension"),
                s -> {
                    final LevelStem template = new LevelStem(() -> BootstrapProperties.registries.dimensionTypes().get((ResourceLocation) (Object) s.worldType().location()), (ChunkGenerator) s.generator());
                    ((LevelStemBridge) (Object) template).bridge$setFromSettings(false);
                    ((LevelStemBridge) (Object) template).bridge$populateFromTemplate((SpongeWorldTemplate) s);
                    return SpongeWorldTemplate.DIRECT_CODEC.encodeStart(RegistryWriteOps.create(JsonOps.INSTANCE, BootstrapProperties.registries), template).getOrThrow(false, e -> {});
                },
                (i1, i2) -> new DataPackSerializedObject(i1.key(), i2),
                true
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
    }
}
