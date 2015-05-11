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
package org.spongepowered.common.data.manipulators.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.BodyPartRotationalData;
import org.spongepowered.api.data.types.BodyPart;
import org.spongepowered.api.data.types.BodyParts;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.manipulators.AbstractMappedData;

import java.util.Map;

public class SpongeBodyPartData extends AbstractMappedData<BodyPart, Vector3d, BodyPartRotationalData> implements BodyPartRotationalData {

    public SpongeBodyPartData() {
        super(BodyPartRotationalData.class);
    }

    @Override
    public Vector3d getHeadDirection() {
        return this.get(BodyParts.HEAD).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setHeadDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.HEAD, checkNotNull(direction));
    }

    @Override
    public Vector3d getBodyRotation() {
        return this.get(BodyParts.CHEST).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setBodyDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.CHEST, checkNotNull(direction));
    }

    @Override
    public Vector3d getLeftArmDirection() {
        return this.get(BodyParts.LEFT_ARM).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setLeftArmDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.LEFT_ARM, checkNotNull(direction));
    }

    @Override
    public Vector3d getRightArmDirection() {
        return this.get(BodyParts.RIGHT_ARM).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setRightArmDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.RIGHT_ARM, checkNotNull(direction));
    }

    @Override
    public Vector3d getLeftLegDirection() {
        return this.get(BodyParts.LEFT_LEG).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setLeftLegDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.LEFT_LEG, checkNotNull(direction));
    }

    @Override
    public Vector3d getRightLegDirection() {
        return this.get(BodyParts.RIGHT_LEG).or(new Vector3d());
    }

    @Override
    public BodyPartRotationalData setRightLegDirection(Vector3d direction) {
        return this.setUnsafe(BodyParts.RIGHT_LEG, checkNotNull(direction));
    }

    @Override
    public DataTransactionResult set(BodyPart key, Vector3d value) {
        final DataTransactionBuilder builder = builder();
        if (this.keyValueMap.containsKey(checkNotNull(key))) {
            builder.replace(new SpongeBodyPartData().setUnsafe(key, this.keyValueMap.get(key)));
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
        return builder().replace(new SpongeBodyPartData().setUnsafe(replaced)).result(DataTransactionResult.Type.SUCCESS).build();
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
        return builder().replace(new SpongeBodyPartData().setUnsafe(replaced)).result(DataTransactionResult.Type.SUCCESS).build();
    }

    @Override
    public BodyPartRotationalData copy() {
        return new SpongeBodyPartData().setUnsafe(this.keyValueMap);
    }

    @Override
    public int compareTo(BodyPartRotationalData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final DataView internal = new MemoryDataContainer().createView(of("BodyPartRotations"));
        for (Map.Entry<BodyPart, Vector3d> entry : this.keyValueMap.entrySet()) {
            final Vector3d vector3d = entry.getValue();
            internal.createView(of(entry.getKey().getId()))
                    .set(of("RotationX"), vector3d.getX())
                    .set(of("RotationY"), vector3d.getY())
                    .set(of("RotationZ"), vector3d.getZ());
        }
        return internal.getContainer();
    }
}
