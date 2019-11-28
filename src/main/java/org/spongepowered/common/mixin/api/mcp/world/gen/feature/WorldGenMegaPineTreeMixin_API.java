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
package org.spongepowered.common.mixin.api.mcp.world.gen.feature;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.HugeTreesFeature;
import net.minecraft.world.gen.feature.MegaPineTree;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(MegaPineTree.class)
public abstract class WorldGenMegaPineTreeMixin_API extends HugeTreesFeature implements PopulatorObject {

    @Shadow @Final private boolean useBaseHeight;

    @Nullable private String api$id;
    @Nullable private String api$name;

    public WorldGenMegaPineTreeMixin_API(final boolean notify, final int baseHeightIn, final int extraRandomHeightIn, final BlockState woodMetadataIn,
        final BlockState leavesMetadataIn) {
        super(notify, baseHeightIn, extraRandomHeightIn, woodMetadataIn, leavesMetadataIn);
    }


    @Override
    public String getId() {
        if (this.api$id == null) {
            this.api$id = this.useBaseHeight ? "minecraft:mega_tall_taiga" : "minecraft:mega_pointy_taiga";
        }
        return this.api$id;
    }

    @Override
    public String getName() {
        if (this.api$name == null) {
            this.api$name = this.useBaseHeight ? "Mega tall taiga tree" : "Mega pointy taiga tree";
        }
        return this.api$name;
    }

    @Override
    public boolean canPlaceAt(final World world, final int x, final int y, final int z) {
        return this.ensureGrowable((net.minecraft.world.World) world, null, new BlockPos(x, y, z), this.baseHeight);
    }

    @Override
    public void placeObject(final World world, final Random random, final int x, final int y, final int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        setDecorationDefaults();
        if (generate((net.minecraft.world.World) world, random, pos)) {
            generateSaplings((net.minecraft.world.World) world, random, pos);
        }
    }

}
