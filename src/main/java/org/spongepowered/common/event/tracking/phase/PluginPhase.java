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
package org.spongepowered.common.event.tracking.phase;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhaseState;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.event.forge.IMixinWorldTickEvent;

import javax.annotation.Nullable;

public final class PluginPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        BLOCK_WORKER,
        CUSTOM_SPAWN,
        COMPLETE,;

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return false;
        }

        @Override
        public PluginPhase getPhase() {
            return TrackingPhases.PLUGIN;
        }

    }

    public enum Listener implements IPhaseState {
        /**
         * A specialized phase for forge event listeners during pre tick, may need to do the same
         * if SpongeAPI adds pre tick events.
         */
        PRE_WORLD_TICK_LISTENER() {
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                context.getCapturedPlayer().ifPresent(player -> builder.named(NamedCause.notifier(player)));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final IMixinWorldTickEvent worldTickEvent = phaseContext
                        .firstNamed(InternalNamedCauses.Tracker.TICK_EVENT, IMixinWorldTickEvent.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a WorldTickEvent but we're not!!!", phaseContext));
                final Object listener = phaseContext.getSource(Object.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a WorldTickEvent listener!", phaseContext));

                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> {
                    if (causeTracker.getMinecraftWorld() != worldTickEvent.getWorld()
                        && SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().reportWorldTickDifferences()) {
                        logWarningOfDifferentWorldchanges(causeTracker, worldTickEvent, listener);
                    }
                    TrackingUtil.processBlockCaptures(blocks, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.getCapturedPlayer().ifPresent(player ->
                        ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos))
                                .setBlockNotifier(notifyPos, player.getUniqueId())
                );
            }

            @Override
            public void capturePlayerUsingStackToBreakBlocks(PhaseContext context, EntityPlayerMP playerMP, @Nullable ItemStack stack) {
                context.getCapturedPlayerSupplier().addPlayer(playerMP);
            }
        },
        /**
         * A specialized phase for forge event listeners during post tick, may need to do the same
         * if SpongeAPI adds post tick events.
         */
        POST_WORLD_TICK_LISTENER() {
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                context.getCapturedPlayer().ifPresent(player -> builder.named(NamedCause.notifier(player)));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final IMixinWorldTickEvent worldTickEvent = phaseContext
                        .firstNamed(InternalNamedCauses.Tracker.TICK_EVENT, IMixinWorldTickEvent.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a WorldTickEvent but we're not!!!", phaseContext));
                final Object listener = phaseContext.getSource(Object.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a WorldTickEvent listener!", phaseContext));

                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> {
                    if (causeTracker.getMinecraftWorld() != worldTickEvent.getWorld()
                        && SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().reportWorldTickDifferences()) {
                        logWarningOfDifferentWorldchanges(causeTracker, worldTickEvent, listener);
                    }
                    TrackingUtil.processBlockCaptures(blocks, causeTracker, this, phaseContext);

                });
            }

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.getCapturedPlayer().ifPresent(player ->
                        ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos))
                                .setBlockNotifier(notifyPos, player.getUniqueId())
                );
            }

            @Override
            public void capturePlayerUsingStackToBreakBlocks(PhaseContext context, EntityPlayerMP playerMP, @Nullable ItemStack stack) {
                context.getCapturedPlayerSupplier().addPlayer(playerMP);
            }
        },
        /**
         * A specialized phase for forge event listeners during pre tick, may need to do the same
         * if SpongeAPI adds pre tick events.
         */
        PRE_SERVER_TICK_LISTENER() {
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                context.getCapturedPlayer().ifPresent(player -> builder.named(NamedCause.notifier(player)));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Object listener = phaseContext.getSource(Object.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a ServerTickEvent listener!", phaseContext));

                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> {
                    if (SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().reportWorldTickDifferences()) {
                        logWarningOfDifferentWorldchanges(causeTracker, listener);
                    }
                    TrackingUtil.processBlockCaptures(blocks, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.getCapturedPlayer().ifPresent(player ->
                        ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos))
                                .setBlockNotifier(notifyPos, player.getUniqueId())
                );
            }

            @Override
            public void capturePlayerUsingStackToBreakBlocks(PhaseContext context, EntityPlayerMP playerMP, @Nullable ItemStack stack) {
                context.getCapturedPlayerSupplier().addPlayer(playerMP);
            }
        },
        /**
         * A specialized phase for forge event listeners during pre tick, may need to do the same
         * if SpongeAPI adds pre tick events.
         */
        POST_SERVER_TICK_LISTENER() {
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                context.getCapturedPlayer().ifPresent(player -> builder.named(NamedCause.notifier(player)));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Object listener = phaseContext.getSource(Object.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a ServerTickEvent listener!", phaseContext));

                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blocks -> {
                    if (SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().reportWorldTickDifferences()) {
                        logWarningOfDifferentWorldchanges(causeTracker, listener);
                    }
                    TrackingUtil.processBlockCaptures(blocks, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.getCapturedPlayer().ifPresent(player ->
                        ((IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos))
                                .setBlockNotifier(notifyPos, player.getUniqueId())
                );
            }

            @Override
            public void capturePlayerUsingStackToBreakBlocks(PhaseContext context, EntityPlayerMP playerMP, @Nullable ItemStack stack) {
                context.getCapturedPlayerSupplier().addPlayer(playerMP);
            }
        },
        ;

        private static void logWarningOfDifferentWorldchanges(CauseTracker causeTracker, IMixinWorldTickEvent worldTickEvent, Object listener) {
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
        private static void logWarningOfDifferentWorldchanges(CauseTracker causeTracker, Object listener) {
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

        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {

        }

        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.PLUGIN;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return state instanceof BlockPhase.State || state instanceof EntityPhaseState || state == GenerationPhase.State.TERRAIN_GENERATION;
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
    }

    public static PluginPhase getInstance() {
        return Holder.INSTANCE;
    }

    private PluginPhase() {
    }

    private static final class Holder {
        static final PluginPhase INSTANCE = new PluginPhase();
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.BLOCK_WORKER) {
            phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(items -> {

            });
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(snapshots -> TrackingUtil.processBlockCaptures(snapshots, causeTracker, state, phaseContext));
        } else if (state instanceof Listener) {
            ((Listener) state).processPostTick(causeTracker, phaseContext);
        }
    }

    @Override
    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        if (state instanceof Listener) {
            ((Listener) state).associateAdditionalBlockChangeCauses(context, builder, causeTracker);
        }
    }

    @Override
    public void addNotifierToBlockEvent(IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker, BlockPos pos,
            IMixinBlockEventData blockEvent) {
        if (phaseState instanceof Listener) {
            ((Listener) phaseState).associateBlockEventNotifier(context, causeTracker, pos, blockEvent);
        }
    }


    @Override
    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        if (state instanceof Listener) {
            ((Listener) state).associateNeighborBlockNotifier(context, sourcePos, block, notifyPos, minecraftWorld, notifier);
        }
    }

    @Override
    public void capturePlayerUsingStackToBreakBlock(@Nullable ItemStack itemStack, EntityPlayerMP playerMP, IPhaseState state, PhaseContext context,
            CauseTracker causeTracker) {
        if (state instanceof PluginPhase.Listener) {
            ((Listener) state).capturePlayerUsingStackToBreakBlocks(context, playerMP, itemStack);
        }
    }

    @Override
    public boolean handlesOwnPhaseCompletion(IPhaseState state) {
        return state == State.BLOCK_WORKER;
    }
}
