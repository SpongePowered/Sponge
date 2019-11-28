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

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.MinableFeature;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinableFeature.class)
public abstract class WorldGenMinableMixin extends WorldGeneratorMixin {

    @SuppressWarnings("Guava") // IntelliJ wants to migrate to Java 8's functionals, but this is an injection.
    @Inject(method = "<init>(Lnet/minecraft/block/state/IBlockState;ILcom/google/common/base/Predicate;)V", at = @At("RETURN") )
    private void onConstructed(final IBlockState ore, final int count, final Predicate<IBlockState> condition, final CallbackInfo ci) {
        ((Ore) this).setDepositSize(VariableAmount.fixed(count));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("Type", "Ore")
                .add("OreBlock", ((Ore) this).getOreBlock())
                .add("Size", ((Ore) this).getDepositSize())
                .add("PerChunk", ((Ore) this).getDepositsPerChunk())
                .add("Height", ((Ore) this).getHeight())
                .toString();
    }

}
