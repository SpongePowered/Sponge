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
package org.spongepowered.common.bridge.world.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collection;
import java.util.function.Predicate;

public interface LevelBridge {

    /**
     * Gets whether this world is a usable world in the context of using
     * as {@link ServerLevelBridge} and an active server world. This
     * lazy loads the flag if {@link Level#isClientSide} is {@code true},
     * {@link Level#getWorldInfo()} returns {@code null},
     * {@link Level#getWorldInfo()} has a null name, or
     * if this world is not an instance of {@link ServerLevelBridge}.
     * By that point, if all those checks succeed, this world is usable,
     * and can be passed through to create snapshots and perform other
     * internal tasks that Sponge needs to operate on said world.
     *
     * @return If this world is fake or not
     */
    boolean bridge$isFake();

    void bridge$adjustDimensionLogic(DimensionType dimensionType);

    <E extends org.spongepowered.api.entity.Entity> E bridge$createEntity(
            final DataContainer dataContainer,
            final @Nullable Vector3d position,
            final @Nullable Predicate<Vector3d> positionCheck);

    <E extends Entity> E bridge$createEntity(EntityType<E> type, Vector3d position, boolean naturally);

    /**
     * Returns the {@link BlockEntity} set to unload in {@link Level}.
     *
     * <p>In Vanilla, this is actually a {@link java.util.List}. In Forge, this is a {@link java.util.Set}</p>
     * @return the entities
     */
    Collection<BlockEntity> bridge$blockEntitiesToUnload();
}
