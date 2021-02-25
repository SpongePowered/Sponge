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
package org.spongepowered.common.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.Collection;

public class BlockChangeFlagsTest {

    @Test
    public void initializeBlockChangeFlags() {
        final BlockChangeFlagManager instance = BlockChangeFlagManager.getInstance();
        final Collection<SpongeBlockChangeFlag> values = instance.getValues();
        assertFalse(values.isEmpty(), "SpongeBlockChangeFlags should be prepopulated");
    }

    @Test
    public void verifyNeighborCheckDisabledAfterFlipping() {
        final SpongeBlockChangeFlag defaultFlag = BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.DEFAULT);
        final int rawFlag = defaultFlag.withUpdateNeighbors(false).getRawFlag();
        assertEquals(Constants.BlockChangeFlags.NOTIFY_CLIENTS, rawFlag, "Notify clients was retained but notify neighbors was still true");
    }

    @Test
    public void verifyClientUpdateDisabledAfterFlipping() {
        final SpongeBlockChangeFlag defaultFlag = BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.DEFAULT);
        final int rawFlag = defaultFlag.withNotifyClients(false).getRawFlag();
        assertEquals(Constants.BlockChangeFlags.NEIGHBOR_MASK, rawFlag, "No");
    }
}
