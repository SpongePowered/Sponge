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

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.generation.feature.PlacedFeature;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPoolElement;
import org.spongepowered.api.world.generation.structure.jigsaw.ProcessorList;

import java.util.List;
import java.util.function.Function;

public class SpongeJigsawFactory implements JigsawPoolElement.Factory {

    @Override
    public Function<JigsawPoolElement.Projection, JigsawPoolElement> legacy(final ResourceKey template, final ProcessorList processors) {
        final Holder<StructureProcessorList> holder = Holder.direct((StructureProcessorList) processors);
        return new Wrapped(StructurePoolElement.legacy(template.toString(), holder));
    }

    @Override
    public Function<JigsawPoolElement.Projection, JigsawPoolElement> single(final ResourceKey template, final ProcessorList processors) {
        final Holder<StructureProcessorList> holder = Holder.direct((StructureProcessorList) processors);
        return new Wrapped(StructurePoolElement.single(template.toString(), holder));
    }

    @Override
    public Function<JigsawPoolElement.Projection, JigsawPoolElement> feature(final PlacedFeature feature) {
        final var holder = Holder.direct(((net.minecraft.world.level.levelgen.placement.PlacedFeature) (Object) feature));
        return new Wrapped(StructurePoolElement.feature(holder));
    }

    @Override
    public Function<JigsawPoolElement.Projection, JigsawPoolElement> list(final List<Function<JigsawPoolElement.Projection, JigsawPoolElement>> elements) {
        var list = elements.stream().map(func -> {
            if (func instanceof Wrapped w) {
                return w.func;
            }
            return (Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>) p -> (StructurePoolElement) func.apply((JigsawPoolElement.Projection) (Object) p);
        }).toList();
        return new Wrapped(StructurePoolElement.list(list));
    }

    @Override
    public Function<JigsawPoolElement.Projection, JigsawPoolElement> empty() {
        return new Wrapped(StructurePoolElement.empty());
    }

    @Override
    public JigsawPoolElement.Projection matchingTerrain() {
        return (JigsawPoolElement.Projection) (Object) StructureTemplatePool.Projection.TERRAIN_MATCHING;
    }

    @Override
    public JigsawPoolElement.Projection rigid() {
        return (JigsawPoolElement.Projection) (Object) StructureTemplatePool.Projection.RIGID;
    }

    private record Wrapped(Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> func) implements Function<JigsawPoolElement.Projection, JigsawPoolElement> {
        public JigsawPoolElement apply(JigsawPoolElement.Projection projection) {
            return (JigsawPoolElement) this.func.apply((StructureTemplatePool.Projection) (Object) projection);
        }
    }

}
