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

import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataSerializable;

import java.util.Arrays;
import java.util.Objects;

public record SimpleData(int testInt, double testDouble, String testString, String[] testList) implements DataSerializable {

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew();
        container.set(DataQuery.of("myInt"), this.testInt);
        container.set(DataQuery.of("myDouble"), this.testDouble);
        container.set(DataQuery.of("myString"), this.testString);
        container.set(DataQuery.of("myStringList"), Arrays.asList(this.testList));
        return container;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.testInt, this.testDouble, this.testString, Arrays.hashCode(this.testList));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final SimpleData that = (SimpleData) other;
        return Objects.equals(this.testInt, that.testInt)
                && Objects.equals(this.testDouble, that.testDouble)
                && Objects.equals(this.testString, that.testString)
                && Arrays.equals(this.testList, that.testList);
    }
}
