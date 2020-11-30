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
package org.spongepowered.common.event.tracking.context.transaction.type;

import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.entity.HarvestEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeCommon;

import java.util.function.Supplier;

public final class TransactionTypes {
    public static final Supplier<TransactionType<ChangeBlockEvent.All>> BLOCK = SpongeCommon.getRegistry().getCatalogRegistry().provideSupplier(TransactionType.class, "block");
    public static final Supplier<TransactionType<NotifyNeighborBlockEvent>> NEIGHBOR_NOTIFICATION = SpongeCommon.getRegistry().getCatalogRegistry().provideSupplier(TransactionType.class, "neighbor_notification");
    public static final Supplier<TransactionType<SpawnEntityEvent>> SPAWN_ENTITY = SpongeCommon.getRegistry().getCatalogRegistry().provideSupplier(TransactionType.class, "spawn_entity");
    public static final Supplier<TransactionType<HarvestEntityEvent>> ENTITY_DEATH_DROPS = SpongeCommon.getRegistry().getCatalogRegistry().provideSupplier(TransactionType.class, "entity_death_drops");
}
