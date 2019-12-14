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
package org.spongepowered.common.data.processor.value.entity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.data.type.WoodTypes;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import net.minecraft.entity.item.BoatEntity;

public class BoatTreeTypeValueProcessor extends AbstractEntitySingleDataProcessor<BoatEntity, WoodType, Mutable<WoodType>, TreeData, ImmutableTreeData> {

    public BoatTreeTypeValueProcessor() {
        super(BoatEntity.class, Keys.TREE_TYPE);
    }

    @Override
    protected TreeData createManipulator() {
        return new SpongeTreeData();
    }

    @Override
    protected boolean set(BoatEntity dataHolder, WoodType value) {
        if (value == WoodTypes.OAK) {
            dataHolder.setBoatType(BoatEntity.Type.OAK);
        } else if (value == WoodTypes.SPRUCE) {
            dataHolder.setBoatType(BoatEntity.Type.SPRUCE);
        } else if (value == WoodTypes.JUNGLE) {
            dataHolder.setBoatType(BoatEntity.Type.JUNGLE);
        } else if (value == WoodTypes.DARK_OAK) {
            dataHolder.setBoatType(BoatEntity.Type.DARK_OAK);
        } else if (value == WoodTypes.BIRCH) {
            dataHolder.setBoatType(BoatEntity.Type.BIRCH);
        } else if (value == WoodTypes.ACACIA) {
            dataHolder.setBoatType(BoatEntity.Type.ACACIA);
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected Optional<WoodType> getVal(BoatEntity dataHolder) {
        switch (dataHolder.getBoatType()) {
            case OAK:
                return Optional.of(WoodTypes.OAK);
            case SPRUCE:
                return Optional.of(WoodTypes.SPRUCE);
            case BIRCH:
                return Optional.of(WoodTypes.BIRCH);
            case JUNGLE:
                return Optional.of(WoodTypes.JUNGLE);
            case ACACIA:
                return Optional.of(WoodTypes.ACACIA);
            case DARK_OAK:
                return Optional.of(WoodTypes.DARK_OAK);
            default:
                break;
        }

        return Optional.empty();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.successNoData();
    }

    @Override
    protected Immutable<WoodType> constructImmutableValue(WoodType value) {
        return new ImmutableSpongeValue<>(this.key, value);
    }

    @Override
    protected Mutable<WoodType> constructValue(WoodType actualValue) {
        return new SpongeValue<>(this.key, actualValue);
    }
}
