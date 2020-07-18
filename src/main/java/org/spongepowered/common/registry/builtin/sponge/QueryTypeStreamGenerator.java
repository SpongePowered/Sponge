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
package org.spongepowered.common.registry.builtin.sponge;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.query.SpongeOneParamQueryType;
import org.spongepowered.common.inventory.query.SpongeTwoParamQueryType;
import org.spongepowered.common.inventory.query.type.GridQuery;
import org.spongepowered.common.inventory.query.type.InventoryTypeQuery;
import org.spongepowered.common.inventory.query.type.ItemStackCustomQuery;
import org.spongepowered.common.inventory.query.type.ItemStackExactQuery;
import org.spongepowered.common.inventory.query.type.ItemStackIgnoreQuantityQuery;
import org.spongepowered.common.inventory.query.type.ItemTypeQuery;
import org.spongepowered.common.inventory.query.type.KeyValueMatcherQuery;
import org.spongepowered.common.inventory.query.type.LensQuery;
import org.spongepowered.common.inventory.query.type.PlayerPrimaryHotbarFirstQuery;
import org.spongepowered.common.inventory.query.type.ReverseQuery;
import org.spongepowered.common.inventory.query.type.SlotLensQuery;
import org.spongepowered.common.inventory.query.type.TypeQuery;
import org.spongepowered.common.inventory.query.type.UnionQuery;

import java.util.stream.Stream;

public final class QueryTypeStreamGenerator {

    private QueryTypeStreamGenerator() {
    }

    // Internal only query types
    public static final QueryType.OneParam<Lens> LENS = new SpongeOneParamQueryType<>(ResourceKey.sponge("lens"), LensQuery::new);
    public static final QueryType.OneParam<ImmutableSet<Inventory>> SLOT_LENS = new SpongeOneParamQueryType<>(ResourceKey.sponge("slot_lens"), SlotLensQuery::new);
    public static final QueryType.OneParam<Inventory> UNION = new SpongeOneParamQueryType<>(ResourceKey.sponge("union"), UnionQuery::new);

    public static Stream<QueryType> stream() {
        return Stream.of(
                new PlayerPrimaryHotbarFirstQuery(ResourceKey.sponge("player_primary_hotbar_first")),
                new ReverseQuery(ResourceKey.sponge("reverse")),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("inventory_type"), InventoryTypeQuery::new),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("item_stack_custom"), ItemStackCustomQuery::new),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("item_stack_exact"), ItemStackExactQuery::new),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("item_stack_ignore_quantity"), ItemStackIgnoreQuantityQuery::new),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("item_type"), ItemTypeQuery::new),
                new SpongeOneParamQueryType<KeyValueMatcher<?>>(ResourceKey.sponge("key_value"), KeyValueMatcherQuery::new),
                new SpongeOneParamQueryType<>(ResourceKey.sponge("type"), TypeQuery::new),
                new SpongeTwoParamQueryType<>(ResourceKey.sponge("grid"), GridQuery::new)
        );
    }
}
