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
package org.spongepowered.common.data.utils;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.world.IWorldNameable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.DisplayNameData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.SpongeDisplayNameData;
import org.spongepowered.common.text.SpongeChatComponent;

public class SpongeDisplayNameDataBuilder implements SpongeDataUtil<DisplayNameData> {

    @Override
    public Optional<DisplayNameData> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public DisplayNameData create() {
        return new SpongeDisplayNameData();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Optional<DisplayNameData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                final DisplayNameData data = create();
                data.setDisplayName(((SpongeChatComponent) ((Entity) dataHolder).getDisplayName()).toText());
                return Optional.of(data);
            }
        } else if (dataHolder instanceof ItemStack) {
            final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getSubCompound("display", false);
            if (mainCompound != null && mainCompound.hasKey("Name", 8)) {
                final String displayString = mainCompound.getString("Name");
                final DisplayNameData data = new SpongeDisplayNameData();
                System.err.println("The retrieved displayname from an item stack was: " + displayString);
                data.setDisplayName(Texts.fromLegacy(displayString));
                data.setCustomNameVisible(true);
                return Optional.of(data);
            }
        } else if (dataHolder instanceof IWorldNameable) {
            if (((IWorldNameable) dataHolder).hasCustomName()) {
                final DisplayNameData data = new SpongeDisplayNameData();
                final String customName = ((IWorldNameable) dataHolder).getCommandSenderName();
                data.setDisplayName(Texts.fromLegacy(customName));
                return Optional.of(data);
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder, DisplayNameData manipulator) {
        return null;
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DisplayNameData manipulator, DataPriority priority) {

        return null;
    }
}
