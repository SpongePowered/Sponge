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
package org.spongepowered.common.data.generator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.generator.DataGenerator;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

@SuppressWarnings({"unchecked", "NullableProblems", "ConstantConditions"})
public class AbstractDataGenerator<M extends DataManipulator<M, I>,
        I extends ImmutableDataManipulator<I, M>,
        G extends DataGenerator<M, I, G, R>,
        R extends DataGenerator<?, ?, ?, R>>
        implements DataGenerator<M, I, G, R> {

    protected String id;
    @Nullable protected String name;
    @Nullable protected Predicate<? extends DataHolder> dataHolderPredicate;
    protected int contentVersion = 1;
    protected final List<KeyEntry> keyEntries = new ArrayList<>();
    @Nullable protected Class<M> mutableInterface;
    @Nullable protected Class<I> immutableInterface;

    @Override
    public R from(DataRegistration<?, ?> value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public R reset() {
        this.id = null;
        this.name = null;
        this.dataHolderPredicate = null;
        this.contentVersion = 1;
        this.keyEntries.clear();
        return (R) this;
    }

    @Override
    public G name(String name) {
        checkArgument(StringUtils.isNotEmpty(name), "name cannot be null or empty");
        this.name = name;
        return (G) this;
    }

    @Override
    public G version(int contentVersion) {
        checkArgument(contentVersion > 0, "content version must be greater then zero");
        this.contentVersion = contentVersion;
        return (G) this;
    }

    @Override
    public G predicate(Predicate<? extends DataHolder> predicate) {
        checkNotNull(predicate, "predicate");
        this.dataHolderPredicate = predicate;
        return (G) this;
    }

    @Override
    public G id(String id) {
        checkArgument(StringUtils.isNotEmpty(id), "id cannot be null or empty");
        this.id = id;
        return (G) this;
    }

    @Override
    public DataRegistration<M, I> build() {
        checkState(this.id != null, "The id must be set");
        preBuild();
        return null;
    }

    void preBuild() {
    }
}
