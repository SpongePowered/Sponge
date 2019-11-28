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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

final class BlockDecayPhaseState extends BlockPhaseState {

    private final BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> BLOCK_DECAY_MODIFIER = super.getFrameModifier().andThen((frame, context) -> {
        final LocatableBlock locatable = context.getSource(LocatableBlock.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
        frame.pushCause(locatable);
    });

    @Override
    public GeneralizedContext createNewContext() {
        return super.createNewContext()
            .addCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return this.BLOCK_DECAY_MODIFIER;
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    @Override
    public void unwind(final GeneralizedContext context) {
        final LocatableBlock locatable = context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
        final Location<World> worldLocation = locatable.getLocation();
        final WorldServerBridge mixinWorld = (WorldServerBridge) worldLocation.getExtent();
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context);

        context.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                final List<Entity> entities = items.stream()
                    .map(entity -> (Entity) entity)
                    .collect(Collectors.toList());
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                SpongeCommonEventFactory.callDropItemDestruct(entities, context);
            });
        context.getCapturedEntitySupplier()
            .acceptAndClearIfNotEmpty(entities -> {
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                SpongeCommonEventFactory.callDropItemDestruct(entities, context);

            });
        context.getCapturedItemStackSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                final List<EntityItem> items = drops.stream()
                    .map(drop -> drop.create((WorldServer) mixinWorld))
                    .collect(Collectors.toList());
                final List<Entity> entities = (List<Entity>) (List<?>) items;
                if (!entities.isEmpty()) {
                    SpongeCommonEventFactory.callDropItemDestruct(entities, context);
                }
            });

        context.getBlockItemDropSupplier()
            .acceptAndClearIfNotEmpty(drops -> {
                drops.asMap().forEach((key, value) -> {
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                    SpongeCommonEventFactory.callDropItemDestruct(new ArrayList<>((Collection<? extends Entity>) (Collection<?>) value), context);
                });
            });


    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(final GeneralizedContext phaseContext, final IBlockState newState,
        final Block newBlock, final IBlockState currentState, final SpongeBlockSnapshot snapshot, final Block originalBlock) {
        if (newBlock == Blocks.field_150350_a) {
            return BlockChange.DECAY;
        } else {
            return super.associateBlockChangeWithSnapshot(phaseContext, newState, newBlock, currentState, snapshot, originalBlock);
        }
    }

}
