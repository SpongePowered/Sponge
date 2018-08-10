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
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;

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
    private final int rawFlag;
    private final String name;

    public SpongeBlockChangeFlag(String name, int flag) {
        this.updateNeighbors = (flag & BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK) != 0;
        this.performBlockPhysics = (flag & BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK) == 0;
        this.notifyObservers = (flag & BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK) == 0;
        this.notifyClients = (flag & BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS) != 0;
        this.ignoreRender = (flag & BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER) != 0;
        this.forceReRender = (flag & BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER) != 0 && !this.ignoreRender;
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
    public SpongeBlockChangeFlag withUpdateNeighbors(boolean updateNeighbors) {
        if (this.updateNeighbors == updateNeighbors) {
            return this;
        }
        return updateNeighbors ? andFlag(BlockChangeFlags.NEIGHBOR) : andNotFlag(BlockChangeFlags.NEIGHBOR);
    }

    @Override
    public SpongeBlockChangeFlag withPhysics(boolean performBlockPhysics) {
        if (this.performBlockPhysics == performBlockPhysics) {
            return this;
        }
        return performBlockPhysics ? andFlag(BlockChangeFlags.PHYSICS) : andNotFlag(BlockChangeFlags.PHYSICS);
    }

    @Override
    public SpongeBlockChangeFlag withNotifyObservers(boolean notifyObservers) {
        if (this.notifyObservers == notifyObservers) {
            return this;
        }
        return notifyObservers ? andFlag(BlockChangeFlags.OBSERVER) : andNotFlag(BlockChangeFlags.OBSERVER);
    }

    @Override
    public BlockChangeFlag inverse() {
        final int maskedFlag = (this.updateNeighbors ? 0 : BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK)
                               | (this.performBlockPhysics ? BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK : 0)
                               | (this.notifyObservers ? BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK : 0)
                               | (this.notifyClients ? 0 : BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS)
                               | (this.ignoreRender ? 0 : BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER)
                               | (this.forceReRender ? 0 : BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag andFlag(BlockChangeFlag flag) {
        final SpongeBlockChangeFlag o = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors || o.updateNeighbors ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics || o.performBlockPhysics ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers || o.notifyObservers ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients || o.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender || o.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender || o.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public SpongeBlockChangeFlag andNotFlag(BlockChangeFlag flag) {
        final SpongeBlockChangeFlag o = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors && !o.updateNeighbors ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics && !o.performBlockPhysics ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers && !o.notifyObservers ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients && !o.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender && !o.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender && !o.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    public boolean isNotifyClients() {
        return this.notifyClients;
    }

    public boolean isIgnoreRender() {
        return this.ignoreRender;
    }

    public boolean isForceReRender() {
        return this.forceReRender;
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
            .add("updateNeighbors", this.updateNeighbors)
            .add("performBlockPhysics", this.performBlockPhysics)
            .add("notifyObservers", this.notifyObservers)
            .add("notifyClients", this.notifyClients)
            .add("ignoreRender", this.ignoreRender)
            .add("forceReRender", this.forceReRender)
            .toString();
    }
}
