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
package org.spongepowered.common.test.gametest.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.common.util.VecHelper;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestGroup("interact_item_primary")
public final class InteractItemPrimaryGameTests {

    private static final BlockPos BLOCK = new BlockPos(0, 2, 2);
    // In reach of the player, who stands at (0.5, 1, 0.5)
    private static final Vec3 ENTITY = new Vec3(0.5, 1, 1.5);
    private static final int CLICKS = 5;

    @GameTest(skyAccess = true)
    void airClicksAfterBlockHits(final GameTestHelper helper) {
        helper.setBlock(InteractItemPrimaryGameTests.BLOCK, Blocks.STONE);
        final ServerPlayer player = InteractItemPrimaryGameTests.survivalPlayer(helper);
        final Counters counters = Counters.listen(player);
        try {
            final BlockPos block = helper.absolutePos(InteractItemPrimaryGameTests.BLOCK);
            for (int i = 0; i < InteractItemPrimaryGameTests.CLICKS; i++) {
                InteractItemPrimaryGameTests.hitBlock(player, block);
            }
            GameTestAssertions.assertValueEqual(helper, counters.blockStarts.get(), InteractItemPrimaryGameTests.CLICKS,
                "InteractBlockEvent.Primary.Start count");
            GameTestAssertions.assertValueEqual(helper, counters.airClicks.get(), 0, "InteractItemEvent.Primary count while hitting a block");

            for (int i = 0; i < InteractItemPrimaryGameTests.CLICKS; i++) {
                InteractItemPrimaryGameTests.clickAir(player);
            }
            GameTestAssertions.assertValueEqual(helper, counters.airClicks.get(), InteractItemPrimaryGameTests.CLICKS,
                "InteractItemEvent.Primary count after clicking air");
        } finally {
            counters.stop();
            InteractItemPrimaryGameTests.remove(player);
        }
        helper.succeed();
    }

    // The swing that goes with dropping an item is still skipped, and only that one
    @GameTest(skyAccess = true)
    void dropSwingSkipped(final GameTestHelper helper) {
        InteractItemPrimaryGameTests.assertSwingSkipped(helper, "the drop", player ->
            player.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.DROP_ITEM, BlockPos.ZERO, Direction.DOWN)));
    }

    @GameTest(skyAccess = true)
    void entityAttackSwingSkipped(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, InteractItemPrimaryGameTests.ENTITY);
        try {
            InteractItemPrimaryGameTests.assertSwingSkipped(helper, "the entity attack", player ->
                player.connection.handleInteract(ServerboundInteractPacket.createAttackPacket(stand, false)));
        } finally {
            stand.discard();
        }
    }

    @GameTest(skyAccess = true)
    void entityInteractSwingSkipped(final GameTestHelper helper) {
        final ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, InteractItemPrimaryGameTests.ENTITY);
        try {
            InteractItemPrimaryGameTests.assertSwingSkipped(helper, "the entity interaction", player ->
                player.connection.handleInteract(ServerboundInteractPacket.createInteractionPacket(stand, false, InteractionHand.MAIN_HAND, Vec3.ZERO)));
        } finally {
            stand.discard();
        }
    }

    // A plugin cancelling the hit doesn't turn the swing that goes with it into a click at air
    @GameTest(skyAccess = true)
    void cancelledBlockHitSwingSkipped(final GameTestHelper helper) {
        helper.setBlock(InteractItemPrimaryGameTests.BLOCK, Blocks.STONE);
        final BlockPos block = helper.absolutePos(InteractItemPrimaryGameTests.BLOCK);
        final AtomicInteger cancelled = new AtomicInteger();
        final EventListener<InteractBlockEvent.Primary.Start> canceller = event -> {
            if (event.block().position().equals(VecHelper.toVector3i(block))) {
                event.setCancelled(true);
                cancelled.incrementAndGet();
            }
        };
        SpongeGameTests.listen(InteractBlockEvent.Primary.Start.class, canceller);
        try {
            InteractItemPrimaryGameTests.assertSwingSkipped(helper, "the cancelled block hit", player ->
                player.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, block, Direction.NORTH)));
            GameTestAssertions.assertValueEqual(helper, cancelled.get(), 1, "cancelled InteractBlockEvent.Primary.Start count");
        } finally {
            Sponge.eventManager().unregisterListeners(canceller);
        }
    }

    private static void assertSwingSkipped(final GameTestHelper helper, final String action, final Consumer<ServerPlayer> perform) {
        final ServerPlayer player = InteractItemPrimaryGameTests.survivalPlayer(helper);
        final Counters counters = Counters.listen(player);
        try {
            perform.accept(player);
            InteractItemPrimaryGameTests.clickAir(player);
            GameTestAssertions.assertValueEqual(helper, counters.airClicks.get(), 0, "InteractItemEvent.Primary count for the swing after " + action);

            InteractItemPrimaryGameTests.clickAir(player);
            GameTestAssertions.assertValueEqual(helper, counters.airClicks.get(), 1, "InteractItemEvent.Primary count after clicking air");
        } finally {
            counters.stop();
            InteractItemPrimaryGameTests.remove(player);
        }
        helper.succeed();
    }

    /** As a survival client sends a short click on a block: start destroying, swing, then let go */
    private static void hitBlock(final ServerPlayer player, final BlockPos block) {
        final Vec3 eye = player.getEyePosition();
        final Vec3 to = Vec3.atCenterOf(block).subtract(eye);
        final float yaw = (float) Math.toDegrees(Math.atan2(-to.x, to.z));
        final float pitch = (float) -Math.toDegrees(Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)));
        player.moveTo(player.getX(), player.getY(), player.getZ(), yaw, pitch);

        player.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, block, Direction.NORTH));
        player.connection.handleAnimate(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        player.connection.handlePlayerAction(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, block, Direction.NORTH));
    }

    private static void clickAir(final ServerPlayer player) {
        InteractItemPrimaryGameTests.lookUp(player);
        player.connection.handleAnimate(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
    }

    private static void lookUp(final ServerPlayer player) {
        player.moveTo(player.getX(), player.getY(), player.getZ(), 0, -90);
    }

    /**
     * A connected player standing in the test. Vanilla's mock player is always in creative, where hitting stone breaks
     * it at once, so it's switched to survival to start destroying the block instead.
     */
    private static ServerPlayer survivalPlayer(final GameTestHelper helper) {
        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        final Vec3 pos = helper.absoluteVec(new Vec3(0.5, 1, 0.5));
        player.moveTo(pos.x, pos.y, pos.z, 0, 0);
        return player;
    }

    private static void remove(final ServerPlayer player) {
        player.level().getServer().getPlayerList().remove(player);
    }

    private record Counters(ServerPlayer player, AtomicInteger airClicks, AtomicInteger blockStarts,
        EventListener<InteractItemEvent.Primary> itemListener, EventListener<InteractBlockEvent.Primary.Start> blockListener) {

        static Counters listen(final ServerPlayer player) {
            final AtomicInteger airClicks = new AtomicInteger();
            final AtomicInteger blockStarts = new AtomicInteger();
            final EventListener<InteractItemEvent.Primary> itemListener = event -> {
                if (event.cause().first(org.spongepowered.api.entity.living.player.server.ServerPlayer.class).orElse(null) == player) {
                    airClicks.incrementAndGet();
                }
            };
            final EventListener<InteractBlockEvent.Primary.Start> blockListener = event -> {
                if (event.cause().first(org.spongepowered.api.entity.living.player.server.ServerPlayer.class).orElse(null) == player) {
                    blockStarts.incrementAndGet();
                }
            };
            SpongeGameTests.listen(InteractItemEvent.Primary.class, itemListener);
            SpongeGameTests.listen(InteractBlockEvent.Primary.Start.class, blockListener);
            return new Counters(player, airClicks, blockStarts, itemListener, blockListener);
        }

        void stop() {
            Sponge.eventManager().unregisterListeners(this.itemListener);
            Sponge.eventManager().unregisterListeners(this.blockListener);
        }
    }
}
