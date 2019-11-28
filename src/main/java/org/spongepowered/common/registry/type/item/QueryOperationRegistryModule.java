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

import org.spongepowered.api.item.inventory.query.QueryOperationType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.inventory.query.SpongeQueryType;
import org.spongepowered.common.inventory.query.SpongeQueryTypes;
import org.spongepowered.common.inventory.query.type.InventoryPropertyMatcherQuery;
import org.spongepowered.common.inventory.query.type.InventoryTranslationQuery;
import org.spongepowered.common.inventory.query.type.InventoryTypeQuery;
import org.spongepowered.common.inventory.query.type.ItemStackCustomQuery;
import org.spongepowered.common.inventory.query.type.ItemStackExactQuery;
import org.spongepowered.common.inventory.query.type.ItemStackIgnoreQuantityQuery;
import org.spongepowered.common.inventory.query.type.ItemTypeQuery;
import org.spongepowered.common.inventory.query.type.TypeQuery;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class QueryOperationRegistryModule implements CatalogRegistryModule<QueryOperationType> {

    @RegisterCatalog(QueryTypes.class)
    private final Map<String, QueryOperationType> types = new HashMap<>();

    @Override
    public Optional<QueryOperationType> getById(String id) {
        return Optional.ofNullable(this.types.get(id));
    }

    @Override
    public Collection<QueryOperationType> getAll() {
        return this.types.values();
    }

    @Override
    public void registerDefaults() {
        register(new SpongeQueryType<>("inventory_type", InventoryTypeQuery::new));
        register(new SpongeQueryType<>("type", TypeQuery::new));
        register(new SpongeQueryType<>("item_type", ItemTypeQuery::new));
        register(new SpongeQueryType<>("item_stack_ignore_quantity", ItemStackIgnoreQuantityQuery::new));
        register(new SpongeQueryType<>("item_stack_exact", ItemStackExactQuery::new));
        register(new SpongeQueryType<>("item_stack_custom", ItemStackCustomQuery::new));
        register(new SpongeQueryType<>("inventory_property", InventoryPropertyMatcherQuery::new));
        register(new SpongeQueryType<>("inventory_translation", InventoryTranslationQuery::new));

        register(SpongeQueryTypes.LENS);
        register(SpongeQueryTypes.SLOT_LENS);
    }

    private void register(QueryOperationType<?> type) {
        this.types.put(type.getId(), type);
    }
}
