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
package org.spongepowered.common.data.provider.inventory;

import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.data.provider.inventory.lens.EquipmentTypeDataProvider;
import org.spongepowered.common.data.provider.inventory.lens.SlotIndexDataProvider;
import org.spongepowered.common.data.provider.inventory.lens.SlotPositionDataProvider;
import org.spongepowered.common.data.provider.inventory.lens.SlotSideDataProvider;
import org.spongepowered.common.data.provider.inventory.root.MaxStackSizeDataProvider;
import org.spongepowered.common.data.provider.inventory.root.PluginDataProvider;
import org.spongepowered.common.data.provider.inventory.root.TitleDataProvider;
import org.spongepowered.common.data.provider.inventory.root.UniqueIdDataProvider;

public class InventoryDataProviders extends DataProviderRegistryBuilder {

    public InventoryDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    protected void register() {
       this.register(new EquipmentTypeDataProvider());
       this.register(new SlotIndexDataProvider());
       this.register(new SlotPositionDataProvider());
       this.register(new SlotSideDataProvider());
       this.register(new MaxStackSizeDataProvider());
       this.register(new PluginDataProvider());
       this.register(new TitleDataProvider());
       this.register(new UniqueIdDataProvider());
    }


}
