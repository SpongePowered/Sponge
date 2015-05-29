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
package org.spongepowered.common.data;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.DataTransactionResult;

import java.util.Collection;
import java.util.List;

public final class DataTransactionBuilder {

    private List<Component<?>> rejected;
    private List<Component<?>> replaced;
    private DataTransactionResult.Type resultType;

    private DataTransactionBuilder() {
    }

    public static DataTransactionResult fail(final Component<?> manipulator) {
        return builder().reject(manipulator).result(DataTransactionResult.Type.FAILURE).build();
    }

    public static DataTransactionResult successNoData() {
        return builder().result(DataTransactionResult.Type.SUCCESS).build();
    }

    public static DataTransactionResult successReplaceData(final Component<?> manipulator) {
        return builder().result(DataTransactionResult.Type.SUCCESS).replace(manipulator).build();
    }

    public static DataTransactionBuilder builder() {
        return new DataTransactionBuilder();
    }

    public DataTransactionBuilder result(final DataTransactionResult.Type type) {
        this.resultType = checkNotNull(type);
        return this;
    }


    public DataTransactionBuilder replace(final Component<?> manipulator) {
        if (this.replaced == null) {
            this.replaced = Lists.newArrayList();
        }
        this.replaced.add(checkNotNull(manipulator));
        return this;
    }

    public DataTransactionBuilder reject(final Component<?> manipulator) {
        if (this.rejected == null) {
            this.rejected = Lists.newArrayList();
        }
        this.rejected.add(checkNotNull(manipulator));
        return this;
    }

    public DataTransactionResult build() {
        checkState(this.resultType != null);
        return new BuilderResult(this);
    }

    private class BuilderResult implements DataTransactionResult {

        private final Type type;
        private final List<Component<?>> rejected;
        private final List<Component<?>> replaced;

        BuilderResult(final DataTransactionBuilder builder) {
            this.type = builder.resultType;
            if (builder.rejected != null) {
                this.rejected = ImmutableList.copyOf(builder.rejected);
            } else {
                this.rejected = null;
            }
            if (builder.replaced != null) {
                this.replaced = ImmutableList.copyOf(builder.replaced);
            } else {
                this.replaced = null;
            }
        }

        @Override
        public Type getType() {
            return this.type;
        }

        @Override
        public Optional<? extends Collection<? extends Component<?>>> getRejectedData() {
            return Optional.fromNullable(this.rejected);
        }

        @Override
        public Optional<? extends Collection<? extends Component<?>>> getReplacedData() {
            return Optional.fromNullable(this.replaced);
        }
    }

}
