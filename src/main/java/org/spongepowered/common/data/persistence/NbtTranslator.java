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
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;
import org.spongepowered.common.data.MemoryDataContainer;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.nbt.IMixinNBTTagLongArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * A translator to convert between {@link DataView}s and {@link NBTTagCompound}s. This
 * translator also introduces extended nbt data tag support. This translator should remain
 * compatible with the following library, which also introduces these changes:
 * <a href="https://github.com/LanternPowered/LanternNBT">LanternNBT</a>
 */
public final class NbtTranslator implements DataTranslator<NBTTagCompound> {

    private static final NbtTranslator instance = new NbtTranslator();
    private static final TypeToken<NBTTagCompound> token = TypeToken.of(NBTTagCompound.class);

    public static NbtTranslator getInstance() {
        return instance;
    }

    enum NbtType {
        // Official types
        END                     (NbtDataUtil.TAG_END),
        BYTE                    (NbtDataUtil.TAG_BYTE),
        SHORT                   (NbtDataUtil.TAG_SHORT),
        INT                     (NbtDataUtil.TAG_INT),
        LONG                    (NbtDataUtil.TAG_LONG),
        FLOAT                   (NbtDataUtil.TAG_FLOAT),
        DOUBLE                  (NbtDataUtil.TAG_DOUBLE),
        BYTE_ARRAY              (NbtDataUtil.TAG_BYTE_ARRAY),
        STRING                  (NbtDataUtil.TAG_STRING),
        LIST                    (NbtDataUtil.TAG_LIST),
        COMPOUND                (NbtDataUtil.TAG_COMPOUND),
        INT_ARRAY               (NbtDataUtil.TAG_INT_ARRAY),
        LONG_ARRAY              (NbtDataUtil.TAG_LONG_ARRAY),

        // Sponge and lantern types, but remaining
        // compatible with the official ones.
        BOOLEAN                 (NbtDataUtil.TAG_BYTE, "Boolean"), // Boolean was used before, so still uppercase
        BOOLEAN_ARRAY           (NbtDataUtil.TAG_BYTE_ARRAY, "boolean[]"),
        SHORT_ARRAY             (NbtDataUtil.TAG_LIST, "short[]"),
        FLOAT_ARRAY             (NbtDataUtil.TAG_LIST, "float[]"),
        DOUBLE_ARRAY            (NbtDataUtil.TAG_LIST, "double[]"),
        STRING_ARRAY            (NbtDataUtil.TAG_LIST, "string[]"),
        CHAR                    (NbtDataUtil.TAG_STRING, "char"),
        CHAR_ARRAY              (NbtDataUtil.TAG_STRING, "char[]"),
        COMPOUND_ARRAY          (NbtDataUtil.TAG_LIST, "compound[]"),

        UNKNOWN                 (99),
        ;

        static final Map<String, NbtType> bySuffix = new HashMap<>();
        static final Int2ObjectMap<NbtType> byIndex = new Int2ObjectOpenHashMap<>();

        final int type;
        @Nullable final String suffix;

        NbtType(int type) {
            this(type, null);
        }

        NbtType(int type, @Nullable String suffix) {
            this.suffix = suffix;
            this.type = type;
        }

        static {
            for (NbtType nbtType : values()) {
                bySuffix.put(nbtType.suffix, nbtType);
                if (nbtType.suffix == null) {
                    byIndex.put(nbtType.type, nbtType);
                }
            }
        }
    }

    private static NBTBase toTag(NbtType nbtType, Object object) {
        NBTTagList tagList;
        switch (nbtType) {
            case BYTE:
                return new NBTTagByte((Byte) object);
            case BYTE_ARRAY:
                return new NBTTagByteArray((byte[]) object);
            case SHORT:
                return new NBTTagShort((Short) object);
            case SHORT_ARRAY:
                final short[] shortArray = (short[]) object;
                tagList = new NBTTagList();
                for (short shortValue : shortArray) {
                    tagList.appendTag(new NBTTagShort(shortValue));
                }
                return tagList;
            case CHAR:
                return new NBTTagString(object.toString());
            case CHAR_ARRAY:
                return new NBTTagString(new String((char[]) object));
            case INT:
                return new NBTTagInt((Integer) object);
            case INT_ARRAY:
                return new NBTTagIntArray((int[]) object);
            case LONG:
                return new NBTTagLong((Long) object);
            case LONG_ARRAY:
                return new NBTTagLongArray((long[]) object);
            case FLOAT:
                return new NBTTagFloat((Float) object);
            case FLOAT_ARRAY:
                final float[] floatArray = (float[]) object;
                tagList = new NBTTagList();
                for (float floatValue : floatArray) {
                    tagList.appendTag(new NBTTagFloat(floatValue));
                }
                return tagList;
            case DOUBLE:
                return new NBTTagDouble((Double) object);
            case DOUBLE_ARRAY:
                final double[] doubleArray = (double[]) object;
                tagList = new NBTTagList();
                for (double doubleValue : doubleArray) {
                    tagList.appendTag(new NBTTagDouble(doubleValue));
                }
                return tagList;
            case STRING:
                return new NBTTagString((String) object);
            case STRING_ARRAY:
                final String[] stringArray = (String[]) object;
                tagList = new NBTTagList();
                for (String string : stringArray) {
                    tagList.appendTag(new NBTTagString(string));
                }
                return tagList;
            case BOOLEAN:
                return new NBTTagByte((byte) ((Boolean) object ? 1 : 0));
            case BOOLEAN_ARRAY:
                final boolean[] booleanArray = (boolean[]) object;
                int length = booleanArray.length / 8;
                if (booleanArray.length % 8 != 0) {
                    length++;
                }
                final int offset = 2; // 2 bytes for the amount of bits
                final byte[] bytes = new byte[length + offset];
                bytes[0] = (byte) (length >> 8);
                bytes[1] = (byte) (length & 0xff);
                int j = 0;
                for (int i = 0; i < length; i++) {
                    byte value = 0;
                    while (j < booleanArray.length) {
                        final int k = j % 8;
                        if (booleanArray[j++]) {
                            value |= 1 << k;
                        }
                    }
                    bytes[i + offset] = value;
                }
                return new NBTTagByteArray(bytes);
            case LIST:
                return toListTag(nbtType, (List<Object>) object);
            case COMPOUND:
                return toCompoundTag(object);
            case COMPOUND_ARRAY:
                final Object[] dataViews = (Object[]) object;
                tagList = new NBTTagList();
                for (Object dataView : dataViews) {
                    tagList.appendTag(toCompoundTag(dataView));
                }
                return tagList;
            default:
                throw new IllegalStateException("Attempted to serialize a unsupported object type: " + object.getClass().getName());
        }
    }

    private static NBTTagCompound toCompoundTag(Object object) {
        final NBTTagCompound tagCompound = new NBTTagCompound();
        // Convert the object in something we can serialize
        if (object instanceof DataView) {
            object = ((DataView) object).getValues(false);
        } else if (object instanceof DataSerializable) {
            object = ((DataSerializable) object).toContainer().getValues(false);
        }
        for (Map.Entry<DataQuery, Object> entry : ((Map<DataQuery, Object>) object).entrySet()) {
            // The base path
            String key = entry.getKey().last().toString();
            // The base nbt type
            NbtType nbtType = typeFor(object);
            // The nbt tag that will be put in the compound
            final NBTBase nbtBase;
            if (nbtType == NbtType.LIST) {
                final List<Object> list = (List<Object>) object;
                if (list.isEmpty()) {
                    nbtType = NbtType.END;
                } else {
                    nbtType = typeFor(list.get(0));
                    if (nbtType.suffix != null) {
                        key += "$List$" + nbtType.suffix;
                    }
                }
                nbtBase = toListTag(nbtType, list);
            } else {
                if (nbtType.suffix != null) {
                    key += '$' + nbtType.suffix;
                }
                try {
                    nbtBase = toTag(nbtType, object);
                } catch (Exception e) {
                    throw new IllegalStateException("Exception while serializing key: " + key, e);
                }
            }
            tagCompound.setTag(key, nbtBase);
        }
        return tagCompound;
    }

    private static NBTTagList toListTag(NbtType elementNbtType, List<Object> list) {
        final NBTTagList tagList = new NBTTagList();
        for (Object object : list) {
            tagList.appendTag(toTag(elementNbtType, object));
        }
        return tagList;
    }

    private static NbtType typeFor(Object object) {
        if (object instanceof Boolean) {
            return NbtType.BOOLEAN;
        } else if (object instanceof boolean[]) {
            return NbtType.BOOLEAN_ARRAY;
        } else if (object instanceof Byte) {
            return NbtType.BYTE;
        } else if (object instanceof byte[]) {
            return NbtType.BYTE_ARRAY;
        } else if (object instanceof Map ||
                object instanceof DataView ||
                object instanceof DataSerializable) {
            return NbtType.COMPOUND;
        } else if (object instanceof Double) {
            return NbtType.DOUBLE;
        } else if (object instanceof double[]) {
            return NbtType.DOUBLE_ARRAY;
        } else if (object instanceof Float) {
            return NbtType.FLOAT;
        } else if (object instanceof float[]) {
            return NbtType.FLOAT_ARRAY;
        } else if (object instanceof Integer) {
            return NbtType.INT;
        } else if (object instanceof int[]) {
            return NbtType.INT_ARRAY;
        } else if (object instanceof List) {
            return NbtType.LIST;
        } else if (object instanceof Long) {
            return NbtType.LONG;
        } else if (object instanceof long[]) {
            return NbtType.LONG_ARRAY;
        } else if (object instanceof Short) {
            return NbtType.SHORT;
        } else if (object instanceof short[]) {
            return NbtType.SHORT_ARRAY;
        } else if (object instanceof String) {
            return NbtType.STRING;
        } else if (object instanceof String[]) {
            return NbtType.STRING_ARRAY;
        } else if (object instanceof Character) {
            return NbtType.CHAR;
        } else if (object instanceof char[]) {
            return NbtType.CHAR_ARRAY;
        } else if (object.getClass().isArray() &&
                DataView.class.isAssignableFrom(object.getClass().getComponentType())) {
            return NbtType.COMPOUND_ARRAY;
        }
        return NbtType.UNKNOWN;
    }

    private static NbtEntry tagAndNameToEntry(String name, NBTBase tag) {
        final byte type = tag.getId();
        if (type == NbtType.END.type) {
            throw new IllegalStateException("Did not expect a END tag.");
        }
        int index = name.lastIndexOf('$');
        NbtType nbtType = NbtType.byIndex.get(type);
        if (nbtType == null) {
            throw new IllegalStateException("Unknown NBT Type with id: " + type);
        }
        NbtType listNbtType = null;
        if (index != -1) {
            final String suffix = name.substring(index + 1);
            name = name.substring(0, index);
            final NbtType nbtType1 = NbtType.bySuffix.get(suffix);
            if (nbtType1 != null) {
                if (nbtType == NbtType.LIST) {
                    index = name.lastIndexOf('$');
                    if (index != -1) {
                        final String li = name.substring(index + 1);
                        if (li.equals("List")) {
                            name = name.substring(0, index);
                            listNbtType = nbtType1;
                        }
                    }
                }
                if (listNbtType == null) {
                    nbtType = nbtType1;
                }
            }
        }
        return new NbtEntry(name, nbtType, listNbtType, tag);
    }

    private static Object fromTag(NBTBase tag) throws InvalidDataFormatException {
        return fromTag(null, NbtType.byIndex.get(tag.getId()), null, tag);
    }

    private static Object fromEntry(@Nullable DataView container, NbtEntry entry) throws InvalidDataFormatException {
        return fromTag(container, entry.type, entry.listType, entry.tag);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object fromTag(@Nullable DataView container, NbtType nbtType, @Nullable NbtType listNbtType, NBTBase tag)
            throws InvalidDataFormatException {
        NBTTagList tagList;
        switch (nbtType) {
            case BYTE:
                return ((NBTTagByte) tag).getByte();
            case BYTE_ARRAY:
                return ((NBTTagByteArray) tag).getByteArray();
            case SHORT:
                return ((NBTTagShort) tag).getShort();
            case SHORT_ARRAY:
                tagList = (NBTTagList) tag;
                if (tagList.getTagType() == NbtType.END.type) {
                    return new short[tagList.tagCount()];
                } else if (tagList.getTagType() != NbtType.SHORT.type) {
                    throw new IllegalStateException("Attempted to deserialize a Short Array (List) but the list type wasn't a short.");
                }
                final short[] shortArray = new short[tagList.tagCount()];
                for (int i = 0; i < shortArray.length; i++) {
                    shortArray[i] = ((NBTTagShort) tagList.get(i)).getShort();
                }
                return shortArray;
            case CHAR:
                return ((NBTTagString) tag).getString().charAt(0);
            case CHAR_ARRAY:
                return ((NBTTagString) tag).getString().toCharArray();
            case INT:
                return ((NBTTagInt) tag).getInt();
            case INT_ARRAY:
                return ((NBTTagIntArray) tag).getIntArray();
            case LONG:
                return ((NBTTagLong) tag).getLong();
            case LONG_ARRAY:
                return ((IMixinNBTTagLongArray) tag).getLongArray();
            case FLOAT:
                return ((NBTTagFloat) tag).getFloat();
            case FLOAT_ARRAY:
                tagList = (NBTTagList) tag;
                if (tagList.getTagType() == NbtType.END.type) {
                    return new float[tagList.tagCount()];
                } else if (tagList.getTagType() != NbtType.FLOAT.type) {
                    throw new IllegalStateException("Attempted to deserialize a Float Array (List) but the list type wasn't a float.");
                }
                final float[] floatArray = new float[tagList.tagCount()];
                for (int i = 0; i < floatArray.length; i++) {
                    floatArray[i] = ((NBTTagFloat) tagList.get(i)).getFloat();
                }
                return floatArray;
            case DOUBLE:
                return ((NBTTagDouble) tag).getDouble();
            case DOUBLE_ARRAY:
                tagList = (NBTTagList) tag;
                if (tagList.getTagType() == NbtType.END.type) {
                    return new double[tagList.tagCount()];
                } else if (tagList.getTagType() != NbtType.DOUBLE.type) {
                    throw new IllegalStateException("Attempted to deserialize a Double Array (List) but the list type wasn't a double.");
                }
                final double[] doubleArray = new double[tagList.tagCount()];
                for (int i = 0; i < doubleArray.length; i++) {
                    doubleArray[i] = ((NBTTagDouble) tagList.get(i)).getDouble();
                }
                return doubleArray;
            case STRING:
                return ((NBTTagString) tag).getString();
            case STRING_ARRAY:
                tagList = (NBTTagList) tag;
                if (tagList.getTagType() == NbtType.END.type) {
                    return new String[tagList.tagCount()];
                } else if (tagList.getTagType() != NbtType.STRING.type) {
                    throw new IllegalStateException("Attempted to deserialize a String Array (List) but the list type wasn't a string.");
                }
                final String[] stringArray = new String[tagList.tagCount()];
                for (int i = 0; i < stringArray.length; i++) {
                    stringArray[i] = ((NBTTagString) tagList.get(i)).getString();
                }
                return stringArray;
            case BOOLEAN:
                return ((NBTTagByte) tag).getByte() != 0;
            case BOOLEAN_ARRAY:
                final byte[] bytes = ((NBTTagByteArray) tag).getByteArray();
                final int offset = 2; // 2 bytes for the amount of bits
                int bitBytes = bytes.length - offset;
                final boolean[] booleanArray = new boolean[(bytes[0] & 0xff) << 8 | bytes[1] & 0xff];
                int j = 0;
                for (int i = 0; i < bitBytes; i++) {
                    final byte value = bytes[offset + i];
                    while (j < booleanArray.length) {
                        final int k = j % 8;
                        booleanArray[j++] = (value & (1 << k)) != 0;
                    }
                }
                return booleanArray;
            case LIST:
                tagList = (NBTTagList) tag;
                final int listType = tagList.getTagType();
                if (listNbtType == null) {
                    listNbtType = NbtType.byIndex.get(listType);
                    if (listNbtType == null) {
                        throw new IllegalStateException("Unknown NBT Type with id: " + listType);
                    }
                }
                final List<Object> list = Lists.newArrayListWithExpectedSize(tagList.tagCount());
                if (tagList.tagCount() == 0 || listNbtType == NbtType.END) {
                    return list;
                }
                for (int i = 0; i < tagList.tagCount(); i++) {
                    list.add(fromTag(null, listNbtType, null, tagList.get(i)));
                }
                return list;
            case COMPOUND:
                if (container == null) {
                    container = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);
                }
                final NBTTagCompound tagCompound = (NBTTagCompound) tag;
                for (String key : tagCompound.getKeySet()) {
                    final NBTBase entryTag = tagCompound.getTag(key);
                    final NbtEntry entry = tagAndNameToEntry(key, entryTag);
                    if (entry.type == NbtType.COMPOUND) {
                        fromEntry(container.createView(DataQuery.of(key)), entry);
                    } else {
                        container.set(DataQuery.of(entry.name), fromEntry(null, entry));
                    }
                }
                return container;
            case COMPOUND_ARRAY:
                tagList = (NBTTagList) tag;
                if (tagList.getTagType() == NbtType.END.type) {
                    return new DataView[tagList.tagCount()];
                } else if (tagList.getTagType() != NbtType.COMPOUND.type) {
                    throw new IllegalStateException("Attempted to deserialize a DataView Array (List) but the list type wasn't a data view.");
                }
                final DataView[] dataViewArray = new DataView[tagList.tagCount()];
                for (int i = 0; i < dataViewArray.length; i++) {
                    dataViewArray[i] = (DataView) fromTag(null, NbtType.COMPOUND, null, tagList.get(i));
                }
                return dataViewArray;
            default:
                throw new InvalidDataFormatException("Attempt to deserialize a unknown nbt tag type: " + nbtType);
        }
    }

    static class NbtEntry {

        private final String name;
        private final NbtType type;
        @Nullable private final NbtType listType;
        private final NBTBase tag;

        NbtEntry(String name, NbtType type, @Nullable NbtType listType, NBTBase tag) {
            this.listType = listType;
            this.name = name;
            this.type = type;
            this.tag = tag;
        }
    }

    private NbtTranslator() {
    }

    public NBTTagCompound translateData(DataView container) {
        return toCompoundTag(container);
    }

    public DataContainer translateFrom(NBTTagCompound node) {
        return (DataContainer) fromTag(node);
    }

    @Override
    public TypeToken<NBTTagCompound> getToken() {
        return token;
    }

    @Override
    public NBTTagCompound translate(DataView view) throws InvalidDataException {
        return translateData(view);
    }

    @Override
    public DataContainer translate(NBTTagCompound obj) throws InvalidDataException {
        return translateFrom(obj);
    }

    @Override
    public DataView addTo(NBTTagCompound compound, DataView container) {
        fromTag(container, NbtType.COMPOUND, null, compound);
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
