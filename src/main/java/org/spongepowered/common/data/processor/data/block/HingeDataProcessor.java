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
package org.spongepowered.common.data.processor.data.block;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableHingeData;
import org.spongepowered.api.data.manipulator.mutable.block.HingeData;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeHingeData;
import org.spongepowered.common.data.processor.common.AbstractBlockOnlyDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class HingeDataProcessor extends AbstractBlockOnlyDataProcessor<Hinge, Value<Hinge>, HingeData, ImmutableHingeData> {

    public HingeDataProcessor() {
        super(Keys.HINGE_POSITION);
    }

    @Override
    public HingeData createManipulator() {
        return new SpongeHingeData();
    }

    @Override
    protected Hinge getDefaultValue() {
        return Hinges.LEFT;
    }

    @Override
    protected Value<Hinge> constructValue(Hinge actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
