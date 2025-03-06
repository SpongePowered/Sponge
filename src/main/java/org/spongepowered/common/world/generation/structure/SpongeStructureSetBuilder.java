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
package org.spongepowered.common.world.generation.structure;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.generation.structure.Structure;
import org.spongepowered.api.world.generation.structure.StructureSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SpongeStructureSetBuilder implements StructureSet.Builder {

    @Nullable
    private StructurePlacement placement;
    private List<net.minecraft.world.level.levelgen.structure.StructureSet.StructureSelectionEntry> structureSelectionEntries = new ArrayList<>();

    public SpongeStructureSetBuilder() {
        this.reset();
    }

    @Override
    public StructureSet.Builder from(final org.spongepowered.api.world.generation.structure.StructureSet structureSet) {
        var mcSet = (net.minecraft.world.level.levelgen.structure.StructureSet) (Object) structureSet;
        this.structureSelectionEntries = mcSet.structures();
        this.placement = mcSet.placement();
        return this;
    }

    @Override
    public StructureSet.Builder placement(final org.spongepowered.api.world.generation.structure.StructurePlacement placement) {
        this.placement = (StructurePlacement) placement;
        return this;
    }

    @Override
    public StructureSet.Builder add(final Structure structure, final int weight) {
        final var mcStructure = (net.minecraft.world.level.levelgen.structure.Structure) structure;
        this.structureSelectionEntries.add(new net.minecraft.world.level.levelgen.structure.StructureSet.StructureSelectionEntry(Holder.direct(mcStructure), weight));
        return this;
    }

    @Override
    public StructureSet.Builder reset() {
        this.placement = null;
        this.structureSelectionEntries = new ArrayList<>();
        return this;
    }

    @Override
    public StructureSet build() {
        Objects.requireNonNull(this.placement, "placement");
        return (StructureSet) (Object) new net.minecraft.world.level.levelgen.structure.StructureSet(this.structureSelectionEntries, this.placement);
    }
}
