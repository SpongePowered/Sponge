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
package org.spongepowered.common.network.packet;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.packet.Packet;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;

public final class RegisterDimensionTypePacket implements Packet {

    public int dimensionId;
    public ResourceLocation actualDimension;
    public ResourceLocation dimensionLogic;

    public RegisterDimensionTypePacket() {
    }

    public RegisterDimensionTypePacket(DimensionType dimensionType) {
        this.dimensionId = dimensionType.getId();
        this.actualDimension = DimensionType.getKey(dimensionType);
        this.dimensionLogic = (ResourceLocation) (Object) ((DimensionTypeBridge) dimensionType).bridge$getSpongeDimensionType().getKey();
    }

    @Override
    public void read(ChannelBuf buf) {
        this.dimensionId = buf.readInt();
        this.actualDimension = new ResourceLocation(buf.readString());
        this.dimensionLogic = new ResourceLocation(buf.readString());
    }

    @Override
    public void write(ChannelBuf buf) {
        buf.writeInt(this.dimensionId);
        buf.writeString(this.actualDimension.toString());
        buf.writeString(this.dimensionLogic.toString());
    }
}
