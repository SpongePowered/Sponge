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
package org.spongepowered.common.registry.type.item;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.inventory.property.GuiId;
import org.spongepowered.api.item.inventory.property.GuiIds;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeGuiId;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(GuiIds.class)
public class GuiIdRegistryModule extends AbstractCatalogRegistryModule<GuiId> implements AdditionalCatalogRegistryModule<GuiId> {

    public static GuiIdRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        this.register(CatalogKey.minecraft("chest"));
        this.register(CatalogKey.minecraft("furnace"));
        this.register(CatalogKey.minecraft("dispenser"));
        this.register(CatalogKey.minecraft("crafting_table"));
        this.register(CatalogKey.minecraft("brewing_stand"));
        this.register(CatalogKey.minecraft("hopper"));
        this.register(CatalogKey.minecraft("beacon"));
        this.register(CatalogKey.minecraft("enchanting_table"));
        this.register(CatalogKey.minecraft("anvil"));
        this.register(CatalogKey.minecraft("villager"));
        this.register(CatalogKey.minecraft("horse"), "EntityHorse");
        this.register(CatalogKey.minecraft("shulker_box"));
    }

    private void register(final CatalogKey key) {
        this.map.put(key, new SpongeGuiId(key));
    }

    private void register(final CatalogKey key, final String internalId) {
        this.map.put(key, new SpongeGuiId(key, internalId));
    }

    @Override
    public void registerAdditionalCatalog(GuiId guiId) {
        if (this.map.containsKey(guiId.getKey())) {
            throw new IllegalArgumentException("GuiId is already registered");
        }
        this.map.put(guiId.getKey(), guiId);
    }

    private GuiIdRegistryModule() {
    }

    private static final class Holder {
        static final GuiIdRegistryModule INSTANCE = new GuiIdRegistryModule();
    }
}
