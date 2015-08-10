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
package org.spongepowered.common.data.processor;

import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.item.DurabilityData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.item.SpongeDurabilityData;

public class SpongeDurabilityDataProcessor implements SpongeDataProcessor<DurabilityData> {

    @Override
    public Optional<DurabilityData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public DurabilityData create() {
        return new SpongeDurabilityData(0);
    }

    @Override
    public Optional<DurabilityData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final Item item = ((ItemStack) dataHolder).getItem();
            if (item instanceof ItemTool || item instanceof ItemArmor || item instanceof ItemSword) {
                final DurabilityData durabilityData = new SpongeDurabilityData(item.getMaxDamage());
                durabilityData.setDurability(((ItemStack) dataHolder).getItemDamage())
                        .setBreakable(((ItemStack) dataHolder).isItemStackDamageable());
                return Optional.of(durabilityData);
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<DurabilityData> fillData(DataHolder dataHolder, DurabilityData manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DurabilityData manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && (((ItemStack) dataHolder).getItem() instanceof ItemArmor || ((ItemStack) dataHolder).getItem()
                instanceof ItemSword|| ((ItemStack) dataHolder).getItem() instanceof ItemTool)) {
            final DurabilityData oldData = createFrom(dataHolder).get();
            // TODO at a later time.

        }
        return fail(manipulator);
    }

    @Override
    public Optional<DurabilityData> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }
}
