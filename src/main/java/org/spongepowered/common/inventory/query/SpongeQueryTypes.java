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
package org.spongepowered.common.inventory.query;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.query.type.GridQuery;
import org.spongepowered.common.inventory.query.type.KeyValueMatcherQuery;
import org.spongepowered.common.inventory.query.type.InventoryTypeQuery;
import org.spongepowered.common.inventory.query.type.ItemStackCustomQuery;
import org.spongepowered.common.inventory.query.type.ItemStackExactQuery;
import org.spongepowered.common.inventory.query.type.ItemStackIgnoreQuantityQuery;
import org.spongepowered.common.inventory.query.type.ItemTypeQuery;
import org.spongepowered.common.inventory.query.type.LensQuery;
import org.spongepowered.common.inventory.query.type.PlayerPrimaryHotbarFirstQuery;
import org.spongepowered.common.inventory.query.type.ReverseQuery;
import org.spongepowered.common.inventory.query.type.SlotLensQuery;
import org.spongepowered.common.inventory.query.type.TypeQuery;
import org.spongepowered.common.inventory.query.type.UnionQuery;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class SpongeQueryTypes {

    // Internal only query types
    public static final QueryType.OneParam<Lens> LENS = new SpongeOneParamQueryType<>("lens", LensQuery::new);

    public static final QueryType.OneParam<ImmutableSet<Inventory>> SLOT_LENS = new SpongeOneParamQueryType<>("slot_lens", SlotLensQuery::new);

    public static final QueryType.OneParam<Inventory> UNION = new SpongeOneParamQueryType<>("union", UnionQuery::new);

    // API query types
    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry.registerSupplier(QueryType.OneParam.class, "inventory_type", () -> new SpongeOneParamQueryType<>("inventory_type", InventoryTypeQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "item_stack_custom", () -> new SpongeOneParamQueryType<>("item_stack_custom", ItemStackCustomQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "item_stack_exact", () -> new SpongeOneParamQueryType<>("item_stack_exact", ItemStackExactQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "item_stack_ignore_quantity", () -> new SpongeOneParamQueryType<>("item_stack_ignore_quantity", ItemStackIgnoreQuantityQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "item_type", () -> new SpongeOneParamQueryType<>("item_type", ItemTypeQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "key_value", () -> new SpongeOneParamQueryType<KeyValueMatcher<?>>("key_value", KeyValueMatcherQuery::new));
        registry.registerSupplier(QueryType.OneParam.class, "type", () -> new SpongeOneParamQueryType<>("type", TypeQuery::new));
        registry.registerSupplier(QueryType.NoParam.class, "player_primary_hotbar_first", PlayerPrimaryHotbarFirstQuery::new);
        registry.registerSupplier(QueryType.NoParam.class, "reverse", ReverseQuery::new);
        registry.registerSupplier(QueryType.TwoParam.class, "grid", () -> new SpongeTwoParamQueryType<>("grid", GridQuery::new));
    }
}
