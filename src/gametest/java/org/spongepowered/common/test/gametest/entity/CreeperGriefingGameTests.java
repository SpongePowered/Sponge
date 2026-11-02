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

import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestAssertions;
import org.spongepowered.common.test.gametest.GameTestGroup;

/**
 * Whether a creeper's explosion breaks blocks while the {@code mobGriefing} rule allows it, which is the rule's
 * default. A plugin can still keep blocks from one creeper with {@link Keys#CAN_GRIEF}, or from one explosion through
 * its {@link org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent}.
 *
 * @see CreeperNoMobGriefingGameTests
 */
@GameTestGroup("creeper_griefing")
public final class CreeperGriefingGameTests {

    @GameTest(structure = CreeperExplosions.STONE_RING, maxTicks = CreeperExplosions.MAX_TICKS)
    void breaksBlocks(final GameTestHelper helper) {
        CreeperExplosions.detonate(CreeperExplosions.spawn(helper)).succeedWhenExploded(helper, true, true);
    }

    @GameTest(structure = CreeperExplosions.STONE_RING, maxTicks = CreeperExplosions.MAX_TICKS)
    void cannotGriefKeepsBlocks(final GameTestHelper helper) {
        final net.minecraft.world.entity.monster.Creeper creeper = CreeperExplosions.spawn(helper);
        GameTestAssertions.assertTrue(helper, ((Creeper) creeper).offer(Keys.CAN_GRIEF, false).isSuccessful(), "Offering CAN_GRIEF to the creeper failed");

        CreeperExplosions.detonate(creeper).succeedWhenExploded(helper, false, false);
    }

    // Allowing it again, as a plugin undoing its own change would, leaves the creeper as it was spawned
    @GameTest(structure = CreeperExplosions.STONE_RING, maxTicks = CreeperExplosions.MAX_TICKS)
    void canGriefAgainBreaksBlocks(final GameTestHelper helper) {
        final net.minecraft.world.entity.monster.Creeper creeper = CreeperExplosions.spawn(helper);
        ((Creeper) creeper).offer(Keys.CAN_GRIEF, false);
        ((Creeper) creeper).offer(Keys.CAN_GRIEF, true);
        GameTestAssertions.assertValueEqual(helper, ((Creeper) creeper).get(Keys.CAN_GRIEF).orElse(null), true, "the creeper's CAN_GRIEF");

        CreeperExplosions.detonate(creeper).succeedWhenExploded(helper, true, true);
    }

    @GameTest(structure = CreeperExplosions.STONE_RING, maxTicks = CreeperExplosions.MAX_TICKS)
    void eventCanKeepBlocks(final GameTestHelper helper) {
        CreeperExplosions.detonate(CreeperExplosions.spawn(helper), CreeperExplosions::keepBlocks)
            .succeedWhenExploded(helper, true, false);
    }
}
