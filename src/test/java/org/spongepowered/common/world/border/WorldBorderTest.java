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
package org.spongepowered.common.world.border;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;

public class WorldBorderTest {

    @Test
    public void testBorderIsApplied() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
        final WorldBorder worldBorder = world.border();
        final WorldBorder border = WorldBorder.builder().center(1, 1).initialDiameter(1).build();

        try {
            world.setBorder(border);

            Assertions.assertEquals(border, world.border());
        } finally {
            world.setBorder(worldBorder);
        }
    }

    @Test
    public void testBorderIsWorldSpecific() {
        final ServerWorld world = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
        final ServerWorld nether = Sponge.server().worldManager().world(DefaultWorldKeys.THE_NETHER).get();

        final WorldBorder worldBorder = world.border();
        final WorldBorder worldBorderNether = nether.border();

        final WorldBorder border = WorldBorder.builder().center(1, 1).initialDiameter(1).build();
        final WorldBorder netherBorder = WorldBorder.builder().center(2, 2).initialDiameter(2).build();

        try {
            world.setBorder(border);
            nether.setBorder(netherBorder);

            Assertions.assertEquals(border, world.border());
            Assertions.assertEquals(netherBorder, nether.border());
        } finally {
            world.setBorder(worldBorder);
            nether.setBorder(worldBorderNether);
        }
    }

}
