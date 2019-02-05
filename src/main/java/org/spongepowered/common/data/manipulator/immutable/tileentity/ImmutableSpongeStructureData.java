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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableStructureData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.StructureData;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.data.value.SpongeImmutableValue;

public final class ImmutableSpongeStructureData extends AbstractImmutableData<ImmutableStructureData, StructureData> implements ImmutableStructureData {

    private final String author;
    private final Value.Immutable<String> authorValue;
    private final boolean ignoreEntities;
    private final Value.Immutable<Boolean> ignoreEntitiesValue;
    private final float integrity;
    private final Value.Immutable<Float> integrityValue;
    private final StructureMode mode;
    private final Value.Immutable<StructureMode> modeValue;
    private final Vector3i position;
    private final Value.Immutable<Vector3i> positionValue;
    private final boolean powered;
    private final Value.Immutable<Boolean> poweredValue;
    private final long seed;
    private final Value.Immutable<Long> seedValue;
    private final boolean showAir;
    private final Value.Immutable<Boolean> showAirValue;
    private final boolean showBoundingBox;
    private final Value.Immutable<Boolean> showBoundingBoxValue;
    private final Vector3i size;
    private final Value.Immutable<Vector3i> sizeValue;

    public ImmutableSpongeStructureData(String author, boolean ignoreEntities, float integrity, StructureMode mode, Vector3i position, boolean powered, boolean showAir, boolean showBoundingBox, long seed, Vector3i size) {
        super(ImmutableStructureData.class);

        this.author = author;
        this.authorValue = new SpongeImmutableValue<>(Keys.STRUCTURE_AUTHOR, this.author);
        this.ignoreEntities = ignoreEntities;
        this.ignoreEntitiesValue = new SpongeImmutableValue<>(Keys.STRUCTURE_IGNORE_ENTITIES, this.ignoreEntities);
        this.integrity = integrity;
        this.integrityValue = new SpongeImmutableValue<>(Keys.STRUCTURE_INTEGRITY, this.integrity);
        this.mode = mode;
        this.modeValue = new SpongeImmutableValue<>(Keys.STRUCTURE_MODE, this.mode);
        this.position = position;
        this.positionValue = new SpongeImmutableValue<>(Keys.STRUCTURE_POSITION, this.position);
        this.powered = powered;
        this.poweredValue = new SpongeImmutableValue<>(Keys.STRUCTURE_POWERED, this.powered);
        this.seed = seed;
        this.seedValue = new SpongeImmutableValue<>(Keys.STRUCTURE_SEED, this.seed);
        this.showAir = showAir;
        this.showAirValue = new SpongeImmutableValue<>(Keys.STRUCTURE_SHOW_AIR, this.showAir);
        this.showBoundingBox = showBoundingBox;
        this.showBoundingBoxValue = new SpongeImmutableValue<>(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this.showBoundingBox);
        this.size = size;
        this.sizeValue = new SpongeImmutableValue<>(Keys.STRUCTURE_SIZE, this.size);

        this.registerGetters();
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.STRUCTURE_AUTHOR, this::author);
        this.registerFieldGetter(Keys.STRUCTURE_AUTHOR, this::getAuthor);
        this.registerKeyValue(Keys.STRUCTURE_IGNORE_ENTITIES, this::ignoreEntities);
        this.registerFieldGetter(Keys.STRUCTURE_IGNORE_ENTITIES, this::shouldIgnoreEntities);
        this.registerKeyValue(Keys.STRUCTURE_INTEGRITY, this::integrity);
        this.registerFieldGetter(Keys.STRUCTURE_INTEGRITY, this::getIntegrity);
        this.registerKeyValue(Keys.STRUCTURE_MODE, this::mode);
        this.registerFieldGetter(Keys.STRUCTURE_MODE, this::getMode);
        this.registerKeyValue(Keys.STRUCTURE_POSITION, this::position);
        this.registerFieldGetter(Keys.STRUCTURE_POSITION, this::getPosition);
        this.registerKeyValue(Keys.STRUCTURE_POWERED, this::powered);
        this.registerFieldGetter(Keys.STRUCTURE_POWERED, this::isPowered);
        this.registerKeyValue(Keys.STRUCTURE_SEED, this::seed);
        this.registerFieldGetter(Keys.STRUCTURE_SEED, this::getSeed);
        this.registerKeyValue(Keys.STRUCTURE_SHOW_AIR, this::showAir);
        this.registerFieldGetter(Keys.STRUCTURE_SHOW_AIR, this::shouldShowAir);
        this.registerKeyValue(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this::showBoundingBox);
        this.registerFieldGetter(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this::shouldShowBoundingBox);
        this.registerKeyValue(Keys.STRUCTURE_SIZE, this::size);
        this.registerFieldGetter(Keys.STRUCTURE_SIZE, this::getSize);
    }

    @Override
    public Value.Immutable<String> author() {
        return this.authorValue;
    }

    private String getAuthor() {
        return this.author;
    }

    @Override
    public Value.Immutable<Boolean> ignoreEntities() {
        return this.ignoreEntitiesValue;
    }

    private boolean shouldIgnoreEntities() {
        return this.ignoreEntities;
    }

    @Override
    public Value.Immutable<Float> integrity() {
        return this.integrityValue;
    }

    private float getIntegrity() {
        return this.integrity;
    }

    @Override
    public Value.Immutable<StructureMode> mode() {
        return this.modeValue;
    }

    private StructureMode getMode() {
        return this.mode;
    }

    @Override
    public Value.Immutable<Vector3i> position() {
        return this.positionValue;
    }

    private Vector3i getPosition() {
        return this.position;
    }

    @Override
    public Value.Immutable<Boolean> powered() {
        return this.poweredValue;
    }

    private boolean isPowered() {
        return this.powered;
    }

    @Override
    public Value.Immutable<Long> seed() {
        return this.seedValue;
    }

    private long getSeed() {
        return this.seed;
    }

    @Override
    public Value.Immutable<Boolean> showAir() {
        return this.showAirValue;
    }

    private boolean shouldShowAir() {
        return this.showAir;
    }

    @Override
    public Value.Immutable<Boolean> showBoundingBox() {
        return this.showBoundingBoxValue;
    }

    private boolean shouldShowBoundingBox() {
        return this.showBoundingBox;
    }

    @Override
    public Value.Immutable<Vector3i> size() {
        return this.sizeValue;
    }

    private Vector3i getSize() {
        return this.size;
    }

    @Override
    public StructureData asMutable() {
        return new SpongeStructureData(this.author, this.ignoreEntities, this.integrity, this.mode, this.position, this.powered, this.showAir, this.showBoundingBox, this.seed, this.size);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.STRUCTURE_AUTHOR.getQuery(), this.author)
            .set(Keys.STRUCTURE_IGNORE_ENTITIES.getQuery(), this.ignoreEntities)
            .set(Keys.STRUCTURE_INTEGRITY.getQuery(), this.integrity)
            .set(Keys.STRUCTURE_MODE.getQuery(), this.mode)
            .set(Keys.STRUCTURE_POSITION.getQuery(), this.position)
            .set(Keys.STRUCTURE_POWERED.getQuery(), this.powered)
            .set(Keys.STRUCTURE_SEED.getQuery(), this.seed)
            .set(Keys.STRUCTURE_SHOW_AIR.getQuery(), this.showAir)
            .set(Keys.STRUCTURE_SHOW_BOUNDING_BOX.getQuery(), this.showBoundingBox)
            .set(Keys.STRUCTURE_SIZE.getQuery(), this.size);
    }

}
