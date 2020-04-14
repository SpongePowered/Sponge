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


import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.event.ShouldFire;

public enum BlockChange {

    BREAK() {
        @Override
        public ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions) {
            return SpongeEventFactory.createChangeBlockEventBreak(cause, transactions);
        }

        @Override
        public boolean shouldFire() {
            return ShouldFire.CHANGE_BLOCK_EVENT_BREAK;
        }

        @Override
        public EventContextKey<? extends ChangeBlockEvent> getKey() {
            return EventContextKeys.BREAK_EVENT;
        }

        @Override
        public boolean allowsLogging(LoggingCategory category) {
            return category.blockBreakLogging();
        }
    },
    DECAY() {
        @Override
        public ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions) {
            return SpongeEventFactory.createChangeBlockEventDecay(cause, transactions);
        }

        @Override
        public boolean shouldFire() {
            return ShouldFire.CHANGE_BLOCK_EVENT_DECAY;
        }

        @Override
        public EventContextKey<? extends ChangeBlockEvent> getKey() {
            return EventContextKeys.DECAY_EVENT;
        }
    },
    MODIFY() {
        @Override
        public ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions) {
            return SpongeEventFactory.createChangeBlockEventModify(cause, transactions);
        }

        @Override
        public boolean shouldFire() {
            return ShouldFire.CHANGE_BLOCK_EVENT_MODIFY;
        }

        @Override
        public EventContextKey<? extends ChangeBlockEvent> getKey() {
            return EventContextKeys.MODIFY_EVENT;
        }

        @Override
        public boolean allowsLogging(LoggingCategory category) {
            return category.blockModifyLogging();
        }
    },
    PLACE() {
        @Override
        public ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions) {
            return SpongeEventFactory.createChangeBlockEventPlace(cause, transactions);
        }

        @Override
        public boolean shouldFire() {
            return ShouldFire.CHANGE_BLOCK_EVENT_PLACE;
        }

        @Override
        public EventContextKey<? extends ChangeBlockEvent> getKey() {
            return EventContextKeys.PLACE_EVENT;
        }

        @Override
        public boolean allowsLogging(LoggingCategory category) {
            return category.blockPlaceLogging();
        }
    },
    GROW() {
        @Override
        public ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions) {
            return SpongeEventFactory.createChangeBlockEventGrow(cause, transactions);
        }

        @Override
        public boolean shouldFire() {
            return ShouldFire.CHANGE_BLOCK_EVENT_GROW;
        }

        @Override
        public EventContextKey<? extends ChangeBlockEvent> getKey() {
            return EventContextKeys.GROW_EVENT;
        }
    };

    BlockChange() { }


    public boolean allowsLogging(LoggingCategory category) {
        return false;
    }

    public abstract ChangeBlockEvent createEvent(Cause cause, ImmutableList<Transaction<BlockSnapshot>> transactions);

    public abstract boolean shouldFire();

    public abstract EventContextKey<? extends ChangeBlockEvent> getKey();
}
