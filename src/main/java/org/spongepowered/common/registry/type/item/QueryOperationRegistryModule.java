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

import org.spongepowered.api.item.inventory.query.SingleParameterQueryType;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperationType;
import org.spongepowered.common.item.inventory.query.SpongeQueryOperationTypes;
import org.spongepowered.common.item.inventory.query.operation.InventoryPropertyMatcherQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.InventoryTranslationQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.InventoryTypeQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.ItemStackCustomOperation;
import org.spongepowered.common.item.inventory.query.operation.ItemStackExactQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.ItemStackIgnoreQuantityOperation;
import org.spongepowered.common.item.inventory.query.operation.ItemTypeQueryOperation;
import org.spongepowered.common.item.inventory.query.operation.TypeQueryOperation;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@SuppressWarnings({"unchecked", "rawtypes"})
@RegisterCatalog(QueryTypes.class)
public final class QueryOperationRegistryModule extends AbstractCatalogRegistryModule<SingleParameterQueryType>
    implements CatalogRegistryModule<SingleParameterQueryType> {

    @Override
    public void registerDefaults() {
        register(new SpongeQueryOperationType<>("inventory_type", InventoryTypeQueryOperation::new));
        register(new SpongeQueryOperationType<>("type", TypeQueryOperation::new));
        register(new SpongeQueryOperationType<>("item_type", ItemTypeQueryOperation::new));
        register(new SpongeQueryOperationType<>("item_stack_ignore_quantity", ItemStackIgnoreQuantityOperation::new));
        register(new SpongeQueryOperationType<>("item_stack_exact", ItemStackExactQueryOperation::new));
        register(new SpongeQueryOperationType<>("item_stack_custom", ItemStackCustomOperation::new));
        register(new SpongeQueryOperationType<>("property", InventoryPropertyMatcherQueryOperation::new));
        register(new SpongeQueryOperationType<>("inventory_translation", InventoryTranslationQueryOperation::new));

        register(SpongeQueryOperationTypes.LENS);
        register(SpongeQueryOperationTypes.SLOT_LENS);
    }
}
