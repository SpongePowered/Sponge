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
package org.spongepowered.common.entity;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.matrix.Matrix4d;
import com.flowpowered.math.vector.Vector3d;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;

public class SpongeTransformTest {

    private static final double EPSILON = 1e-4;
    private Extent mockExtent1;
    private Extent mockExtent2;

    @Before
    public void generateMockExtent() {
        this.mockExtent1 = Mockito.when(Mockito.mock(Extent.class).isLoaded()).thenReturn(false).getMock();
        this.mockExtent2 = Mockito.when(Mockito.mock(Extent.class).isLoaded()).thenReturn(true).getMock();
    }

    @Test
    public void testLocation() {
        final Vector3d position1 = new Vector3d(1, 2, 3);
        final Vector3d position2 = new Vector3d(4, 5, 6);

        final Transform transform = new SpongeTransform(this.mockExtent1, position1);
        Assert.assertEquals(new Location(this.mockExtent1, position1), transform.getLocation());
        Assert.assertEquals(this.mockExtent1, transform.getExtent());
        assertEquals(position1, transform.getPosition());

        transform.setLocation(new Location(this.mockExtent2, position2));
        Assert.assertEquals(new Location(this.mockExtent2, position2), transform.getLocation());
        Assert.assertEquals(this.mockExtent2, transform.getExtent());
        assertEquals(position2, transform.getPosition());

        transform.setExtent(this.mockExtent1);
        Assert.assertEquals(this.mockExtent1, transform.getExtent());

        transform.setPosition(position1);
        assertEquals(position1, transform.getPosition());

        transform.addTranslation(position2);
        assertEquals(position1.add(position2), transform.getPosition());
    }

    @Test
    public void testRotation() {
        final Vector3d rotation1 = new Vector3d(20, 40, 60);
        final Quaterniond rotationQuat1 = Quaterniond.fromAxesAnglesDeg(rotation1.getX(), -rotation1.getY(), rotation1.getZ());
        final Vector3d rotation2 = new Vector3d(45, 135, 225);
        final Quaterniond rotationQuat2 = Quaterniond.fromAxesAnglesDeg(rotation2.getX(), -rotation2.getY(), rotation2.getZ());
        final Quaterniond rotationQuat1Plus2 = rotationQuat2.mul(rotationQuat1);
        final Vector3d axesAnglesDeg = rotationQuat1Plus2.getAxesAngleDeg();
        final Vector3d rotation1Plus2 = new Vector3d(axesAnglesDeg.getX(), -axesAnglesDeg.getY(), axesAnglesDeg.getZ());

        final Transform transform = new SpongeTransform(this.mockExtent1, Vector3d.ZERO, rotation1, Vector3d.ONE);
        assertEquals(rotation1, transform.getRotation());
        assertEquals(rotationQuat1, transform.getRotationAsQuaternion());
        Assert.assertEquals(rotation1.getX(), transform.getPitch(), EPSILON);
        Assert.assertEquals(rotation1.getY(), transform.getYaw(), EPSILON);
        Assert.assertEquals(rotation1.getZ(), transform.getRoll(), EPSILON);

        transform.setRotation(rotation2);
        assertEquals(rotation2, transform.getRotation());
        assertEquals(rotationQuat2, transform.getRotationAsQuaternion());

        transform.setRotation(rotationQuat1);
        assertEquals(rotation1, transform.getRotation());
        assertEquals(rotationQuat1, transform.getRotationAsQuaternion());

        transform.addRotation(rotation2);
        assertEquals(rotationQuat1Plus2, transform.getRotationAsQuaternion());
        assertEquals(rotation1Plus2, transform.getRotation());

        transform.setRotation(rotationQuat1).addRotation(rotationQuat2);
        assertEquals(rotationQuat1Plus2, transform.getRotationAsQuaternion());
        assertEquals(rotation1Plus2, transform.getRotation());
    }

    @Test
    public void testScale() {
        final Vector3d scale1 = new Vector3d(1, 2, 3);
        final Vector3d scale2 = new Vector3d(4, 5, 6);

        final Transform transform = new SpongeTransform(this.mockExtent1, Vector3d.ZERO, Vector3d.ZERO, scale1);
        assertEquals(scale1, transform.getScale());

        transform.setScale(scale2);
        assertEquals(scale2, transform.getScale());

        transform.addScale(scale1);
        assertEquals(scale2.mul(scale1), transform.getScale());
    }

    @Test
    public void testValid() {
        Assert.assertFalse(new SpongeTransform(this.mockExtent1, Vector3d.ZERO).isValid());
        Assert.assertTrue(new SpongeTransform(this.mockExtent2, Vector3d.ZERO).isValid());
    }

    @Test
    public void testMatrix() {
        final Vector3d position = new Vector3d(1, 2, 3);
        final Quaterniond rotation = Quaterniond.fromAxesAnglesDeg(20, 30, 60);
        final Vector3d scale = new Vector3d(4, 5, 6);

        Assert.assertEquals(Matrix4d.createTranslation(position), new SpongeTransform(this.mockExtent1, position).toMatrix());
        Assert.assertEquals(Matrix4d.createRotation(rotation), new SpongeTransform(this.mockExtent1, Vector3d.ZERO).setRotation(rotation).toMatrix());
        Assert.assertEquals(Matrix4d.createScaling(scale.toVector4(1)),
            new SpongeTransform(this.mockExtent1, Vector3d.ZERO).setScale(scale).toMatrix());
        Assert.assertEquals(
            new Matrix4d(
                4, 0, 0, 1,
                0, 5, 0, 2,
                0, 0, 6, 3,
                0, 0, 0, 1
            ), new SpongeTransform(this.mockExtent1, position, Vector3d.ZERO, scale).toMatrix());
    }

    @Test
    public void testTransformation() {
        final Vector3d position = new Vector3d(1, 2, 3);
        final Quaterniond rotation = Quaterniond.fromAngleDegAxis(90, Vector3d.UNIT_Y);
        final Vector3d scale = new Vector3d(4, 5, 6);

        final Transform transform = new SpongeTransform(this.mockExtent1, position);
        assertTransforms(new Vector3d(11, 12, 13), transform, new Vector3d(10, 10, 10));

        transform.addScale(scale);
        assertTransforms(new Vector3d(41, 52, 63), transform, new Vector3d(10, 10, 10));

        transform.addRotation(rotation);
        assertTransforms(new Vector3d(61, 52, -37), transform, new Vector3d(10, 10, 10));

        transform.add(transform);
        assertTransforms(new Vector3d(-158, 254, -354), transform, new Vector3d(10, 10, 10));
    }

    private void assertTransforms(Vector3d expected, Transform with, Vector3d original) {
        assertEquals(expected, with.toMatrix().transform(original.toVector4(1)).toVector3());
    }

    private void assertEquals(Vector3d expected, Vector3d actual) {
        Assert.assertEquals(expected.getX(), actual.getX(), EPSILON);
        Assert.assertEquals(expected.getY(), actual.getY(), EPSILON);
        Assert.assertEquals(expected.getZ(), actual.getZ(), EPSILON);
    }

    private void assertEquals(Quaterniond expected, Quaterniond actual) {
        Assert.assertEquals(expected.getX(), actual.getX(), EPSILON);
        Assert.assertEquals(expected.getY(), actual.getY(), EPSILON);
        Assert.assertEquals(expected.getZ(), actual.getZ(), EPSILON);
        Assert.assertEquals(expected.getW(), actual.getW(), EPSILON);
    }

}
