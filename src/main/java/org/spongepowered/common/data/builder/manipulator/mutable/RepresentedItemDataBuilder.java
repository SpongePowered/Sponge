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
package org.spongepowered.common.data.builder.manipulator.mutable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.util.DataUtil;

import java.util.Optional;

public class RepresentedItemDataBuilder implements DataManipulatorBuilder<RepresentedItemData, ImmutableRepresentedItemData> {

    private final SerializationService serializationService = Sponge.getGame().getServiceManager().provide(SerializationService.class).get();

    @Override
    public Optional<RepresentedItemData> build(DataView container) throws InvalidDataException {
        DataUtil.checkDataExists(container, Keys.REPRESENTED_ITEM.getQuery());
        final ItemStackSnapshot snapshot = container.getSerializable(Keys.REPRESENTED_ITEM.getQuery(),
                                                                     ItemStackSnapshot.class,
                                                                     this.serializationService).get();
        return Optional.of(create().set(Keys.REPRESENTED_ITEM, snapshot));
    }

    @Override
    public RepresentedItemData create() {
        return new SpongeRepresentedItemData();
    }

    @Override
    public Optional<RepresentedItemData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityItemFrame) {
            final net.minecraft.item.ItemStack itemStack = ((EntityItemFrame) dataHolder).getDisplayedItem();
            if (itemStack != null) {
                return Optional.<RepresentedItemData>of(new SpongeRepresentedItemData(((ItemStack) itemStack).createSnapshot()));
            }
        } else if (dataHolder instanceof EntityItem) {
            final net.minecraft.item.ItemStack itemStack = ((EntityItem) dataHolder).getEntityItem();
            return Optional.<RepresentedItemData>of(new SpongeRepresentedItemData(((ItemStack) itemStack).createSnapshot()));
        }
        return Optional.empty();
    }
}
