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

import com.google.common.base.Optional;
import com.google.common.collect.MapMaker;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataManipulatorBuilder;
import org.spongepowered.api.data.DataManipulatorRegistry;

import java.util.Map;

public class SpongeManipulatorRegistry implements DataManipulatorRegistry {

    private static final SpongeManipulatorRegistry instance = new SpongeManipulatorRegistry();

    private final Map<Class<? extends DataManipulator<?>>, DataManipulatorBuilder<?>> builderMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?>>, SpongeDataUtil<?>> setterMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends DataManipulator<?>>, SpongeBlockUtil<?>> blockMap = new MapMaker().concurrencyLevel(4).makeMap();

    private SpongeManipulatorRegistry() {
    }

    public static SpongeManipulatorRegistry getInstance() {
        return instance;
    }

    @Override
    public <T extends DataManipulator<T>> void register(Class<T> manipulatorClass, DataManipulatorBuilder<T> builder) {
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
        } else {
            throw new IllegalStateException("Already registered the DataManipulatorBuilder for " + manipulatorClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<T>> Optional<DataManipulatorBuilder<T>> getBuilder(Class<T> manipulatorClass) {
        return Optional.fromNullable((DataManipulatorBuilder<T>) (Object) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends DataManipulator<T>> void registerDataUtil(Class<T> manipulatorclass, SpongeDataUtil<T> setter) {
        if (!this.setterMap.containsKey(checkNotNull(manipulatorclass))) {
            this.setterMap.put(manipulatorclass, checkNotNull(setter));
        } else {
            throw new IllegalStateException("Already registered a DataSetter for the given DataManipulator: " + manipulatorclass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends DataManipulator<T>> Optional<SpongeDataUtil<T>> getUtil(Class<T> manipulatorClass) {
        return Optional.fromNullable((SpongeDataUtil<T>) (Object) this.setterMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends DataManipulator<T>> void registerBlockUtil(Class<T> manipulatorclass, SpongeBlockUtil<T> util) {
        if (!this.blockMap.containsKey(checkNotNull(manipulatorclass))) {
            this.blockMap.put(manipulatorclass, checkNotNull(util));
        } else {
            throw new IllegalStateException("Already registered a SpongeBlockUtil for the given DataManipulator: " + manipulatorclass
                    .getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends DataManipulator<T>> Optional<SpongeBlockUtil<T>> getBlockUtil(Class<T> manipulatorClass) {
        return Optional.fromNullable((SpongeBlockUtil<T>) (Object) this.blockMap.get(checkNotNull(manipulatorClass)));
    }
}
