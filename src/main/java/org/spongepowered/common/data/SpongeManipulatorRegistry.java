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
import com.google.common.collect.MapMaker;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.ComponentBuilder;
import org.spongepowered.api.data.ComponentRegistry;

import java.util.Map;

public class SpongeManipulatorRegistry implements ComponentRegistry {

    private static final SpongeManipulatorRegistry instance = new SpongeManipulatorRegistry();

    private final Map<Class<? extends Component<?>>, ComponentBuilder<?>> builderMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends Component<?>>, SpongeDataProcessor<?>> dataProcessorMap = new MapMaker().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends Component<?>>, SpongeBlockProcessor<?>> blockProcessorMap = new MapMaker().concurrencyLevel(4).makeMap();

    private SpongeManipulatorRegistry() {
    }

    public static SpongeManipulatorRegistry getInstance() {
        return instance;
    }

    @Override
    public <T extends Component<T>> void register(Class<T> manipulatorClass, ComponentBuilder<T> builder) {
        if (!this.builderMap.containsKey(checkNotNull(manipulatorClass))) {
            this.builderMap.put(manipulatorClass, checkNotNull(builder));
        } else {
            throw new IllegalStateException("Already registered the DataUtil for " + manipulatorClass.getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component<T>> Optional<ComponentBuilder<T>> getBuilder(Class<T> manipulatorClass) {
        return Optional.fromNullable((ComponentBuilder<T>) (Object) this.builderMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends Component<T>> void registerDataProcessor(Class<T> manipulatorClass, SpongeDataProcessor<T> processor) {
        checkState(!this.dataProcessorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "Component: " + manipulatorClass.getCanonicalName());
        this.dataProcessorMap.put(manipulatorClass, checkNotNull(processor));
    }

    public <T extends Component<T>> void registerDataProcessorAndImpl(Class<T> manipulatorClass, Class<? extends T> implClass,
            SpongeDataProcessor<T> processor) {
        checkState(!this.dataProcessorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "Component: " + manipulatorClass.getCanonicalName());
        checkState(!this.dataProcessorMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "Component: " + implClass.getCanonicalName());
        this.dataProcessorMap.put(manipulatorClass, checkNotNull(processor));
        this.dataProcessorMap.put(implClass, processor);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component<T>> Optional<SpongeDataProcessor<T>> getUtil(Class<T> manipulatorClass) {
        return Optional.fromNullable((SpongeDataProcessor<T>) (Object) this.dataProcessorMap.get(checkNotNull(manipulatorClass)));
    }

    public <T extends Component<T>> void registerBlockProcessor(Class<T> manipulatorclass, SpongeBlockProcessor<T> util) {
        if (!this.blockProcessorMap.containsKey(checkNotNull(manipulatorclass))) {
            this.blockProcessorMap.put(manipulatorclass, checkNotNull(util));
        } else {
            throw new IllegalStateException("Already registered a SpongeBlockProcessor for the given Component: " + manipulatorclass
                    .getCanonicalName());
        }
    }

    public <T extends Component<T>> void registerBlockProcessorAndImpl(Class<T> manipulatorClass, Class<? extends T> implClass,
            SpongeBlockProcessor<T> processor) {
        checkState(!this.blockProcessorMap.containsKey(checkNotNull(manipulatorClass)), "Already registered a DataProcessor for the given "
                + "Component: " + manipulatorClass.getCanonicalName());
        checkState(!this.blockProcessorMap.containsKey(checkNotNull(implClass)), "Already registered a DataProcessor for the given "
                + "Component: " + implClass.getCanonicalName());
        this.blockProcessorMap.put(manipulatorClass, checkNotNull(processor));
        this.blockProcessorMap.put(implClass, processor);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component<T>> Optional<SpongeBlockProcessor<T>> getBlockUtil(Class<T> manipulatorClass) {
        return Optional.fromNullable((SpongeBlockProcessor<T>) (Object) this.blockProcessorMap.get(checkNotNull(manipulatorClass)));
    }
}
