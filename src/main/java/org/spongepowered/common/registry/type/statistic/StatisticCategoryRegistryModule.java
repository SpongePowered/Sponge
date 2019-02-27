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
package org.spongepowered.common.registry.type.statistic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.StatisticCategories;
import org.spongepowered.api.statistic.StatisticCategory;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

@RegisterCatalog(StatisticCategories.class)
public final class StatisticCategoryRegistryModule extends AbstractCatalogRegistryModule<StatisticCategory> implements
    SpongeAdditionalCatalogRegistryModule<StatisticCategory> {

    public static StatisticCategoryRegistryModule getInstance() {
        return StatisticCategoryRegistryModule.Holder.instance;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(StatisticCategory catalog) {
        checkNotNull(catalog);
        this.map.put(catalog.getKey(), catalog);
    }


    private StatisticCategoryRegistryModule() {
    }

    private static final class Holder {
        static final StatisticCategoryRegistryModule instance = new StatisticCategoryRegistryModule();
    }
}
