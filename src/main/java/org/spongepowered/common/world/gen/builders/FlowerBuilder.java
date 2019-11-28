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
package org.spongepowered.common.world.gen.builders;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.type.PlantType;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.populator.Flower;
import org.spongepowered.api.world.gen.populator.Flower.Builder;

import java.util.function.Function;

import javax.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.world.gen.feature.FlowersFeature;

public class FlowerBuilder implements Flower.Builder {

    private WeightedTable<PlantType> flowers;
    @Nullable private Function<Location<Extent>, PlantType> override;
    private VariableAmount count;

    public FlowerBuilder() {
        reset();
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        checkNotNull(count);
        this.count = count;
        return this;
    }

    @Override
    public Builder types(WeightedTable<PlantType> types) {
        checkNotNull(types);
        this.flowers = types;
        return this;
    }

    @Override
    public Builder type(PlantType type, double weight) {
        checkNotNull(type);
        this.flowers.add(new WeightedObject<PlantType>(type, weight));
        return this;
    }

    @Override
    public Builder supplier(@Nullable Function<Location<Extent>, PlantType> override) {
        this.override = override;
        return this;
    }

    @Override
    public Builder from(Flower value) {
        WeightedTable<PlantType> table = new WeightedTable<>();
        table.addAll(value.getFlowerTypes());
        return perChunk(value.getFlowersPerChunk())
            .types(table)
            .supplier(value.getSupplierOverride().orElse(null));
    }

    @Override
    public Builder reset() {
        this.flowers = new WeightedTable<>();
        this.count = VariableAmount.fixed(2);
        this.override = null;
        return this;
    }

    @Override
    public Flower build() throws IllegalStateException {
        // these values passed in really don't matter, they are set immediately
        // before the populator is first called from the chunk
        Flower populator = (Flower) new FlowersFeature(Blocks.field_150327_N, FlowerBlock.EnumFlowerType.DANDELION);
        populator.setFlowersPerChunk(this.count);
        populator.getFlowerTypes().addAll(this.flowers);
        populator.setSupplierOverride(this.override);
        return populator;
    }

}
