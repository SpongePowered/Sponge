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
import net.minecraft.data.IFinishedRecipe;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.datapack.DataPackSerializable;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypeRegistration;
import org.spongepowered.common.datapack.recipe.RecipeDataPackSerializer;
import org.spongepowered.common.datapack.recipe.RecipeSerializedObject;

import java.util.function.BiFunction;

public final class SpongeDataPackType<T extends DataPackSerializable, U extends DataPackSerializedObject> implements DataPackType {

    private final DataPackSerializer<U> packSerializer;
    private final DataPackSerializableSerializer<T> objectSerializer;
    private final BiFunction<T, JsonObject, U> objectFunction;
    private final boolean persistent;

    public SpongeDataPackType(final DataPackSerializer<U> packSerializer, final DataPackSerializableSerializer<T> objectSerializer,
            final BiFunction<T, JsonObject, U> objectFunction, final boolean persistent) {
        this.packSerializer = packSerializer;
        this.objectSerializer = objectSerializer;
        this.objectFunction = objectFunction;
        this.persistent = persistent;
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

    @Override
    public boolean persistent() {
        return this.persistent;
    }

    public static final class FactoryImpl implements DataPackType.Factory {

        private final SpongeDataPackType<Advancement, DataPackSerializedObject> advancement = new SpongeDataPackType<>(
                new DataPackSerializer<>(DataPackTypes.ADVANCEMENT, "Advancements", "advancements"),
                s -> ((net.minecraft.advancements.Advancement) s).deconstruct().serializeToJson(),
                (i1, i2) -> new DataPackSerializedObject(i1.getKey(), i2),
                false
        );

        private final SpongeDataPackType<RecipeRegistration, RecipeSerializedObject> recipe = new SpongeDataPackType<>(
                new RecipeDataPackSerializer(),
                s -> ((IFinishedRecipe) s).serializeRecipe(),
                (i1, i2) -> new RecipeSerializedObject(i1.getKey(), i2, new DataPackSerializedObject(i1.getKey(), ((IFinishedRecipe) i1).serializeAdvancement())),
                false
        );

        private final SpongeDataPackType<DimensionTypeRegistration, DataPackSerializedObject> dimensionType = new SpongeDataPackType<>(
                new DataPackSerializer<>(DataPackTypes.DIMENSION_TYPE, "Dimension Types", "dimension_type"),
                s -> net.minecraft.world.DimensionType.DIRECT_CODEC.encodeStart(JsonOps.INSTANCE, (net.minecraft.world.DimensionType) s).getOrThrow(false, e -> {}),
                (i1, i2) -> new DataPackSerializedObject(i1.getKey(), i2),
                true
        );

        @Override
        public DataPackType recipe() {
            return this.recipe;
        }

        @Override
        public DataPackType advancement() {
            return this.advancement;
        }

        @Override
        public DataPackType dimensionType() {
            return this.dimensionType;
        }
    }
}
