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
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public final class ImmutableSpongeStructureData extends AbstractImmutableData<ImmutableStructureData, StructureData> implements ImmutableStructureData {

    private final String author;
    private final ImmutableValue<String> authorValue;
    private final boolean ignoreEntities;
    private final ImmutableValue<Boolean> ignoreEntitiesValue;
    private final float integrity;
    private final ImmutableValue<Float> integrityValue;
    private final StructureMode mode;
    private final ImmutableValue<StructureMode> modeValue;
    private final Vector3i position;
    private final ImmutableValue<Vector3i> positionValue;
    private final boolean powered;
    private final ImmutableValue<Boolean> poweredValue;
    private final long seed;
    private final ImmutableValue<Long> seedValue;
    private final boolean showAir;
    private final ImmutableValue<Boolean> showAirValue;
    private final boolean showBoundingBox;
    private final ImmutableValue<Boolean> showBoundingBoxValue;
    private final Vector3i size;
    private final ImmutableValue<Vector3i> sizeValue;

    public ImmutableSpongeStructureData(String author, boolean ignoreEntities, float integrity, StructureMode mode, Vector3i position, boolean powered, boolean showAir, boolean showBoundingBox, long seed, Vector3i size) {
        super(ImmutableStructureData.class);

        this.author = author;
        this.authorValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_AUTHOR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_AUTHOR, this.author);
        this.ignoreEntities = ignoreEntities;
        this.ignoreEntitiesValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_IGNORE_ENTITIES, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_IGNORE_ENTITIES, this.ignoreEntities);
        this.integrity = integrity;
        this.integrityValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_INTEGRITY, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_INTEGRITY, this.integrity);
        this.mode = mode;
        this.modeValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_MODE, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_MODE, this.mode);
        this.position = position;
        this.positionValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_POSITION, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_POSITION, this.position);
        this.powered = powered;
        this.poweredValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_POWERED, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_POWERED, this.powered);
        this.seed = seed;
        this.seedValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_SEED, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SEED, this.seed);
        this.showAir = showAir;
        this.showAirValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_SHOW_AIR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_AIR, this.showAir);
        this.showBoundingBox = showBoundingBox;
        this.showBoundingBoxValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_SHOW_BOUNDING_BOX, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_BOUNDING_BOX, this.showBoundingBox);
        this.size = size;
        this.sizeValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_SIZE, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SIZE, this.size);

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
    public ImmutableValue<String> author() {
        return this.authorValue;
    }

    private String getAuthor() {
        return this.author;
    }

    @Override
    public ImmutableValue<Boolean> ignoreEntities() {
        return this.ignoreEntitiesValue;
    }

    private boolean shouldIgnoreEntities() {
        return this.ignoreEntities;
    }

    @Override
    public ImmutableValue<Float> integrity() {
        return this.integrityValue;
    }

    private float getIntegrity() {
        return this.integrity;
    }

    @Override
    public ImmutableValue<StructureMode> mode() {
        return this.modeValue;
    }

    private StructureMode getMode() {
        return this.mode;
    }

    @Override
    public ImmutableValue<Vector3i> position() {
        return this.positionValue;
    }

    private Vector3i getPosition() {
        return this.position;
    }

    @Override
    public ImmutableValue<Boolean> powered() {
        return this.poweredValue;
    }

    private boolean isPowered() {
        return this.powered;
    }

    @Override
    public ImmutableValue<Long> seed() {
        return this.seedValue;
    }

    private long getSeed() {
        return this.seed;
    }

    @Override
    public ImmutableValue<Boolean> showAir() {
        return this.showAirValue;
    }

    private boolean shouldShowAir() {
        return this.showAir;
    }

    @Override
    public ImmutableValue<Boolean> showBoundingBox() {
        return this.showBoundingBoxValue;
    }

    private boolean shouldShowBoundingBox() {
        return this.showBoundingBox;
    }

    @Override
    public ImmutableValue<Vector3i> size() {
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
