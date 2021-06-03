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
package org.spongepowered.common.map.color;

import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.common.util.Constants;

public final class SpongeMapShade implements MapShade {

    private final int shadeNum;
    private final int multiplier;

    /**
     * Create a SpongeMapShade with given parameters
     *
     * @param shadeNum   Number to add to color byte after multiplying by 4
     * @param multiplier Number to multiply R,G and B before dividing by {@value Constants.Map#SHADE_DIVIDER}
     */
    public SpongeMapShade(final int shadeNum, final int multiplier) {
        this.shadeNum = shadeNum;
        this.multiplier = multiplier;
    }

    public int getShadeNum() {
        return this.shadeNum;
    }

    public int getMultiplier() {
        return this.multiplier;
    }
}
