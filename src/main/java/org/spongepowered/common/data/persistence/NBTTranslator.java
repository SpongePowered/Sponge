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

import com.google.common.collect.Lists;
import io.leangen.geantyref.TypeToken;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public final class NBTTranslator implements DataTranslator<CompoundTag> {

    public static final NBTTranslator INSTANCE = new NBTTranslator();

    private static final TypeToken<CompoundTag> TOKEN = TypeToken.get(CompoundTag.class);
    public static final String BOOLEAN_IDENTIFIER = "$Boolean";

    private static CompoundTag containerToCompound(final DataView container) {
        Objects.requireNonNull(container);
        CompoundTag compound = new CompoundTag();
        NBTTranslator.containerToCompound(container, compound);
        return compound;
    }

    private static void containerToCompound(final DataView container, final CompoundTag compound) {
        // We don't need to get deep values since all nested DataViews will be found
        // from the instance of checks.
        Objects.requireNonNull(container);
        Objects.requireNonNull(compound);
        container.streamRootValues().forEach(entry -> {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof DataView) {
                CompoundTag inner = new CompoundTag();
                NBTTranslator.containerToCompound(container.getView(entry.getKey()).get(), inner);
                compound.put(key, inner);
            } else if (value instanceof Boolean) {
                compound.put(key + NBTTranslator.BOOLEAN_IDENTIFIER, ByteTag.valueOf((Boolean) value));
            } else {
                compound.put(key, NBTTranslator.getBaseFromObject(value));
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Tag getBaseFromObject(final Object value) {
        Objects.requireNonNull(value);
        if (value instanceof Boolean) {
            return ByteTag.valueOf((Boolean) value);
        } else if (value instanceof Byte) {
            return ByteTag.valueOf((Byte) value);
        } else if (value instanceof Short) {
            return ShortTag.valueOf((Short) value);
        } else if (value instanceof Integer) {
            return IntTag.valueOf((Integer) value);
        } else if (value instanceof Long) {
            return LongTag.valueOf((Long) value);
        } else if (value instanceof Float) {
            return FloatTag.valueOf((Float) value);
        } else if (value instanceof Double) {
            return DoubleTag.valueOf((Double) value);
        } else if (value instanceof String) {
            return StringTag.valueOf((String) value);
        } else if (value.getClass().isArray()) {
            switch (value) {
                case byte[] bytes -> {
                    return new ByteArrayTag(bytes);
                }
                case Byte[] bytes -> {
                    byte[] array = new byte[bytes.length];
                    int counter = 0;
                    for (Byte data : bytes) {
                        array[counter++] = data;
                    }
                    return new ByteArrayTag(array);
                }
                case int[] ints -> {
                    return new IntArrayTag(ints);
                }
                case Integer[] integers -> {
                    int[] array = new int[integers.length];
                    int counter = 0;
                    for (Integer data : integers) {
                        array[counter++] = data;
                    }
                    return new IntArrayTag(array);
                }
                case long[] longs -> {
                    return new LongArrayTag(longs);
                }
                case Long[] longs -> {
                    long[] array = new long[longs.length];
                    int counter = 0;
                    for (Long data : longs) {
                        array[counter++] = data;
                    }
                    return new LongArrayTag(array);
                }
                default -> {
                }
            }
        } else if (value instanceof List) {
            ListTag list = new ListTag();
            for (Object object : (List) value) {
                // Oh hey, we already have a translation already
                // since DataView only supports some primitive types anyways...
                list.add(NBTTranslator.getBaseFromObject(object));
            }
            return list;
        } else if (value instanceof Map) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if (entry.getKey() instanceof DataQuery) {
                    if (entry.getValue() instanceof Boolean) {
                        compound.putBoolean(((DataQuery) entry.getKey()).asString('.') + NBTTranslator.BOOLEAN_IDENTIFIER, (Boolean) entry.getValue());
                    } else {
                        compound.put(((DataQuery) entry.getKey()).asString('.'), NBTTranslator.getBaseFromObject(entry.getValue()));
                    }
                } else if (entry.getKey() instanceof String) {
                    compound.put((String) entry.getKey(), NBTTranslator.getBaseFromObject(entry.getValue()));
                } else {
                    compound.put(entry.getKey().toString(), NBTTranslator.getBaseFromObject(entry.getValue()));
                }
            }
            return compound;
        } else if (value instanceof DataSerializable) {
            return NBTTranslator.containerToCompound(((DataSerializable) value).toContainer());
        } else if (value instanceof DataView) {
            return NBTTranslator.containerToCompound((DataView) value);
        }
        throw new IllegalArgumentException("Unable to translate object to NBTBase: " + value);
    }

    private static DataContainer getViewFromCompound(CompoundTag compound) {
        Objects.requireNonNull(compound);
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        NBTTranslator.INSTANCE.addTo(compound, container);
        return container;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setInternal(Tag base, DataView view, String key) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(view, "view");
        Objects.requireNonNull(key, "key");
        switch (base) {
            case ByteTag bt:
                if (key.contains(NBTTranslator.BOOLEAN_IDENTIFIER)) {
                    view.set(key.replace(NBTTranslator.BOOLEAN_IDENTIFIER, ""), ((bt.byteValue() != 0)));
                } else {
                    view.set(key, bt.byteValue());
                }
                break;
            case ShortTag st:
                view.set(key, st.shortValue());
                break;
            case IntTag it:
                view.set(key, it.intValue());
                break;
            case LongTag lt:
                view.set(key, lt.longValue());
                break;
            case FloatTag ft:
                view.set(key, ft.floatValue());
                break;
            case DoubleTag dt:
                view.set(key, dt.doubleValue());
                break;
            case ByteArrayTag bat:
                view.set(key, bat.getAsByteArray());
                break;
            case StringTag st:
                view.set(key, st.value());
                break;
            case ListTag lt:
                ListTag list = (ListTag) base;
                int count = list.size();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (final Tag inbt : list) {
                    objectList.add(NBTTranslator.fromTagBase(inbt));
                }
                view.set(key, objectList);
                break;
            case CompoundTag ct:
                DataView internalView = view.createView(key);
                for (String internalKey : ct.keySet()) {
                    @Nullable Tag internalBase = ct.get(internalKey);
                    if (internalBase == null) {
                        continue;
                    }
                    // Basically.... more recursion.
                    // Reasoning: This avoids creating a new DataContainer which would
                    // then be copied in to the owning DataView anyways. We can internally
                    // set the actual data directly to the child view instead.
                    NBTTranslator.setInternal(internalBase, internalView, internalKey);
                }
                break;
            case IntArrayTag iat:
                view.set(key, iat.getAsIntArray());
                break;
            case LongArrayTag lat:
                view.set(key, lat.getAsLongArray());
                break;
            default:
                throw new IllegalArgumentException("Unknown NBT type " + base.getClass().getName());
        }
    }

    private static Object fromTagBase(final Tag base) {
        return NBTTranslator.fromTagBase(base, null, (k, c) -> NBTTranslator.getViewFromCompound(c));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Object fromTagBase(final Tag base, final @Nullable String key, final BiFunction<@Nullable String, CompoundTag, Object> compoundFunction) {
        switch (base) {
            case ByteTag bt:
                return bt.byteValue();
            case ShortTag st:
                return st.shortValue();
            case IntTag it:
                return it.intValue();
            case LongTag lt:
                return lt.longValue();
            case FloatTag ft:
                return ft.floatValue();
            case DoubleTag dt:
                return dt.doubleValue();
            case ByteArrayTag bat:
                return bat.getAsByteArray();
            case StringTag st:
                return st.value();
            case ListTag lt:
                int count = lt.size();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (Tag inbt : lt) {
                    objectList.add(NBTTranslator.fromTagBase(inbt, null, compoundFunction));
                }
                return objectList;
            case CompoundTag ct:
                return compoundFunction.apply(key, ct);
            case IntArrayTag iat:
                return iat.getAsIntArray();
            case LongArrayTag lat:
                return lat.getAsLongArray();
            default:
                return null;
        }
    }

    public void translateContainerToData(CompoundTag node, DataView container) {
        NBTTranslator.containerToCompound(container, node);
    }

    public DataContainer translateFrom(CompoundTag node) {
        return NBTTranslator.getViewFromCompound(node);
    }

    @Override
    public TypeToken<CompoundTag> token() {
        return NBTTranslator.TOKEN;
    }

    @Override
    public CompoundTag translate(DataView view) throws InvalidDataException {
        return NBTTranslator.containerToCompound(view);
    }

    @Override
    public DataContainer translate(CompoundTag obj) throws InvalidDataException {
        return NBTTranslator.getViewFromCompound(obj);
    }

    @Override
    public DataView addTo(CompoundTag compound, DataView container) {
        for (String key : compound.keySet()) {
            Tag base = compound.get(key);
            byte type = base.getId();
            NBTTranslator.setInternal(base, container, key); // gotta love recursion
        }
        return container;
    }
}
