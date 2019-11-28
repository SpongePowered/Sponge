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
package org.spongepowered.common.bridge.entity.player;

import net.minecraft.util.math.BlockPos;
import org.spongepowered.common.bridge.entity.EntityBridge;

import java.util.UUID;

import javax.annotation.Nullable;

public interface EntityPlayerBridge {

    @Nullable BlockPos bridge$getBedLocation(int dim);

    boolean bridge$isSpawnForced(int dim);

    /**
     * {@link EntityPlayer#addExperienceLevel(int)} doesn't update the total
     * experience. This recalculates it for plugins to properly make use of it.
     */
    void bridge$recalculateTotalExperience();

    boolean bridge$affectsSpawning();

    void bridge$setAffectsSpawning(boolean affectsSpawning);

    boolean bridge$keepInventory();

    void bridge$shouldRestoreInventory(boolean flag);

    boolean bridge$shouldRestoreInventory();

    int bridge$getExperienceSinceLevel();

    void bridge$setExperienceSinceLevel(int experience);
}
