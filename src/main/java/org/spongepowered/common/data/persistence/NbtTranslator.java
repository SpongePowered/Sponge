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


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.ShortNBT;
import net.minecraft.nbt.StringNBT;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.mixin.core.nbt.NBTTagLongArrayAccessor;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Map;

public final class NbtTranslator implements DataTranslator<CompoundNBT> {

    private static final NbtTranslator instance = new NbtTranslator();
    private static final TypeToken<CompoundNBT> TOKEN = TypeToken.of(CompoundNBT.class);
    public static final String BOOLEAN_IDENTIFIER = "$Boolean";

    public static NbtTranslator getInstance() {
        return instance;
    }

    private NbtTranslator() { } // #NOPE

    private static CompoundNBT containerToCompound(final DataView container) {
        checkNotNull(container);
        CompoundNBT compound = new CompoundNBT();
        containerToCompound(container, compound);
        return compound;
    }

    private static void containerToCompound(final DataView container, final CompoundNBT compound) {
        // We don't need to get deep values since all nested DataViews will be found
        // from the instance of checks.
        checkNotNull(container);
        checkNotNull(compound);
        for (Map.Entry<DataQuery, Object> entry : container.getValues(false).entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey().asString('.');
            if (value instanceof DataView) {
                CompoundNBT inner = new CompoundNBT();
                containerToCompound(container.getView(entry.getKey()).get(), inner);
                compound.func_74782_a(key, inner);
            } else if (value instanceof Boolean) {
                compound.func_74782_a(key + BOOLEAN_IDENTIFIER, new ByteNBT(((Boolean) value) ? (byte) 1 : 0));
            } else {
                compound.func_74782_a(key, getBaseFromObject(value));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static NBTBase getBaseFromObject(Object value) {
        checkNotNull(value);
        if (value instanceof Boolean) {
            return new ByteNBT((Boolean) value ? (byte) 1 : 0);
        } else if (value instanceof Byte) {
            return new ByteNBT((Byte) value);
        } else if (value instanceof Short) {
            return new ShortNBT((Short) value);
        } else if (value instanceof Integer) {
            return new IntNBT((Integer) value);
        } else if (value instanceof Long) {
            return new LongNBT((Long) value);
        } else if (value instanceof Float) {
            return new FloatNBT((Float) value);
        } else if (value instanceof Double) {
            return new DoubleNBT((Double) value);
        } else if (value instanceof String) {
            return new StringNBT((String) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof byte[]) {
                return new ByteArrayNBT((byte[]) value);
            } else if (value instanceof Byte[]) {
                byte[] array = new byte[((Byte[]) value).length];
                int counter = 0;
                for (Byte data : (Byte[]) value) {
                    array[counter++] = data;
                }
                return new ByteArrayNBT(array);
            } else if (value instanceof int[]) {
                return new IntArrayNBT((int[]) value);
            } else if (value instanceof Integer[]) {
                int[] array = new int[((Integer[]) value).length];
                int counter = 0;
                for (Integer data : (Integer[]) value) {
                    array[counter++] = data;
                }
                return new IntArrayNBT(array);
            } else if (value instanceof long[]) {
                return new LongArrayNBT((long[]) value);
            } else if (value instanceof Long[]) {
                long[] array = new long[((Long[]) value).length];
                int counter = 0;
                for (Long data : (Long[]) value) {
                    array[counter++] = data;
                }
                return new LongArrayNBT(array);
            }
        } else if (value instanceof List) {
            ListNBT list = new ListNBT();
            for (Object object : (List) value) {
                // Oh hey, we already have a translation already
                // since DataView only supports some primitive types anyways...
                list.func_74742_a(getBaseFromObject(object));
            }
            return list;
        } else if (value instanceof Map) {
            CompoundNBT compound = new CompoundNBT();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if (entry.getKey() instanceof DataQuery) {
                    if (entry.getValue() instanceof Boolean) {
                        compound.func_74757_a(((DataQuery) entry.getKey()).asString('.') + BOOLEAN_IDENTIFIER, (Boolean) entry.getValue());
                    } else {
                        compound.func_74782_a(((DataQuery) entry.getKey()).asString('.'), getBaseFromObject(entry.getValue()));
                    }
                } else if (entry.getKey() instanceof String) {
                    compound.func_74782_a((String) entry.getKey(), getBaseFromObject(entry.getValue()));
                } else {
                    compound.func_74782_a(entry.getKey().toString(), getBaseFromObject(entry.getValue()));
                }
            }
            return compound;
        } else if (value instanceof DataSerializable) {
            return containerToCompound(((DataSerializable) value).toContainer());
        } else if (value instanceof DataView) {
            return containerToCompound((DataView) value);
        }
        throw new IllegalArgumentException("Unable to translate object to NBTBase: " + value);
    }

    private static DataContainer getViewFromCompound(CompoundNBT compound) {
        checkNotNull(compound);
        DataContainer container = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        NbtTranslator.getInstance().addTo(compound, container);
        return container;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setInternal(NBTBase base, byte type, DataView view, String key) {
        checkNotNull(base);
        checkNotNull(view);
        checkNotNull(key);
        checkArgument(!key.isEmpty());
        checkArgument(type > Constants.NBT.TAG_END && type <= Constants.NBT.TAG_INT_ARRAY);
        switch (type) {
            case Constants.NBT.TAG_BYTE:
                if (key.contains(BOOLEAN_IDENTIFIER)) {
                    view.set(of(key.replace(BOOLEAN_IDENTIFIER, "")), (((ByteNBT) base).func_150290_f() != 0));
                } else {
                    view.set(of(key), ((ByteNBT) base).func_150290_f());
                }
                break;
            case Constants.NBT.TAG_SHORT:
                view.set(of(key), ((ShortNBT) base).func_150289_e());
                break;
            case Constants.NBT.TAG_INT:
                view.set(of(key), ((IntNBT) base).func_150287_d());
                break;
            case Constants.NBT.TAG_LONG:
                view.set(of(key), ((LongNBT) base).func_150291_c());
                break;
            case Constants.NBT.TAG_FLOAT:
                view.set(of(key), ((FloatNBT) base).func_150288_h());
                break;
            case Constants.NBT.TAG_DOUBLE:
                view.set(of(key), ((DoubleNBT) base).func_150286_g());
                break;
            case Constants.NBT.TAG_BYTE_ARRAY:
                view.set(of(key), ((ByteArrayNBT) base).func_150292_c());
                break;
            case Constants.NBT.TAG_STRING:
                view.set(of(key), ((StringNBT) base).func_150285_a_());
                break;
            case Constants.NBT.TAG_LIST:
                ListNBT list = (ListNBT) base;
                byte listType = (byte) list.func_150303_d();
                int count = list.func_74745_c();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (int i = 0; i < count; i++) {
                    objectList.add(fromTagBase(list.func_179238_g(i), listType));
                }
                view.set(of(key), objectList);
                break;
            case Constants.NBT.TAG_COMPOUND:
                DataView internalView = view.createView(of(key));
                CompoundNBT compound = (CompoundNBT) base;
                for (String internalKey : compound.func_150296_c()) {
                    NBTBase internalBase = compound.func_74781_a(internalKey);
                    byte internalType = internalBase.func_74732_a();
                    // Basically.... more recursion.
                    // Reasoning: This avoids creating a new DataContainer which would
                    // then be copied in to the owning DataView anyways. We can internally
                    // set the actual data directly to the child view instead.
                    setInternal(internalBase, internalType, internalView, internalKey);
                }
                break;
            case Constants.NBT.TAG_INT_ARRAY:
                view.set(of(key), ((IntArrayNBT) base).func_150302_c());
                break;
            case Constants.NBT.TAG_LONG_ARRAY:
                view.set(of(key), ((NBTTagLongArrayAccessor) base).accessor$getLongArray());
                break;
            default:
                throw new IllegalArgumentException("Unknown NBT type " + type);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromTagBase(NBTBase base, byte type) {
        switch (type) {
            case Constants.NBT.TAG_BYTE:
                return ((ByteNBT) base).func_150290_f();
            case Constants.NBT.TAG_SHORT:
                return (((ShortNBT) base)).func_150289_e();
            case Constants.NBT.TAG_INT:
                return ((IntNBT) base).func_150287_d();
            case Constants.NBT.TAG_LONG:
                return ((LongNBT) base).func_150291_c();
            case Constants.NBT.TAG_FLOAT:
                return ((FloatNBT) base).func_150288_h();
            case Constants.NBT.TAG_DOUBLE:
                return ((DoubleNBT) base).func_150286_g();
            case Constants.NBT.TAG_BYTE_ARRAY:
                return ((ByteArrayNBT) base).func_150292_c();
            case Constants.NBT.TAG_STRING:
                return ((StringNBT) base).func_150285_a_();
            case Constants.NBT.TAG_LIST:
                ListNBT list = (ListNBT) base;
                byte listType = (byte) list.func_150303_d();
                int count = list.func_74745_c();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (int i = 0; i < list.func_74745_c(); i++) {
                    objectList.add(fromTagBase(list.func_179238_g(i), listType));
                }
                return objectList;
            case Constants.NBT.TAG_COMPOUND:
                return getViewFromCompound((CompoundNBT) base);
            case Constants.NBT.TAG_INT_ARRAY:
                return ((IntArrayNBT) base).func_150302_c();
            case Constants.NBT.TAG_LONG_ARRAY:
                return ((NBTTagLongArrayAccessor) base).accessor$getLongArray();
            default :
                return null;
        }
    }

    public CompoundNBT translateData(DataView container) {
        return NbtTranslator.containerToCompound(container);
    }

    public void translateContainerToData(CompoundNBT node, DataView container) {
        NbtTranslator.containerToCompound(container, node);
    }

    public DataContainer translateFrom(CompoundNBT node) {
        return NbtTranslator.getViewFromCompound(node);
    }

    @Override
    public TypeToken<CompoundNBT> getToken() {
        return TOKEN;
    }

    @Override
    public CompoundNBT translate(DataView view) throws InvalidDataException {
        return containerToCompound(view);
    }

    @Override
    public DataContainer translate(CompoundNBT obj) throws InvalidDataException {
        return getViewFromCompound(obj);
    }

    @Override
    public DataView addTo(CompoundNBT compound, DataView container) {
        for (String key : compound.func_150296_c()) {
            NBTBase base = compound.func_74781_a(key);
            byte type = base.func_74732_a();
            setInternal(base, type, container, key); // gotta love recursion
        }
        return container;
    }

    @Override
    public String getId() {
        return "sponge:nbt";
    }

    @Override
    public String getName() {
        return "NbtTagCompoundTranslator";
    }
}
