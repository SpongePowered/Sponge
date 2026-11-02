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
package org.spongepowered.common.test.gametest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Blocks;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.world.explosion.ExplosionBlockInteractions;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * What the creeper gametests share. {@code sponge:explosion/stone_ring}: a bedrock floor with stone on each side of
 * (3,1,3), where the creeper is spawned, so a creeper whose explosion breaks blocks breaks all four stones.
 */
final class CreeperExplosions {

    static final String STONE_RING = "sponge:explosion/stone_ring";
    // The default fuse is 30 ticks, after which the creeper is gone whether its explosion broke anything or not
    static final int MAX_TICKS = 80;

    private static final BlockPos CREEPER = new BlockPos(3, 1, 3);
    private static final List<BlockPos> STONES = List.of(new BlockPos(2, 1, 3), new BlockPos(4, 1, 3), new BlockPos(3, 1, 2), new BlockPos(3, 1, 4));

    /** A creeper in the middle of the stones, not yet lit */
    static Creeper spawn(final GameTestHelper helper) {
        // Spawned through the API rather than GameTestHelper#spawn, as vanilla's entity type constants moved in 26.2
        final ServerWorld world = (ServerWorld) helper.getLevel();
        final BlockPos position = helper.absolutePos(CreeperExplosions.CREEPER);
        final org.spongepowered.api.entity.living.monster.Creeper creeper = world.createEntity(EntityTypes.CREEPER,
            Vector3d.from(position.getX() + 0.5, position.getY(), position.getZ() + 0.5));
        GameTestAssertions.assertTrue(helper, world.spawnEntity(creeper), "The creeper didn't spawn");
        return (Creeper) creeper;
    }

    // How a plugin changes whether the explosion breaks blocks is the one thing that differs between Sponge API
    // versions: api-14 replaced shouldBreakBlocks with blockInteraction

    /** Has the explosion break blocks, whatever it would have done, as a plugin can before the creeper detonates */
    static void breakBlocks(final DetonateExplosiveEvent event) {
        event.explosionBuilder().blockInteraction(ExplosionBlockInteractions.DESTROY.get());
    }

    /** Has the explosion keep every block, whatever it would have done, as a plugin can before the creeper detonates */
    static void keepBlocks(final DetonateExplosiveEvent event) {
        event.explosionBuilder().blockInteraction(ExplosionBlockInteractions.KEEP.get());
    }

    /**
     * Lights the creeper, passing each {@link DetonateExplosiveEvent} it throws to the listener, which may change the
     * explosion it is about to cause.
     */
    static Detonation detonate(final Creeper creeper, final Consumer<DetonateExplosiveEvent> listener) {
        final AtomicReference<@Nullable DetonateExplosiveEvent> seen = new AtomicReference<>();
        final EventListener<DetonateExplosiveEvent> registered = event -> {
            if (event.explosive() == creeper) {
                seen.set(event);
                listener.accept(event);
            }
        };
        SpongeGameTests.listen(DetonateExplosiveEvent.class, registered);
        creeper.ignite();
        return new Detonation(creeper, seen, registered);
    }

    static Detonation detonate(final Creeper creeper) {
        return CreeperExplosions.detonate(creeper, event -> {});
    }

    record Detonation(Creeper creeper, AtomicReference<@Nullable DetonateExplosiveEvent> event, EventListener<DetonateExplosiveEvent> listener) {

        /**
         * Waits for the creeper to explode, then checks whether the explosion it started with would break blocks, and
         * whether the stones were broken by the one it caused.
         */
        void succeedWhenExploded(final GameTestHelper helper, final boolean originallyBreaks, final boolean breaks) {
            helper.succeedWhen(() -> {
                final @Nullable DetonateExplosiveEvent event = this.event.get();
                GameTestAssertions.assertTrue(helper, event != null, "The creeper threw no DetonateExplosiveEvent");
                GameTestAssertions.assertTrue(helper, this.creeper.isRemoved(), "The creeper hasn't exploded");
                GameTestAssertions.assertValueEqual(helper, event.originalExplosion().shouldBreakBlocks(), originallyBreaks,
                    "whether the creeper's original explosion breaks blocks");
                for (final BlockPos stone : CreeperExplosions.STONES) {
                    if (breaks) {
                        helper.assertBlockNotPresent(Blocks.STONE, stone);
                    } else {
                        helper.assertBlockPresent(Blocks.STONE, stone);
                    }
                }
                Sponge.eventManager().unregisterListeners(this.listener);
            });
        }
    }

    private CreeperExplosions() {
    }
}
