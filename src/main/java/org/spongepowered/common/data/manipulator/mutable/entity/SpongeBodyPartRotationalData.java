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
package org.spongepowered.common.data.manipulator.mutable.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.Queries.POSITION_X;
import static org.spongepowered.api.data.Queries.POSITION_Y;
import static org.spongepowered.api.data.Queries.POSITION_Z;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBodyPartRotationalData;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeBodyPartRotationalData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Map;

public class SpongeBodyPartRotationalData extends AbstractData<BodyPartRotationalData, ImmutableBodyPartRotationalData>
        implements BodyPartRotationalData {

    private Map<BodyPart, Vector3d> rotations;

    public SpongeBodyPartRotationalData() {
        this(ImmutableMap.<BodyPart, Vector3d>builder()
                .put(BodyParts.HEAD, Constants.Entity.ArmorStand.DEFAULT_HEAD_ROTATION)
                .put(BodyParts.CHEST, Constants.Entity.ArmorStand.DEFAULT_CHEST_ROTATION)
                .put(BodyParts.LEFT_ARM, Constants.Entity.ArmorStand.DEFAULT_LEFT_ARM_ROTATION)
                .put(BodyParts.RIGHT_ARM, Constants.Entity.ArmorStand.DEFAULT_RIGHT_ARM_ROTATION)
                .put(BodyParts.LEFT_LEG, Constants.Entity.ArmorStand.DEFAULT_LEFT_LEG_ROTATION)
                .put(BodyParts.RIGHT_LEG, Constants.Entity.ArmorStand.DEFAULT_RIGHT_LEG_ROTATION)
                .build());
    }

    public SpongeBodyPartRotationalData(Map<BodyPart, Vector3d> rotations) {
        super(BodyPartRotationalData.class);
        this.rotations = Maps.newHashMap(checkNotNull(rotations, "rotations"));
        registerGettersAndSetters();
    }

    @Override
    public DataContainer toContainer() {
        Vector3d headRotation = this.rotations.get(BodyParts.HEAD);
        Vector3d chestRotation = this.rotations.get(BodyParts.CHEST);
        Vector3d leftArmRotation = this.rotations.get(BodyParts.LEFT_ARM);
        Vector3d rightArmRotation = this.rotations.get(BodyParts.RIGHT_ARM);
        Vector3d leftLegRotation = this.rotations.get(BodyParts.LEFT_LEG);
        Vector3d rightLegRotation = this.rotations.get(BodyParts.RIGHT_LEG);

        return super.toContainer()
                .set(Keys.BODY_ROTATIONS, this.rotations)
                .set(Keys.HEAD_ROTATION.getQuery().then(POSITION_X), headRotation.getX())
                .set(Keys.HEAD_ROTATION.getQuery().then(POSITION_Y), headRotation.getY())
                .set(Keys.HEAD_ROTATION.getQuery().then(POSITION_Z), headRotation.getZ())
                .set(Keys.CHEST_ROTATION.getQuery().then(POSITION_X), chestRotation.getX())
                .set(Keys.CHEST_ROTATION.getQuery().then(POSITION_Y), chestRotation.getY())
                .set(Keys.CHEST_ROTATION.getQuery().then(POSITION_Z), chestRotation.getZ())
                .set(Keys.LEFT_ARM_ROTATION.getQuery().then(POSITION_X), leftArmRotation.getX())
                .set(Keys.LEFT_ARM_ROTATION.getQuery().then(POSITION_Y), leftArmRotation.getY())
                .set(Keys.LEFT_ARM_ROTATION.getQuery().then(POSITION_Z), leftArmRotation.getZ())
                .set(Keys.RIGHT_ARM_ROTATION.getQuery().then(POSITION_X), rightArmRotation.getX())
                .set(Keys.RIGHT_ARM_ROTATION.getQuery().then(POSITION_Y), rightArmRotation.getY())
                .set(Keys.RIGHT_ARM_ROTATION.getQuery().then(POSITION_Z), rightArmRotation.getZ())
                .set(Keys.LEFT_LEG_ROTATION.getQuery().then(POSITION_X), leftLegRotation.getX())
                .set(Keys.LEFT_LEG_ROTATION.getQuery().then(POSITION_Y), leftLegRotation.getY())
                .set(Keys.LEFT_LEG_ROTATION.getQuery().then(POSITION_Z), leftLegRotation.getZ())
                .set(Keys.RIGHT_LEG_ROTATION.getQuery().then(POSITION_X), rightLegRotation.getX())
                .set(Keys.RIGHT_LEG_ROTATION.getQuery().then(POSITION_Y), rightLegRotation.getY())
                .set(Keys.RIGHT_LEG_ROTATION.getQuery().then(POSITION_Z), rightLegRotation.getZ());
    }

    @Override
    public MapValue<BodyPart, Vector3d> partRotation() {
        return new SpongeMapValue<>(Keys.BODY_ROTATIONS, this.rotations);
    }

    @Override
    public Value<Vector3d> headDirection() {
        return new SpongeValue<>(Keys.HEAD_ROTATION, this.rotations.get(BodyParts.HEAD));
    }

    @Override
    public Value<Vector3d> bodyRotation() {
        return new SpongeValue<>(Keys.CHEST_ROTATION, this.rotations.get(BodyParts.CHEST));
    }

    @Override
    public Value<Vector3d> leftArmDirection() {
        return new SpongeValue<>(Keys.LEFT_ARM_ROTATION, this.rotations.get(BodyParts.LEFT_ARM));
    }

    @Override
    public Value<Vector3d> rightArmDirection() {
        return new SpongeValue<>(Keys.RIGHT_ARM_ROTATION, this.rotations.get(BodyParts.RIGHT_ARM));
    }

    @Override
    public Value<Vector3d> leftLegDirection() {
        return new SpongeValue<>(Keys.LEFT_LEG_ROTATION, this.rotations.get(BodyParts.LEFT_LEG));
    }

    @Override
    public Value<Vector3d> rightLegDirection() {
        return new SpongeValue<>(Keys.RIGHT_LEG_ROTATION, this.rotations.get(BodyParts.RIGHT_LEG));
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.BODY_ROTATIONS, () -> this.rotations);
        registerFieldSetter(Keys.BODY_ROTATIONS, rotations -> this.rotations = Maps.newHashMap(rotations));
        registerKeyValue(Keys.BODY_ROTATIONS, this::partRotation);

        registerFieldGetter(Keys.HEAD_ROTATION, () -> this.rotations.get(BodyParts.HEAD));
        registerFieldSetter(Keys.HEAD_ROTATION, value -> this.rotations.put(BodyParts.HEAD, value));
        registerKeyValue(Keys.HEAD_ROTATION, this::headDirection);

        registerFieldGetter(Keys.CHEST_ROTATION, () -> this.rotations.get(BodyParts.CHEST));
        registerFieldSetter(Keys.CHEST_ROTATION, value -> this.rotations.put(BodyParts.CHEST, value));
        registerKeyValue(Keys.CHEST_ROTATION, this::bodyRotation);

        registerFieldGetter(Keys.LEFT_ARM_ROTATION, () -> this.rotations.get(BodyParts.LEFT_ARM));
        registerFieldSetter(Keys.LEFT_ARM_ROTATION, value -> this.rotations.put(BodyParts.LEFT_ARM, value));
        registerKeyValue(Keys.LEFT_ARM_ROTATION, this::leftArmDirection);

        registerFieldGetter(Keys.RIGHT_ARM_ROTATION, () -> this.rotations.get(BodyParts.RIGHT_ARM));
        registerFieldSetter(Keys.RIGHT_ARM_ROTATION, value -> this.rotations.put(BodyParts.RIGHT_ARM, value));
        registerKeyValue(Keys.RIGHT_ARM_ROTATION, this::rightArmDirection);

        registerFieldGetter(Keys.LEFT_LEG_ROTATION, () -> this.rotations.get(BodyParts.LEFT_LEG));
        registerFieldSetter(Keys.LEFT_LEG_ROTATION, value -> this.rotations.put(BodyParts.LEFT_LEG, value));
        registerKeyValue(Keys.LEFT_LEG_ROTATION, this::leftLegDirection);

        registerFieldGetter(Keys.RIGHT_LEG_ROTATION, () -> this.rotations.get(BodyParts.RIGHT_LEG));
        registerFieldSetter(Keys.RIGHT_LEG_ROTATION, value -> this.rotations.put(BodyParts.RIGHT_LEG, value));
        registerKeyValue(Keys.RIGHT_LEG_ROTATION, this::rightLegDirection);
    }

    @Override
    public BodyPartRotationalData copy() {
        return new SpongeBodyPartRotationalData(this.rotations);
    }

    @Override
    public ImmutableBodyPartRotationalData asImmutable() {
        return new ImmutableSpongeBodyPartRotationalData(this.rotations);
    }

}
