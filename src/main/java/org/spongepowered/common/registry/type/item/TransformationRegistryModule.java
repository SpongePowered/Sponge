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

import org.spongepowered.api.item.inventory.InventoryTransformation;
import org.spongepowered.api.item.inventory.InventoryTransformations;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.query.QueryOperation;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.common.item.inventory.EmptyInventoryImpl;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.query.ReverseTransformation;
import org.spongepowered.common.item.inventory.query.SpongeQueryTransformation;
import org.spongepowered.common.registry.RegistryHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RegistrationDependency(QueryOperationRegistryModule.class)
public class TransformationRegistryModule implements RegistryModule {

    public static TransformationRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        this.register("NO_OP", Collections.emptyList());
        this.register("PLAYER_MAIN_HOTBAR_FIRST", Arrays.asList(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class),
                                                       QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class)));
        this.register("REVERSE", new ReverseTransformation());
        this.register("EMPTY", EmptyInventoryImpl::new);
    }

    @SuppressWarnings("rawtypes")
    private void register(String field, List<QueryOperation> operations) {
        SpongeQueryTransformation transformation = new SpongeQueryTransformation(operations);
        register(field, transformation);
    }

    private void register(String field, InventoryTransformation transformation) {
        RegistryHelper.setFinalStatic(InventoryTransformations.class, field, transformation);
    }

    private TransformationRegistryModule() {
    }

    private static final class Holder {
        static final TransformationRegistryModule INSTANCE = new TransformationRegistryModule();
    }
}
