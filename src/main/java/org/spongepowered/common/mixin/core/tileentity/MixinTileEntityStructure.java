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
package org.spongepowered.common.mixin.core.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.tileentity.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntityStructure;
import org.spongepowered.common.util.VecHelper;

@Mixin(TileEntityStructure.class)
public abstract class MixinTileEntityStructure extends MixinTileEntity implements Structure, IMixinTileEntityStructure {

    @Shadow private BlockPos position;
    @Shadow private BlockPos size;

    @Override
    public Vector3i getPosition() {
        return VecHelper.toVector3i(this.position);
    }

    @Override
    public void setPosition(Vector3i position) {
        this.position = VecHelper.toBlockPos(checkNotNull(position, "position"));
    }

    @Override
    public Vector3i getSize() {
        return VecHelper.toVector3i(this.size);
    }

    @Override
    public void setSize(Vector3i size) {
        this.size = VecHelper.toBlockPos(checkNotNull(size, "size"));
    }

}
