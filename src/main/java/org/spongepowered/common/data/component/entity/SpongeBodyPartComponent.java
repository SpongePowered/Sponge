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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.BodyPartRotationalComponent;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.component.AbstractMappedComponent;

import java.util.Map;

public class SpongeBodyPartComponent extends AbstractMappedComponent<BodyPart, Vector3d, BodyPartRotationalComponent> implements BodyPartRotationalComponent {

    public static final DataQuery BODY_PART_ROTATIONS = of("BodyPartRotations");
    public static final DataQuery ROTATION_X = of("RotationX");
    public static final DataQuery ROTATION_Y = of("RotationY");
    public static final DataQuery ROTATION_Z = of("RotationZ");

    public SpongeBodyPartComponent() {
        super(BodyPartRotationalComponent.class);
    }

    @Override
    public Vector3d getHeadDirection() {
        return this.get(BodyParts.HEAD).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setHeadDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.HEAD, checkNotNull(direction));
    }

    @Override
    public Vector3d getBodyRotation() {
        return this.get(BodyParts.CHEST).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setBodyDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.CHEST, checkNotNull(direction));
    }

    @Override
    public Vector3d getLeftArmDirection() {
        return this.get(BodyParts.LEFT_ARM).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setLeftArmDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.LEFT_ARM, checkNotNull(direction));
    }

    @Override
    public Vector3d getRightArmDirection() {
        return this.get(BodyParts.RIGHT_ARM).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setRightArmDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.RIGHT_ARM, checkNotNull(direction));
    }

    @Override
    public Vector3d getLeftLegDirection() {
        return this.get(BodyParts.LEFT_LEG).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setLeftLegDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.LEFT_LEG, checkNotNull(direction));
    }

    @Override
    public Vector3d getRightLegDirection() {
        return this.get(BodyParts.RIGHT_LEG).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalComponent setRightLegDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.RIGHT_LEG, checkNotNull(direction));
    }

    @Override
    public DataTransactionResult set(BodyPart key, Vector3d value) {
        final DataTransactionBuilder builder = builder();
        if (this.keyValueMap.containsKey(checkNotNull(key))) {
            builder.replace(new SpongeBodyPartComponent().setUnsafe(key, this.keyValueMap.get(key)));
        }
        this.keyValueMap.put(key, checkNotNull(value));
        return builder.result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public DataTransactionResult set(Map<BodyPart, Vector3d> mapped) {
        final Map<BodyPart, Vector3d> replaced = Maps.newHashMap();
        for (Map.Entry<BodyPart, Vector3d> entry : checkNotNull(mapped).entrySet()) {
            if (this.keyValueMap.containsKey(entry.getKey())) {
                replaced.put(entry.getKey(), this.keyValueMap.get(entry.getKey()));
            }
            this.keyValueMap.put(checkNotNull(entry.getKey()), checkNotNull(entry.getValue()));
        }
        return builder().replace(new SpongeBodyPartComponent().setUnsafe(replaced)).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public DataTransactionResult set(BodyPart... mapped) {
        final Map<BodyPart, Vector3d> replaced = Maps.newHashMap();
        for (BodyPart entry : checkNotNull(mapped)) {
            if (this.keyValueMap.containsKey(entry)) {
                replaced.put(entry, this.keyValueMap.get(entry));
            }
            this.keyValueMap.put(checkNotNull(entry), new Vector3d());
        }
        return builder().replace(new SpongeBodyPartComponent().setUnsafe(replaced)).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public BodyPartRotationalComponent copy() {
        return new SpongeBodyPartComponent().setUnsafe(this.keyValueMap);
    }

    @Override
    public int compareTo(BodyPartRotationalComponent o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final DataView internal = new MemoryDataContainer().createView(BODY_PART_ROTATIONS);
        for (Map.Entry<BodyPart, Vector3d> entry : this.keyValueMap.entrySet()) {
            final Vector3d vector3d = entry.getValue();
            internal.createView(of(entry.getKey().getId()))
                    .set(ROTATION_X, vector3d.getX())
                    .set(ROTATION_Y, vector3d.getY())
                    .set(ROTATION_Z, vector3d.getZ());
        }
        return internal.getContainer();
    }
}
