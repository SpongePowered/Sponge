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
package org.spongepowered.common.accessor.data.worldgen.material;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.material.OverworldMaterialRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedInvokerError;

import java.util.List;

@Mixin(OverworldMaterialRules.class)
public interface OverworldMaterialRulesAccessor {

    @Accessor("SURFACE") static ResourceKey<MaterialRule> accessor$SURFACE() {
        throw new UntransformedInvokerError();
    }

    @Accessor("UNDERGROUND") static ResourceKey<MaterialRule> accessor$UNDERGROUND() {
        throw new UntransformedInvokerError();
    }

    @Invoker("createOverworldLike") static MaterialRule invoker$createOverworldLike(
        final HolderGetter<MaterialRule> rules, final boolean doPreliminarySurfaceCheck, final boolean bedrockRoof,
        final boolean bedrockFloor, final MaterialRule mainRuleCloseToSurface, final MaterialRule underground, final List<MaterialRule> oreVeins
    ) {
        throw new UntransformedInvokerError();
    }
}
