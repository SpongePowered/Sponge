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
package org.spongepowered.forge.mixin.core.api.event.block;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.forge.launch.bridge.event.SpongeEventBridge_Forge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(value = ChangeBlockEvent.All.class, remap = false)
public interface ChangeBlockEvent_AllMixin_Forge extends SpongeEventBridge_Forge {

    @Override
    default @Nullable Collection<? extends Event> bridge$createForgeEvents() {
        final ChangeBlockEvent.All thisEvent = ((ChangeBlockEvent.All) this);
        final Player player = thisEvent.cause().first(Player.class).orElse(null);
        final Operation breakOp = Operations.BREAK.get();

        final List<Event> forgeEvents = new ArrayList<>();
        for (final BlockTransaction transaction : thisEvent.transactions()) {
            if (player != null && transaction.operation() == breakOp) {
                forgeEvents.add(new BlockEvent.BreakEvent((Level) thisEvent.world(),
                        VecHelper.toBlockPos(transaction.original().position()),
                        (BlockState) transaction.original().state(), player));
            }
            // TODO: Other event types may go here - break is a PoC.
        }
        return forgeEvents;
    }

}
