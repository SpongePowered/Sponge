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
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.material.Material;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;

import java.util.Set;

public class DefaultTeleportHelperFilter implements TeleportHelperFilter {

    // Materials it is NOT safe to put players on top of.
    private static final Set<Material> NOT_SAFE_FLOOR = ImmutableSet.of(Material.AIR, Material.CACTUS, Material.FIRE, Material.LAVA);

    @Override
    public boolean isSafeFloorMaterial(BlockState blockState) {
        return !DefaultTeleportHelperFilter.NOT_SAFE_FLOOR.contains(((net.minecraft.world.level.block.state.BlockState) blockState).getMaterial());
    }

    @Override
    public boolean isSafeBodyMaterial(BlockState blockState) {
        net.minecraft.world.level.block.state.BlockState state = (net.minecraft.world.level.block.state.BlockState) blockState;
        Material material = state.getMaterial();

        // Deny blocks that suffocate
        if (state.isSuffocating(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            return false;
        }
        // Deny dangerous lava
        if (material == Material.LAVA) {
            return false;
        }

        // Sadly there is no easy way to check for this using vanilla right now as Blocks like Cauldron are technically marked as passable.

        // Deny non-passable non "full" blocks
        return !(state.getBlock() instanceof SlabBlock ||
                 state.getBlock() instanceof CauldronBlock ||
                 state.getBlock() instanceof AnvilBlock ||
                 state.getBlock() instanceof FenceBlock ||
                 state.getBlock() instanceof ChorusPlantBlock ||
                 state.getBlock() instanceof SnowLayerBlock ||
                 material == Material.GLASS ||
                 material == Material.LEAVES);
    }
}
