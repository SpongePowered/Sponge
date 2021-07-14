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
package org.spongepowered.common.util.transformation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.spongepowered.api.util.Angle;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.math.vector.Vector3d;

import java.util.stream.Stream;

final class TransformationTest {

    private static Stream<Arguments> testRotatingAroundOrigin90DegreesAroundYAxis() {
        return Stream.of(
                Arguments.of(new Vector3d( 1, 0,  0), new Vector3d( 0, 0, -1)),
                Arguments.of(new Vector3d( 1, 1,  0), new Vector3d( 0, 1, -1)),
                Arguments.of(new Vector3d(-1, 1,  0), new Vector3d( 0, 1,  1)),
                Arguments.of(new Vector3d( 0, 1,  1), new Vector3d( 1, 1,  0)),
                Arguments.of(new Vector3d( 0, 1, -1), new Vector3d(-1, 1,  0)),
                Arguments.of(new Vector3d( 0, 0,  0), new Vector3d( 0, 0,  0)),
                Arguments.of(new Vector3d( 0, 1,  0), new Vector3d( 0, 1,  0)),
                Arguments.of(new Vector3d(-2, 1,  0), new Vector3d( 0, 1,  2)),
                Arguments.of(new Vector3d(-1, 1,  1), new Vector3d( 1, 1,  1)),
                Arguments.of(new Vector3d(-1, 1, -1), new Vector3d(-1, 1,  1))
        );
    }

    private static Stream<Arguments> testRotatingAroundOrigin180DegreesAroundYAxis() {
        return Stream.of(
                Arguments.of(new Vector3d( 1, 0,  0), new Vector3d(-1, 0,  0)),
                Arguments.of(new Vector3d( 1, 1,  0), new Vector3d(-1, 1,  0)),
                Arguments.of(new Vector3d(-1, 1,  0), new Vector3d( 1, 1,  0)),
                Arguments.of(new Vector3d( 0, 1,  1), new Vector3d( 0, 1, -1)),
                Arguments.of(new Vector3d( 0, 1, -1), new Vector3d( 0, 1,  1))
        );
    }

    private static Stream<Arguments> testMirrorInXDirection() {
        return Stream.of(
                Arguments.of(new Vector3d( 1, 0,  0), new Vector3d(-1, 0,  0)),
                Arguments.of(new Vector3d( 1, 1,  0), new Vector3d(-1, 1,  0)),
                Arguments.of(new Vector3d(-1, 1,  0), new Vector3d( 1, 1,  0)),
                Arguments.of(new Vector3d( 0, 1,  1), new Vector3d( 0, 1,  1)),
                Arguments.of(new Vector3d( 0, 1, -1), new Vector3d( 0, 1, -1))
        );
    }

    private static Stream<Arguments> testTranslationOnly() {
        return Stream.of(
                new Vector3d( 1, 0,  0),
                new Vector3d( 1, 1,  0),
                new Vector3d(-1, 1,  0),
                new Vector3d( 0, 1,  1),
                new Vector3d( 0, 1, -1)
        ).map(x -> Arguments.of(x, x.add(Vector3d.ONE)));
    }

    private static Stream<Arguments> testRotatingAroundOrigin90DegreesAroundYAxisAtDisplacedOrigin() {
        return Stream.of(
                Arguments.of(new Vector3d( 1, 0,  0), new Vector3d( 1, 0,  0), new Vector3d( 1, 0, 0)),
                Arguments.of(new Vector3d( 1, 1,  0), new Vector3d( 1, 1,  0), new Vector3d( 1, 0, 0)),
                Arguments.of(new Vector3d(-1, 1,  0), new Vector3d( 1, 1, 2), new Vector3d( 1, 0, 0)),
                Arguments.of(new Vector3d( 0, 1,  1), new Vector3d( 2, 1, 1), new Vector3d( 1, 0, 0)),
                Arguments.of(new Vector3d( 0, 1, -1), new Vector3d( 0, 1, 1), new Vector3d( 1, 0, 0)),
                Arguments.of(new Vector3d( 2, 0,  2), new Vector3d( 0, 0,  2), new Vector3d( 1, 0, 1))
        );
    }

    private static Stream<Arguments> testRotationThenTranslation() {
        return TransformationTest.testRotatingAroundOrigin90DegreesAroundYAxis()
                .map(x -> {
                    final Vector3d newExpected = ((Vector3d) x.get()[1]).add(Vector3d.ONE);
                    return Arguments.of(x.get()[0], newExpected);
                });
    }

    private Transformation performRotationTest(final Rotation rotation, final Vector3d original, final Vector3d expected) {
        return this.performRotationTest(rotation, original, expected, Vector3d.ZERO, 1);
    }

    private Transformation performRotationTest(
            final Rotation rotation, final Vector3d original, final Vector3d expected, final Vector3d origin, final int rotateTimes) {
        // given this builder
        final SpongeTransformationBuilder transformationBuilder = new SpongeTransformationBuilder();

        // when rotating
        for (int i = 0; i < rotateTimes; ++i) {
            transformationBuilder.rotate(rotation);
        }
        final Transformation transformation = transformationBuilder.origin(origin).build();

        // then perform the transformation
        final Vector3d result = transformation.transformPosition(original);

        Assertions.assertEquals(expected, result, "Did not get expected rotation.");
        return transformation;
    }

    private static SpongeTransformationBuilder createZeroBuilder() {
        final SpongeTransformationBuilder transformationBuilder = new SpongeTransformationBuilder();
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(0));
        transformationBuilder.rotate(mockRotation);
        return transformationBuilder;
    }

    @ParameterizedTest
    @MethodSource
    void testRotatingAroundOrigin90DegreesAroundYAxis(final Vector3d original, final Vector3d expected) {
        // and this rotation
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(90));

        this.performRotationTest(mockRotation, original, expected);
    }

    @ParameterizedTest
    @MethodSource
    void testRotatingAroundOrigin180DegreesAroundYAxis(final Vector3d original, final Vector3d expected) {
        // and this rotation
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(180));

        this.performRotationTest(mockRotation, original, expected);
    }

    @ParameterizedTest
    @MethodSource("testRotatingAroundOrigin180DegreesAroundYAxis")
    void testRotatingAroundOrigin180DegreesAroundYAxisWithTwoSteps(final Vector3d original, final Vector3d expected) {
        // and this rotation
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(90));
        Mockito.when(mockRotation.and(Mockito.any(Rotation.class))).thenAnswer((Answer<Rotation>) invocation -> {
            final Rotation rotation = invocation.getArgument(0);
            final Rotation newMock = Mockito.mock(Rotation.class, Mockito.withSettings()
                .defaultAnswer(Mockito.CALLS_REAL_METHODS));
            Mockito.when(newMock.angle()).thenAnswer((Answer<Angle>) x -> Angle.fromDegrees(rotation.angle().degrees() + 90));
            return newMock;
        });

        final Transformation transformation = this.performRotationTest(mockRotation, original, expected, Vector3d.ZERO, 2);
        Assertions.assertEquals(180, transformation.rotation().angle().degrees(), "Did not get expected angle.");
    }

    @ParameterizedTest
    @MethodSource
    void testMirrorInXDirection(final Vector3d original, final Vector3d expected) {
        // given this builder
        final SpongeTransformationBuilder transformationBuilder = TransformationTest.createZeroBuilder();

        // when rotating by 90 degrees
        final Transformation transformation = transformationBuilder.mirror(Axis.X).build();

        // then perform the transformation
        final Vector3d result = transformation.transformPosition(original);

        Assertions.assertEquals(expected, result, "Did not get expected mirroring.");
        Assertions.assertTrue(transformation.mirror(Axis.X), "Did not get X axis was mirrored.");
        Assertions.assertFalse(transformation.mirror(Axis.Z), "Did not get Z axis was not mirrored.");
    }

    @ParameterizedTest
    @MethodSource
    void testTranslationOnly(final Vector3d original, final Vector3d expected) {
        // given this builder
        final SpongeTransformationBuilder transformationBuilder = TransformationTest.createZeroBuilder();

        // when translating by (1,1,1)
        final Transformation transformation = transformationBuilder.translate(Vector3d.ONE).build();

        // then perform the transformation
        final Vector3d result = transformation.transformPosition(original);

        Assertions.assertEquals(expected, result, "Did not get expected rotation.");
    }

    @ParameterizedTest
    @MethodSource
    void testRotatingAroundOrigin90DegreesAroundYAxisAtDisplacedOrigin(final Vector3d original, final Vector3d expected, final Vector3d origin) {
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(90));

        this.performRotationTest(mockRotation, original, expected, origin, 1);
    }

    @ParameterizedTest
    @MethodSource
    void testRotationThenTranslation(final Vector3d original, final Vector3d expected) {
        // given this builder
        final SpongeTransformationBuilder transformationBuilder = new SpongeTransformationBuilder();

        // and this rotation
        final Rotation mockRotation = Mockito.mock(Rotation.class, Mockito.withSettings()
            .defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockRotation.angle()).thenReturn(Angle.fromDegrees(90));

        // when rotating by 90 degrees with this translation
        final Transformation transformation = transformationBuilder
                .rotate(mockRotation)
                .translate(Vector3d.ONE)
                .build();

        // then perform the transformation
        final Vector3d result = transformation.transformPosition(original);

        Assertions.assertEquals(expected, result, "Did not get expected result.");
    }

}
