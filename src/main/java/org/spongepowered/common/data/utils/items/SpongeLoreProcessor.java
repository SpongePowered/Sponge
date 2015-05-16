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
package org.spongepowered.common.data.utils.items;

import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;

import com.google.common.base.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.item.LoreData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulators.items.SpongeLoreData;
import org.spongepowered.common.interfaces.text.IMixinText;

import java.util.Locale;

public class SpongeLoreProcessor implements SpongeDataProcessor<LoreData> {

    @Override
    public Optional<LoreData> fillData(DataHolder holder, LoreData manipulator, DataPriority priority) {
        if (holder instanceof ItemStack) {

        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, LoreData manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack) {
            final NBTTagList loreList = new NBTTagList();
            for (Text text : manipulator.getAll()) {
                loreList.appendTag(new NBTTagString(((IMixinText) text).toLegacy('\247', Locale.ENGLISH)));
            }
            ((ItemStack) dataHolder).getSubCompound("display", true).setTag("Lore", loreList);
            return successNoData();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<LoreData> build(DataView container) throws InvalidDataException {
        return Optional.absent();
    }

    @Override
    public LoreData create() {
        return new SpongeLoreData();
    }

    @Override
    public Optional<LoreData> createFrom(DataHolder dataHolder) {
        return Optional.absent();
    }

    @Override
    public Optional<LoreData> getFrom(DataHolder holder) {
        return Optional.absent();
    }
}
