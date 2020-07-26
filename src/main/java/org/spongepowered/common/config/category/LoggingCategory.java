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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class LoggingCategory extends ConfigCategory {

    @Setting(value = "block-break", comment = "Log when blocks are broken")
    private boolean blockBreakLogging = false;
    @Setting(value = "block-modify", comment = "Log when blocks are modified")
    private boolean blockModifyLogging = false;
    @Setting(value = "block-place", comment = "Log when blocks are placed")
    private boolean blockPlaceLogging = false;
    @Setting(value = "block-populate", comment = "Log when blocks are populated in a chunk")
    private boolean blockPopulateLogging = false;
    @Setting(value = "block-tracking", comment = "Log when blocks are placed by players and tracked")
    private boolean blockTrackLogging = false;
    @Setting(value = "chunk-load", comment = "Log when chunks are loaded")
    private boolean chunkLoadLogging = false;
    @Setting(value = "chunk-unload", comment = "Log when chunks are unloaded")
    private boolean chunkUnloadLogging = false;
    @Setting(value = "chunk-gc-queue-unload",
            comment = "Log when chunks are queued to be unloaded by the chunk garbage collector.")
    private boolean chunkGCQueueUnloadLogging = false;
    @Setting(value = "entity-spawn", comment = "Log when living entities are spawned")
    private boolean entitySpawnLogging = false;
    @Setting(value = "entity-despawn", comment = "Log when living entities are despawned")
    private boolean entityDespawnLogging = false;
    @Setting(value = "entity-death", comment = "Log when living entities are destroyed")
    private boolean entityDeathLogging = false;
    @Setting(value = "exploit-sign-command-updates", comment = ""
            + "Log when server receives exploited packet to update a sign containing commands\n"
            + "from player with no permission.")
    public boolean logExploitSignCommandUpdates = false;
    @Setting(value = "exploit-itemstack-name-overflow",
            comment = "Log when server receives exploited packet with itemstack name exceeding string limit.")
    public boolean logExploitItemStackNameOverflow = false;
    @Setting(value = "exploit-respawn-invisibility",
            comment = "Log when player attempts to respawn invisible to surrounding players.")
    public boolean logExploitRespawnInvisibility = false;
    @Setting(value = "log-stacktraces", comment = "Add stack traces to dev logging")
    private boolean logWithStackTraces = false;
    @Setting(value = "entity-collision-checks", comment = "Whether to log entity collision/count checks")
    private boolean logEntityCollisionChecks = false;
    @Setting(value = "entity-speed-removal", comment = "Whether to log entity removals due to speed")
    private boolean logEntitySpeedRemoval = false;
    @Setting(value = "transaction-merge-fail",
            comment = "Log when two conflicting changes are merged into one. (This number specifies the maximum number of"
                    + "\nmessages to log. Set to 0 to show all messages.)")
    private int logTransactionMergeFailure = 25;
    @Setting(value = "world-auto-save", comment = ""
            + "Log when a world auto-saves its chunk data.\n"
            + "Note: This may be spammy depending on the auto-save-interval configured for world.")
    private boolean logWorldAutomaticSaving = false;

    public boolean blockBreakLogging() {
        return this.blockBreakLogging;
    }

    public void setBlockBreakLogging(boolean flag) {
        this.blockBreakLogging = flag;
    }

    public boolean blockModifyLogging() {
        return this.blockModifyLogging;
    }

    public void setBlockModifyLogging(boolean flag) {
        this.blockModifyLogging = flag;
    }

    public boolean blockPlaceLogging() {
        return this.blockPlaceLogging;
    }

    public void setBlockPlaceLogging(boolean flag) {
        this.blockPlaceLogging = flag;
    }

    public boolean blockPopulateLogging() {
        return this.blockPopulateLogging;
    }

    public void setBlockPopulateLogging(boolean flag) {
        this.blockPopulateLogging = flag;
    }

    public boolean blockTrackLogging() {
        return this.blockTrackLogging;
    }

    public void setBlockTrackLogging(boolean flag) {
        this.blockTrackLogging = flag;
    }

    public boolean chunkLoadLogging() {
        return this.chunkLoadLogging;
    }

    public void setChunkLoadLogging(boolean flag) {
        this.chunkLoadLogging = flag;
    }

    public boolean chunkUnloadLogging() {
        return this.chunkUnloadLogging;
    }

    public void setChunkUnloadLogging(boolean flag) {
        this.chunkUnloadLogging = flag;
    }

    public void setChunkGCQueueUnloadLogging(boolean flag) {
        this.chunkGCQueueUnloadLogging = flag;
    }

    public boolean chunkGCQueueUnloadLogging() {
        return this.chunkGCQueueUnloadLogging;
    }

    public boolean entitySpawnLogging() {
        return this.entitySpawnLogging;
    }

    public void setEntitySpawnLogging(boolean flag) {
        this.entitySpawnLogging = flag;
    }

    public boolean entityDespawnLogging() {
        return this.entityDespawnLogging;
    }

    public void setEntityDespawnLogging(boolean flag) {
        this.entityDespawnLogging = flag;
    }

    public boolean entityDeathLogging() {
        return this.entityDeathLogging;
    }

    public void setEntityDeathLogging(boolean flag) {
        this.entityDeathLogging = flag;
    }

    public boolean logWithStackTraces() {
        return this.logWithStackTraces;
    }

    public void setLogWithStackTraces(boolean flag) {
        this.logWithStackTraces = flag;
    }

    public boolean logEntityCollisionChecks() {
        return this.logEntityCollisionChecks;
    }

    public void setLogEntityCollisionChecks(boolean flag) {
        this.logEntityCollisionChecks = flag;
    }

    public boolean logEntitySpeedRemoval() {
        return this.logEntitySpeedRemoval;
    }

    public void setLogEntitySpeedRemoval(boolean flag) {
        this.logEntitySpeedRemoval = flag;
    }

    public int logTransactionMergeFailure() {
        return this.logTransactionMergeFailure;
    }

    public boolean logWorldAutomaticSaving() {
        return this.logWorldAutomaticSaving;
    }

}
