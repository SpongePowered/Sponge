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

import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryKeys;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.bridge.inventory.InventoryBridge;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.util.InventoryUtil;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;
import java.util.UUID;

public class InventoryDataProviders extends DataProviderRegistryBuilder {

    public InventoryDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    @Override
    protected void register() {
        // Lens Providers
        this.register(new GenericSlotLensDataProvider<>(InventoryKeys.EQUIPMENT_TYPE.get()));
        this.register(new GenericSlotLensDataProvider<>(InventoryKeys.SLOT_INDEX.get()));
        this.register(new GenericSlotLensDataProvider<>(InventoryKeys.SLOT_POSITION.get()));
        this.register(new GenericSlotLensDataProvider<>(InventoryKeys.SLOT_SIDE.get()));


        this.register(new GenericImmutableInventoryDataProvider<Integer>(InventoryKeys.MAX_STACK_SIZE.get()) {
            @Override protected Optional<Integer> getFrom(Inventory dataHolder) {
                return Optional.of(((InventoryBridge)dataHolder).bridge$getAdapter().inventoryAdapter$getFabric().fabric$getMaxStackSize());
            }
        });
        this.register(new GenericImmutableInventoryDataProvider<PluginContainer>(InventoryKeys.PLUGIN.get()) {
            @Override protected Optional<PluginContainer> getFrom(Inventory dataHolder) {
                return Optional.ofNullable(InventoryUtil.getPluginContainer(dataHolder));
            }
        });
        this.register(new GenericImmutableInventoryDataProvider<Text>(InventoryKeys.TITLE.get()) {
            @Override protected Optional<Text> getFrom(Inventory dataHolder) {
                if (dataHolder instanceof INameable) {
                    ITextComponent name = ((INameable) dataHolder).getName();
                    if (name == null) {
                        return Optional.empty();
                    }
                    return Optional.ofNullable(SpongeTexts.toText(name));
                }
                return Optional.empty();
            }
        });
        this.register(new GenericImmutableInventoryDataProvider<UUID>(InventoryKeys.UNIQUE_ID.get()) {
            @Override protected Optional<UUID> getFrom(Inventory dataHolder) {
                if (dataHolder instanceof CustomInventory) {
                    return Optional.ofNullable(((CustomInventory) dataHolder).getIdentity());
                }
                return Optional.empty();
            }
        });
    }
}
