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
package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDyeableData;
import org.spongepowered.api.data.manipulator.mutable.DyeableData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDyeableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDyeableData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataProcessor;

import java.util.Optional;

public class DyeableDataProcessor extends AbstractSingleDataProcessor<DyeColor, Value<DyeColor>, DyeableData, ImmutableDyeableData> {

    public DyeableDataProcessor() {
        super(Keys.DYE_COLOR);
    }

    public static boolean isDyeable(Item item) {
        if (item.equals(Items.DYE)) {
            return true;
        }

        Block block = Block.getBlockFromItem(item);
        return block != null
            && (block.equals(Blocks.WOOL)
                || block.equals(Blocks.STAINED_GLASS)
                || block.equals(Blocks.STAINED_GLASS_PANE)
                || block.equals(Blocks.STAINED_HARDENED_CLAY));
    }

    @Override
    protected DyeableData createManipulator() {
        return new SpongeDyeableData();
    }

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityWolf || dataHolder instanceof EntitySheep || dataHolder instanceof ItemStack &&
                isDyeable(((ItemStack) dataHolder).getItem());
    }

    @Override
    public Optional<DyeableData> from(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            if (dataHolder instanceof EntitySheep) {
                return Optional.of(new SpongeDyeableData((DyeColor) (Object) ((EntitySheep) dataHolder).getFleeceColor()));
            } else if (dataHolder instanceof EntityWolf) {
                return Optional.of(new SpongeDyeableData((DyeColor) (Object) ((EntityWolf) dataHolder).getCollarColor()));
            } else if (dataHolder instanceof ItemStack) {
                if(((ItemStack) dataHolder).getItem().equals(Items.DYE)) {
                    return Optional.of(new SpongeDyeableData((DyeColor) (Object) EnumDyeColor.byDyeDamage(((ItemStack) dataHolder).getItemDamage())));
                }
                return Optional.of(new SpongeDyeableData((DyeColor) (Object) EnumDyeColor.byMetadata(((ItemStack) dataHolder).getItemDamage())));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<DyeableData> fill(DataContainer container, DyeableData dyeableData) {
        Optional<String> id = container.getString(Keys.DYE_COLOR.getQuery());
        if (id.isPresent()) {
            dyeableData.set(Keys.DYE_COLOR, Sponge.getGame().getRegistry().getType(DyeColor.class, id.get()).get());
            return Optional.of(dyeableData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, DyeableData manipulator, MergeFunction function) {
        if (supports(dataHolder)) {
            DyeableData origin = from(dataHolder).get();
            DyeableData merged = checkNotNull(function, "function").merge(origin, manipulator);
            if (dataHolder instanceof EntitySheep) {
                ((EntitySheep) dataHolder).setFleeceColor((EnumDyeColor) (Object) merged.type().get());
                return DataTransactionResult.successReplaceResult(origin.type().asImmutable(), merged.type().asImmutable());
            } else if (dataHolder instanceof EntityWolf) {
                ((EntityWolf) dataHolder).setCollarColor(((EnumDyeColor) (Object) merged.type().get()));
                return DataTransactionResult.successReplaceResult(origin.type().asImmutable(), merged.type().asImmutable());
            } else if (dataHolder instanceof ItemStack) {
                if(((ItemStack) dataHolder).getItem().equals(Items.DYE)) {
                    ((ItemStack) dataHolder).setItemDamage(((EnumDyeColor) (Object) merged.type().get()).getDyeDamage());
                } else {
                    ((ItemStack) dataHolder).setItemDamage(((EnumDyeColor) (Object) merged.type().get()).getMetadata());
                }
                return DataTransactionResult.successReplaceResult(origin.type().asImmutable(), merged.type().asImmutable());
            }
        }
        return DataTransactionResult.failResult(manipulator.type().asImmutable());
    }

    @Override
    public Optional<ImmutableDyeableData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableDyeableData immutable) {
        if (key.equals(Keys.DYE_COLOR)) {
            return Optional.of(new ImmutableSpongeDyeableData((DyeColor) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
