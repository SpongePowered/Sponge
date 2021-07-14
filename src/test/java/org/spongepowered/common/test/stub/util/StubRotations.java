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
package org.spongepowered.common.test.stub.util;

import org.spongepowered.api.util.Angle;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.common.test.stub.StubKey;
import org.spongepowered.common.test.stub.registry.StubbedRegistry;

public enum StubRotations implements Rotation {
    NONE(0),
    CLOCKWISE_90(90),
    CLOCKWISE_180(180),
    COUNTERCLOCKWISE_90(-90);

    public static void registerDefaults(final StubbedRegistry<Rotation> registry) {
        registry.register(new StubKey("sponge", "none"), StubRotations.NONE);
        registry.register(new StubKey("sponge", "clockwise_90"), StubRotations.CLOCKWISE_90);
        registry.register(new StubKey("sponge", "clockwise_180"), StubRotations.CLOCKWISE_180);
        registry.register(new StubKey("sponge", "counterclockwise_90"), StubRotations.COUNTERCLOCKWISE_90);
    }

    private final int angle;

    StubRotations(final int angle) {
        this.angle = angle;
    }

    @Override
    public Rotation and(final Rotation rotation) {
        if (!(rotation instanceof StubRotations)) {
            throw new IllegalStateException("Shouldn't be operating on anything but stubrotations");
        }
        if (((StubRotations) rotation).angle == 0) {
            return this;
        }
        final StubRotations stub = (StubRotations) rotation;
        switch (this) {
            case NONE: return rotation;
            case CLOCKWISE_90: {
                switch (stub) {
                    case CLOCKWISE_90: return StubRotations.CLOCKWISE_180;
                    case CLOCKWISE_180: return StubRotations.COUNTERCLOCKWISE_90;
                    case COUNTERCLOCKWISE_90: return StubRotations.NONE;
                    default: return this;
                }
            }
            case CLOCKWISE_180: {
                switch (stub) {
                    case CLOCKWISE_90: return StubRotations.COUNTERCLOCKWISE_90;
                    case CLOCKWISE_180: return StubRotations.NONE;
                    case COUNTERCLOCKWISE_90: return StubRotations.CLOCKWISE_90;
                }
            }
            case COUNTERCLOCKWISE_90: {
                switch (stub) {
                    case CLOCKWISE_90: return StubRotations.NONE;
                    case CLOCKWISE_180: return StubRotations.CLOCKWISE_90;
                    case COUNTERCLOCKWISE_90: return StubRotations.CLOCKWISE_180;
                }
            }
        }
        throw new IllegalStateException("Impossible state");
    }

    @Override
    public Angle angle() {
        return Angle.fromDegrees(this.angle);
    }
}
