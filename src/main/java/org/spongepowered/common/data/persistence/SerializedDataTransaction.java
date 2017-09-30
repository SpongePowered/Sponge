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
package org.spongepowered.common.data.persistence;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;

public class SerializedDataTransaction {

    public static Builder builder() {
        return new Builder();
    }

    public final ImmutableList<DataView> failedData;
    public final ImmutableList<DataManipulator<?, ?>> deserializedManipulators;

    private SerializedDataTransaction(Builder builder) {
        this.failedData = builder.serializedData.build();
        this.deserializedManipulators = builder.deserializedManipulators.build();
    }


    public static final class Builder {

        private final ImmutableList.Builder<DataView> serializedData = ImmutableList.builder();
        private final ImmutableList.Builder<DataManipulator<?, ?>> deserializedManipulators = ImmutableList.builder();

        Builder() {
        }

        public void failedData(DataView serializedData) {
            this.serializedData.add(serializedData);
        }

        public void successfulData(DataManipulator<?, ?> success) {
            this.deserializedManipulators.add(success);
        }

        public SerializedDataTransaction build() {
            return new SerializedDataTransaction(this);
        }
    }
}
