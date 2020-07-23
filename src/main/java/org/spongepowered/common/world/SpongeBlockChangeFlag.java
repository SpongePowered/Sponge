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
package org.spongepowered.common.world;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.util.Constants;

/**
 * A flag of sorts that determines whether a block change will perform various
 * interactions, such as notifying neighboring blocks, performing block physics
 * on placement, etc.
 */
public final class SpongeBlockChangeFlag implements BlockChangeFlag {

    private final boolean updateNeighbors;
    private final boolean performBlockPhysics;
    private final boolean notifyObservers;
    private final boolean notifyClients;
    private final boolean ignoreRender;
    private final boolean forceReRender;
    private final boolean blockMoving;
    private final int rawFlag;
    private final String name;

    public SpongeBlockChangeFlag(final String name, final int flag) {
        this.updateNeighbors = (flag & Constants.BlockChangeFlags.NEIGHBOR_MASK) != 0;
        this.performBlockPhysics = (flag & Constants.BlockChangeFlags.PHYSICS_MASK) == 0;
        this.notifyObservers = (flag & Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE) == 0;
        this.notifyClients = (flag & Constants.BlockChangeFlags.NOTIFY_CLIENTS) != 0;
        this.ignoreRender = (flag & Constants.BlockChangeFlags.IGNORE_RENDER) != 0;
        this.forceReRender = (flag & Constants.BlockChangeFlags.FORCE_RE_RENDER) != 0 && !this.ignoreRender;
        this.blockMoving = (flag & Constants.BlockChangeFlags.BLOCK_MOVING) != 0;
        this.rawFlag = flag;
        this.name = name;
    }

    @Override
    public boolean updateNeighbors() {
        return this.updateNeighbors;
    }

    @Override
    public boolean performBlockPhysics() {
        return this.performBlockPhysics;
    }

    @Override
    public boolean notifyObservers() {
        return this.notifyObservers;
    }

    @Override
    public SpongeBlockChangeFlag withUpdateNeighbors(final boolean updateNeighbors) {
        if (this.updateNeighbors == updateNeighbors) {
            return this;
        }
        final int maskedFlag = (updateNeighbors ? Constants.BlockChangeFlags.NEIGHBOR_MASK : 0)
                                   | (this.performBlockPhysics ? 0 : Constants.BlockChangeFlags.PHYSICS_MASK)
                                   | (this.notifyObservers ? 0 : Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE)
                                   | (this.notifyClients ? Constants.BlockChangeFlags.NOTIFY_CLIENTS : 0)
                                   | (this.ignoreRender ? Constants.BlockChangeFlags.IGNORE_RENDER : 0)
                                   | (this.forceReRender ? Constants.BlockChangeFlags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag withPhysics(final boolean performBlockPhysics) {
        if (this.performBlockPhysics == performBlockPhysics) {
            return this;
        }
        final int maskedFlag = (this.updateNeighbors ? Constants.BlockChangeFlags.NEIGHBOR_MASK : 0)
                                   | (performBlockPhysics ? 0 : Constants.BlockChangeFlags.PHYSICS_MASK)
                                   | (this.notifyObservers ? 0 : Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE)
                                   | (this.notifyClients ? Constants.BlockChangeFlags.NOTIFY_CLIENTS : 0)
                                   | (this.ignoreRender ? Constants.BlockChangeFlags.IGNORE_RENDER : 0)
                                   | (this.forceReRender ? Constants.BlockChangeFlags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag withNotifyObservers(final boolean notifyObservers) {
        if (this.notifyObservers == notifyObservers) {
            return this;
        }
        final int maskedFlag = (this.updateNeighbors ? Constants.BlockChangeFlags.NEIGHBOR_MASK : 0)
                                   | (this.performBlockPhysics ? 0 : Constants.BlockChangeFlags.PHYSICS_MASK)
                                   | (notifyObservers ? 0 : Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE)
                                   | (this.notifyClients ? Constants.BlockChangeFlags.NOTIFY_CLIENTS : 0)
                                   | (this.ignoreRender ? Constants.BlockChangeFlags.IGNORE_RENDER : 0)
                                   | (this.forceReRender ? Constants.BlockChangeFlags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);

    }

    @Override
    public BlockChangeFlag inverse() {
        final int maskedFlag = (this.updateNeighbors ? 0 : Constants.BlockChangeFlags.NEIGHBOR_MASK)
                                   | (this.performBlockPhysics ? Constants.BlockChangeFlags.PHYSICS_MASK : 0)
                                   | (this.notifyObservers ? Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE : 0)
                                   | (this.notifyClients ? 0 : Constants.BlockChangeFlags.NOTIFY_CLIENTS)
                                   | (this.ignoreRender ? 0 : Constants.BlockChangeFlags.IGNORE_RENDER)
                                   | (this.forceReRender ? 0 : Constants.BlockChangeFlags.FORCE_RE_RENDER);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag andFlag(final BlockChangeFlag flag) {
        final SpongeBlockChangeFlag o = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors || o.updateNeighbors ? Constants.BlockChangeFlags.NEIGHBOR_MASK : 0)
                                   | (this.performBlockPhysics || o.performBlockPhysics ? 0 : Constants.BlockChangeFlags.PHYSICS_MASK)
                                   | (this.notifyObservers || o.notifyObservers ? 0 : Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE)
                                   | (this.notifyClients || o.notifyClients ? Constants.BlockChangeFlags.NOTIFY_CLIENTS : 0)
                                   | (this.ignoreRender || o.ignoreRender ? Constants.BlockChangeFlags.IGNORE_RENDER : 0)
                                   | (this.forceReRender || o.forceReRender ? Constants.BlockChangeFlags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag andNotFlag(final BlockChangeFlag flag) {
        final SpongeBlockChangeFlag o = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors && !o.updateNeighbors ? Constants.BlockChangeFlags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics && !o.performBlockPhysics ? 0 : Constants.BlockChangeFlags.PHYSICS_MASK)
                               | (this.notifyObservers && !o.notifyObservers ? 0 : Constants.BlockChangeFlags.DENY_NEIGHBOR_SHAPE_UPDATE)
                               | (this.notifyClients && !o.notifyClients ? Constants.BlockChangeFlags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender && !o.ignoreRender ? Constants.BlockChangeFlags.IGNORE_RENDER : 0)
                               | (this.forceReRender && !o.forceReRender ? Constants.BlockChangeFlags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagManager.fromNativeInt(maskedFlag);
    }

    public boolean notifyClients() {
        return this.notifyClients;
    }

    public boolean isIgnoreRender() {
        return this.ignoreRender;
    }

    public boolean isForceReRender() {
        return this.forceReRender;
    }

    public boolean isBlockMoving() {
        return this.blockMoving;
    }

    public String getName() {
        return this.name;
    }

    public int getRawFlag() {
        return this.rawFlag;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                   .add("rawFlag", this.rawFlag)
                   .add("notifyNeighbors", this.updateNeighbors)
                   .add("performBlockPhysics", this.performBlockPhysics)
                   .add("notifyObservers", this.notifyObservers)
                   .add("notifyClients", this.notifyClients)
                   .add("ignoreRender", this.ignoreRender)
                   .add("forceReRender", this.forceReRender)
                   .toString();
    }
}
