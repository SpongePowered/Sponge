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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.ISpawnWorldInfo;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

@Mixin(ISpawnWorldInfo.class)
@Implements(@Interface(iface = WorldProperties.class, prefix = "worldProperties$"))
public interface ISpawnWorldInfoMixin_API extends IWorldInfoMixin_API, WorldProperties {

    // @formatter:off
    @Shadow void shadow$setXSpawn(int p_76058_1_);
    @Shadow void shadow$setYSpawn(int p_76056_1_);
    @Shadow void shadow$setZSpawn(int p_76087_1_);
    @Shadow void shadow$setSpawnAngle(float p_241859_1_);
    @Shadow void shadow$setSpawn(BlockPos p_176143_1_, float p_176143_2_);

    // @formatter:on

    @Override
    default void setSpawnPosition(final Vector3i position) {
        Objects.requireNonNull(position, "position");

        this.shadow$setSpawn(VecHelper.toBlockPos(position), this.shadow$getSpawnAngle());
    }
}
