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
package org.spongepowered.common.bridge.world;

import net.minecraft.world.World;
import org.spongepowered.common.world.SpongeEmptyChunk;

public interface WorldBridge {

    /**
     * Gets whether this world is a usable world in the context of using
     * as {@link WorldServerBridge} and an active server world. This
     * lazy loads the flag if {@link World#isRemote} is {@code true},
     * {@link World#getWorldInfo()} returns {@code null},
     * {@link World#getWorldInfo()} has a null name, or
     * if this world is not an instance of {@link WorldServerBridge}.
     * By that point, if all those checks succeed, this world is usable,
     * and can be passed through to create snapshots and perform other
     * internal tasks that Sponge needs to operate on said world.
     *
     * @return If this world is fake or not
     */
    boolean bridge$isFake();

    void bridge$clearFakeCheck();

    boolean bridge$isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);

    SpongeEmptyChunk bridge$getEmptyChunk();
}
