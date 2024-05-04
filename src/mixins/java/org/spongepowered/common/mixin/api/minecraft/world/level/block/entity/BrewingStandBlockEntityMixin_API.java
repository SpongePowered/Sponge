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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.api.block.entity.carrier.BrewingStand;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin_API extends BaseContainerBlockEntityMixin_API implements BrewingStand {

    // @formatter:off
    @Shadow private NonNullList<ItemStack> items;

    @Shadow private static void shadow$doBrew(final Level param0, final BlockPos param1, final NonNullList<ItemStack> param2) {};
    @Shadow private static boolean shadow$isBrewable(PotionBrewing $$0, NonNullList<ItemStack> $$1) {
        return false;
    }
    // @formatter:on

    @Override
    public boolean brew() {
        if (BrewingStandBlockEntityMixin_API.shadow$isBrewable(this.level.potionBrewing(), this.items)) {
            BrewingStandBlockEntityMixin_API.shadow$doBrew(this.level, this.shadow$getBlockPos(), this.items);
            return true;
        }
        return false;
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.fuel().asImmutable());
        values.add(this.remainingBrewTime().asImmutable());

        return values;
    }

}
