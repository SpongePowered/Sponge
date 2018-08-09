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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.statistic.StatisticType;
import org.spongepowered.api.statistic.StatisticTypes;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.statistic.SpongeStatisticType;

@RegisterCatalog(StatisticTypes.class)
public final class StatisticTypeRegistryModule extends AbstractCatalogRegistryModule<StatisticType> implements CatalogRegistryModule<StatisticType> {

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("basic"), new SpongeStatisticType("basic"));
        SpongeStatisticType type7 = new SpongeStatisticType("blocks_broken");
        register(CatalogKey.minecraft("blocks_broken"), type7);
        register(CatalogKey.minecraft("mine_block"), type7);
        SpongeStatisticType type6 = new SpongeStatisticType("entities_killed");
        register(CatalogKey.minecraft("entities_killed"), type6);
        register(CatalogKey.minecraft("kill_entity"), type6);
        SpongeStatisticType type5 = new SpongeStatisticType("items_broken");
        register(CatalogKey.minecraft("items_broken"), type5);
        register(CatalogKey.minecraft("break_item"), type5);
        SpongeStatisticType type4 = new SpongeStatisticType("items_crafted");
        register(CatalogKey.minecraft("items_crafted"), type4);
        register(CatalogKey.minecraft("craft_item"), type4);
        SpongeStatisticType type3 = new SpongeStatisticType("items_dropped");
        register(CatalogKey.minecraft("items_dropped"), type3);
        register(CatalogKey.minecraft("drop"), type3);
        SpongeStatisticType type2 = new SpongeStatisticType("items_picked_up");
        register(CatalogKey.minecraft("items_picked_up"), type2);
        register(CatalogKey.minecraft("pickup"), type2);
        SpongeStatisticType type1 = new SpongeStatisticType("items_used");
        register(CatalogKey.minecraft("items_used"), type1);
        register(CatalogKey.minecraft("use_item"), type1);
        SpongeStatisticType type = new SpongeStatisticType("killed_by_entity");
        register(CatalogKey.minecraft("killed_by_entity"), type);
        register(CatalogKey.minecraft("entity_killed_by"), type);
    }

}
