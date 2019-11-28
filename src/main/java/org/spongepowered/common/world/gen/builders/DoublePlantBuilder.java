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

import net.minecraft.block.DoublePlantBlock.EnumPlantType;
import net.minecraft.world.gen.feature.DoublePlantFeature;
import org.spongepowered.api.data.type.DoublePlantType;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.DoublePlant.Builder;

import java.util.function.Function;

import javax.annotation.Nullable;

public class DoublePlantBuilder implements DoublePlant.Builder {

    private WeightedTable<DoublePlantType> types;
    @Nullable private Function<Location<Extent>, DoublePlantType> override;
    private VariableAmount count;

    public DoublePlantBuilder() {
        reset();
    }

    @Override
    public Builder types(WeightedTable<DoublePlantType> types) {
        checkNotNull(types, "types");
        this.types = types;
        return this;
    }

    @Override
    public Builder type(DoublePlantType type, double weight) {
        checkNotNull(type);
        this.types.add(new WeightedObject<DoublePlantType>(type, weight));
        return this;
    }

    @Override
    public Builder perChunk(VariableAmount count) {
        checkNotNull(count, "count");
        this.count = count;
        return this;
    }

    @Override
    public Builder supplier(@Nullable Function<Location<Extent>, DoublePlantType> override) {
        this.override = override;
        return this;
    }

    @Override
    public Builder from(DoublePlant value) {
        WeightedTable<DoublePlantType> table = new WeightedTable<>();
        table.addAll(value.getPossibleTypes());
        types(table);
        perChunk(value.getPlantsPerChunk());
        supplier(value.getSupplierOverride().orElse(null));
        return this;
    }

    @Override
    public Builder reset() {
        this.types = new WeightedTable<>();
        this.count = VariableAmount.fixed(1);
        this.override = null;
        return this;
    }

    @Override
    public DoublePlant build() throws IllegalStateException {
        if (this.types.isEmpty()) {
            throw new IllegalStateException("Builder is missing required weighted plant types.");
        }
        DoublePlantFeature wgen = new DoublePlantFeature();
        // Set a default just in case the weighted table is ever empty
        wgen.setPlantType(EnumPlantType.GRASS);
        DoublePlant populator = (DoublePlant) wgen;
        populator.getPossibleTypes().addAll(this.types);
        populator.setPlantsPerChunk(this.count);
        populator.setSupplierOverride(this.override);
        return populator;
    }

}
