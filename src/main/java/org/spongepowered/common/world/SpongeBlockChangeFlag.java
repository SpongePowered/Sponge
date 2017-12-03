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
import com.google.common.base.Objects;
import org.spongepowered.api.world.BlockChangeFlag;
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

    public SpongeBlockChangeFlag(String name) {
        this.updateNeighbors = false;
        this.performBlockPhysics = false;
        this.notifyObservers = false;
        this.notifyClients = false;
        this.ignoreRender = false;
        this.forceReRender = false;
        this.rawFlag = 0;
        this.name = name;
    }

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

    /**
     * Gets whether this flag defines that a block change should
     * notify neighboring blocks.
     *
     * @return True if this is set to notify neighboring blocks
     */
    public boolean updateNeighbors() {
        return this.updateNeighbors;
    }

    /**
     * Gets whether this flag defines that a block change should
     * perform block physics checks or not. If not, no checks
     * are performed.
     *
     * @return True if this is set to perform block physics on placement
     */
    public boolean performBlockPhysics() {
        return this.performBlockPhysics;
    }

    @Override
    public boolean notifyObservers() {
        return false;
    }


    /**
     * Gets the equivalent {@link SpongeBlockChangeFlag} of this flag with all
     * other flags while having the desired {@code updateNeighbors}
     * as defined by the parameter.
     *
     * @param updateNeighbors Whether to update neighboring blocks
     * @return The relative flag with the desired update neighbors
     */
    public SpongeBlockChangeFlag withUpdateNeighbors(boolean updateNeighbors) {
        if (this.updateNeighbors == updateNeighbors) {
            return this;
        }
        final int maskedFlag = (updateNeighbors ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    /**
     * Gets the equivalent {@link SpongeBlockChangeFlag} of this flag
     * with all other flags while having the desired {@code performBlockPhysics}
     * as defined by the parameter.
     *
     * @param performBlockPhysics Whether to perform block physics
     * @return The relative flag with the desired block physics
     */
    public SpongeBlockChangeFlag withPhysics(boolean performBlockPhysics) {
        if (this.performBlockPhysics == performBlockPhysics) {
            return this;
        }
        final int maskedFlag = (this.updateNeighbors ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (performBlockPhysics ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);

        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);

    }

    @Override
    public SpongeBlockChangeFlag withNotifyObservers(boolean notifyObservers) {
        if (this.notifyObservers == notifyObservers) {
            return this;
        }
        final int maskedFlag = (this.updateNeighbors ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (notifyObservers ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                                | (this.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                                | (this.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                                | (this.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);

        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);

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
    public BlockChangeFlag orFlag(BlockChangeFlag flag) {
        final SpongeBlockChangeFlag spongeFlag = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors | flag.updateNeighbors() ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics | flag.performBlockPhysics() ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers | flag.notifyObservers() ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients | spongeFlag.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender | spongeFlag.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender | spongeFlag.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public BlockChangeFlag exclusiveOrFlag(BlockChangeFlag flag) {
        final SpongeBlockChangeFlag spongeFlag = (SpongeBlockChangeFlag) flag;
        final int maskedFlag = (this.updateNeighbors ^ flag.updateNeighbors() ? BlockChangeFlagRegistryModule.Flags.NEIGHBOR_MASK : 0)
                               | (this.performBlockPhysics ^ flag.performBlockPhysics() ? 0 : BlockChangeFlagRegistryModule.Flags.PHYSICS_MASK)
                               | (this.notifyObservers ^ flag.notifyObservers() ? 0 : BlockChangeFlagRegistryModule.Flags.OBSERVER_MASK)
                               | (this.notifyClients ^ spongeFlag.notifyClients ? BlockChangeFlagRegistryModule.Flags.NOTIFY_CLIENTS : 0)
                               | (this.ignoreRender ^ spongeFlag.ignoreRender ? BlockChangeFlagRegistryModule.Flags.IGNORE_RENDER : 0)
                               | (this.forceReRender ^ spongeFlag.forceReRender ? BlockChangeFlagRegistryModule.Flags.FORCE_RE_RENDER : 0);
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public BlockChangeFlag andFlag(BlockChangeFlag flag) {
        final int maskedFlag = ((SpongeBlockChangeFlag) flag).rawFlag & this.rawFlag;
        return BlockChangeFlagRegistryModule.fromNativeInt(maskedFlag);
    }

    @Override
    public BlockChangeFlag andNotFlag(BlockChangeFlag flag) {
        final int maskedFlag = ((SpongeBlockChangeFlag) flag).rawFlag & ~this.rawFlag;
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
