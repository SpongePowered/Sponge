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
package org.spongepowered.common.data.component;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.SpongeManipulatorRegistry;

public abstract class SpongeAbstractComponent<T extends Component<T>> implements Component<T> {

    private final Class<T> manipulatorClass;

    protected SpongeAbstractComponent(Class<T> manipulatorClass) {
        this.manipulatorClass = checkNotNull(manipulatorClass);
    }

    @Override
    public Optional<T> fill(DataHolder dataHolder) {
        return fill(dataHolder, DataPriority.DATA_HOLDER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> fill(DataHolder dataHolder, DataPriority overlap) {
        SpongeDataProcessor<T> registry = SpongeManipulatorRegistry.getInstance().getUtil(this.manipulatorClass).get();
        return registry.fillData(dataHolder, (T) (Object) this, overlap);
    }

    @Override
    public Optional<T> from(DataContainer container) {
        SpongeDataProcessor<T> builder = SpongeManipulatorRegistry.getInstance().getUtil(this.manipulatorClass).get();
        return builder.build(container);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.manipulatorClass);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SpongeAbstractComponent other = (SpongeAbstractComponent) obj;
        return Objects.equal(this.manipulatorClass, other.manipulatorClass);
    }
}
