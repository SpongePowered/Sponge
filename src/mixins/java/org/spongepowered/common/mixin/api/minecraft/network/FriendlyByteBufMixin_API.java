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
package org.spongepowered.common.mixin.api.minecraft.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.network.channel.ChannelBuffers;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

@Mixin(FriendlyByteBuf.class)
@Implements(@Interface(iface = ChannelBuf.class, prefix = "cbuf$", remap = Remap.NONE))
public abstract class FriendlyByteBufMixin_API extends ByteBuf {

    // @formatter:off
    // mojang methods, fluent in target
    @Shadow public abstract FriendlyByteBuf shadow$writeByteArray(byte[] array);
    @Shadow public abstract FriendlyByteBuf shadow$writeVarInt(int input);
    @Shadow public abstract FriendlyByteBuf shadow$writeVarLong(long input);
    @Shadow public abstract FriendlyByteBuf shadow$writeUtf(String string);
    @Shadow public abstract FriendlyByteBuf shadow$writeNbt(@Nullable CompoundTag nbt);
    @Shadow public abstract FriendlyByteBuf shadow$writeUUID(UUID uniqueId);
    
    // mojang methods, non-fluent
    @Shadow public abstract byte[] shadow$readByteArray();
    @Shadow public abstract byte[] shadow$readByteArray(int limit);
    @Shadow public abstract int shadow$readVarInt();
    @Shadow public abstract long shadow$readVarLong();
    @Shadow public abstract String shadow$readUtf(int maxLength);
    @Shadow public abstract CompoundTag shadow$readNbt() throws IOException;
    @Shadow public abstract UUID shadow$readUUID();

    // @formatter:on

    @Intrinsic
    public int cbuf$capacity() {
        return this.capacity();
    }

    public int cbuf$available() {
        return this.writerIndex() - this.readerIndex();
    }

    @Intrinsic
    public int cbuf$readerIndex() {
        return this.readerIndex();
    }

    public ChannelBuf cbuf$readerIndex(final int index) {
        this.readerIndex(index);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$writerIndex() {
        return this.writerIndex();
    }

    public ChannelBuf cbuf$writerIndex(final int index) {
        this.writerIndex(index);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setIndex(final int readIndex, final int writeIndex) {
        this.setIndex(readIndex, writeIndex);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$clear() {
        this.clear();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$slice() {
        return ChannelBuffers.wrap(this.slice());
    }

    public ChannelBuf cbuf$slice(final int index, final int length) {
        return ChannelBuffers.wrap(this.slice(index, length));
    }

    public ChannelBuf cbuf$readSlice(final int length) {
        return ChannelBuffers.wrap(this.readSlice(length));
    }

    @Intrinsic
    public boolean cbuf$hasArray() {
        return this.hasArray();
    }

    @Intrinsic
    public byte[] cbuf$array() {
        return this.array();
    }

    public ChannelBuf cbuf$writeBoolean(final boolean data) {
        this.writeBoolean(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBoolean(final int index, final boolean data) {
        this.setBoolean(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public boolean cbuf$readBoolean() {
        return this.readBoolean();
    }

    @Intrinsic
    public boolean cbuf$getBoolean(final int index) {
        return this.getBoolean(index);
    }

    public ChannelBuf cbuf$writeByte(final byte data) {
        this.writeByte(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByte(final int index, final byte data) {
        this.setByte(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte cbuf$readByte() {
        return this.readByte();
    }

    @Intrinsic
    public byte cbuf$getByte(final int index) {
        return this.getByte(index);
    }

    public ChannelBuf cbuf$writeByteArray(final byte[] data) {
        return (ChannelBuf) this.shadow$writeByteArray(data); // fluent in target
    }

    public ChannelBuf cbuf$writeByteArray(final byte[] data, final int start, final int length) {
        this.writeBytes(data, start, length);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByteArray(final int index, final byte[] data) {
        this.setBytes(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByteArray(final int index, final byte[] data, final int start, final int length) {
        this.setBytes(index, data, start, length);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte[] cbuf$readByteArray() {
        return this.shadow$readByteArray();
    }

    @Intrinsic
    public byte[] cbuf$readByteArray(final int limit) {
        return this.shadow$readByteArray(limit);
    }

    public byte[] cbuf$getByteArray(final int index) {
        final int readerIndex = this.readerIndex();
        try {
            this.readerIndex(index);
            return this.shadow$readByteArray();
        } finally {
            this.readerIndex(readerIndex);
        }
    }

    public byte[] cbuf$getByteArray(final int index, final int limit) {
        final int readerIndex = this.readerIndex();
        try {
            this.readerIndex(index);
            return this.shadow$readByteArray(limit);
        } finally {
            this.readerIndex(readerIndex);
        }
    }

    public ChannelBuf cbuf$writeBytes(final byte[] data) {
        this.writeBytes(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeBytes(final byte[] data, final int start, final int length) {
        this.writeBytes(data, start, length);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBytes(final int index, final byte[] data) {
        this.setBytes(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBytes(final int index, final byte[] data, final int start, final int length) {
        this.setBytes(index, data, start, length);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte[] cbuf$readBytes(final int length) {
        final byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return bytes;
    }

    public byte[] cbuf$readBytes(final int index, final int length) {
        final byte[] dest = new byte[length];
        this.readBytes(dest, index, length);
        return dest;
    }

    public ChannelBuf cbuf$writeShort(final short data) {
        this.writeShort(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeShortLE(final short data) {
        this.writeShortLE(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setShort(final int index, final short data) {
        this.setShort(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setShortLE(final int index, final short data) {
        this.setShortLE(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public short cbuf$readShort() {
        return this.readShort();
    }

    @Intrinsic
    public short cbuf$readShortLE() {
        return this.readShortLE();
    }

    @Intrinsic
    public short cbuf$getShort(final int index) {
        return this.getShort(index);
    }

    @Intrinsic
    public short cbuf$getShortLE(final int index) {
        return this.getShortLE(index);
    }

    public ChannelBuf cbuf$writeChar(final char data) {
        this.writeChar(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setChar(final int index, final char data) {
        this.setChar(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public char cbuf$readChar() {
        return this.readChar();
    }

    @Intrinsic
    public char cbuf$getChar(final int index) {
        return this.getChar(index);
    }

    public ChannelBuf cbuf$writeInt(final int data) {
        this.writeInt(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeIntLE(final int data) {
        this.writeIntLE(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setInt(final int index, final int data) {
        this.setInt(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setIntLE(final int index, final int data) {
        this.setIntLE(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$readInt() {
        return this.readInt();
    }

    @Intrinsic
    public int cbuf$readIntLE() {
        return this.readIntLE();
    }

    @Intrinsic
    public int cbuf$getInt(final int index) {
        return this.getInt(index);
    }

    @Intrinsic
    public int cbuf$getIntLE(final int index) {
        return this.getIntLE(index);
    }

    public ChannelBuf cbuf$writeLong(final long data) {
        this.writeLong(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeLongLE(final long data) {
        this.writeLongLE(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setLong(final int index, final long data) {
        this.setLong(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setLongLE(final int index, final long data) {
        this.setLongLE(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public long cbuf$readLong() {
        return this.readLong();
    }

    @Intrinsic
    public long cbuf$readLongLE() {
        return this.readLongLE();
    }

    @Intrinsic
    public long cbuf$getLong(final int index) {
        return this.getLong(index);
    }

    @Intrinsic
    public long cbuf$getLongLE(final int index) {
        return this.getLongLE(index);
    }

    public ChannelBuf cbuf$writeFloat(final float data) {
        this.writeFloat(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeFloatLE(final float data) {
        this.writeFloatLE(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setFloat(final int index, final float data) {
        this.setFloat(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setFloatLE(final int index, final float data) {
        this.setFloatLE(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public float cbuf$readFloat() {
        return this.readFloat();
    }

    @Intrinsic
    public float cbuf$readFloatLE() {
        return this.readFloatLE();
    }

    @Intrinsic
    public float cbuf$getFloat(final int index) {
        return this.getFloat(index);
    }

    @Intrinsic
    public float cbuf$getFloatLE(final int index) {
        return this.getFloatLE(index);
    }

    public ChannelBuf cbuf$writeDouble(final double data) {
        this.writeDouble(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeDoubleLE(final double data) {
        this.writeDoubleLE(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDouble(final int index, final double data) {
        this.setDouble(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDoubleLE(final int index, final double data) {
        this.setDoubleLE(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public double cbuf$readDouble() {
        return this.readDouble();
    }

    @Intrinsic
    public double cbuf$readDoubleLE() {
        return this.readDoubleLE();
    }

    @Intrinsic
    public double cbuf$getDouble(final int index) {
        return this.getDouble(index);
    }

    @Intrinsic
    public double cbuf$getDoubleLE(final int index) {
        return this.getDoubleLE(index);
    }

    public ChannelBuf cbuf$writeVarInt(final int value) {
        return (ChannelBuf) this.shadow$writeVarInt(value); // fluent in target
    }

    public ChannelBuf cbuf$setVarInt(final int index, final int value) {
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.shadow$writeVarInt(value);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$readVarInt() {
        return this.shadow$readVarInt();
    }

    public int cbuf$getVarInt(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final int value = this.shadow$readVarInt();
        this.readerIndex(oldIndex);
        return value;
    }

    public ChannelBuf cbuf$writeVarLong(final long value) {
        return (ChannelBuf) this.shadow$writeVarLong(value); // fluent in target
    }

    public ChannelBuf cbuf$setVarLong(final int index, final long value) {
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.shadow$writeVarLong(value);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public long cbuf$readVarLong() {
        return this.shadow$readVarLong();
    }

    public long cbuf$getVarLong(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final long value = this.shadow$readVarLong();
        this.readerIndex(oldIndex);
        return value;
    }

    public ChannelBuf cbuf$writeString(final String data) {
        return (ChannelBuf) this.shadow$writeUtf(checkNotNull(data)); // fluent in target
    }

    public ChannelBuf cbuf$setString(final int index, final String data) {
        checkNotNull(data);
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.shadow$writeUtf(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public String cbuf$readString() {
        return this.shadow$readUtf(Constants.Networking.MAX_STRING_LENGTH);
    }

    public String cbuf$getString(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final String value = this.shadow$readUtf(Constants.Networking.MAX_STRING_LENGTH);
        this.readerIndex(oldIndex);
        return value;
    }

    public ChannelBuf cbuf$writeUTF(final String data) {
        final byte[] bytes = data.getBytes(Charsets.UTF_8);
        if (bytes.length > Constants.Networking.MAX_STRING_LENGTH_BYTES) {
            throw new EncoderException("String too big (was " + data.length() + " bytes encoded, max "
                                       + Constants.Networking.MAX_STRING_LENGTH_BYTES + ")");
        }
        this.writeShort(bytes.length);
        this.writeBytes(bytes);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setUTF(final int index, final String data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.cbuf$writeUTF(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public String cbuf$readUTF() {
        final short length = this.readShort();
        final byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return new String(bytes, Charsets.UTF_8);
    }

    public String cbuf$getUTF(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final short length = this.readShort();
        final byte[] bytes = new byte[length];
        this.readBytes(bytes);
        final String data = new String(bytes, Charsets.UTF_8);
        this.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeUniqueId(final UUID data) {
        checkNotNull(data, "data");
        return (ChannelBuf) this.shadow$writeUUID(data); // fluent in target
    }

    public ChannelBuf cbuf$setUniqueId(final int index, final UUID data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.shadow$writeUUID(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public UUID cbuf$readUniqueId() {
        return this.shadow$readUUID();
    }

    public UUID getUniqueId(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final UUID data = this.shadow$readUUID();
        this.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeDataView(final DataView data) {
        final CompoundTag compound = NBTTranslator.INSTANCE.translate(checkNotNull(data, "data"));
        this.shadow$writeNbt(compound);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDataView(final int index, final DataView data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.cbuf$writeDataView(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public DataView cbuf$readDataView() {
        try {
            return NBTTranslator.INSTANCE.translateFrom(this.shadow$readNbt());
        } catch (final IOException e) {
            throw new DecoderException(e);
        }
    }

    public DataView cbuf$getDataView(final int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final DataView data = this.cbuf$readDataView();
        this.readerIndex(oldIndex);
        return data;
    }
}
