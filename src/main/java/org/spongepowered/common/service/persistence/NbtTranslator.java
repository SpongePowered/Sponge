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
package org.spongepowered.common.service.persistence;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.translator.DataTranslator;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NbtTranslator implements DataTranslator<NBTTagCompound> {

    private static final NbtTranslator instance = new NbtTranslator();

    public static NbtTranslator getInstance() {
        return instance;
    }

    private NbtTranslator() { } // #NOPE

    private static NBTTagCompound containerToCompound(final DataView container) {
        checkNotNull(container);
        NBTTagCompound compound = new NBTTagCompound();
        containerToCompound(container, compound);
        return compound;
    }

    private static void containerToCompound(final DataView container, final NBTTagCompound compound) {
        // We don't need to get deep values since all nested DataViews will be found
        // from the instance of checks.
        checkNotNull(container);
        checkNotNull(compound);
        for (Map.Entry<DataQuery, Object> entry : container.getValues(false).entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey().asString('.');
            if (value instanceof DataView) {
                NBTTagCompound inner = new NBTTagCompound();
                containerToCompound(container.getView(entry.getKey()).get(), inner);
                compound.setTag(key, inner);
            } else {
                compound.setTag(key, getBaseFromObject(value));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static NBTBase getBaseFromObject(Object value) {
        checkNotNull(value);
        if (value instanceof Byte) {
            return new NBTTagByte((Byte) value);
        } else if (value instanceof Short) {
            return new NBTTagShort((Short) value);
        } else if (value instanceof Integer) {
            return new NBTTagInt((Integer) value);
        } else if (value instanceof Long) {
            return new NBTTagLong((Long) value);
        } else if (value instanceof Float) {
            return new NBTTagFloat((Float) value);
        } else if (value instanceof Double) {
            return new NBTTagDouble((Double) value);
        } else if (value instanceof String) {
            return new NBTTagString((String) value);
        } else if (value instanceof Byte[]) {
            byte[] array = new byte[((Byte[]) value).length];
            int counter = 0;
            for (Byte data : (Byte[]) value) {
                array[counter++] = data;
            }
            return new NBTTagByteArray(array);
        } else if (value instanceof Integer[]) {
            int[] array = new int[((Integer[]) value).length];
            int counter = 0;
            for (Integer data : (Integer[]) value) {
                array[counter++] = data;
            }
            return new NBTTagIntArray(array);
        } else if (value instanceof List) {
            NBTTagList list = new NBTTagList();
            for (Object object : (List) value) {
                // Oh hey, we already have a translation already
                // since DataView only supports some primitive types anyways...
                list.appendTag(getBaseFromObject(object));
            }
            return list;
        } else if (value instanceof Map) {
            NBTTagCompound compound = new NBTTagCompound();
            for (Map.Entry<DataQuery, Object> entry : ((Map<DataQuery, Object>) value).entrySet()) {
                compound.setTag(entry.getKey().asString('.'), getBaseFromObject(entry.getValue()));
            }
            return compound;
        } else if (value instanceof DataSerializable) {
            return containerToCompound(((DataSerializable) value).toContainer());
        } else if (value instanceof DataView) {
            return containerToCompound((DataView) value);
        }
        throw new IllegalArgumentException("Unable to translate object to NBTBase!");
    }

    @SuppressWarnings("unchecked")
    private static DataContainer getViewFromCompound(NBTTagCompound compound) {
        checkNotNull(compound);
        DataContainer container = new MemoryDataContainer();
        for (String key : (Set<String>) compound.getKeySet()) {
            NBTBase base = compound.getTag(key);
            byte type = base.getId();
            setInternal(base, type, container, key); // gotta love recursion
        }
        return container;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void setInternal(NBTBase base, byte type, DataView view, String key) {
        checkNotNull(base);
        checkNotNull(view);
        checkNotNull(key);
        checkArgument(!key.isEmpty());
        checkArgument(type > NbtDataUtil.TAG_END && type <= NbtDataUtil.TAG_INT_ARRAY);
        switch (type) {
            case NbtDataUtil.TAG_BYTE:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getByte());
                break;
            case NbtDataUtil.TAG_SHORT:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getShort());
                break;
            case NbtDataUtil.TAG_INT:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getInt());
                break;
            case NbtDataUtil.TAG_LONG:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getLong());
                break;
            case NbtDataUtil.TAG_FLOAT:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getFloat());
                break;
            case NbtDataUtil.TAG_DOUBLE:
                view.set(of('.', key), ((NBTBase.NBTPrimitive) base).getDouble());
                break;
            case NbtDataUtil.TAG_BYTE_ARRAY:
                view.set(of('.', key), ((NBTTagByteArray) base).getByteArray());
                break;
            case NbtDataUtil.TAG_STRING:
                view.set(of('.', key), ((NBTTagString) base).getString());
                break;
            case NbtDataUtil.TAG_LIST:
                NBTTagList list = (NBTTagList) base;
                byte listType = (byte) list.getTagType();
                int count = list.tagCount();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (int i = 0; i < count; i++) {
                    objectList.add(fromTagBase(list.get(i), listType));
                }
                view.set(of('.', key), objectList);
                break;
            case NbtDataUtil.TAG_COMPOUND:
                DataView internalView = view.createView(of('.', key));
                NBTTagCompound compound = (NBTTagCompound) base;
                for (String internalKey : (Set<String>) compound.getKeySet()) {
                    NBTBase internalBase = compound.getTag(internalKey);
                    byte internalType = internalBase.getId();
                    // Basically.... more recursion.
                    // Reasoning: This avoids creating a new DataContainer which would
                    // then be copied in to the owning DataView anyways. We can internally
                    // set the actual data directly to the child view instead.
                    setInternal(internalBase, internalType, internalView, internalKey);
                }
                break;
            case NbtDataUtil.TAG_INT_ARRAY:
                view.set(of('.', key), ((NBTTagIntArray) base).getIntArray());
                break;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object fromTagBase(NBTBase base, byte type) {
        switch (type) {
            case NbtDataUtil.TAG_BYTE:
                return ((NBTBase.NBTPrimitive) base).getByte();
            case NbtDataUtil.TAG_SHORT:
                return (((NBTBase.NBTPrimitive) base)).getShort();
            case NbtDataUtil.TAG_INT:
                return ((NBTBase.NBTPrimitive) base).getInt();
            case NbtDataUtil.TAG_LONG:
                return ((NBTBase.NBTPrimitive) base).getLong();
            case NbtDataUtil.TAG_FLOAT:
                return ((NBTBase.NBTPrimitive) base).getFloat();
            case NbtDataUtil.TAG_DOUBLE:
                return ((NBTBase.NBTPrimitive) base).getDouble();
            case NbtDataUtil.TAG_BYTE_ARRAY:
                return ((NBTTagByteArray) base).getByteArray();
            case NbtDataUtil.TAG_STRING:
                return ((NBTTagString) base).getString();
            case NbtDataUtil.TAG_LIST:
                NBTTagList list = (NBTTagList) base;
                byte listType = (byte) list.getTagType();
                int count = list.tagCount();
                List objectList = Lists.newArrayListWithCapacity(count);
                for (int i = 0; i < list.tagCount(); i++) {
                    objectList.add(fromTagBase(list.get(i), listType));
                }
                return objectList;
            case NbtDataUtil.TAG_COMPOUND:
                return getViewFromCompound((NBTTagCompound) base);
            case NbtDataUtil.TAG_INT_ARRAY:
                return ((NBTTagIntArray) base).getIntArray();
            default :
                return null;
        }
    }

    @Override
    public NBTTagCompound translateData(DataView container) {
        return NbtTranslator.containerToCompound(container);
    }

    @Override
    public void translateContainerToData(NBTTagCompound node, DataView container) {
        NbtTranslator.containerToCompound(container, node);
    }

    @Override
    public DataContainer translateFrom(NBTTagCompound node) {
        return NbtTranslator.getViewFromCompound(node);
    }
}
