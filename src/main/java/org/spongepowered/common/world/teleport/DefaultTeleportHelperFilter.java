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
package org.spongepowered.common.world.teleport;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import org.spongepowered.common.block.BlockUtil;

import java.util.Set;

public class DefaultTeleportHelperFilter implements TeleportHelperFilter {

    // Materials it is NOT safe to put players on top of.
    private static final Set<Material> NOT_SAFE_FLOOR = ImmutableSet.of(Material.AIR, Material.CACTUS, Material.FIRE, Material.LAVA);

    @Override
    public String getId() {
        return "sponge:default";
    }

    @Override
    public String getName() {
        return "Default Teleport Helper filter";
    }

    @Override
    public boolean isSafeFloorMaterial(BlockState blockState) {
        return !NOT_SAFE_FLOOR.contains(BlockUtil.toNative(blockState).getMaterial());
    }

    @Override
    public boolean isSafeBodyMaterial(BlockState blockState) {
        IBlockState state = BlockUtil.toNative(blockState);
        Material material = state.getMaterial();

        // Also avoid slabs.
        return !state.causesSuffocation() && material != Material.LAVA && !(state.getBlock() instanceof BlockSlab);
    }
}
