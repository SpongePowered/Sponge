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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class ItemDyeColorDataProcessor extends AbstractItemSingleDataProcessor<DyeColor, Value<DyeColor>, DyeableData, ImmutableDyeableData> {

    public ItemDyeColorDataProcessor() {
        super(x -> isDyeable(x.getItem()), Keys.DYE_COLOR);
    }

    public static boolean isDyeable(Item item) {
        if (item.equals(Items.field_151100_aR) || item.equals(Items.field_151104_aV)) {
            return true;
        }

        final Block block = Block.getBlockFromItem(item);

        return block.equals(Blocks.field_150325_L)
                || block.equals(Blocks.field_180393_cK)
                || block.equals(Blocks.field_150399_cn)
                || block.equals(Blocks.field_150404_cg)
                || block.equals(Blocks.field_150397_co)
                || block.equals(Blocks.field_150406_ce)
                || block.equals(Blocks.field_192443_dR)
                || block.equals(Blocks.field_192444_dS);
    }

    @Override
    protected Value<DyeColor> constructValue(DyeColor actualValue) {
        return SpongeValueFactory.getInstance().createValue(Keys.DYE_COLOR, actualValue, DyeColors.BLACK);
    }

    @Override
    protected boolean set(ItemStack container, DyeColor value) {
        Item item = container.getItem();

        if(item.equals(Items.field_151100_aR) || item.equals(Items.field_179564_cE)) {
            container.func_77964_b(((net.minecraft.item.DyeColor) (Object) value).func_176767_b());
        } else {
            container.func_77964_b(((net.minecraft.item.DyeColor) (Object) value).func_176765_a());
        }
        return true;
    }

    @Override
    protected Optional<DyeColor> getVal(ItemStack container) {
        Item item = container.getItem();

        if(item.equals(Items.field_151100_aR) || item.equals(Items.field_179564_cE)) {
            return Optional.of((DyeColor) (Object) net.minecraft.item.DyeColor.func_176766_a(container.getDamage()));
        }
        return Optional.of((DyeColor) (Object) net.minecraft.item.DyeColor.func_176764_b(container.getDamage()));
    }

    @Override
    protected ImmutableValue<DyeColor> constructImmutableValue(DyeColor value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected DyeableData createManipulator() {
        return new SpongeDyeableData();
    }
}
