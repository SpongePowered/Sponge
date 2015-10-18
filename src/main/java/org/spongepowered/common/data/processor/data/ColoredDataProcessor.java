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

import java.awt.Color;
import java.util.Optional;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeColoredData;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.util.ColorUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorldNameable;

public class ColoredDataProcessor extends AbstractSpongeDataProcessor<ColoredData, ImmutableColoredData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntitySheep || dataHolder instanceof ItemStack || dataHolder instanceof IWorldNameable;
    }

    @Override
    public Optional<ColoredData> from(DataHolder dataHolder) {
        Color color = null;
        if (dataHolder instanceof EntitySheep) {
            color = ColorUtil.colorFromDyeColor(((EntitySheep) dataHolder).getFleeceColor());
        } else if (dataHolder instanceof ItemStack) {
            final ItemStack stack = (ItemStack) dataHolder;
            final Item item = stack.getItem();
            final int meta = stack.getItemDamage();
            if (item == Items.dye) {
                // Interpret dyes via "dye damage"
                color = ColorUtil.colorFromDyeColor(EnumDyeColor.byDyeDamage(meta));
            } else if (item instanceof ItemBlock) {
                final Block block = ((ItemBlock) item).getBlock();
                if (block == Blocks.wool) {
                    // Interpret wools via metadata
                    color = ColorUtil.colorFromDyeColor(EnumDyeColor.byMetadata(meta));
                }
            } else if (item instanceof ItemArmor) {
                color = new Color(((ItemArmor) item).getColor(stack));
            } else if (stack.hasTagCompound()) {
                final NBTTagCompound tag = stack.getTagCompound();
                if (tag.hasKey("display", NbtDataUtil.TAG_COMPOUND)) {
                    final NBTTagCompound display = tag.getCompoundTag("display");
                    if (display.hasKey("color", NbtDataUtil.TAG_INT)) {
                        color = new Color(display.getInteger("color"));
                    }
                }
            }
        }
        return Optional.ofNullable(color).map(SpongeColoredData::new);
    }

    @Override
    public Optional<ColoredData> fill(DataHolder dataHolder, ColoredData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final ColoredData data = from(dataHolder).orElse(null);
            final ColoredData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Color color = newData.color().get();
            return Optional.of(manipulator.set(Keys.COLOR, color));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ColoredData> fill(DataContainer container, ColoredData colorData) {
        final Color color = DataUtil.getData(container, Keys.COLOR, Color.class);
        return Optional.of(colorData.set(Keys.COLOR, color));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, ColoredData manipulator, MergeFunction function) {
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableColoredData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableColoredData immutable) {
        if (key == Keys.COLOR) {
            return Optional.of(new ImmutableSpongeColoredData((Color) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        // color cannot be removed
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<ColoredData> createFrom(DataHolder dataHolder) {
        return from(dataHolder);
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Entity.class.isAssignableFrom(entityType.getEntityClass());
    }

}
