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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPool;
import org.spongepowered.api.world.generation.structure.jigsaw.JigsawPoolElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;

import java.util.List;

@Mixin(StructureTemplatePool.class)
public abstract class StructureTemplatePoolMixin_API implements JigsawPool {

    // @formatter:off
    @Shadow @Final private ResourceLocation fallback;
    @Shadow @Final private List<Pair<StructurePoolElement, Integer>> rawTemplates;
    // @formatter:on

    @Override
    public JigsawPool fallback() {
        final Registry<StructureTemplatePool> registry = SpongeCommon.server().registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        return (JigsawPool) registry.get(this.fallback);
    }

    @Override
    public WeightedTable<JigsawPoolElement> elements() {
        final WeightedTable<JigsawPoolElement> weightedTable = new WeightedTable<>();
        for (final Pair<StructurePoolElement, Integer> pair : this.rawTemplates) {
            weightedTable.add((JigsawPoolElement) pair.getFirst(), pair.getSecond());
        }
        return weightedTable;
    }
}
