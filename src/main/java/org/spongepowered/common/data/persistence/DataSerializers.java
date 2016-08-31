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
package org.spongepowered.common.data.persistence;

import com.flowpowered.math.imaginary.Complexd;
import com.flowpowered.math.imaginary.Complexf;
import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector2l;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.math.vector.Vector3l;
import com.flowpowered.math.vector.Vector4d;
import com.flowpowered.math.vector.Vector4f;
import com.flowpowered.math.vector.Vector4i;
import com.flowpowered.math.vector.Vector4l;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataSerializer;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataQueries;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public final class DataSerializers {

    public static final DataSerializer<UUID> UUID_DATA_SERIALIZER;
    public static final DataSerializer<Vector2d> VECTOR_2_D_DATA_SERIALIZER;
    public static final DataSerializer<Vector2f> VECTOR_2_F_DATA_SERIALIZER;
    public static final DataSerializer<Vector2i> VECTOR_2_I_DATA_SERIALIZER;
    public static final DataSerializer<Vector2l> VECTOR_2_L_DATA_SERIALIZER;
    public static final DataSerializer<Vector3d> VECTOR_3_D_DATA_SERIALIZER;
    public static final DataSerializer<Vector3f> VECTOR_3_F_DATA_SERIALIZER;
    public static final DataSerializer<Vector3i> VECTOR_3_I_DATA_SERIALIZER;
    public static final DataSerializer<Vector3l> VECTOR_3_L_DATA_SERIALIZER;
    public static final DataSerializer<Vector4d> VECTOR_4_D_DATA_SERIALIZER;
    public static final DataSerializer<Vector4f> VECTOR_4_F_DATA_SERIALIZER;
    public static final DataSerializer<Vector4i> VECTOR_4_I_DATA_SERIALIZER;
    public static final DataSerializer<Vector4l> VECTOR_4_L_DATA_SERIALIZER;
    public static final DataSerializer<Complexd> COMPLEXD_DATA_SERIALIZER;
    public static final DataSerializer<Complexf> COMPLEXF_DATA_SERIALIZER;
    public static final DataSerializer<Quaterniond> QUATERNIOND_DATA_SERIALIZER;
    public static final DataSerializer<Quaternionf> QUATERNIONF_DATA_SERIALIZER;
    public static final DataSerializer<LocalTime> LOCAL_TIME_DATA_SERIALIZER;
    public static final DataSerializer<LocalDate> LOCAL_DATE_DATA_SERIALIZER;
    public static final DataSerializer<LocalDateTime> LOCAL_DATE_TIME_DATA_SERIALIZER;
    public static final DataSerializer<Instant> INSTANT_DATA_SERIALIZER;
    public static final DataSerializer<ZonedDateTime> ZONED_DATE_TIME_DATA_SERIALIZER;

    static {
        UUID_DATA_SERIALIZER = new DataSerializer<UUID>() {
            final TypeToken<UUID> token = TypeToken.of(UUID.class);

            @Override
            public TypeToken<UUID> getToken() {
                return this.token;
            }

            @Override
            public Optional<UUID> deserialize(DataView view) throws InvalidDataException {
                if (!view.contains(Queries.UUID_MOST, Queries.UUID_LEAST)) {
                    return Optional.empty();
                }
                final long most = view.getLong(Queries.UUID_MOST).orElseThrow(invalidDataQuery(Queries.UUID_MOST));
                final long least = view.getLong(Queries.UUID_LEAST).orElseThrow(invalidDataQuery(Queries.UUID_LEAST));
                return Optional.of(new UUID(most, least));
            }

            @Override
            public DataContainer serialize(UUID obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(Queries.UUID_MOST, obj.getMostSignificantBits())
                        .set(Queries.UUID_LEAST, obj.getLeastSignificantBits());
            }
        };
        VECTOR_2_D_DATA_SERIALIZER = new DataSerializer<Vector2d>() {
            final TypeToken<Vector2d> token = TypeToken.of(Vector2d.class);

            @Override
            public TypeToken<Vector2d> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector2d> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final Double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Vector2d(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector2d obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        VECTOR_2_F_DATA_SERIALIZER = new DataSerializer<Vector2f>() {
            final TypeToken<Vector2f> token = TypeToken.of(Vector2f.class);

            @Override
            public TypeToken<Vector2f> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector2f> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Vector2f(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector2f obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        VECTOR_2_I_DATA_SERIALIZER = new DataSerializer<Vector2i>() {
            final TypeToken<Vector2i> token = TypeToken.of(Vector2i.class);

            @Override
            public TypeToken<Vector2i> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector2i> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS)) {
                    final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Vector2i(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector2i obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        VECTOR_2_L_DATA_SERIALIZER = new DataSerializer<Vector2l>() {
            final TypeToken<Vector2l> token = TypeToken.of(Vector2l.class);

            @Override
            public TypeToken<Vector2l> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector2l> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS)) {
                    final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Vector2l(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector2l obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        VECTOR_3_D_DATA_SERIALIZER = new DataSerializer<Vector3d>() {
            final TypeToken<Vector3d> token = TypeToken.of(Vector3d.class);

            @Override
            public TypeToken<Vector3d> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector3d> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    return Optional.of(new Vector3d(x, y, z));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector3d obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ());
            }
        };
        VECTOR_3_F_DATA_SERIALIZER = new DataSerializer<Vector3f>() {
            final TypeToken<Vector3f> token = TypeToken.of(Vector3f.class);

            @Override
            public TypeToken<Vector3f> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector3f> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    return Optional.of(new Vector3f(x, y, z));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector3f obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ());
            }
        };
        VECTOR_3_I_DATA_SERIALIZER = new DataSerializer<Vector3i>() {
            final TypeToken<Vector3i> token = TypeToken.of(Vector3i.class);

            @Override
            public TypeToken<Vector3i> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector3i> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final int z = view.getInt(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    return Optional.of(new Vector3i(x, y, z));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector3i obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ());
            }
        };
        VECTOR_3_L_DATA_SERIALIZER = new DataSerializer<Vector3l>() {
            final TypeToken<Vector3l> token = TypeToken.of(Vector3l.class);

            @Override
            public TypeToken<Vector3l> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector3l> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final long z = view.getLong(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    return Optional.of(new Vector3l(x, y, z));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector3l obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ());
            }
        };
        VECTOR_4_F_DATA_SERIALIZER = new DataSerializer<Vector4f>() {
            final TypeToken<Vector4f> token = TypeToken.of(Vector4f.class);

            @Override
            public TypeToken<Vector4f> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector4f> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Vector4f(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector4f obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        VECTOR_4_I_DATA_SERIALIZER = new DataSerializer<Vector4i>() {
            final TypeToken<Vector4i> token = TypeToken.of(Vector4i.class);

            @Override
            public TypeToken<Vector4i> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector4i> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final int x = view.getInt(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final int y = view.getInt(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final int z = view.getInt(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final int w = view.getInt(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Vector4i(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector4i obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        VECTOR_4_L_DATA_SERIALIZER = new DataSerializer<Vector4l>() {
            final TypeToken<Vector4l> token = TypeToken.of(Vector4l.class);

            @Override
            public TypeToken<Vector4l> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector4l> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final long x = view.getLong(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final long y = view.getLong(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final long z = view.getLong(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final long w = view.getLong(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Vector4l(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector4l obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        VECTOR_4_D_DATA_SERIALIZER = new DataSerializer<Vector4d>() {
            final TypeToken<Vector4d> token = TypeToken.of(Vector4d.class);

            @Override
            public TypeToken<Vector4d> getToken() {
                return this.token;
            }

            @Override
            public Optional<Vector4d> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Vector4d(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Vector4d obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        COMPLEXD_DATA_SERIALIZER = new DataSerializer<Complexd>() {
            final TypeToken<Complexd> token = TypeToken.of(Complexd.class);

            @Override
            public TypeToken<Complexd> getToken() {
                return this.token;
            }

            @Override
            public Optional<Complexd> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Complexd(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Complexd obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        COMPLEXF_DATA_SERIALIZER = new DataSerializer<Complexf>() {
            final TypeToken<Complexf> token = TypeToken.of(Complexf.class);

            @Override
            public TypeToken<Complexf> getToken() {
                return this.token;
            }

            @Override
            public Optional<Complexf> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    return Optional.of(new Complexf(x, y));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Complexf obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY());
            }
        };
        QUATERNIOND_DATA_SERIALIZER = new DataSerializer<Quaterniond>() {
            final TypeToken<Quaterniond> token = TypeToken.of(Quaterniond.class);

            @Override
            public TypeToken<Quaterniond> getToken() {
                return this.token;
            }

            @Override
            public Optional<Quaterniond> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Quaterniond(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Quaterniond obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        QUATERNIONF_DATA_SERIALIZER = new DataSerializer<Quaternionf>() {
            final TypeToken<Quaternionf> token = TypeToken.of(Quaternionf.class);

            @Override
            public TypeToken<Quaternionf> getToken() {
                return this.token;
            }

            @Override
            public Optional<Quaternionf> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.X_POS, DataQueries.Y_POS, DataQueries.Z_POS)) {
                    final double x = view.getDouble(DataQueries.X_POS).orElseThrow(invalidDataQuery(DataQueries.X_POS));
                    final double y = view.getDouble(DataQueries.Y_POS).orElseThrow(invalidDataQuery(DataQueries.Y_POS));
                    final double z = view.getDouble(DataQueries.Z_POS).orElseThrow(invalidDataQuery(DataQueries.Z_POS));
                    final double w = view.getDouble(DataQueries.W_POS).orElseThrow(invalidDataQuery(DataQueries.W_POS));
                    return Optional.of(new Quaternionf(x, y, z, w));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Quaternionf obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.X_POS, obj.getX())
                        .set(DataQueries.Y_POS, obj.getY())
                        .set(DataQueries.Z_POS, obj.getZ())
                        .set(DataQueries.W_POS, obj.getW());
            }
        };
        LOCAL_TIME_DATA_SERIALIZER = new DataSerializer<LocalTime>() {
            final TypeToken<LocalTime> token = TypeToken.of(LocalTime.class);

            @Override
            public TypeToken<LocalTime> getToken() {
                return this.token;
            }

            @Override
            public Optional<LocalTime> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.LOCAL_TIME_HOUR, DataQueries.LOCAL_TIME_MINUTE, DataQueries.LOCAL_TIME_NANO, DataQueries.LOCAL_TIME_SECOND)) {
                    final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                    final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                    final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                    final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                    if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                        throw new InvalidDataException("Invalid hour of day: " + hour);
                    }
                    if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                        throw new InvalidDataException("Invalid minute of hour: " + minute);
                    }
                    if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                        throw new InvalidDataException("Invalid second of minute: " + second);
                    }
                    if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                        throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                    }
                    return Optional.of(LocalTime.of(hour, minute, second, nano));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(LocalTime obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                        .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                        .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                        .set(DataQueries.LOCAL_TIME_NANO, obj.getNano());
            }
        };
        LOCAL_DATE_DATA_SERIALIZER = new DataSerializer<LocalDate>() {
            final TypeToken<LocalDate> token = TypeToken.of(LocalDate.class);

            @Override
            public TypeToken<LocalDate> getToken() {
                return this.token;
            }

            @Override
            public Optional<LocalDate> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.LOCAL_DATE_YEAR, DataQueries.LOCAL_DATE_MONTH, DataQueries.LOCAL_DATE_DAY)) {
                    final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                    final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                    final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                    if (!ChronoField.YEAR.range().isValidValue(year)) {
                        throw new InvalidDataException("Invalid year: " + year);
                    }
                    if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                        throw new InvalidDataException("Invalid month of year: " + month);
                    }
                    if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                        throw new InvalidDataException("Invalid day of month: " + day);
                    }
                    return Optional.of(LocalDate.of(year, month, day));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(LocalDate obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                        .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                        .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth());
            }
        };
        LOCAL_DATE_TIME_DATA_SERIALIZER = new DataSerializer<LocalDateTime>() {
            final TypeToken<LocalDateTime> token = TypeToken.of(LocalDateTime.class);

            @Override
            public TypeToken<LocalDateTime> getToken() {
                return this.token;
            }

            @Override
            public Optional<LocalDateTime> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.LOCAL_DATE_YEAR, DataQueries.LOCAL_DATE_MONTH, DataQueries.LOCAL_DATE_DAY, DataQueries.LOCAL_TIME_HOUR,
                        DataQueries.LOCAL_TIME_MINUTE, DataQueries.LOCAL_TIME_NANO, DataQueries.LOCAL_TIME_SECOND)) {
                    final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                    final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                    final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                    final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                    final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                    final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                    final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                    if (!ChronoField.YEAR.range().isValidValue(year)) {
                        throw new InvalidDataException("Invalid year: " + year);
                    }
                    if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                        throw new InvalidDataException("Invalid month of year: " + month);
                    }
                    if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                        throw new InvalidDataException("Invalid day of month: " + day);
                    }
                    if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                        throw new InvalidDataException("Invalid hour of day: " + hour);
                    }
                    if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                        throw new InvalidDataException("Invalid minute of hour: " + minute);
                    }
                    if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                        throw new InvalidDataException("Invalid second of minute: " + second);
                    }
                    if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                        throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                    }
                    return Optional.of(LocalDateTime.of(year, month, day, hour, minute, second, nano));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(LocalDateTime obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                        .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                        .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth())
                        .set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                        .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                        .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                        .set(DataQueries.LOCAL_TIME_NANO, obj.getNano());
            }
        };
        ZONED_DATE_TIME_DATA_SERIALIZER = new DataSerializer<ZonedDateTime>() {
            final TypeToken<ZonedDateTime> token = TypeToken.of(ZonedDateTime.class);

            @Override
            public TypeToken<ZonedDateTime> getToken() {
                return this.token;
            }

            @Override
            public Optional<ZonedDateTime> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.LOCAL_DATE_YEAR, DataQueries.LOCAL_DATE_MONTH, DataQueries.LOCAL_DATE_DAY, DataQueries.LOCAL_TIME_HOUR,
                        DataQueries.LOCAL_TIME_MINUTE, DataQueries.LOCAL_TIME_NANO, DataQueries.LOCAL_TIME_SECOND)) {
                    final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                    final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                    final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                    final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                    final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                    final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                    final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                    final String zoneId = view.getString(DataQueries.ZONE_TIME_ID).orElseThrow(invalidDataQuery(DataQueries.ZONE_TIME_ID));
                    if (!ChronoField.YEAR.range().isValidValue(year)) {
                        throw new InvalidDataException("Invalid year: " + year);
                    }
                    if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                        throw new InvalidDataException("Invalid month of year: " + month);
                    }
                    if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                        throw new InvalidDataException("Invalid day of month: " + day);
                    }
                    if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                        throw new InvalidDataException("Invalid hour of day: " + hour);
                    }
                    if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                        throw new InvalidDataException("Invalid minute of hour: " + minute);
                    }
                    if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                        throw new InvalidDataException("Invalid second of minute: " + second);
                    }
                    if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                        throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                    }
                    if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
                        throw new InvalidDataException("Unrecognized ZoneId: " + zoneId);
                    }
                    return Optional.of(ZonedDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, nano), ZoneId.of(zoneId)));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(ZonedDateTime obj) throws InvalidDataException {
                return new MemoryDataContainer()
                        .set(DataQueries.LOCAL_DATE_YEAR, obj.getYear())
                        .set(DataQueries.LOCAL_DATE_MONTH, obj.getMonth())
                        .set(DataQueries.LOCAL_DATE_DAY, obj.getDayOfMonth())
                        .set(DataQueries.LOCAL_TIME_HOUR, obj.getHour())
                        .set(DataQueries.LOCAL_TIME_MINUTE, obj.getMinute())
                        .set(DataQueries.LOCAL_TIME_SECOND, obj.getSecond())
                        .set(DataQueries.LOCAL_TIME_NANO, obj.getNano())
                        .set(DataQueries.ZONE_TIME_ID, obj.getZone().getId());
            }
        };
        INSTANT_DATA_SERIALIZER = new DataSerializer<Instant>() {
            final TypeToken<Instant> token = TypeToken.of(Instant.class);

            @Override
            public TypeToken<Instant> getToken() {
                return this.token;
            }

            @Override
            public Optional<Instant> deserialize(DataView view) throws InvalidDataException {
                if (view.contains(DataQueries.LOCAL_DATE_YEAR, DataQueries.LOCAL_DATE_MONTH, DataQueries.LOCAL_DATE_DAY, DataQueries.LOCAL_TIME_HOUR,
                        DataQueries.LOCAL_TIME_MINUTE, DataQueries.LOCAL_TIME_NANO, DataQueries.LOCAL_TIME_SECOND)) {
                    final int year = view.getInt(DataQueries.LOCAL_DATE_YEAR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_YEAR));
                    final int month = view.getInt(DataQueries.LOCAL_DATE_MONTH).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_MONTH));
                    final int day = view.getInt(DataQueries.LOCAL_DATE_DAY).orElseThrow(invalidDataQuery(DataQueries.LOCAL_DATE_DAY));
                    final int hour = view.getInt(DataQueries.LOCAL_TIME_HOUR).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_HOUR));
                    final int minute = view.getInt(DataQueries.LOCAL_TIME_MINUTE).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_MINUTE));
                    final int second = view.getInt(DataQueries.LOCAL_TIME_SECOND).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_SECOND));
                    final int nano = view.getInt(DataQueries.LOCAL_TIME_NANO).orElseThrow(invalidDataQuery(DataQueries.LOCAL_TIME_NANO));
                    if (!ChronoField.YEAR.range().isValidValue(year)) {
                        throw new InvalidDataException("Invalid year: " + year);
                    }
                    if (!ChronoField.MONTH_OF_YEAR.range().isValidValue(month)) {
                        throw new InvalidDataException("Invalid month of year: " + month);
                    }
                    if (!ChronoField.DAY_OF_MONTH.range().isValidValue(day)) {
                        throw new InvalidDataException("Invalid day of month: " + day);
                    }
                    if (!ChronoField.HOUR_OF_DAY.range().isValidValue(hour)) {
                        throw new InvalidDataException("Invalid hour of day: " + hour);
                    }
                    if (!ChronoField.MINUTE_OF_HOUR.range().isValidValue(minute)) {
                        throw new InvalidDataException("Invalid minute of hour: " + minute);
                    }
                    if (!ChronoField.SECOND_OF_MINUTE.range().isValidValue(second)) {
                        throw new InvalidDataException("Invalid second of minute: " + second);
                    }
                    if (!ChronoField.NANO_OF_SECOND.range().isValidValue(nano)) {
                        throw new InvalidDataException("Invalid nanosecond of second: " + nano);
                    }
                    return Optional.of(LocalDateTime.of(year, month, day, hour, minute, second, nano).toInstant(ZoneOffset.UTC));
                }
                return Optional.empty();
            }

            @Override
            public DataContainer serialize(Instant obj) throws InvalidDataException {
                final LocalDateTime local = obj.atZone(ZoneOffset.UTC).toLocalDateTime();
                return new MemoryDataContainer()
                        .set(DataQueries.LOCAL_DATE_YEAR, local.getYear())
                        .set(DataQueries.LOCAL_DATE_MONTH, local.getMonth())
                        .set(DataQueries.LOCAL_DATE_DAY, local.getDayOfMonth())
                        .set(DataQueries.LOCAL_TIME_HOUR, local.getHour())
                        .set(DataQueries.LOCAL_TIME_MINUTE, local.getMinute())
                        .set(DataQueries.LOCAL_TIME_SECOND, local.getSecond())
                        .set(DataQueries.LOCAL_TIME_NANO, local.getNano());
            }
        };

    }

    static Supplier<InvalidDataException> invalidDataQuery(DataQuery query) {
        return () -> {
            throw new InvalidDataException("Invalid data located at: " + query.toString());
        };
    }

    public static void registerSerializers(DataManager dataManager) {
        dataManager.registerSerializer(UUID.class, UUID_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector2d.class, VECTOR_2_D_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector2f.class, VECTOR_2_F_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector2i.class, VECTOR_2_I_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector2l.class, VECTOR_2_L_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector3d.class, VECTOR_3_D_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector3f.class, VECTOR_3_F_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector3i.class, VECTOR_3_I_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector3l.class, VECTOR_3_L_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector4d.class, VECTOR_4_D_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector4f.class, VECTOR_4_F_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector4i.class, VECTOR_4_I_DATA_SERIALIZER);
        dataManager.registerSerializer(Vector4l.class, VECTOR_4_L_DATA_SERIALIZER);
        dataManager.registerSerializer(Complexd.class, COMPLEXD_DATA_SERIALIZER);
        dataManager.registerSerializer(Complexf.class, COMPLEXF_DATA_SERIALIZER);
        dataManager.registerSerializer(Quaterniond.class, QUATERNIOND_DATA_SERIALIZER);
        dataManager.registerSerializer(Quaternionf.class, QUATERNIONF_DATA_SERIALIZER);
        dataManager.registerSerializer(LocalTime.class, LOCAL_TIME_DATA_SERIALIZER);
        dataManager.registerSerializer(LocalDate.class, LOCAL_DATE_DATA_SERIALIZER);
        dataManager.registerSerializer(LocalDateTime.class, LOCAL_DATE_TIME_DATA_SERIALIZER);
        dataManager.registerSerializer(ZonedDateTime.class, ZONED_DATE_TIME_DATA_SERIALIZER);
    }

}
