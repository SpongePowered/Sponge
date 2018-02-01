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
package org.spongepowered.common.data.generator.test;

import org.spongepowered.api.data.generator.KeyValue;
import org.spongepowered.api.data.manipulator.DataManipulator;

import java.util.Optional;

import javax.annotation.Nullable;

public interface TestData extends DataManipulator<TestData, ImmutableTestData> {

    @KeyValue("test_string")
    String getString();

    @KeyValue("test_int")
    Integer getInteger();

    @KeyValue("test_int")
    int getInt();

    @KeyValue("test_int")
    void setInt(int value);

    @KeyValue("test_int")
    void setInt(Integer value);

    @KeyValue("test_opt_double")
    Optional<Double> getDouble();

    @KeyValue("test_opt_double")
    @Nullable
    Double getNullableDouble();

    @KeyValue("test_opt_double")
    void setDouble(@Nullable Double value);

    @KeyValue("test_opt_double")
    void setDouble(Optional<Double> value);

    @KeyValue("test_opt_double")
    void setDouble(double value);
}
