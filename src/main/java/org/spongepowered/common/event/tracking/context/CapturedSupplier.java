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
package org.spongepowered.common.event.tracking.context;

import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public abstract class CapturedSupplier<T> implements Supplier<List<T>>, ICaptureSupplier {
    @Nullable private List<T> captured;

    CapturedSupplier() {
    }

    @Override
    public final List<T> get() {
        if (this.captured == null) {
            this.captured = NonNullList.func_191196_a();
        }
        return this.captured;
    }

    /**
     * Returns {@code true} if there are no captured objects.
     *
     * @return {@code true} if empty
     */
    @Override
    public final boolean isEmpty() {
        return this.captured == null || this.captured.isEmpty();
    }

    /**
     * If not empty, activates the consumer then clears all captures.
     *
     * @param consumer The consumer to activate
     */
    public final void acceptAndClearIfNotEmpty(Consumer<List<T>> consumer) {
        if (!this.isEmpty()) {
            final List<T> consumed = new ArrayList<>(this.captured);
            this.captured.clear(); // We should be clearing after it is processed. Avoids extraneous issues
            consumer.accept(consumed);
            // with recycling the captured object.
        }
    }

    /**
     * If not empty, returns the captured {@link List}.
     * Otherwise, this will return the passed list.
     *
     * @param list The fallback list
     * @return If not empty, the captured list otherwise the fallback list
     */
    public final List<T> orElse(List<T> list) {
        return this.isEmpty() ? list : this.captured;
    }

    public final List<T> orEmptyList() {
        return this.captured == null ? Collections.emptyList() : this.captured;
    }

    /**
     * If not empty, returns a sequential stream of values associated with key.
     *
     * @return A sequential stream of values
     */
    public final Stream<T> stream() {
        return this.captured == null ? Stream.empty() : this.captured.stream();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.captured);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CapturedSupplier<?> other = (CapturedSupplier<?>) obj;
        return Objects.equals(this.captured, other.captured);
    }


    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("Captured", this.captured == null ? 0 : this.captured.size())
                .toString();
    }

    public final void reset() {
        if (this.captured == null) {
            return;
        }
        this.captured.clear();
    }
}
