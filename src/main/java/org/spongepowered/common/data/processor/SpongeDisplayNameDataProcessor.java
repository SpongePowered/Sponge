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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.item.ItemsHelper.getTagCompound;

import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IWorldNameable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.base.DisplayNameComponent;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.base.SpongeDisplayNameComponent;
import org.spongepowered.common.text.SpongeTexts;

public class SpongeDisplayNameDataProcessor implements SpongeDataProcessor<DisplayNameComponent> {

    @Override
    public Optional<DisplayNameComponent> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public DisplayNameComponent create() {
        return new SpongeDisplayNameComponent();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Optional<DisplayNameComponent> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((Entity) dataHolder).hasCustomName()) {
                final DisplayNameComponent data = create();
                data.setValue(SpongeTexts.toText(((Entity) dataHolder).getDisplayName()));
                data.setCustomNameVisible(((Entity) dataHolder).getAlwaysRenderNameTag());
                return Optional.of(data);
            } else {
                return Optional.of(create());
            }
        } else if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() == Items.written_book) {
                final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getTagCompound();
                final String titleString = mainCompound.getString("title");
                final DisplayNameComponent data = create();
                data.setValue(Texts.fromLegacy(titleString));
                data.setCustomNameVisible(true);
                return Optional.of(data);
            }
            final NBTTagCompound mainCompound = ((ItemStack) dataHolder).getSubCompound("display", false);
            if (mainCompound != null && mainCompound.hasKey("Name", 8)) {
                final String displayString = mainCompound.getString("Name");
                final DisplayNameComponent data = new SpongeDisplayNameComponent();
                System.err.println("The retrieved displayname from an item stack was: " + displayString);
                data.setValue(Texts.fromLegacy(displayString));
                data.setCustomNameVisible(true);
                return Optional.of(data);
            } else {
                return Optional.of(create());
            }
        } else if (dataHolder instanceof IWorldNameable) {
            if (((IWorldNameable) dataHolder).hasCustomName()) {
                final DisplayNameComponent data = new SpongeDisplayNameComponent();
                final String customName = ((IWorldNameable) dataHolder).getCommandSenderName();
                data.setValue(Texts.fromLegacy(customName));
                data.setCustomNameVisible(true);
                return Optional.of(data);
            }
        }
        return Optional.absent();
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            if (((ItemStack) dataHolder).getItem() == Items.written_book) {
                // todo
            }
            ((ItemStack) dataHolder).clearCustomName();
            return true;
        } // todo
        return false;
    }

    @Override
    public Optional<DisplayNameComponent> fillData(DataHolder dataHolder, DisplayNameComponent manipulator, DataPriority priority) {
        return Optional.absent(); // todo
    }

    @SuppressWarnings("deprecation")
    @Override
    public DataTransactionResult setData(DataHolder dataHolder, DisplayNameComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack) {
            switch (checkNotNull(priority)) {
                case COMPONENT:
                    if (((ItemStack) dataHolder).getItem() == Items.written_book) {
                        getTagCompound((ItemStack) dataHolder).setString("title", Texts.toLegacy(manipulator.getValue()));
                    } else {
                        ((ItemStack) dataHolder).setStackDisplayName(Texts.toLegacy(manipulator.getValue()));
                    }
                    return successNoData();
                case DATA_HOLDER:
                case POST_MERGE:
                    // todo
                default:
                    return fail(manipulator);
            }

        } else if (dataHolder instanceof Entity) {
            ((Entity) dataHolder).setCustomNameTag(Texts.toLegacy(manipulator.getValue()));
            ((Entity) dataHolder).setAlwaysRenderNameTag(manipulator.isCustomNameVisible());
        }
        return fail(manipulator);
    }

    @Override
    public Optional<DisplayNameComponent> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }
}
