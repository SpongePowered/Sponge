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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormat;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataFormatException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class NBTDataFormat implements DataFormat {

    @Override
    @SuppressWarnings("resource")
    public DataContainer readFrom(InputStream input) throws InvalidDataFormatException, IOException {
        DataInputStream dis;
        if (input instanceof DataInputStream) {
            dis = (DataInputStream) input;
        } else {
            dis = new DataInputStream(input);
        }
        try {
            CompoundTag tag = NbtIo.read(dis);
            return NBTTranslator.INSTANCE.translateFrom(tag);
        } finally {
            dis.close();
        }
    }

    @Override
    @SuppressWarnings("resource")
    public void writeTo(OutputStream output, DataView data) throws IOException {
        CompoundTag tag = NBTTranslator.INSTANCE.translate(data);
        DataOutputStream dos;
        if (output instanceof DataOutputStream) {
            dos = (DataOutputStream) output;
        } else {
            dos = new DataOutputStream(output);
        }
        try {
            NbtIo.write(tag, dos);
        } finally {
            dos.close();
        }
    }

}
