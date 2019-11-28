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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenBush;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.populator.Mushroom;
import org.spongepowered.api.world.gen.populator.Mushroom.Builder;
import org.spongepowered.api.world.gen.type.MushroomType;

import java.util.function.Function;

import javax.annotation.Nullable;

public class MushroomBuilder implements Mushroom.Builder {

    private ChanceTable<MushroomType> types;
    @Nullable private Function<Location<Extent>, MushroomType> override;
    private VariableAmount count;

    public MushroomBuilder() {
        reset();
    }

    @Override
    public Builder types(ChanceTable<MushroomType> types) {
        checkNotNull(types, "types");
        this.types = types;
        return this;
    }

    @Override
    public Builder type(MushroomType type, double weight) {
        checkNotNull(type);
        checkArgument(weight > 0);
        this.types.add(new WeightedObject<MushroomType>(type, weight));
        return this;
    }

    @Override
    public Builder mushroomsPerChunk(VariableAmount count) {
        this.count = checkNotNull(count, "count");
        return this;
    }

    @Override
    public Builder supplier(@Nullable Function<Location<Extent>, MushroomType> override) {
        this.override = override;
        return this;
    }

    @Override
    public Builder from(Mushroom value) {
        ChanceTable<MushroomType> table = new ChanceTable<>();
        table.addAll(value.getTypes());
        return types(table)
            .mushroomsPerChunk(value.getMushroomsPerChunk())
            .supplier(value.getSupplierOverride().orElse(null));
    }

    @Override
    public Builder reset() {
        this.types = new ChanceTable<>();
        this.count = VariableAmount.fixed(1);
        this.override = null;
        return this;
    }

    @Override
    public Mushroom build() throws IllegalStateException {
        Mushroom populator = (Mushroom) new WorldGenBush(Blocks.field_150338_P);
        populator.getTypes().addAll(this.types);
        populator.setMushroomsPerChunk(this.count);
        populator.setSupplierOverride(this.override);
        return populator;
    }

}
