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
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableStructureData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.StructureData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeStructureData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public final class ImmutableSpongeStructureData extends AbstractImmutableData<ImmutableStructureData, StructureData> implements ImmutableStructureData {

    private final Vector3i position;
    private final ImmutableValue<Vector3i> positionValue;
    private final Vector3i size;
    private final ImmutableValue<Vector3i> sizeValue;

    public ImmutableSpongeStructureData(Vector3i position, Vector3i size) {
        super(ImmutableStructureData.class);
        this.position = position;
        this.positionValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_POSITION, Vector3i.ONE, this.position);
        this.size = size;
        this.sizeValue = new ImmutableSpongeValue<>(Keys.STRUCTURE_SIZE, Vector3i.ONE, this.size);
        this.registerGetters();
    }

    @Override
    protected void registerGetters() {
        this.registerKeyValue(Keys.STRUCTURE_POSITION, this::position);
        this.registerFieldGetter(Keys.STRUCTURE_POSITION, this::getPosition);
        this.registerKeyValue(Keys.STRUCTURE_SIZE, this::size);
        this.registerFieldGetter(Keys.STRUCTURE_SIZE, this::getSize);
    }

    @Override
    public ImmutableValue<Vector3i> position() {
        return this.positionValue;
    }

    private Vector3i getPosition() {
        return this.position;
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
        return new SpongeStructureData();
    }

    @Override
    public int compareTo(ImmutableStructureData o) {
        return ComparisonChain.start()
            .compare(this.position, o.position().get())
            .compare(this.size, o.size().get())
            .result();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.STRUCTURE_POSITION.getQuery(), this.positionValue)
            .set(Keys.STRUCTURE_SIZE.getQuery(), this.size);
    }

}
