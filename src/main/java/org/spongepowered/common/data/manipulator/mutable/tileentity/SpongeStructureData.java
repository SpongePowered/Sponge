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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableStructureData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.StructureData;
import org.spongepowered.api.data.type.StructureMode;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeStructureData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public final class SpongeStructureData extends AbstractData<StructureData, ImmutableStructureData> implements StructureData {

    private String author;
    private boolean ignoreEntities;
    private float integrity;
    private StructureMode mode;
    private Vector3i position;
    private boolean powered;
    private long seed;
    private boolean showAir;
    private boolean showBoundingBox;
    private Vector3i size;

    public SpongeStructureData() {
        this(Constants.TileEntity.Structure.DEFAULT_STRUCTURE_AUTHOR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_IGNORE_ENTITIES, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_INTEGRITY,
                Constants.TileEntity.Structure.DEFAULT_STRUCTURE_MODE, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_POSITION, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_POWERED,
                Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_AIR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_BOUNDING_BOX, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SEED,
                Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SIZE);
    }

    public SpongeStructureData(String author, boolean ignoreEntities, float integrity, StructureMode mode, Vector3i position, boolean powered, boolean showAir, boolean showBoundingBox, long seed, Vector3i size) {
        super(StructureData.class);

        this.author = author;
        this.ignoreEntities = ignoreEntities;
        this.integrity = integrity;
        this.mode = mode;
        this.position = position;
        this.powered = powered;
        this.seed = seed;
        this.showAir = showAir;
        this.showBoundingBox = showBoundingBox;
        this.size = size;

        this.registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        this.registerKeyValue(Keys.STRUCTURE_AUTHOR, this::author);
        this.registerFieldGetter(Keys.STRUCTURE_AUTHOR, this::getAuthor);
        this.registerFieldSetter(Keys.STRUCTURE_AUTHOR, this::setAuthor);
        this.registerKeyValue(Keys.STRUCTURE_IGNORE_ENTITIES, this::ignoreEntities);
        this.registerFieldGetter(Keys.STRUCTURE_IGNORE_ENTITIES, this::shouldIgnoreEntities);
        this.registerFieldSetter(Keys.STRUCTURE_IGNORE_ENTITIES, this::setIgnoreEntities);
        this.registerKeyValue(Keys.STRUCTURE_INTEGRITY, this::integrity);
        this.registerFieldGetter(Keys.STRUCTURE_INTEGRITY, this::getIntegrity);
        this.registerFieldSetter(Keys.STRUCTURE_INTEGRITY, this::setIntegrity);
        this.registerKeyValue(Keys.STRUCTURE_MODE, this::mode);
        this.registerFieldGetter(Keys.STRUCTURE_MODE, this::getMode);
        this.registerFieldSetter(Keys.STRUCTURE_MODE, this::setMode);
        this.registerKeyValue(Keys.STRUCTURE_POSITION, this::position);
        this.registerFieldGetter(Keys.STRUCTURE_POSITION, this::getPosition);
        this.registerFieldSetter(Keys.STRUCTURE_POSITION, this::setPosition);
        this.registerKeyValue(Keys.STRUCTURE_POWERED, this::powered);
        this.registerFieldGetter(Keys.STRUCTURE_POWERED, this::isPowered);
        this.registerFieldSetter(Keys.STRUCTURE_POWERED, this::setPowered);
        this.registerKeyValue(Keys.STRUCTURE_SEED, this::seed);
        this.registerFieldGetter(Keys.STRUCTURE_SEED, this::getSeed);
        this.registerFieldSetter(Keys.STRUCTURE_SEED, this::setSeed);
        this.registerKeyValue(Keys.STRUCTURE_SHOW_AIR, this::showAir);
        this.registerFieldGetter(Keys.STRUCTURE_SHOW_AIR, this::shouldShowAir);
        this.registerFieldSetter(Keys.STRUCTURE_SHOW_AIR, this::setShowAir);
        this.registerKeyValue(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this::showBoundingBox);
        this.registerFieldGetter(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this::shouldShowBoundingBox);
        this.registerFieldSetter(Keys.STRUCTURE_SHOW_BOUNDING_BOX, this::setShowBoundingBox);
        this.registerKeyValue(Keys.STRUCTURE_SIZE, this::size);
        this.registerFieldGetter(Keys.STRUCTURE_SIZE, this::getSize);
        this.registerFieldSetter(Keys.STRUCTURE_SIZE, this::setSize);
    }

    @Override
    public Value<String> author() {
        return new SpongeValue<>(Keys.STRUCTURE_AUTHOR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_AUTHOR, this.author);
    }

    private String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public Value<Boolean> ignoreEntities() {
        return new SpongeValue<>(Keys.STRUCTURE_IGNORE_ENTITIES, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_IGNORE_ENTITIES, this.ignoreEntities);
    }

    private boolean shouldIgnoreEntities() {
        return this.ignoreEntities;
    }

    public void setIgnoreEntities(boolean ignoreEntities) {
        this.ignoreEntities = ignoreEntities;
    }

    @Override
    public Value<Float> integrity() {
        return new SpongeValue<>(Keys.STRUCTURE_INTEGRITY, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_INTEGRITY, this.integrity);
    }

    private float getIntegrity() {
        return this.integrity;
    }

    private void setIntegrity(float integrity) {
        this.integrity = integrity;
    }

    @Override
    public Value<StructureMode> mode() {
        return new SpongeValue<>(Keys.STRUCTURE_MODE, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_MODE, this.mode);
    }

    private StructureMode getMode() {
        return this.mode;
    }

    private void setMode(StructureMode mode) {
        this.mode = mode;
    }

    @Override
    public Value<Vector3i> position() {
        return new SpongeValue<>(Keys.STRUCTURE_POSITION, Vector3i.ONE, this.position);
    }

    private Vector3i getPosition() {
        return this.position;
    }

    private void setPosition(Vector3i position) {
        this.position = position;
    }

    @Override
    public Value<Boolean> powered() {
        return new SpongeValue<>(Keys.STRUCTURE_POWERED, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_POWERED, this.powered);
    }

    private boolean isPowered() {
        return this.powered;
    }

    private void setPowered(boolean powered) {
        this.powered = powered;
    }

    @Override
    public Value<Long> seed() {
        return new SpongeValue<>(Keys.STRUCTURE_SEED, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SEED, this.seed);
    }

    private long getSeed() {
        return this.seed;
    }

    private void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public Value<Boolean> showAir() {
        return new SpongeValue<>(Keys.STRUCTURE_SHOW_AIR, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_AIR, this.showAir);
    }

    private boolean shouldShowAir() {
        return this.showAir;
    }

    private void setShowAir(boolean showAir) {
        this.showAir = showAir;
    }

    @Override
    public Value<Boolean> showBoundingBox() {
        return new SpongeValue<>(Keys.STRUCTURE_SHOW_BOUNDING_BOX, Constants.TileEntity.Structure.DEFAULT_STRUCTURE_SHOW_BOUNDING_BOX, this.showBoundingBox);
    }

    private boolean shouldShowBoundingBox() {
        return this.showBoundingBox;
    }

    private void setShowBoundingBox(boolean showBoundingBox) {
        this.showBoundingBox = showBoundingBox;
    }

    @Override
    public Value<Vector3i> size() {
        return new SpongeValue<>(Keys.STRUCTURE_SIZE, Vector3i.ONE, this.size);
    }

    private Vector3i getSize() {
        return this.size;
    }

    private void setSize(Vector3i size) {
        this.size = size;
    }

    @Override
    public StructureData copy() {
        return new SpongeStructureData(this.author, this.ignoreEntities, this.integrity, this.mode, this.position, this.powered, this.showAir, this.showBoundingBox, this.seed, this.size);
    }

    @Override
    public ImmutableStructureData asImmutable() {
        return new ImmutableSpongeStructureData(this.author, this.ignoreEntities, this.integrity, this.mode, this.position, this.powered, this.showAir, this.showBoundingBox, this.seed, this.size);
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
