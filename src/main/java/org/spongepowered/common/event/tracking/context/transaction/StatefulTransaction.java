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
package org.spongepowered.common.event.tracking.context.transaction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.Optional;

interface StatefulTransaction {

    /**
     * Gets whether this transaction has been marked as recorded. This is
     * considered a sanity check for transactions in performing any behaviors
     * such as throwing events etc. that otherwise would normally be consdiered
     * as "absorbed".
     *
     * @return Whether this transaction was recorded
     */
    boolean recorded();

    /**
     * Toggles the recorded state of this transaction and should perform any
     * potentially deferred initialization here. Deferred initialization can
     * mean things such as performing {@link Entity#save(CompoundTag)} or
     * {@link BlockSnapshot} creation involving {@link BlockEntity BlockEntities}.
     * Generally consider this to be the final step where a transaction is
     * being "persisted" to a {@link TransactionSink} and has not been absorbed
     * by virtue of a {@link #parentAbsorber()}.
     *
     * @return The now-capture-completed transaction ready for recording
     */
    GameTransaction<@NonNull ?> recordState();

    /**
     * Simple function used to verify whether this particular transaction has a
     * behavior of being "merged" or "absorbed" by an already-recorded
     * transaction. This has uses for cases where "fire-and-forget" tend to have
     * unnatural recording behaviors that are non-deterministic based on their
     * root alone, and often times need to be gathered by some parent target.
     *
     * @return The absorbing function, if meant to be absorbed
     */
    default Optional<TransactionFlow.AbsorbingFlowStep> parentAbsorber() {
        return Optional.empty();
    }

    default boolean shouldHaveBeenAbsorbed() {
        return false;
    }

}
