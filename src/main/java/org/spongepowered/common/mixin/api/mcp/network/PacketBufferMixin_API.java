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
package org.spongepowered.common.mixin.api.mcp.network;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.network.SpongeNetworkManager;
import org.spongepowered.common.util.Constants;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(PacketBuffer.class)
@Implements(@Interface(iface = ChannelBuf.class, prefix = "cbuf$"))
public abstract class PacketBufferMixin_API extends ByteBuf {

    // mojang methods, fluent in target
    @Shadow public abstract PacketBuffer writeByteArray(byte[] array);
    @Shadow public abstract PacketBuffer writeVarInt(int input);
    @Shadow public abstract PacketBuffer writeString(String string);
    @Shadow public abstract PacketBuffer writeCompoundTag(@Nullable CompoundNBT nbt);
    @Shadow public abstract PacketBuffer writeUniqueId(UUID uniqueId);
    
    // mojang methods, non-fluent
    @Shadow public abstract byte[] readByteArray();
    @Shadow public abstract byte[] readByteArray(int limit);
    @Shadow public abstract int readVarInt();
    @Shadow public abstract String readString(int maxLength);
    @Shadow public abstract CompoundNBT readCompoundTag() throws IOException;
    @Shadow public abstract UUID readUniqueId();

    public int cbuf$getCapacity() {
        return this.capacity();
    }

    public int cbuf$available() {
        return this.writerIndex() - this.readerIndex();
    }

    @SuppressWarnings("deprecation")
    public ChannelBuf cbuf$order(ByteOrder order) {
        this.order(order);
        return (ChannelBuf) this;
    }

    @SuppressWarnings("deprecation")
    public ByteOrder cbuf$getByteOrder() {
        return this.order();
    }

    @Intrinsic
    public int cbuf$readerIndex() {
        return this.readerIndex();
    }

    public ChannelBuf cbuf$setReadIndex(int index) {
        this.readerIndex(index);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$writerIndex() {
        return this.writerIndex();
    }

    public ChannelBuf cbuf$setWriteIndex(int index) {
        this.writerIndex(index);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setIndex(int readIndex, int writeIndex) {
        this.setIndex(readIndex, writeIndex);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$clear() {
        this.clear();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$markRead() {
        this.markReaderIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$markWrite() {
        this.markWriterIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$resetRead() {
        this.resetReaderIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$resetWrite() {
        this.resetWriterIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$slice() {
        return SpongeNetworkManager.toChannelBuf(this.slice());
    }

    public ChannelBuf cbuf$slice(int index, int length) {
        return SpongeNetworkManager.toChannelBuf(this.slice(index, length));
    }

    @Intrinsic
    public boolean cbuf$hasArray() {
        return this.hasArray();
    }

    @Intrinsic
    public byte[] cbuf$array() {
        return this.array();
    }

    public ChannelBuf cbuf$writeBoolean(boolean data) {
        this.writeBoolean(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBoolean(int index, boolean data) {
        this.setBoolean(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public boolean cbuf$readBoolean() {
        return this.readBoolean();
    }

    @Intrinsic
    public boolean cbuf$getBoolean(int index) {
        return this.getBoolean(index);
    }

    public ChannelBuf cbuf$writeByte(byte data) {
        this.writeByte(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByte(int index, byte data) {
        this.setByte(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte cbuf$readByte() {
        return this.readByte();
    }

    @Intrinsic
    public byte cbuf$getByte(int index) {
        return this.getByte(index);
    }

    public ChannelBuf cbuf$writeByteArray(byte[] data) {
        return (ChannelBuf) this.writeByteArray(data); // fluent in target
    }

    public ChannelBuf cbuf$writeByteArray(byte[] data, int start, int length) {
        this.writeBytes(data, start, length);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByteArray(int index, byte[] data) {
        this.setBytes(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByteArray(int index, byte[] data, int start, int length) {
        this.setBytes(index, data, start, length);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte[] cbuf$readByteArray() {
        return this.readByteArray();
    }

    @Intrinsic
    public byte[] cbuf$readByteArray(int index) {
        return this.readByteArray(index);
    }

    public ChannelBuf cbuf$writeBytes(byte[] data) {
        this.writeBytes(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$writeBytes(byte[] data, int start, int length) {
        this.writeBytes(data, start, length);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBytes(int index, byte[] data) {
        this.setBytes(index, data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBytes(int index, byte[] data, int start, int length) {
        this.setBytes(index, data, start, length);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public byte[] cbuf$readBytes(int length) {
        final byte[] bytes = new byte[length];
        this.readBytes(bytes);
        return bytes;
    }

    public byte[] cbuf$readBytes(int index, int length) {
        final byte[] dest = new byte[length];
        this.readBytes(dest, index, length);
        return dest;
    }

    public ChannelBuf cbuf$writeShort(short data) {
        this.writeShort(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setShort(int index, short data) {
        this.setShort(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public short cbuf$readShort() {
        return this.readShort();
    }

    @Intrinsic
    public short cbuf$getShort(int index) {
        return this.getShort(index);
    }

    public ChannelBuf cbuf$writeChar(char data) {
        this.writeChar(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setChar(int index, char data) {
        this.setChar(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public char cbuf$readChar() {
        return this.readChar();
    }

    @Intrinsic
    public char cbuf$getChar(int index) {
        return this.getChar(index);
    }

    public ChannelBuf cbuf$writeInteger(int data) {
        this.writeInt(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setInteger(int index, int data) {
        this.setInt(index, data);
        return (ChannelBuf) this;
    }

    public int cbuf$readInteger() {
        return this.readInt();
    }

    public int getInteger(int index) {
        return this.getInt(index);
    }

    public ChannelBuf cbuf$writeLong(long data) {
        this.writeLong(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setLong(int index, long data) {
        this.setLong(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public long cbuf$readLong() {
        return this.readLong();
    }

    @Intrinsic
    public long cbuf$getLong(int index) {
        return this.getLong(index);
    }

    public ChannelBuf cbuf$writeFloat(float data) {
        this.writeFloat(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setFloat(int index, float data) {
        this.setFloat(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public float cbuf$readFloat() {
        return this.readFloat();
    }

    @Intrinsic
    public float cbuf$getFloat(int index) {
        return this.getFloat(index);
    }

    public ChannelBuf cbuf$writeDouble(double data) {
        this.writeDouble(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDouble(int index, double data) {
        this.setDouble(index, data);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public double cbuf$readDouble() {
        return this.readDouble();
    }

    @Intrinsic
    public double cbuf$getDouble(int index) {
        return this.getDouble(index);
    }

    public ChannelBuf cbuf$writeVarInt(int value) {
        return (ChannelBuf) this.writeVarInt(value); // fluent in target
    }

    public ChannelBuf cbuf$setVarInt(int index, int value) {
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeVarInt(value);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public int cbuf$readVarInt() {
        return this.readVarInt();
    }

    public int cbuf$getVarInt(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final int value = readVarInt();
        this.readerIndex(oldIndex);
        return value;
    }

    public ChannelBuf cbuf$writeString(String data) {
        return (ChannelBuf) this.writeString(checkNotNull(data)); // fluent in target
    }

    public ChannelBuf cbuf$setString(int index, String data) {
        checkNotNull(data);
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeString(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public String cbuf$readString() {
        return this.readString(Constants.Networking.MAX_STRING_LENGTH);
    }

    public String cbuf$getString(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final String value = this.readString(Constants.Networking.MAX_STRING_LENGTH);
        this.readerIndex(oldIndex);
        return value;
    }

    public ChannelBuf cbuf$writeUTF(String data) {
        byte[] bytes = data.getBytes(Charsets.UTF_8);
        if (bytes.length > Constants.Networking.MAX_STRING_LENGTH_BYTES) {
            throw new EncoderException("String too big (was " + data.length() + " bytes encoded, max "
                                       + Constants.Networking.MAX_STRING_LENGTH_BYTES + ")");
        }
        this.writeShort(bytes.length);
        this.writeBytes(bytes);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setUTF(int index, String data) {
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

    public String cbuf$getUTF(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final short length = this.readShort();
        final byte[] bytes = new byte[length];
        this.readBytes(bytes);
        final String data = new String(bytes, Charsets.UTF_8);
        this.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeUniqueId(UUID data) {
        checkNotNull(data, "data");
        return (ChannelBuf) this.writeUniqueId(data); // fluent in target
    }

    public ChannelBuf cbuf$setUniqueId(int index, UUID data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.writeUniqueId(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    @Intrinsic
    public UUID cbuf$readUniqueId() {
        return this.readUniqueId();
    }

    public UUID getUniqueId(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final UUID data = this.readUniqueId();
        this.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeDataView(DataView data) {
        final CompoundNBT compound = NbtTranslator.getInstance().translateData(checkNotNull(data, "data"));
        this.writeCompoundTag(compound);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDataView(int index, DataView data) {
        checkNotNull(data, "data");
        final int oldIndex = this.writerIndex();
        this.writerIndex(index);
        this.cbuf$writeDataView(data);
        this.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public DataView cbuf$readDataView() {
        try {
            return NbtTranslator.getInstance().translateFrom(this.readCompoundTag());
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }

    public DataView cbuf$getDataView(int index) {
        final int oldIndex = this.readerIndex();
        this.readerIndex(index);
        final DataView data = this.cbuf$readDataView();
        this.readerIndex(oldIndex);
        return data;
    }
}
