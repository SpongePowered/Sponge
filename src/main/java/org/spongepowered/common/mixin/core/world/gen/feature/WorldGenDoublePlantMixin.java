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
package org.spongepowered.common.mixin.core.world.gen.feature;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.DoublePlantFeature;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.feature.WorldGenDoublePlantBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(DoublePlantFeature.class)
public abstract class WorldGenDoublePlantMixin extends WorldGeneratorMixin implements WorldGenDoublePlantBridge {

    @Shadow private DoublePlantBlock.EnumPlantType plantType;

    @Nullable private Extent impl$currentExtent = null;

    private DoublePlantType impl$getType(final Vector3i position, final Random rand) {
        final Optional<Function<Location<Extent>, DoublePlantType>> override = ((DoublePlant) this).getSupplierOverride();
        if (override.isPresent() && this.impl$currentExtent != null) {
            final Location<Extent> pos = new Location<>(this.impl$currentExtent, position);
            return override.get().apply(pos);
        }
        final List<DoublePlantType> result = ((DoublePlant) this).getPossibleTypes().get(rand);
        if (result.isEmpty()) {
            return DoublePlantTypes.GRASS;
        }
        return result.get(0);
    }

    /**
     * @author Deamon - December 12th, 2015
     *
     * @reason Completely changes the method to leverage the WeightedTable
     *         types. This method was almost completely rewritten.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    @Overwrite
    public boolean generate(final World worldIn, final Random rand, final BlockPos position) {
        boolean flag = false;

        if (((DoublePlant) this).getPossibleTypes().isEmpty()) {
            ((DoublePlant) this).getPossibleTypes().add(new WeightedObject<>((DoublePlantType) (Object) this.plantType, 1));
        }
        for (int i = 0; i < 8; ++i) {
            final BlockPos next = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4),
                    rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(next) && (!worldIn.dimension.isNether() || next.getY() < 254)
                    && Blocks.field_150398_cm.func_176196_c(worldIn, next)) {
                final DoublePlantType type = impl$getType(VecHelper.toVector3i(next), rand);
                Blocks.field_150398_cm.func_176491_a(worldIn, next,
                        (DoublePlantBlock.EnumPlantType) (Object) type, 2);
                flag = true;
            }
        }

        return flag;
    }

    @Override
    public void bridge$setCurrentExtent(@Nullable final Extent extent) {
        this.impl$currentExtent = extent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "DoublePlant")
                .add("PerChunk", ((DoublePlant) this).getPlantsPerChunk())
                .toString();
    }
}
