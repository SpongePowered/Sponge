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
package org.spongepowered.common.item.inventory;

import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;

public final class ItemStackSnapshotDuplicateManipulatorUpdater implements DataContentUpdater {

    @Override
    public int getInputVersion() {
        return Constants.Sponge.ItemStackSnapshot.DUPLICATE_MANIPULATOR_DATA_VERSION;
    }

    @Override
    public int getOutputVersion() {
        return Constants.Sponge.ItemStackSnapshot.REMOVED_DUPLICATE_DATA;
    }

    @Override
    public DataView update(DataView content) {
        if (content.contains(Constants.Sponge.UNSAFE_NBT)) {
            CompoundNBT compound = NbtTranslator.getInstance().translateData(content.getView(Constants.Sponge.UNSAFE_NBT).get());
            if (compound.contains(Constants.Sponge.SPONGE_DATA)) {
                final CompoundNBT spongeCompound = compound.getCompound(Constants.Sponge.SPONGE_DATA);
                if (spongeCompound.contains(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST)) {
                    spongeCompound.remove(Constants.Sponge.CUSTOM_MANIPULATOR_TAG_LIST);
                }
            }
            Constants.NBT.filterSpongeCustomData(compound);
            content.remove(Constants.Sponge.UNSAFE_NBT);
            if (!compound.func_82582_d()) {
                content.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translate(compound));
            }
        }
        content.set(Queries.CONTENT_VERSION, this.getOutputVersion());
        return content;
    }
}
