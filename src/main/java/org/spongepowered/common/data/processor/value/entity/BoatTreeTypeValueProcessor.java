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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import net.minecraft.entity.item.BoatEntity;

public class BoatTreeTypeValueProcessor extends AbstractEntitySingleDataProcessor<BoatEntity, TreeType, Value<TreeType>, TreeData, ImmutableTreeData> {

    public BoatTreeTypeValueProcessor() {
        super(BoatEntity.class, Keys.TREE_TYPE);
    }

    @Override
    protected TreeData createManipulator() {
        return new SpongeTreeData();
    }

    @Override
    protected boolean set(BoatEntity dataHolder, TreeType value) {
        if (value == TreeTypes.OAK) {
            dataHolder.func_184458_a(BoatEntity.Type.OAK);
        } else if (value == TreeTypes.SPRUCE) {
            dataHolder.func_184458_a(BoatEntity.Type.SPRUCE);
        } else if (value == TreeTypes.JUNGLE) {
            dataHolder.func_184458_a(BoatEntity.Type.JUNGLE);
        } else if (value == TreeTypes.DARK_OAK) {
            dataHolder.func_184458_a(BoatEntity.Type.DARK_OAK);
        } else if (value == TreeTypes.BIRCH) {
            dataHolder.func_184458_a(BoatEntity.Type.BIRCH);
        } else if (value == TreeTypes.ACACIA) {
            dataHolder.func_184458_a(BoatEntity.Type.ACACIA);
        } else {
            return false;
        }

        return true;
    }

    @Override
    protected Optional<TreeType> getVal(BoatEntity dataHolder) {
        switch (dataHolder.func_184453_r()) {
            case OAK:
                return Optional.of(TreeTypes.OAK);
            case SPRUCE:
                return Optional.of(TreeTypes.SPRUCE);
            case BIRCH:
                return Optional.of(TreeTypes.BIRCH);
            case JUNGLE:
                return Optional.of(TreeTypes.JUNGLE);
            case ACACIA:
                return Optional.of(TreeTypes.ACACIA);
            case DARK_OAK:
                return Optional.of(TreeTypes.DARK_OAK);
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
    protected ImmutableValue<TreeType> constructImmutableValue(TreeType value) {
        return new ImmutableSpongeValue<>(this.key, value);
    }

    @Override
    protected Value<TreeType> constructValue(TreeType actualValue) {
        return new SpongeValue<>(this.key, actualValue);
    }
}
