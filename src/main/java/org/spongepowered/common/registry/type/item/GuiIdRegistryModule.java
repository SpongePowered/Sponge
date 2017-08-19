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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.inventory.property.GuiId;
import org.spongepowered.api.item.inventory.property.GuiIds;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeGuiId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class GuiIdRegistryModule implements CatalogRegistryModule<GuiId>, AdditionalCatalogRegistryModule<GuiId> {

    public static GuiIdRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(GuiIds.class)
    private final Map<String, GuiId> guiIdMap = new HashMap<>();

    @Override
    public Optional<GuiId> getById(String id) {
        return Optional.ofNullable(this.guiIdMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<GuiId> getAll() {
        return ImmutableList.copyOf(this.guiIdMap.values());
    }

    @Override
    public void registerDefaults() {
        this.register("minecraft:chest");
        this.register("minecraft:furnace");
        this.register("minecraft:dispenser");
        this.register("minecraft:crafting_table");
        this.register("minecraft:brewing_stand");
        this.register("minecraft:hopper");
        this.register("minecraft:beacon");
        this.register("minecraft:enchanting_table");
        this.register("minecraft:anvil");
        this.register("minecraft:villager");
        this.register("minecraft:horse", "EntityHorse");
        this.register("minecraft:shulker_box");
    }

    private void register(String id) {
        this.guiIdMap.put(id, new SpongeGuiId(id));
    }

    private void register(String id, String internalId) {
        this.guiIdMap.put(id, new SpongeGuiId(id, internalId));
    }

    @Override
    public void registerAdditionalCatalog(GuiId guiId) {
        if (this.guiIdMap.containsKey(guiId.getId())) {
            throw new IllegalArgumentException("GuiId is already registered");
        }
        this.guiIdMap.put(guiId.getId(), guiId);
    }

    private GuiIdRegistryModule() {
    }

    private static final class Holder {
        static final GuiIdRegistryModule INSTANCE = new GuiIdRegistryModule();
    }
}
