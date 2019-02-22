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
package org.spongepowered.test;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Plugin(id = "flowerpottest", name = "Flower Pot Destruction", description = "A plugin to test causes for breaking blocks beneath flower pots", version = "0.0.0")
public class FlowerPotTest implements LoadableModule {

    @Inject private Logger logger;
    @Inject private PluginContainer container;
    private final FlowerPotListener listener = new FlowerPotListener();

    /*
    Basically, this verifies that the context for breaking a block that performs a drop will never have an incorrect drop
    position compared to the LocatableBlock that will be provided as part of the Cause stack for the ConstructEvent.Pre
    because the BlockHit position was previously used instead of the source position of the LocatableBlock.

     */

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class FlowerPotListener {

        @Listener
        public void onConstruct(ConstructEntityEvent.Pre event, @Root BlockState state, @First LocatableBlock blockBeingDropped) {

            final Cause cause = event.getCause();
            final Optional<BlockSnapshot> blockSnapshot = cause.getContext().get(EventContextKeys.BLOCK_HIT);
            if (!blockSnapshot.isPresent()) {
                return;
            }
            final BlockSnapshot hitBlock = blockSnapshot.get();
            final Location<World> hitLocation = hitBlock.getLocation().get();
            final Vector3i droppedPosition = blockBeingDropped.getPosition();
            final boolean isDroppedPotentiallyOrigin = droppedPosition.getX() == 0 || droppedPosition.getZ() == 0;
            final boolean isHitSameAsDropped = (hitLocation.getBlockX() == droppedPosition.getX() || hitLocation.getBlockZ() == droppedPosition.getZ());
            final Vector3i targetPosition = event.getTransform().getPosition().toInt();
            final boolean isTargetEqualToDropped = targetPosition.getX() != droppedPosition.getX() || targetPosition.getZ() != droppedPosition.getZ();
            if (isDroppedPotentiallyOrigin && isHitSameAsDropped && !isTargetEqualToDropped) {
                System.err.println("Failed Drop Test for Blocks, positions are not matching!");
            }


        }

    }

}
