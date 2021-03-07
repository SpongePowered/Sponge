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
package org.spongepowered.common.event.tracking;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

class BlockChangeFlagManagerTest {

    @BeforeAll
    public static void setupBlockChangeFlagManager() {
        BlockChangeFlagManager.fromNativeInt(0);
    }

    private static SpongeBlockChangeFlag createDefaultFlag() {
        return BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.DEFAULT);
    }

    @Test
    void verifyDefaultFlag() {
        final SpongeBlockChangeFlag flag = BlockChangeFlagManagerTest.createDefaultFlag();
        assertTrue(flag.updateNeighbors()); // 1
        assertTrue(flag.notifyClients()); // 2
        assertFalse(flag.isIgnoreRender()); // 4
        assertFalse(flag.isForceReRender()); // 8
        assertTrue(flag.updateNeighboringShapes()); // 16
        assertTrue(flag.neighborDrops()); // 32
        assertFalse(flag.isBlockMoving()); // 64
        assertTrue(flag.updateLighting()); // 128
        assertTrue(flag.performBlockPhysics()); // 256 - Sponge added
        assertTrue(flag.notifyPathfinding()); // 512 - Sponge added
    }

    @Test
    void verifyNotifyClientFlagChanges() {
        final SpongeBlockChangeFlag flag = BlockChangeFlagManagerTest.createDefaultFlag();
        final SpongeBlockChangeFlag updated = flag.withNotifyClients(false);
        assertTrue(updated.updateNeighbors()); // 1
        assertFalse(updated.notifyClients(), "Flipping withNotifyClients false is not being changed"); // 2
        assertFalse(updated.isIgnoreRender()); // 4
        assertFalse(updated.isForceReRender()); // 8
        assertTrue(updated.updateNeighboringShapes()); // 16
        assertTrue(updated.neighborDrops()); // 32
        assertFalse(updated.isBlockMoving()); // 64
        assertTrue(updated.updateLighting()); // 128
        assertTrue(updated.performBlockPhysics()); // 256 - Sponge added
        assertTrue(updated.notifyPathfinding()); // 512 - Sponge added
    }

    @Test
    void verifyNoneFlag() {
        final SpongeBlockChangeFlag none = BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.NONE);
        assertFalse(none.updateNeighbors());
        assertTrue(none.notifyClients());
        assertFalse(none.isIgnoreRender());
        assertFalse(none.isForceReRender());
        assertFalse(none.updateNeighboringShapes());
        assertFalse(none.neighborDrops());
        assertFalse(none.isBlockMoving());
        assertTrue(none.updateLighting());
        assertFalse(none.performBlockPhysics());
        assertFalse(none.notifyPathfinding());
    }

    @Test
    void verifyRestoration() {
        final SpongeBlockChangeFlag restore = BlockChangeFlagManager.fromNativeInt(Constants.BlockChangeFlags.FORCED_RESTORE);
        assertFalse(restore.updateNeighbors());
        assertTrue(restore.notifyClients());
        assertFalse(restore.isIgnoreRender());
        assertTrue(restore.isForceReRender());
        assertFalse(restore.updateNeighboringShapes());
        assertFalse(restore.neighborDrops());
        assertFalse(restore.isBlockMoving());
        assertTrue(restore.updateLighting());
        assertFalse(restore.performBlockPhysics());
        assertTrue(restore.notifyPathfinding());
    }

    @Test
    void verifyEmpty() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        assertFalse(empty.updateNeighbors());
        assertFalse(empty.notifyClients());
        assertFalse(empty.isIgnoreRender());
        assertFalse(empty.isForceReRender());
        assertTrue(empty.updateNeighboringShapes());
        assertTrue(empty.neighborDrops());
        assertFalse(empty.isBlockMoving());
        assertTrue(empty.updateLighting());
        assertTrue(empty.performBlockPhysics());
        assertTrue(empty.notifyPathfinding());
    }

    @Test
    void verifyEmptyToNotifyNeighbors() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag neighbors = empty.withUpdateNeighbors(true);
        assertTrue(neighbors.updateNeighbors());
        assertFalse(neighbors.notifyClients());
        assertFalse(neighbors.isIgnoreRender());
        assertFalse(neighbors.isForceReRender());
        assertTrue(neighbors.updateNeighboringShapes());
        assertTrue(neighbors.neighborDrops());
        assertFalse(neighbors.isBlockMoving());
        assertTrue(neighbors.updateLighting());
        assertTrue(neighbors.performBlockPhysics());
        assertTrue(neighbors.notifyPathfinding());
    }

    @Test
    void verifyEmptyToNotifyClients() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag clients = empty.withNotifyClients(true);
        assertFalse(clients.updateNeighbors());
        assertTrue(clients.notifyClients());
        assertFalse(clients.isIgnoreRender());
        assertFalse(clients.isForceReRender());
        assertTrue(clients.updateNeighboringShapes());
        assertTrue(clients.neighborDrops());
        assertFalse(clients.isBlockMoving());
        assertTrue(clients.updateLighting());
        assertTrue(clients.performBlockPhysics());
        assertTrue(clients.notifyPathfinding());
    }

    @Test
    void verifyWithNotifyObservers() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag observers = empty.withNotifyObservers(true);
        assertFalse(observers.updateNeighbors());
        assertFalse(observers.notifyClients());
        assertFalse(observers.isIgnoreRender());
        assertFalse(observers.isForceReRender());
        assertTrue(observers.updateNeighboringShapes());
        assertTrue(observers.neighborDrops());
        assertFalse(observers.isBlockMoving());
        assertTrue(observers.updateLighting());
        assertTrue(observers.performBlockPhysics());
        assertTrue(observers.notifyPathfinding());
    }

    @Test
    void verifyWithNoPhysics() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag noPhysics = empty.withPhysics(false);
        assertFalse(noPhysics.updateNeighbors());
        assertFalse(noPhysics.notifyClients());
        assertFalse(noPhysics.isIgnoreRender());
        assertFalse(noPhysics.isForceReRender());
        assertTrue(noPhysics.updateNeighboringShapes());
        assertTrue(noPhysics.neighborDrops());
        assertFalse(noPhysics.isBlockMoving());
        assertTrue(noPhysics.updateLighting());
        assertFalse(noPhysics.performBlockPhysics());
        assertTrue(noPhysics.notifyPathfinding());
    }

    @Test
    void verifyWithNoLighting() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag noLighting = empty.withLightingUpdates(false);
        assertFalse(noLighting.updateNeighbors());
        assertFalse(noLighting.notifyClients());
        assertFalse(noLighting.isIgnoreRender());
        assertFalse(noLighting.isForceReRender());
        assertTrue(noLighting.updateNeighboringShapes());
        assertTrue(noLighting.neighborDrops());
        assertFalse(noLighting.isBlockMoving());
        assertFalse(noLighting.updateLighting());
        assertTrue(noLighting.performBlockPhysics());
        assertTrue(noLighting.notifyPathfinding());
    }

    @Test
    void verifyWithNoPathfinding() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag noPathfinding = empty.withPathfindingUpdates(false);
        assertFalse(noPathfinding.updateNeighbors());
        assertFalse(noPathfinding.notifyClients());
        assertFalse(noPathfinding.isIgnoreRender());
        assertFalse(noPathfinding.isForceReRender());
        assertTrue(noPathfinding.updateNeighboringShapes());
        assertTrue(noPathfinding.neighborDrops());
        assertFalse(noPathfinding.isBlockMoving());
        assertTrue(noPathfinding.updateLighting());
        assertTrue(noPathfinding.performBlockPhysics());
        assertFalse(noPathfinding.notifyPathfinding());
    }

    @Test
    void verifyEmptyToInverse() {
        final SpongeBlockChangeFlag empty = BlockChangeFlagManager.fromNativeInt(0);
        final SpongeBlockChangeFlag inverse = empty.inverse();
        assertTrue(inverse.updateNeighbors());
        assertTrue(inverse.notifyClients());
        assertTrue(inverse.isIgnoreRender());
        assertFalse(inverse.isForceReRender());
        assertFalse(inverse.updateNeighboringShapes());
        assertFalse(inverse.neighborDrops());
        assertTrue(inverse.isBlockMoving());
        assertFalse(inverse.updateLighting());
        assertFalse(inverse.performBlockPhysics());
        assertFalse(inverse.notifyPathfinding());
    }

    /**
     * This is a test that Mojang is doing a bitwise and'ing of a flag
     * with -34 if the flag if flag.updateNeighboringShapes() is true
     * Basically put:
     *  <pre>{@code if ((var3 & 16) == 0 && var4 > 0) {
     *     int var9 = var3 & -34; <---- this code right here is where we want to verify
     *     var7.updateIndirectNeighbourShapes(this, var1, var9, var4 - 1);
     *     var2.updateNeighbourShapes(this, var1, var9, var4 - 1);
     *     var2.updateIndirectNeighbourShapes(this, var1, var9, var4 - 1);
     * }}</pre>
     * This is used by {@link org.spongepowered.common.event.tracking.context.transaction.effect.UpdateConnectingBlocksEffect}
     * with {@link BlockChangeFlag#updateNeighboringShapes()}
     */
    @Test
    void verifyNestedNeighborPhysics() {
        final SpongeBlockChangeFlag flag = BlockChangeFlagManagerTest.createDefaultFlag();
        final SpongeBlockChangeFlag otherFlag = BlockChangeFlagManager.fromNativeInt(47);
        assertTrue(otherFlag.updateNeighboringShapes()); // 16
        assertTrue(otherFlag.getRawFlag() > 0);
        assertTrue(flag.updateNeighboringShapes()); // 16
        assertTrue(flag.getRawFlag() > 0);
        final SpongeBlockChangeFlag nestedNeighbor = flag.asNestedNeighborUpdates();
        assertFalse(nestedNeighbor.updateNeighbors());
        assertTrue(nestedNeighbor.notifyClients());
        assertFalse(nestedNeighbor.isIgnoreRender());
        assertFalse(nestedNeighbor.isForceReRender());
        assertTrue(nestedNeighbor.updateNeighboringShapes());
        assertFalse(nestedNeighbor.neighborDrops());
        assertFalse(nestedNeighbor.isBlockMoving());
        assertTrue(nestedNeighbor.updateLighting());
        assertTrue(nestedNeighbor.performBlockPhysics());
        assertTrue(nestedNeighbor.notifyPathfinding());
        final SpongeBlockChangeFlag nestedOther = otherFlag.asNestedNeighborUpdates();
        assertFalse(nestedOther.updateNeighbors()); // 1
        assertTrue(nestedOther.notifyClients());    // 2
        assertTrue(nestedOther.isIgnoreRender());   // 4
        assertFalse(nestedOther.isForceReRender()); // 8
        assertTrue(nestedOther.updateNeighboringShapes()); // 16
        assertFalse(nestedOther.neighborDrops());    // 32


    }


}