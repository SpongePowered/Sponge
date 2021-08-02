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
package org.spongepowered.common.bridge.block;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.world.level.block.Block;

/**
 * A quasi interface to mix into every possible {@link Block} such that their
 * acceptable {@link BlockState}s can be created, manipulated, and applied
 * with the safety of using these instance checks of the {@link BlockBridge}.
 * The advantage of this is that a simple cast from {@link Block} to a
 * particular {@link BlockBridge} to take advantage of particular {@link Value}
 * types, are really simple to perform.
 *
 * <p>It is important to note that when using this level of implementation,
 * it is already guaranteed that a particular {@link BlockBridge} is capable
 * of a particular type thanks to {@link Mixin}s. All that is needed to handle
 * a particular type of {@link Value} or {@link Immutable} is a
 * simple cast. This is particularly useful for {@link BlockState}s as
 * they already know the type they need to focus on.</p>
 */
public interface BlockBridge {

    /**
     * Gets all the {@link Immutable}s for the provided
     * {@link BlockState}.
     *
     * @param blockState The block state being passed in
     * @return The list of immutable manipulators
     */
    List<Immutable> bridge$getManipulators(BlockState blockState);

    /**
     * A simple check whether the class is supported by the block or not.
     *
     * @param immutable The immutable class
     * @return True if the data possibly represented by an instance of the class is supported
     */
    boolean bridge$supports(Class<? extends Immutable> immutable);

    /**
     * Instead of delegating to a block processor, we can delegate to the block
     * to retrieve the correct {@link BlockState} if supported. Considering
     * block processors would require to know of the blocks themselves, it is
     * easier to use the block to understand what data is being offered,
     * and the current block state being used. Since all of the data is already
     * relatively kept in the block state instance, it is therefor very well
     * possible to cycle according to the block instance.
     *
     * @param blockState The block state to use as a base
     * @param key The key to the data
     * @param value The value
     * @param <E> The type of value, for type checking
     * @return The blockstate with the new value, if available and compatible
     */
    <E> Optional<BlockState> bridge$getStateWithValue(BlockState blockState, Key<? extends Value<E>> key, E value);

    /**
     * Again, another delegate method directly to the block, usually not all
     * required, but it does help if the block does support the manipulator
     * in the first place. Considering that most manipulators are single
     * data typed, it is discernible for the block to easily check what
     * data is being offered and therefor validate whether the data is
     * allowed for the specific block state. The block state passed in
     * is not changed, but rather used as a blueprint so to speak due to
     * the various pre-set data that the block state may contain, such as
     * red stone power levels, plant types, etc. etc. etc.
     *
     * @param blockState The block state to base off of
     * @param manipulator The manipulator being offered
     * @return The block state with the requested data, if available
     */
    Optional<org.spongepowered.api.block.BlockState> bridge$getStateWithData(BlockState blockState, Immutable manipulator);

    // Normal API methods

    boolean bridge$isVanilla();

    boolean bridge$hasCollideLogic();

    boolean bridge$hasCollideWithStateLogic();

    /**
     * Used only for Forge's dummy air block that is acting as a surrogate block for missing
     * mod blocks. Usually when a block is simply marked for replacement when a mod is re-introduced.
     *
     * @return True if this block is a surrogate dummy block. Should only be used for forge blocks.
     */
    default boolean bridge$isDummy() {
        return false;
    }

    default BiConsumer<CauseStackManager.StackFrame, ServerLevelBridge> bridge$getTickFrameModifier() {
        return (frame, world) -> {
        };
    }
}
