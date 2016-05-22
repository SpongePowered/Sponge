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

package org.spongepowered.common.mixin.core.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NbtTranslator;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(PacketBuffer.class)
@Implements(@Interface(iface = ChannelBuf.class, prefix = "cbuf$"))
public abstract class MixinPacketBuffer extends ByteBuf {

    @Shadow public abstract PacketBuffer writeByteArray(byte[] array);
    @Shadow public abstract byte[] readByteArray();
    @Shadow public abstract byte[] readByteArray(int p_189425_1_);
    @Shadow public abstract PacketBuffer writeVarIntToBuffer(int input);
    @Shadow public abstract int readVarIntFromBuffer();
    @Shadow public abstract PacketBuffer writeString(String string);
    @Shadow public abstract String readStringFromBuffer(int maxLength);
    @Shadow public abstract PacketBuffer writeNBTTagCompoundToBuffer(@Nullable NBTTagCompound nbt);
    @Shadow public abstract NBTTagCompound readNBTTagCompoundFromBuffer() throws IOException;

    @Intrinsic
    public int cbuf$getCapacity() {
        return this.capacity();
    }

    @Intrinsic
    public int cbuf$available() {
        return this.writerIndex() - this.readerIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$order(ByteOrder order) {
        return (ChannelBuf) this.order(order);
    }

    @Intrinsic
    public ByteOrder cbuf$getByteOrder() {
        return this.order();
    }

    @Intrinsic
    public int cbuf$readerIndex() {
        return this.readerIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$setReadIndex(int index) {
        return (ChannelBuf) this.readerIndex(index);
    }

    @Intrinsic
    public int cbuf$writerIndex() {
        return this.writerIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$setWriteIndex(int index) {
        return (ChannelBuf) this.writerIndex(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$setIndex(int readIndex, int writeIndex) {
        return (ChannelBuf) this.setIndex(readIndex, writeIndex);
    }

    @Intrinsic
    public ChannelBuf cbuf$clear() {
        return (ChannelBuf) this.clear();
    }

    @Intrinsic
    public ChannelBuf cbuf$markRead() {
        return (ChannelBuf) this.markReaderIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$markWrite() {
        return (ChannelBuf) this.markWriterIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$resetRead() {
        return (ChannelBuf) this.resetReaderIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$resetWrite() {
        return (ChannelBuf) this.resetWriterIndex();
    }

    @Intrinsic
    public ChannelBuf cbuf$slice() {
        return (ChannelBuf) this.slice();
    }

    @Intrinsic
    public ChannelBuf cbuf$slice(int index, int length) {
        return (ChannelBuf) this.slice(index, length);
    }

    @Intrinsic
    public byte[] cbuf$array() {
        return this.array();
    }

    @Intrinsic
    public ChannelBuf cbuf$writeBoolean(boolean data) {
        return (ChannelBuf) this.writeBoolean(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setBoolean(int index, boolean data) {
        return (ChannelBuf) this.setBoolean(index, data);
    }

    @Intrinsic
    public boolean cbuf$readBoolean() {
        return this.readBoolean();
    }

    @Intrinsic
    public boolean cbuf$getBoolean(int index) {
        return this.getBoolean(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeByte(byte data) {
        return (ChannelBuf) this.writeByte(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setByte(int index, byte data) {
        return (ChannelBuf) this.setByte(index, data);
    }

    @Intrinsic
    public byte cbuf$readByte() {
        return this.readByte();
    }

    @Intrinsic
    public byte cbuf$getByte(int index) {
        return this.getByte(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeByteArray(byte[] data) {
        return (ChannelBuf) this.writeByteArray(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeByteArray(byte[] data, int start, int length) {
        return (ChannelBuf) this.writeBytes(data, start, length);
    }

    @Intrinsic
    public ChannelBuf cbuf$setByteArray(int index, byte[] data) {
        return (ChannelBuf) this.setBytes(index, data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setByteArray(int index, byte[] data, int start, int length) {
        return (ChannelBuf) this.setBytes(index, data, start, length);
    }

    @Intrinsic
    public byte[] cbuf$readByteArray() {
        return this.readByteArray();
    }

    @Intrinsic
    public byte[] cbuf$readByteArray(int index) {
        return this.readByteArray(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeBytes(byte[] data) {
        return (ChannelBuf) this.writeBytes(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeBytes(byte[] data, int start, int length) {
        return (ChannelBuf) this.writeBytes(data, start, length);
    }

    @Intrinsic
    public ChannelBuf cbuf$setBytes(int index, byte[] data) {
        return (ChannelBuf) this.setBytes(index, data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setBytes(int index, byte[] data, int start, int length) {
        return (ChannelBuf) this.setBytes(index, data, start, length);
    }

    @Intrinsic
    public byte[] cbuf$readBytes(int length) {
        return this.readBytes(length).array();
    }

    @Intrinsic
    public byte[] cbuf$readBytes(int index, int length) {
        final byte[] dest = new byte[length];
        this.readBytes(dest, index, length);
        return dest;
    }

    @Intrinsic
    public ChannelBuf cbuf$writeShort(short data) {
        return (ChannelBuf) this.writeShort(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setShort(int index, short data) {
        return (ChannelBuf) this.setShort(index, data);
    }

    @Intrinsic
    public short cbuf$readShort() {
        return this.readShort();
    }

    @Intrinsic
    public short cbuf$getShort(int index) {
        return this.getShort(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeChar(char data) {
        return (ChannelBuf) this.writeChar(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setChar(int index, char data) {
        return (ChannelBuf) this.setChar(index, data);
    }

    @Intrinsic
    public char cbuf$readChar() {
        return this.readChar();
    }

    @Intrinsic
    public char cbuf$getChar(int index) {
        return this.getChar(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeInteger(int data) {
        return (ChannelBuf) this.writeInt(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setInteger(int index, int data) {
        return (ChannelBuf) this.setInt(index, data);
    }

    @Intrinsic
    public int cbuf$readInteger() {
        return this.readInt();
    }

    @Intrinsic
    public int getInteger(int index) {
        return this.getInt(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeLong(long data) {
        return (ChannelBuf) this.writeLong(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setLong(int index, long data) {
        return (ChannelBuf) this.setLong(index, data);
    }

    @Intrinsic
    public long cbuf$readLong() {
        return this.readLong();
    }

    @Intrinsic
    public long cbuf$getLong(int index) {
        return this.getLong(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeFloat(float data) {
        return (ChannelBuf) this.writeFloat(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setFloat(int index, float data) {
        return (ChannelBuf) this.setFloat(index, data);
    }

    @Intrinsic
    public float cbuf$readFloat() {
        return this.readFloat();
    }

    @Intrinsic
    public float cbuf$getFloat(int index) {
        return this.getFloat(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeDouble(double data) {
        return (ChannelBuf) this.writeDouble(data);
    }

    @Intrinsic
    public ChannelBuf cbuf$setDouble(int index, double data) {
        return (ChannelBuf) this.setDouble(index, data);
    }

    @Intrinsic
    public double cbuf$readDouble() {
        return this.readDouble();
    }

    @Intrinsic
    public double cbuf$getDouble(int index) {
        return this.getDouble(index);
    }

    @Intrinsic
    public ChannelBuf cbuf$writeVarInt(int value) {
        return (ChannelBuf) this.writeVarIntToBuffer(value);
    }

    @Intrinsic
    public ChannelBuf cbuf$setVarInt(int index, int value) {
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeVarIntToBuffer(value);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$readVarInt() {
        return this.readVarIntFromBuffer();
    }

    @Intrinsic
    public int cbuf$getVarInt(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final int value = readVarIntFromBuffer();
        this.readerIndex(oldIndex);
        return value;
    }

    @Intrinsic
    public ChannelBuf cbuf$writeString(String data) {
        return (ChannelBuf) this.writeString(checkNotNull(data));
    }

    @Intrinsic
    public ChannelBuf cbuf$setString(int index, String data) {
        checkNotNull(data);
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeString(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public String cbuf$readString() {
        return this.readStringFromBuffer(32767);
    }

    @Intrinsic
    public String cbuf$getString(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final String value = this.readStringFromBuffer(32767);
        this.readerIndex(oldIndex);
        return value;
    }

    @Intrinsic
    public ChannelBuf cbuf$writeUTF(String data) {
        byte[] bytes = data.getBytes(Charsets.UTF_8);
        if (bytes.length > 32767) {
            throw new EncoderException("String too big (was " + data.length() + " bytes encoded, max " + 32767 + ")");
        }
        this.writeShort(bytes.length);
        this.writeBytes(bytes);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public ChannelBuf cbuf$setUTF(int index, String data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.cbuf$writeUTF(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public String cbuf$readUTF() {
        final short length = this.readShort();
        return new String(this.readBytes(length).array(), Charsets.UTF_8);
    }

    @Intrinsic
    public String cbuf$getUTF(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final short length = this.readShort();
        final String data = new String(this.readBytes(length).array(), Charsets.UTF_8);
        this.readerIndex(oldIndex);
        return data;
    }

    @Intrinsic
    public ChannelBuf cbuf$writeUniqueId(UUID data) {
        checkNotNull(data, "data");
        return this.cbuf$writeLong(data.getMostSignificantBits()).writeLong(data.getLeastSignificantBits());
    }

    @Intrinsic
    public ChannelBuf cbuf$setUniqueId(int index, UUID data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeLong(data.getMostSignificantBits()).writeLong(data.getLeastSignificantBits());
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public UUID cbuf$readUniqueId() {
        return new UUID(this.readLong(), this.readLong());
    }

    @Intrinsic
    public UUID getUniqueId(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final UUID data = new UUID(this.readLong(), this.readLong());
        this.readerIndex(oldIndex);
        return data;
    }

    @Intrinsic
    public ChannelBuf cbuf$writeDataView(DataView data) {
        final NBTTagCompound compound = NbtTranslator.getInstance().translateData(checkNotNull(data, "data"));
        this.writeNBTTagCompoundToBuffer(compound);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public ChannelBuf cbuf$setDataView(int index, DataView data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.cbuf$writeDataView(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public DataView cbuf$readDataView() {
        try {
            return NbtTranslator.getInstance().translateFrom(this.readNBTTagCompoundFromBuffer());
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }

    @Intrinsic
    public DataView cbuf$getDataView(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final DataView data = this.cbuf$readDataView();
        this.readerIndex(oldIndex);
        return data;
    }
}
