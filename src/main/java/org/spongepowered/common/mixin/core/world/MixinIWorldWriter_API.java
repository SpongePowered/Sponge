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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.IWorldWriter;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.volume.composite.MutableGameVolume;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

@Mixin(IWorldWriter.class)
public interface MixinIWorldWriter_API extends MutableGameVolume {

    @Shadow boolean setBlockState(BlockPos pos, IBlockState newState, int flags);
    @Shadow boolean spawnEntity(net.minecraft.entity.Entity entityIn);
    @Shadow boolean removeBlock(BlockPos pos);
    @Shadow void setLightFor(EnumLightType type, BlockPos pos, int lightValue);
    @Shadow boolean destroyBlock(BlockPos pos, boolean dropBlock);

    @Override
    default boolean setBlockstate(int x, int y, int z, BlockState state, BlockChangeFlag flag) {
        return setBlockState(new BlockPos(x, y, z), BlockUtil.toNative(state), ((SpongeBlockChangeFlag) flag).getRawFlag());
    }

    @Override
    default boolean spawnEntity(Entity entity) {
        return spawnEntity(EntityUtil.toNative(entity));
    }

    @Override
    default boolean removeBlock(int x, int y, int z) {
        return removeBlock(new BlockPos(x, y, z));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    default void setLight(LightType light, Vector3i position, int lightValue) {
        setLightFor((EnumLightType) (Object) light, VecHelper.toBlockPos(position), lightValue);
    }

    @Override
    default boolean destroyBlock(Vector3i pos, boolean performDrops) {
        return destroyBlock(VecHelper.toBlockPos(pos), performDrops);
    }

}
