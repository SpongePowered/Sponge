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
package org.spongepowered.common.data.type;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.spongepowered.api.attribute.Operation;

public class SpongeOperation implements Operation {
    private final String id;
    private final boolean immediately;

    public SpongeOperation(String id, boolean immediately) {
        this.id = id;
        this.immediately = immediately;
    }

    public SpongeOperation() {
        this("ADD_AMOUNT", false);
    }

    @Override
    public double getIncrementation(double base, double modifier, double currentValue) {
        switch (this.id) {
            case "MULTIPLY_BASE":
                return base * modifier;
            case "MULTIPLY":
                return currentValue * modifier;
            case "ADD_AMOUNT":
            default:
                return currentValue + modifier;
        }
    }

    @Override
    public boolean changeValueImmediately() {
        return this.immediately;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public int compareTo(Operation o) {
        return new CompareToBuilder()
                .append(this.id, o.getId())
                .build();
    }
}
