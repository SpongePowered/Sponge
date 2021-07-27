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
package org.spongepowered.common.world.volume.buffer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.spongepowered.api.util.Angle;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.common.util.transformation.SpongeTransformationBuilder;
import org.spongepowered.common.world.volume.buffer.archetype.AbstractReferentArchetypeVolume;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class AbstractReferentArchetypeVolumeTest {

    private static Stream<Arguments> testSizeTransformations() {
        final Rotation rotate0  = Mockito.mock(Rotation.class, Mockito.withSettings()
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        final Rotation rotate90 = Mockito.mock(Rotation.class, Mockito.withSettings()
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(rotate0.angle()).thenReturn(Angle.fromDegrees(0));
        Mockito.when(rotate90.angle()).thenReturn(Angle.fromDegrees(90));
        return Stream.of(
                Arguments.of(
                        new Vector3i(0, 0, 0),
                        new Vector3i(1, 1, 1),
                        new Vector3i(0, 0, 0),
                        new Vector3i(1, 1, 1),
                        new SpongeTransformationBuilder().rotate(rotate0).origin(new Vector3d(1, 0, 1)).build()
                ),
                Arguments.of(
                        new Vector3i(0, 0, 0),
                        new Vector3i(1, 1, 1),
                        new Vector3i(1, 0, 0),
                        new Vector3i(0, 1, 1),
                        new SpongeTransformationBuilder().rotate(rotate90).origin(new Vector3d(1, 1, 1)).build()
                ),
                Arguments.of(
                        new Vector3i(1, 1, 1),
                        new Vector3i(2, 2, 2),
                        new Vector3i(2, 1, 1),
                        new Vector3i(1, 2, 2),
                        new SpongeTransformationBuilder().rotate(rotate90).origin(new Vector3d(2, 2, 2)).build()
                ),
                Arguments.of(
                        new Vector3i(1, 1, 1),
                        new Vector3i(5, 5, 5),
                        new Vector3i(5, 1, 1),
                        new Vector3i(1, 5, 5),
                        new SpongeTransformationBuilder().rotate(rotate90).origin(new Vector3d(3.5, 3.5, 3.5)).build()
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testSizeTransformations(final Vector3i min, final Vector3i max, final Vector3i expectedMin, final Vector3i expectedMax,
            final Transformation transformation) {

        // don't care about the reference for this test
        final TestAbstractReferentArchetypeVolume volume = new TestAbstractReferentArchetypeVolume(() -> null, transformation);

        volume.transformBlockSizes(min, max, (actualMin, actualMax) -> {
            Assertions.assertEquals(expectedMin, actualMin, "min is incorrect");
            Assertions.assertEquals(expectedMax, actualMax, "max is incorrect");
            return expectedMax;
        });

    }

    final static class TestAbstractReferentArchetypeVolume extends AbstractReferentArchetypeVolume<ArchetypeVolume> {

        protected TestAbstractReferentArchetypeVolume(
                final Supplier<ArchetypeVolume> reference,
                final Transformation transformation) {
            super(reference, transformation);
        }

        // upgrade access visibility for test.
        @Override
        public Vector3i transformBlockSizes(final Vector3i min, final Vector3i max, final BiFunction<Vector3i, Vector3i, Vector3i> minmax) {
            return super.transformBlockSizes(min, max, minmax);
        }
    }

}
