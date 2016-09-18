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
package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.block.BlockPhaseState;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhaseState;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.event.forge.IMixinWorldTickEvent;

import javax.annotation.Nullable;

/**
 * A specialized phase for forge event listeners during pre tick, may need to do the same
 * if SpongeAPI adds pre tick events.
 */
abstract class ListenerPhaseState extends PluginPhaseState {

    @Override
    public boolean canSwitchTo(IPhaseState state) {
        return state instanceof BlockPhaseState || state instanceof EntityPhaseState || state == GenerationPhase.State.TERRAIN_GENERATION;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }


    public abstract void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker);

    public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {

    }

    public void capturePlayerUsingStackToBreakBlocks(PhaseContext context, EntityPlayerMP playerMP, @Nullable ItemStack stack) {

    }

    static void logWarningOfDifferentWorldchanges(CauseTracker causeTracker, IMixinWorldTickEvent worldTickEvent, Object listener) {
        final PrettyPrinter printer = new PrettyPrinter(50).add("Changing a different World than expected!!").centre().hr();
        printer.add("Sponge is going to process the block changes as normal, however, a mod seems to be changing");
        printer.add("a world without checking for the world equality of the event! If you do not wish to see this");
        printer.add("message, you may disable this check in the <gamedir>/config/sponge/global.conf under");
        printer.add("cause-tracker.report-different-world-changes = false");
        printer.hr();
        printer.add("Providing information of the event:");
        printer.add("%s : %s", "Event world", worldTickEvent.getWorld());
        printer.addWrapped("%s : %s", "Changed world", causeTracker.getMinecraftWorld());
        printer.addWrapped("%s : %s", "Listener", listener);
        printer.add("Stacktrace:");
        printer.add(new Exception());
        printer.trace(System.err, SpongeImpl.getLogger(), Level.DEBUG);
    }
    static void logWarningOfDifferentWorldchanges(CauseTracker causeTracker, Object listener) {
        final PrettyPrinter printer = new PrettyPrinter(50).add("Changing a different World than expected!!").centre().hr();
        printer.add("Sponge is going to process the block changes as normal, however, a mod seems to be changing");
        printer.add("a world during a general server tick event! If you do not wish to see this");
        printer.add("message, you may disable this check in the <gamedir>/config/sponge/global.conf under");
        printer.add("cause-tracker.report-different-world-changes = false");
        printer.hr();
        printer.add("Providing information of the event:");
        printer.addWrapped("%s : %s", "Changed world", causeTracker.getMinecraftWorld());
        printer.addWrapped("%s : %s", "Listener", listener);
        printer.add("Stacktrace:");
        printer.add(new Exception());
        printer.trace(System.err, SpongeImpl.getLogger(), Level.DEBUG);
    }


}
