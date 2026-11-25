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
package org.spongepowered.common.test.gametest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.ScheduleBlockUpdateEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.accessor.server.network.ServerGamePacketListenerImplAccessor;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Bone meal on a sapling, used by a player through the use-item-on packet, one use at a time until the tree grows.
 * Growth is random, so each use is checked against the shape for what it did to the sapling:
 * <ul>
 *     <li>the roll failed: no growth events;</li>
 *     <li>stage 0 to 1: {@code ChangeBlockEvent.All} then {@code Post};</li>
 *     <li>the tree: {@code All}, a {@code NotifyNeighborBlockEvent} per update, one {@code ScheduleBlockUpdateEvent},
 *     then {@code Post}.</li>
 * </ul>
 * <p>A use starts with {@code InteractBlockEvent.Secondary}. Every growth event carries the bone meal as
 * {@code USED_ITEM} and the sapling as {@code GROWTH_ORIGIN}, every block change is a growth, and the root is the
 * player's interaction.</p>
 *
 * <p>{@code sponge:growth/oak_sapling}: a 5x5 bedrock floor with dirt at (2,0,2) under a stage 0 oak sapling at
 * (2,1,2). The test has sky access so the tree has room.</p>
 */
@GameTestGroup("growth")
public final class GrowthGameTests {

    private static final BlockPos SAPLING = new BlockPos(2, 1, 2);
    private static final int MAX_USES = 30;
    private static final String INTERACT = "InteractBlockEvent.Secondary";
    private static final String INVENTORY = "ChangeInventoryEvent";
    private static final List<String> STAGE = List.of("ChangeBlockEvent.All", "ChangeBlockEvent.Post");
    // Notifications collapsed to one entry, their count depending on the tree
    private static final List<String> TREE = List.of("ChangeBlockEvent.All", "NotifyNeighborBlockEvent", "ScheduleBlockUpdateEvent", "ChangeBlockEvent.Post");

    @GameTest(structure = "sponge:growth/oak_sapling", maxTicks = 100, skyAccess = true)
    void playerBoneMealOnSapling(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // The join teleport leaves the server waiting on the client, which ignores the player's interactions until acked
        player.connection.handleAcceptTeleportPacket(
            new ServerboundAcceptTeleportationPacket(((ServerGamePacketListenerImplAccessor) player.connection).accessor$awaitingTeleport()));
        player.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 0))));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 64));

        final BlockPos sapling = helper.absolutePos(GrowthGameTests.SAPLING);
        final AtomicBoolean using = new AtomicBoolean();
        // The mock player is in creative, so bone meal is never used up and its hand is left out
        final Uses uses = new Uses(helper, event -> using.get() && !(event instanceof ChangeInventoryEvent),
            InteractBlockEvent.Secondary.class, List.of(GrowthGameTests.INTERACT), List.of());

        helper.onEachTick(() -> {
            if (uses.finishIfDone()) {
                return;
            }
            final ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(sapling), Direction.UP, sapling, false), uses.count());
            using.set(true);
            try {
                PacketPhaseUtil.onProcessPacket(packet, player.connection);
            } finally {
                using.set(false);
            }
            uses.endUse();
        });
    }

    /** Checks each use of bone meal as it ends, against the shape for what it did to the sapling */
    private static final class Uses {

        private final GameTestHelper helper;
        private final BlockPos sapling;
        private final Vector3i origin;
        private final Class<?> root;
        private final List<String> before;
        private final List<String> after;
        private final EventListener<Event> listener;
        private final List<String> events = new ArrayList<>();
        private final List<String> problems = new ArrayList<>();
        private BlockState previous;
        private int count;
        private boolean done;

        /**
         * @param root what each growth event's cause is rooted in
         * @param before the events a use throws ahead of any growth
         * @param after the events a use throws once any growth is done
         */
        Uses(final GameTestHelper helper, final Predicate<Event> filter, final Class<?> root, final List<String> before, final List<String> after) {
            this.helper = helper;
            this.sapling = helper.absolutePos(GrowthGameTests.SAPLING);
            this.origin = VecHelper.toVector3i(this.sapling);
            this.root = root;
            this.before = before;
            this.after = after;
            this.previous = helper.getLevel().getBlockState(this.sapling);
            this.listener = event -> {
                if (filter.test(event)) {
                    this.record(event);
                }
            };
            SpongeGameTests.listen(Event.class, this.listener);
        }

        int count() {
            return this.count;
        }

        void endUse() {
            this.count++;
            final BlockState now = this.helper.getLevel().getBlockState(this.sapling);
            final List<String> growth;
            if (now.is(BlockTags.LOGS)) {
                growth = GrowthGameTests.TREE;
            } else if (now.getValue(SaplingBlock.STAGE) > this.previous.getValue(SaplingBlock.STAGE)) {
                growth = GrowthGameTests.STAGE;
            } else {
                growth = List.of();
            }
            final List<String> expected = new ArrayList<>(this.before);
            expected.addAll(growth);
            expected.addAll(this.after);
            final List<String> collapsed = new ArrayList<>();
            for (final String event : this.events) {
                if (collapsed.isEmpty() || !collapsed.get(collapsed.size() - 1).equals(event) || !event.startsWith("NotifyNeighbor")) {
                    collapsed.add(event);
                }
            }
            this.helper.assertTrue(collapsed.equals(expected), "Use " + this.count + " (" + this.previous + " -> " + now + "): expected "
                + expected + " but got " + collapsed);
            this.helper.assertTrue(this.problems.isEmpty(), "Use " + this.count + ": " + this.problems);
            this.events.clear();
            this.previous = now;
        }

        boolean finishIfDone() {
            if (this.done) {
                return true;
            }
            final boolean grown = this.previous.is(BlockTags.LOGS);
            if (!grown && this.count < GrowthGameTests.MAX_USES) {
                return false;
            }
            this.done = true;
            Sponge.eventManager().unregisterListeners(this.listener);
            this.helper.assertTrue(grown, "No tree after " + this.count + " uses");
            this.helper.succeed();
            return true;
        }

        private void record(final Event event) {
            final String name;
            if (event instanceof InteractBlockEvent.Secondary) {
                name = GrowthGameTests.INTERACT;
            } else if (event instanceof ChangeInventoryEvent) {
                name = GrowthGameTests.INVENTORY;
            } else if (event instanceof ChangeBlockEvent.All all) {
                name = "ChangeBlockEvent.All";
                all.transactions().stream().filter(transaction -> transaction.operation() != Operations.GROWTH.get())
                    .forEach(transaction -> this.problems.add("not a growth: " + transaction));
            } else if (event instanceof ChangeBlockEvent.Post) {
                name = "ChangeBlockEvent.Post";
            } else if (event instanceof NotifyNeighborBlockEvent) {
                name = "NotifyNeighborBlockEvent";
            } else if (event instanceof ScheduleBlockUpdateEvent) {
                name = "ScheduleBlockUpdateEvent";
            } else {
                return;
            }
            this.events.add(name);
            if (name.equals(GrowthGameTests.INTERACT) || name.equals(GrowthGameTests.INVENTORY)) {
                return;
            }
            if (!event.context().get(EventContextKeys.USED_ITEM).filter(item -> item.type() == ItemTypes.BONE_MEAL.get()).isPresent()) {
                this.problems.add(name + " has no bone meal as its used item");
            }
            if (!event.context().get(EventContextKeys.GROWTH_ORIGIN).filter(snapshot -> snapshot.position().equals(this.origin)).isPresent()) {
                this.problems.add(name + " is not from the sapling");
            }
            // A notification is rooted in its own ticket
            if (!(event instanceof NotifyNeighborBlockEvent) && !this.root.isInstance(event.cause().root())) {
                this.problems.add(name + " is rooted in " + event.cause().root() + ", not a " + this.root.getSimpleName());
            }
        }
    }
}
