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

import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.Item;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.base.RepresentedItemComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.base.SpongeRepresentedItemComponent;
import org.spongepowered.common.item.SpongeItemStackBuilder;

public class SpongeRepresentedItemProcessor implements SpongeDataProcessor<RepresentedItemComponent> {

    @Override
    public Optional<RepresentedItemComponent> fillData(DataHolder dataHolder, RepresentedItemComponent manipulator, DataPriority priority) {
        // todo
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, RepresentedItemComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof EntityItem) {
            final ItemStack underlying = ((ItemStack) ((EntityItem) dataHolder).getEntityItem());
            final ItemStack clone = new SpongeItemStackBuilder().fromItemStack(underlying).build();
            final RepresentedItemComponent old = create();
            old.setValue(clone);

            final ItemStack newItem = manipulator.getValue();
            ((EntityItem) dataHolder).setEntityItemStack(((net.minecraft.item.ItemStack) newItem));
            return builder().replace(old).result(DataTransactionResult.Type.SUCCESS).build();
        } else if (dataHolder instanceof EntityItemFrame) {
            final ItemStack underlying = ((ItemStack) ((EntityItemFrame) dataHolder).getDisplayedItem().copy());
            final RepresentedItemComponent old = create().setValue(underlying);
            final ItemStack newItem = manipulator.getValue();
            ((EntityItemFrame) dataHolder).setDisplayedItem(((net.minecraft.item.ItemStack) newItem).copy());
            return successReplaceData(old);
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityItemFrame)) {
            return false;
        }
        ((EntityItemFrame) dataHolder).setDisplayedItem(null);
        return true;
    }

    @Override
    public Optional<RepresentedItemComponent> build(DataView container) throws InvalidDataException {
        checkDataExists(container, Tokens.REPRESENTED_ITEM.getQuery());
        final ItemStack itemStack = container.getSerializable(Tokens.REPRESENTED_ITEM.getQuery(), ItemStack.class, Sponge.getGame().getServiceManager()
                .provide(SerializationService.class).get()).get();
        return Optional.of(create().setValue(itemStack));
    }

    @Override
    public RepresentedItemComponent create() {
        return new SpongeRepresentedItemComponent();
    }

    @Override
    public Optional<RepresentedItemComponent> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityItem) {
            final ItemStack underlying = ((ItemStack) ((EntityItem) dataHolder).getEntityItem());
            if (underlying.getItem() == null) {
                ((net.minecraft.item.ItemStack) underlying).setItem((Item) ItemTypes.STONE);
            }
            final ItemStack clone = new SpongeItemStackBuilder().fromItemStack(underlying).build();
            final RepresentedItemComponent old = create();
            old.setValue(clone);
            return Optional.of(old);
        }
        return Optional.absent();
    }

    @Override
    public Optional<RepresentedItemComponent> getFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityItem) {
            return createFrom(dataHolder);
        } else if (dataHolder instanceof EntityItemFrame) {
            final net.minecraft.item.ItemStack itemStack = ((EntityItemFrame) dataHolder).getDisplayedItem();
            if (itemStack == null) {
                return Optional.absent();
            } else {
                final RepresentedItemComponent data = create();
                return Optional.of(data.setValue((ItemStack) itemStack.copy()));
            }
        }
        return Optional.absent();
    }
}
