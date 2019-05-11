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

import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.PrimaryPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.item.inventory.query.type.PlayerPrimaryHotbarFirstQuery;
import org.spongepowered.common.item.inventory.query.type.ReverseQuery;
import org.spongepowered.common.item.inventory.query.SpongeOneParamQueryType;
import org.spongepowered.common.item.inventory.query.SpongeQueryTypes;
import org.spongepowered.common.item.inventory.query.SpongeTwoParamQueryType;
import org.spongepowered.common.item.inventory.query.type.EmptyQuery;
import org.spongepowered.common.item.inventory.query.type.GridQuery;
import org.spongepowered.common.item.inventory.query.type.InventoryPropertyMatcherQuery;
import org.spongepowered.common.item.inventory.query.type.InventoryTranslationQueryOperation;
import org.spongepowered.common.item.inventory.query.type.InventoryTypeQuery;
import org.spongepowered.common.item.inventory.query.type.ItemStackCustomQuery;
import org.spongepowered.common.item.inventory.query.type.ItemStackExactQuery;
import org.spongepowered.common.item.inventory.query.type.ItemStackIgnoreQuantityQuery;
import org.spongepowered.common.item.inventory.query.type.ItemTypeQuery;
import org.spongepowered.common.item.inventory.query.type.TypeQuery;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Arrays;

@SuppressWarnings({"unchecked", "rawtypes"})
@RegisterCatalog(QueryTypes.class)
public final class QueryTypeRegistryModule extends AbstractCatalogRegistryModule<QueryType> implements CatalogRegistryModule<QueryType> {

    @Override
    public void registerDefaults() {

        register(new SpongeOneParamQueryType<>("inventory_translation", InventoryTranslationQueryOperation::new));

        register(new SpongeOneParamQueryType<>("inventory_type", InventoryTypeQuery::new));
        register(new SpongeOneParamQueryType<>("item_stack_custom", ItemStackCustomQuery::new));
        register(new SpongeOneParamQueryType<>("item_stack_exact", ItemStackExactQuery::new));
        register(new SpongeOneParamQueryType<>("item_stack_ignore_quantity", ItemStackIgnoreQuantityQuery::new));
        register(new SpongeOneParamQueryType<>("item_type", ItemTypeQuery::new));
        register(new SpongeOneParamQueryType<>("property", InventoryPropertyMatcherQuery::new));
        register(new SpongeOneParamQueryType<>("type", TypeQuery::new));

        register(new PlayerPrimaryHotbarFirstQuery());
        register(new ReverseQuery());
        register(new EmptyQuery());

        register(new SpongeTwoParamQueryType<>("grid", GridQuery::new));

        register(SpongeQueryTypes.LENS);
        register(SpongeQueryTypes.SLOT_LENS);
    }
}
