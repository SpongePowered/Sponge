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
package org.spongepowered.common.util;

import org.spongepowered.api.util.orientation.Orientation;

import java.util.Optional;

public final class SpongeOrientation implements Orientation {

    public static final class Factory implements Orientation.Factory {

        @Override
        public Optional<Orientation> fromDegrees(final int degrees) {
            if (degrees % 45 == 0) {
                return Optional.of(new SpongeOrientation(degrees % 360));
            }

            return Optional.empty();
        }
    }

    private final int angle;

    public SpongeOrientation(final int angle) {
        if (angle % 45 != 0) {
            throw new IllegalArgumentException("The angle should be a multiple of 45 degrees!");
        }
        this.angle = angle % 360;
    }

    @Override
    public int angle() {
        return this.angle;
    }

    @Override
    public int hashCode() {
        return this.angle;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SpongeOrientation)) {
            return false;
        }

        return ((SpongeOrientation) obj).angle == this.angle;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{angle=" + this.angle + "}";
    }
}
