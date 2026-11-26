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
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
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
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A sapling growing into a tree, from bone meal used by a player through the use-item-on packet or by a dispenser,
 * or by itself on random ticks, one use or tick at a time until the tree grows. Growth is random, so each is checked
 * against the shape for what it did to the sapling:
 * <ul>
 *     <li>the roll failed: no growth events;</li>
 *     <li>stage 0 to 1: {@code ChangeBlockEvent.All} then {@code Post};</li>
 *     <li>the tree: {@code All}, a {@code NotifyNeighborBlockEvent} per update, one {@code ScheduleBlockUpdateEvent},
 *     then {@code Post}.</li>
 * </ul>
 * <p>A player's use starts with {@code InteractBlockEvent.Secondary}; a dispenser's ends with the
 * {@code ChangeInventoryEvent} for the bone meal it used, after the growth, which unwinds in a phase of its own; a
 * random tick's starts with {@code TickBlockEvent.Random}, and its stage change is not attributed as growth. Every
 * growth event carries the sapling as {@code GROWTH_ORIGIN}, and the bone meal, if any, as {@code USED_ITEM}; every
 * block change is a growth, and the root is the player's interaction, the dispenser or the sapling. Once grown, the
 * tree belongs to whoever the growth is attributed to: the player, the dispenser's creator, or whoever placed the
 * sapling. A block someone else already created stays theirs, with the growth's player as its notifier.</p>
 *
 * <p>{@code sponge:growth/oak_sapling}: a 5x5 bedrock floor with dirt at (2,0,2) under a stage 0 oak sapling at
 * (2,1,2). {@code sponge:growth/oak_sapling_dispenser} adds a south-facing dispenser at (2,1,1), facing the sapling and
 * powered from (2,1,0). The tests have sky access so the tree has room.</p>
 */
@GameTestGroup("growth")
public final class GrowthGameTests {

    private static final BlockPos SAPLING = new BlockPos(2, 1, 2);
    private static final BlockPos DISPENSER = new BlockPos(2, 1, 1);
    private static final int MAX_USES = 30;
    // Power, unpower, then check once the scheduled dispense four ticks after powering has run
    private static final int PULSE = 6;
    private static final String INTERACT = "InteractBlockEvent.Secondary";
    private static final String INVENTORY = "ChangeInventoryEvent";
    private static final String RANDOM_TICK = "TickBlockEvent.Random";
    private static final List<String> STAGE = List.of("ChangeBlockEvent.All", "ChangeBlockEvent.Post");
    // Notifications collapsed to one entry, their count depending on the tree
    private static final List<String> TREE = List.of("ChangeBlockEvent.All", "NotifyNeighborBlockEvent", "ScheduleBlockUpdateEvent", "ChangeBlockEvent.Post");

    @GameTest(structure = "sponge:growth/oak_sapling", maxTicks = 100, skyAccess = true)
    void playerBoneMealOnSapling(final GameTestHelper helper) {
        GrowthGameTests.boneMealAsPlayer(helper, player -> GrowthGameTests.assertGrownBy(helper, player.getUUID()));
    }

    // The trunk stays its planter's, with the player as its notifier; the blocks the growth adds are the player's
    @GameTest(structure = "sponge:growth/oak_sapling", maxTicks = 100, skyAccess = true)
    void playerBoneMealOnAnotherPlayersSapling(final GameTestHelper helper) {
        final UUID planter = UUID.randomUUID();
        final BlockPos sapling = helper.absolutePos(GrowthGameTests.SAPLING);
        final LevelChunkBridge chunk = (LevelChunkBridge) helper.getLevel().getChunkAt(sapling);
        chunk.bridge$setBlockCreator(sapling, planter);
        GrowthGameTests.boneMealAsPlayer(helper, player -> {
            final UUID creator = chunk.bridge$getBlockCreatorUUID(sapling).orElse(null);
            final UUID notifier = chunk.bridge$getBlockNotifierUUID(sapling).orElse(null);
            GameTestAssertions.assertTrue(helper, planter.equals(creator), "The trunk was created by " + creator + ", not its planter " + planter);
            GameTestAssertions.assertTrue(helper, player.getUUID().equals(notifier), "The trunk's notifier is " + notifier + ", not the player " + player.getUUID());
            final BlockPos above = sapling.above();
            final UUID grown = ((LevelChunkBridge) helper.getLevel().getChunkAt(above)).bridge$getBlockCreatorUUID(above).orElse(null);
            GameTestAssertions.assertTrue(helper, player.getUUID().equals(grown), "The log above the trunk was created by " + grown + ", not the player");
        });
    }

    private static void boneMealAsPlayer(final GameTestHelper helper, final Consumer<ServerPlayer> assertGrown) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 1, 0))));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_MEAL, 64));

        final BlockPos sapling = helper.absolutePos(GrowthGameTests.SAPLING);
        final AtomicBoolean using = new AtomicBoolean();
        // The mock player is in creative, so bone meal is never used up and its hand is left out
        final Uses uses = new Uses(helper, event -> using.get() && !(event instanceof ChangeInventoryEvent),
            InteractBlockEvent.Secondary.class, List.of(GrowthGameTests.INTERACT), List.of());

        helper.onEachTick(() -> {
            if (uses.finishIfDone(() -> assertGrown.accept(player))) {
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

    @GameTest(structure = "sponge:growth/oak_sapling_dispenser", maxTicks = 200, skyAccess = true)
    void dispenserBoneMealOnSapling(final GameTestHelper helper) {
        final DispenserBlockEntity dispenser = helper.getBlockEntity(GrowthGameTests.DISPENSER, DispenserBlockEntity.class);
        dispenser.setItem(0, new ItemStack(Items.BONE_MEAL, 64));
        final UUID placer = UUID.randomUUID();
        final BlockPos dispenserPos = helper.absolutePos(GrowthGameTests.DISPENSER);
        ((LevelChunkBridge) helper.getLevel().getChunkAt(dispenserPos)).bridge$setBlockCreator(dispenserPos, placer);
        final Vector3i at = VecHelper.toVector3i(dispenserPos);
        final Uses uses = new Uses(helper,
            event -> event.cause().allOf(LocatableBlock.class).stream().anyMatch(block -> block.blockPosition().equals(at)),
            LocatableBlock.class, List.of(), List.of(GrowthGameTests.INVENTORY));
        final AtomicInteger ticks = new AtomicInteger();

        helper.onEachTick(() -> {
            final int step = ticks.getAndIncrement() % GrowthGameTests.PULSE;
            if (step == 0) {
                if (uses.finishIfDone(() -> GrowthGameTests.assertGrownBy(helper, placer))) {
                    return;
                }
                helper.setBlock(GrowthGameTests.DISPENSER.north(), Blocks.REDSTONE_BLOCK);
            } else if (step == 2) {
                helper.setBlock(GrowthGameTests.DISPENSER.north(), Blocks.AIR);
            } else if (step == GrowthGameTests.PULSE - 1) {
                uses.endUse();
            }
        });
    }

    // Each tick past the light check and 1 in 7 roll of the random tick the server would give it: stage 1, then the tree
    @GameTest(structure = "sponge:growth/oak_sapling", maxTicks = 100, skyAccess = true)
    void saplingGrowsByItself(final GameTestHelper helper) {
        final UUID planter = UUID.randomUUID();
        final BlockPos sapling = helper.absolutePos(GrowthGameTests.SAPLING);
        ((LevelChunkBridge) helper.getLevel().getChunkAt(sapling)).bridge$setBlockCreator(sapling, planter);
        final AtomicBoolean ticking = new AtomicBoolean();
        final Uses uses = new Uses(helper, event -> ticking.get(), LocatableBlock.class, List.of(GrowthGameTests.RANDOM_TICK), List.of());

        helper.onEachTick(() -> {
            if (uses.finishIfDone(() -> GrowthGameTests.assertGrownBy(helper, planter))) {
                return;
            }
            final ServerLevel level = helper.getLevel();
            final BlockState state = level.getBlockState(sapling);
            ticking.set(true);
            try {
                TrackingUtil.randomTickBlock((TrackedWorldBridge) level, state, sapling, level.random,
                    () -> ((SaplingBlock) state.getBlock()).advanceTree(level, sapling, state, level.random));
            } finally {
                ticking.set(false);
            }
            uses.endUse();
        });
    }

    // The trunk grown in place of the sapling, and the log above it
    private static void assertGrownBy(final GameTestHelper helper, final UUID creator) {
        for (final BlockPos relative : List.of(GrowthGameTests.SAPLING, GrowthGameTests.SAPLING.above())) {
            final BlockPos pos = helper.absolutePos(relative);
            final UUID actual = ((LevelChunkBridge) helper.getLevel().getChunkAt(pos)).bridge$getBlockCreatorUUID(pos).orElse(null);
            GameTestAssertions.assertTrue(helper, creator.equals(actual), "The log at " + relative + " was created by " + actual + ", not " + creator);
        }
    }

    /** Checks each use of bone meal as it ends, against the shape for what it did to the sapling */
    private static final class Uses {

        private final GameTestHelper helper;
        private final BlockPos sapling;
        private final Vector3i origin;
        private final Class<?> root;
        private final List<String> before;
        private final List<String> after;
        private final boolean boneMeal;
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
            this.boneMeal = !before.contains(GrowthGameTests.RANDOM_TICK);
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
            GameTestAssertions.assertTrue(this.helper, collapsed.equals(expected), "Use " + this.count + " (" + this.previous + " -> " + now + "): expected "
                + expected + " but got " + collapsed);
            // A random tick advances the stage outside the growing phase, which only wraps growing the tree
            if (growth == GrowthGameTests.STAGE && !this.boneMeal) {
                this.problems.clear();
            }
            GameTestAssertions.assertTrue(this.helper, this.problems.isEmpty(), "Use " + this.count + ": " + this.problems);
            this.events.clear();
            this.previous = now;
        }

        boolean finishIfDone(final Runnable assertGrown) {
            if (this.done) {
                return true;
            }
            final boolean grown = this.previous.is(BlockTags.LOGS);
            if (!grown && this.count < GrowthGameTests.MAX_USES) {
                return false;
            }
            this.done = true;
            Sponge.eventManager().unregisterListeners(this.listener);
            GameTestAssertions.assertTrue(this.helper, grown, "No tree after " + this.count + " uses");
            assertGrown.run();
            this.helper.succeed();
            return true;
        }

        private void record(final Event event) {
            final String name;
            // Only its Pre: the Post rolls the interaction's events up once they are done
            if (event instanceof InteractBlockEvent.Secondary.Pre) {
                name = GrowthGameTests.INTERACT;
            } else if (event instanceof TickBlockEvent.Random) {
                name = GrowthGameTests.RANDOM_TICK;
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
            if (name.equals(GrowthGameTests.INTERACT) || name.equals(GrowthGameTests.INVENTORY) || name.equals(GrowthGameTests.RANDOM_TICK)) {
                return;
            }
            if (this.boneMeal && !event.context().get(EventContextKeys.USED_ITEM).filter(item -> item.type() == ItemTypes.BONE_MEAL.get()).isPresent()) {
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
