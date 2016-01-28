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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.network.ByteBufUtils;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.UUID;

@Mixin(PacketBuffer.class)
@Implements(@Interface(iface = ChannelBuf.class, prefix = "cbuf$"))
public abstract class MixinPacketBuffer extends ByteBuf {

    @Shadow @Final private ByteBuf buf;

    @Shadow protected abstract NBTTagCompound readNBTTagCompoundFromBuffer() throws IOException;
    @Shadow protected abstract void writeNBTTagCompoundToBuffer(NBTTagCompound compound);

    private ChannelBuf oppositeOrder;

    public int cbuf$getCapacity() {
        return this.buf.capacity();
    }

    public int cbuf$available() {
        return this.buf.writerIndex() - this.buf.readerIndex();
    }

    public ChannelBuf cbuf$order(ByteOrder order) {
        checkNotNull(order, "order");
        if (this.buf.order().equals(order)) {
            return (ChannelBuf) this;
        }
        if (this.oppositeOrder == null) {
            this.oppositeOrder = (ChannelBuf) new PacketBuffer(this.buf.order(order));
        }
        return this.oppositeOrder;
    }

    public ByteOrder cbuf$getByteOrder() {
        return this.buf.order();
    }

    public int cbuf$readerIndex() {
        return this.buf.readerIndex();
    }

    public ChannelBuf cbuf$setReadIndex(int index) {
        this.buf.readerIndex(index);
        return (ChannelBuf) this;
    }

    public int cbuf$writerIndex() {
        return this.buf.writerIndex();
    }

    public ChannelBuf cbuf$setWriteIndex(int index) {
        this.buf.writerIndex(index);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setIndex(int readIndex, int writeIndex) {
        this.buf.setIndex(readIndex, writeIndex);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$clear() {
        this.buf.clear();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$markRead() {
        this.buf.markReaderIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$markWrite() {
        this.buf.markWriterIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$resetRead() {
        this.buf.resetReaderIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$resetWrite() {
        this.buf.resetWriterIndex();
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$slice() {
        return (ChannelBuf) new PacketBuffer(this.buf.slice());
    }

    public ChannelBuf cbuf$slice(int index, int length) {
        return (ChannelBuf) new PacketBuffer(this.buf.slice(index, length));
    }

    public byte[] cbuf$array() {
        return this.buf.array();
    }

    public ChannelBuf cbuf$writeBoolean(boolean data) {
        this.buf.writeBoolean(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setBoolean(int index, boolean data) {
        this.buf.setBoolean(index, data);
        return (ChannelBuf) this;
    }

    public boolean cbuf$readBoolean() {
        return this.buf.readBoolean();
    }

    public boolean cbuf$getBoolean(int index) {
        return this.buf.getBoolean(index);
    }

    public ChannelBuf cbuf$writeByte(byte data) {
        this.buf.writeByte(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setByte(int index, byte data) {
        this.buf.setByte(index, data);
        return (ChannelBuf) this;
    }

    public byte cbuf$readByte() {
        return this.buf.readByte();
    }

    public byte cbuf$getByte(int index) {
        return this.buf.getByte(index);
    }

    public ChannelBuf cbuf$writeShort(short data) {
        this.buf.writeShort(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setShort(int index, short data) {
        this.buf.setShort(index, data);
        return (ChannelBuf) this;
    }

    public short cbuf$readShort() {
        return this.buf.readShort();
    }

    public short cbuf$getShort(int index) {
        return this.buf.getShort(index);
    }

    public ChannelBuf cbuf$writeChar(char data) {
        this.buf.writeChar(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setChar(int index, char data) {
        this.buf.setChar(index, data);
        return (ChannelBuf) this;
    }

    public char cbuf$readChar() {
        return this.buf.readChar();
    }

    public char cbuf$getChar(int index) {
        return this.buf.getChar(index);
    }

    public ChannelBuf cbuf$writeInteger(int data) {
        this.buf.writeInt(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setInteger(int index, int data) {
        this.buf.setInt(index, data);
        return (ChannelBuf) this;
    }

    public int cbuf$readInteger() {
        return this.buf.readInt();
    }

    public int cbuf$getInteger(int index) {
        return this.buf.getInt(index);
    }

    public ChannelBuf cbuf$writeLong(long data) {
        this.buf.writeLong(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setLong(int index, long data) {
        this.buf.setLong(index, data);
        return (ChannelBuf) this;
    }

    public long cbuf$readLong() {
        return this.buf.readLong();
    }

    public long cbuf$getLong(int index) {
        return this.buf.getLong(index);
    }

    public ChannelBuf cbuf$writeFloat(float data) {
        this.buf.writeFloat(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setFloat(int index, float data) {
        this.buf.setFloat(index, data);
        return (ChannelBuf) this;
    }

    public float cbuf$readFloat() {
        return this.buf.readFloat();
    }

    public float cbuf$getFloat(int index) {
        return this.buf.getFloat(index);
    }

    public ChannelBuf cbuf$writeDouble(double data) {
        this.buf.writeDouble(data);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDouble(int index, double data) {
        this.buf.setDouble(index, data);
        return (ChannelBuf) this;
    }

    public double cbuf$readDouble() {
        return this.buf.readDouble();
    }

    public double cbuf$getDouble(int index) {
        return this.buf.getDouble(index);
    }

    public ChannelBuf cbuf$writeString(String data) {
        ByteBufUtils.writeUTF8String(this.buf, checkNotNull(data, "data"));
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setString(int index, String data) {
        checkNotNull(data, "data");
        int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        ByteBufUtils.writeUTF8String(this.buf, data);
        this.buf.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public String cbuf$readString() {
        return ByteBufUtils.readUTF8String(this.buf);
    }

    public String cbuf$getString(int index) {
        int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        String data = ByteBufUtils.readUTF8String(this.buf);
        this.buf.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeUniqueId(UUID data) {
        checkNotNull(data, "data");
        return this.cbuf$writeLong(data.getMostSignificantBits()).writeLong(data.getLeastSignificantBits());
    }

    public ChannelBuf cbuf$setUniqueId(int index, UUID data) {
        checkNotNull(data, "data");
        int oldIndex = this.buf.writerIndex();
        this.buf.writerIndex(index);
        this.writeLong(data.getMostSignificantBits()).writeLong(data.getLeastSignificantBits());
        this.buf.writerIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public UUID cbuf$readUniqueId() {
        return new UUID(this.readLong(), this.readLong());
    }

    public UUID cbuf$getUniqueId(int index) {
        int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        UUID data = new UUID(this.readLong(), this.readLong());
        this.buf.readerIndex(oldIndex);
        return data;
    }

    public ChannelBuf cbuf$writeDataView(DataView data) {
        NBTTagCompound compound = NbtTranslator.getInstance().translateData(checkNotNull(data, "data"));
        this.writeNBTTagCompoundToBuffer(compound);
        return (ChannelBuf) this;
    }

    public ChannelBuf cbuf$setDataView(int index, DataView data) {
        checkNotNull(data, "data");
        int oldIndex = this.writerIndex();
        this.cbuf$setWriteIndex(index);
        this.cbuf$writeDataView(data);
        this.cbuf$setWriteIndex(oldIndex);
        return (ChannelBuf) this;
    }

    public DataView cbuf$readDataView() {
        try {
            NBTTagCompound compound = this.readNBTTagCompoundFromBuffer();
            return NbtTranslator.getInstance().translateFrom(compound);
        } catch (IOException e) {
            throw new DecoderException(e);
        }
    }

    public DataView cbuf$getDataView(int index) {
        int oldIndex = this.buf.readerIndex();
        this.buf.readerIndex(index);
        DataView data = this.cbuf$readDataView();
        this.buf.readerIndex(oldIndex);
        return data;
    }

}
