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

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPool;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPoolElement;
import org.spongepowered.common.accessor.world.level.levelgen.structure.pools.StructureTemplatePoolAccessor;

import java.util.ArrayList;
import java.util.List;

public final class SpongeJigsawPoolBuilder implements JigsawPool.Builder {

    private Holder<StructureTemplatePool> fallback;
    private List<Pair<StructurePoolElement, Integer>> templates;

    public SpongeJigsawPoolBuilder() {
        this.reset();
    }

    @Override
    public JigsawPool.Builder from(final JigsawPool StructureTemplatePool) {
        var mcPool = (StructureTemplatePool) StructureTemplatePool;
        this.fallback = mcPool.getFallback();
        this.templates = new ArrayList<>(((StructureTemplatePoolAccessor) mcPool).accessor$rawTemplates());
        return this;
    }

    @Override
    public JigsawPool.Builder add(final JigsawPoolElement element, final int weight) {
        this.templates.add(Pair.of((StructurePoolElement) element, weight));
        return this;
    }

    @Override
    public JigsawPool.Builder name(final ResourceKey name) {
// TODO            this.name = (ResourceLocation) (Object) name;
        return this;
    }

    @Override
    public JigsawPool.Builder fallback(final RegistryReference<JigsawPool> fallback) {
// TODO            this.fallback = (ResourceLocation) (Object) fallback.location();
        return this;
    }

    @Override
    public JigsawPool.Builder fallback(final JigsawPool fallback) {
// TODO            this.fallback = (ResourceLocation) (Object) fallback.key();
        return this;
    }

    @Override
    public JigsawPool.Builder reset() {
        // TODO this.name = null;
        // TODO this.fallback = new ResourceLocation("empty");
        this.templates = new ArrayList<>();
        return this;
    }

    @Override
    public JigsawPool build() {
        // TODO Objects.requireNonNull(this.name, "name");
        return (JigsawPool) new StructureTemplatePool(this.fallback, this.templates);
    }
}
