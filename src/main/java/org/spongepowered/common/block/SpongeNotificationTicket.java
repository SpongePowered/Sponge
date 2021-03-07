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
package org.spongepowered.common.block;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.NotificationTicket;
import org.spongepowered.api.world.LocatableBlock;

import java.util.Objects;
import java.util.StringJoiner;

public final class SpongeNotificationTicket implements NotificationTicket {

    private final LocatableBlock notifier;
    private final BlockSnapshot target;
    private boolean valid = true;

    public SpongeNotificationTicket(final LocatableBlock notifier, final BlockSnapshot target) {
        this.notifier = notifier;
        this.target = target;
    }

    @Override
    public LocatableBlock notifier() {
        return this.notifier;
    }

    @Override
    public BlockSnapshot target() {
        return this.target;
    }

    @Override
    public boolean valid() {
        return this.valid;
    }

    @Override
    public void valid(final boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeNotificationTicket that = (SpongeNotificationTicket) o;
        return this.valid == that.valid && this.notifier.equals(that.notifier) && this.target.equals(that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.notifier, this.target, this.valid);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeNotificationTicket.class.getSimpleName() + "[", "]")
            .add("notifier=" + this.notifier)
            .add("target=" + this.target)
            .add("valid=" + this.valid)
            .toString();
    }
}
