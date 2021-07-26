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
package org.spongepowered.fabric.util;

public final class Bounds {

    public final int x0, x1, y0, y1;

    public Bounds(final int x0, final int y0, final int x1, final int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    private boolean isInBounds(final int x, final int y, final int x0, final int y0, final int x1, final int y1) {
        return x >= x0 && x <= x1 && y >= y0 && y <= y1;
    }

    public boolean isInBounds(final int x, final int y) {
        return this.isInBounds(x, y, this.x0, this.y0, this.x1, this.y1);
    }

    public boolean isInBounds(final int x, final int y, final int offsetX, final int offsetY) {
        return this.isInBounds(x, y, this.x0 + offsetX, this.y0 + offsetY, this.x1 + offsetX, this.y1 + offsetY);
    }
}
