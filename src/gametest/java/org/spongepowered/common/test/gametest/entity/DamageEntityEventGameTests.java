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
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;
import org.spongepowered.math.vector.Vector3d;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Health changes by the final damage {@link DamageEntityEvent} leaves, and not at all when it is cancelled. Forge and
 * Neo apply a non-player living entity's damage from locals of their own, so a pig stands in for every such entity.
 *
 * <p>{@code sponge:damage/pig_floor}: a 3x3 bedrock floor for the pig spawned at (1,1,1).</p>
 */
@GameTestGroup("damage")
public final class DamageEntityEventGameTests {

    private static final String STRUCTURE = "sponge:damage/pig_floor";
    private static final BlockPos PIG = new BlockPos(1, 1, 1);
    private static final double DAMAGE = 4;

    @GameTest(structure = DamageEntityEventGameTests.STRUCTURE)
    void cancelledDamageLeavesPigUnhurt(final GameTestHelper helper) {
        final Pig pig = DamageEntityEventGameTests.hurt(helper, event -> event.setCancelled(true));
        GameTestAssertions.assertValueEqual(helper, pig.getHealth(), pig.getMaxHealth(), "health after a cancelled hit");
        helper.succeed();
    }

    @GameTest(structure = DamageEntityEventGameTests.STRUCTURE)
    void pigLosesChangedBaseDamage(final GameTestHelper helper) {
        final Pig pig = DamageEntityEventGameTests.hurt(helper, event -> event.setBaseDamage(1));
        GameTestAssertions.assertValueEqual(helper, pig.getHealth(), pig.getMaxHealth() - 1, "health after a hit changed to 1 damage");
        helper.succeed();
    }

    /** Hits a new pig for {@link #DAMAGE}, handing its {@link DamageEntityEvent} to {@code listener} */
    private static Pig hurt(final GameTestHelper helper, final Consumer<DamageEntityEvent> listener) {
        final ServerWorld world = (ServerWorld) helper.getLevel();
        final BlockPos position = helper.absolutePos(DamageEntityEventGameTests.PIG);
        final var pig = world.createEntity(EntityTypes.PIG, Vector3d.from(position.getX() + 0.5, position.getY(), position.getZ() + 0.5));
        GameTestAssertions.assertTrue(helper, world.spawnEntity(pig), "The pig didn't spawn");

        final AtomicBoolean fired = new AtomicBoolean();
        final EventListener<DamageEntityEvent> onDamage = event -> {
            if (event.entity() == pig) {
                fired.set(true);
                listener.accept(event);
            }
        };
        SpongeGameTests.listen(DamageEntityEvent.class, onDamage);
        try {
            pig.damage(DamageEntityEventGameTests.DAMAGE, DamageSources.GENERIC);
        } finally {
            Sponge.eventManager().unregisterListeners(onDamage);
        }
        GameTestAssertions.assertTrue(helper, fired.get(), "No DamageEntityEvent for the pig");
        return (Pig) pig;
    }
}
