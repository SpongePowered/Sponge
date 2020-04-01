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
package org.spongepowered.common.mixin.api.mcp.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldWriter;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.volume.game.MutableGameVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

@Mixin(IWorldWriter.class)
public interface IWorldWriterMixin_API extends MutableGameVolume {
    @Shadow boolean shadow$setBlockState(BlockPos p_180501_1_, BlockState p_180501_2_, int p_180501_3_);
    @Shadow boolean shadow$removeBlock(BlockPos p_217377_1_, boolean p_217377_2_);
    @Shadow boolean shadow$destroyBlock(BlockPos p_175655_1_, boolean p_175655_2_);
    @Shadow boolean shadow$addEntity(Entity p_217376_1_);

    @Override
    default boolean setBlock(int x, int y, int z, org.spongepowered.api.block.BlockState state, BlockChangeFlag flag) {
        return this.shadow$setBlockState(new BlockPos(x, y, z), (BlockState) state, ((SpongeBlockChangeFlag) flag).getRawFlag());
    }

    @Override
    default boolean spawnEntity(org.spongepowered.api.entity.Entity entity) {
        return this.shadow$addEntity((Entity) entity);
    }

    @Override
    default boolean removeBlock(int x, int y, int z) {
        return this.shadow$removeBlock(new BlockPos(x, y, z), false);
    }

    @Override
    default boolean destroyBlock(Vector3i pos, boolean performDrops) {
        return this.shadow$destroyBlock(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), performDrops);
    }

}
