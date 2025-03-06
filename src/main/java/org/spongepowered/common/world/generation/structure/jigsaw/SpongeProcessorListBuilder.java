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
package org.spongepowered.common.world.generation.structure.jigsaw;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.generation.structure.jigsaw.Processor;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorList;

import java.util.List;
import java.util.Objects;

public final class SpongeProcessorListBuilder implements ProcessorList.Builder {

    @Nullable private StructureProcessorList processorList;

    public SpongeProcessorListBuilder() {
        this.reset();
    }

    @Override
    public ProcessorList.Builder fromValues(final List<Processor> processorList) {
        this.processorList = new StructureProcessorList((List) processorList);
        return this;
    }

    @Override
    public ProcessorList.Builder from(final ProcessorList processorList) {
        this.processorList = new StructureProcessorList((List) processorList.processors());
        return this;
    }

    @Override
    public ProcessorList.Builder reset() {
        this.processorList = null;
        return this;
    }

    @Override
    public ProcessorList build() {
        Objects.requireNonNull(this.processorList, "processorList");
        return (ProcessorList) this.processorList;
    }
}
