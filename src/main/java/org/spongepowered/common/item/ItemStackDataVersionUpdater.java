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
package org.spongepowered.common.item;

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.util.Constants;

public final class ItemStackDataVersionUpdater implements DataContentUpdater {

    public static final DataContentUpdater INSTANCE = new ItemStackDataVersionUpdater();

    @Override
    public int inputVersion() {
        return Constants.ItemStack.Data.DATA_COMPONENTS;
    }

    @Override
    public int outputVersion() {
        return Constants.ItemStack.Data.DATA_VERSIONED;
    }

    @Override
    public DataView update(final DataView content) {
        final boolean isAir = content.getRegistryValue(Constants.ItemStack.V3.TYPE, RegistryTypes.ITEM_TYPE)
            .map(i -> i == ItemTypes.AIR.get())
            .orElse(false);

        final DataContainer updated = DataContainer.createNew();
        updated.set(Queries.CONTENT_VERSION, this.outputVersion());
        updated.set(Constants.ItemStack.V4.DATA_VERSION, 3833); // The version from the previous ItemStackDataComponentsUpdater.

        if (isAir) {
            return updated.set(Constants.ItemStack.V4.DATA, DataContainer.createNew());
        }

        final DataContainer data = DataContainer.createNew()
            .set(Constants.ItemStack.V3.TYPE, content.getString(Constants.ItemStack.V3.TYPE).get())
            .set(Constants.ItemStack.V3.COUNT, content.getInt(Constants.ItemStack.V3.COUNT).get());

        content.getView(Constants.ItemStack.V3.COMPONENTS)
            .ifPresent(components -> data.set(Constants.ItemStack.V3.COMPONENTS, components));

        return updated.set(Constants.ItemStack.V4.DATA, data);
    }
}
