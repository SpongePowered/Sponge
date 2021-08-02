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
package org.spongepowered.common.event.tracking.phase.packet.player;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;

public class InteractionPacketContext extends PacketContext<InteractionPacketContext> {

    private BlockSnapshot targetBlock = BlockSnapshot.empty();
    private ItemStack activeItem = ItemStack.empty();

    InteractionPacketContext(PacketState<InteractionPacketContext> state, PhaseTracker tracker) {
        super(state, tracker);
    }

    @Override
    protected void reset() {
        super.reset();
        this.targetBlock = BlockSnapshot.empty();
    }

    InteractionPacketContext targetBlock(BlockSnapshot snapshot) {
        this.targetBlock = snapshot;
        return this;
    }

    BlockSnapshot getTargetBlock() {
        return this.targetBlock;
    }
    
    InteractionPacketContext activeItem(ItemStack item) {
        this.activeItem = item;
        return this;
    }
    
    ItemStack getActiveItem() {
        return this.activeItem;
    }
}
