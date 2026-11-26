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
package org.spongepowered.common.event.tracking.phase.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

final class DispensePhaseState extends BlockPhaseState {

    @Override
    public void unwind(final GeneralizedContext phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    // The items a dispenser or dropper drops, after their DropItemEvent.Pre; anything else it spawns stays a plain spawn
    @Override
    public SpawnEntityEvent createSpawnEvent(
        final GeneralizedContext context,
        final @Nullable GameTransaction<@NonNull ?> parent,
        final List<Entity> collect,
        final Cause currentCause
    ) {
        if (collect.stream().allMatch(entity -> entity instanceof ItemEntity)) {
            return SpongeEventFactory.createDropItemEventDispense(currentCause, (List) collect);
        }
        return super.createSpawnEvent(context, parent, collect, currentCause);
    }

    @Override
    public Supplier<ResourceKey> attemptWorldKey(GeneralizedContext context) {
        final Optional<BlockSnapshot> source = context.getSource(BlockSnapshot.class);
        if (source.isPresent()) {
            return () -> source.get().world();
        }
        return super.attemptWorldKey(context);
    }
}
