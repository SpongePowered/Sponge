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
package org.spongepowered.common.registry.provider;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.data.persistence.DataSerializers;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.world.schematic.SchematicTranslator;
import org.spongepowered.math.imaginary.Complexd;
import org.spongepowered.math.imaginary.Complexf;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.imaginary.Quaternionf;
import org.spongepowered.math.vector.Vector2d;
import org.spongepowered.math.vector.Vector2f;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector2l;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3f;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.math.vector.Vector3l;
import org.spongepowered.math.vector.Vector4d;
import org.spongepowered.math.vector.Vector4f;
import org.spongepowered.math.vector.Vector4i;
import org.spongepowered.math.vector.Vector4l;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DataTranslatorProvider {

    public static final DataTranslatorProvider INSTANCE = new DataTranslatorProvider();
    private final Map<Class, DataTranslator> mappings = new IdentityHashMap<>();

    DataTranslatorProvider() {
        this.mappings.put(Component.class, DataSerializers.COMPONENT_DATA_SERIALIZER);
        this.mappings.put(UUID.class, DataSerializers.UUID_DATA_SERIALIZER);
        this.mappings.put(Vector2d.class, DataSerializers.VECTOR_2_D_DATA_SERIALIZER);
        this.mappings.put(Vector2f.class, DataSerializers.VECTOR_2_F_DATA_SERIALIZER);
        this.mappings.put(Vector2i.class, DataSerializers.VECTOR_2_I_DATA_SERIALIZER);
        this.mappings.put(Vector2l.class, DataSerializers.VECTOR_2_L_DATA_SERIALIZER);
        this.mappings.put(Vector3d.class, DataSerializers.VECTOR_3_D_DATA_SERIALIZER);
        this.mappings.put(Vector3f.class, DataSerializers.VECTOR_3_F_DATA_SERIALIZER);
        this.mappings.put(Vector3i.class, DataSerializers.VECTOR_3_I_DATA_SERIALIZER);
        this.mappings.put(Vector3l.class, DataSerializers.VECTOR_3_L_DATA_SERIALIZER);
        this.mappings.put(Vector4d.class, DataSerializers.VECTOR_4_D_DATA_SERIALIZER);
        this.mappings.put(Vector4f.class, DataSerializers.VECTOR_4_F_DATA_SERIALIZER);
        this.mappings.put(Vector4i.class, DataSerializers.VECTOR_4_I_DATA_SERIALIZER);
        this.mappings.put(Vector4l.class, DataSerializers.VECTOR_4_L_DATA_SERIALIZER);
        this.mappings.put(Complexd.class, DataSerializers.COMPLEXD_DATA_SERIALIZER);
        this.mappings.put(Complexf.class, DataSerializers.COMPLEXF_DATA_SERIALIZER);
        this.mappings.put(Quaterniond.class, DataSerializers.QUATERNIOND_DATA_SERIALIZER);
        this.mappings.put(Quaternionf.class, DataSerializers.QUATERNIONF_DATA_SERIALIZER);
        this.mappings.put(LocalTime.class, DataSerializers.LOCAL_TIME_DATA_SERIALIZER);
        this.mappings.put(LocalDate.class, DataSerializers.LOCAL_DATE_DATA_SERIALIZER);
        this.mappings.put(LocalDateTime.class, DataSerializers.LOCAL_DATE_TIME_DATA_SERIALIZER);
        this.mappings.put(Instant.class, DataSerializers.INSTANT_DATA_SERIALIZER);
        this.mappings.put(ZonedDateTime.class, DataSerializers.ZONED_DATE_TIME_DATA_SERIALIZER);
        this.mappings.put(Month.class, DataSerializers.MONTH_DATA_SERIALIZER);
        this.mappings.put(CompoundTag.class, NBTTranslator.INSTANCE);
        this.mappings.put(Schematic.class, SchematicTranslator.get());
        this.mappings.put(Direction.class, DataSerializers.DIRECTION_SERIALIZER);
    }


    @SuppressWarnings("unchecked")
    public <T> Optional<DataTranslator<T>> getSerializer(Class clazz) {
        final DataTranslator dataTranslator = this.mappings.get(clazz);
        return Optional.ofNullable((DataTranslator<T>) dataTranslator);
    }

    public <T> void register(final Class<T> objectClass, final DataTranslator<T> translator) {
        if (!this.mappings.containsKey(objectClass)) {
            this.mappings.put(objectClass, translator);
        } else {
            throw new IllegalArgumentException("DataTranslator already registered for " + objectClass);
        }
    }
}
