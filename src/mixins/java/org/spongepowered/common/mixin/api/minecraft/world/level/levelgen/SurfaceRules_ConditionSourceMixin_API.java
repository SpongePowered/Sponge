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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.api.world.generation.config.SurfaceRule;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SurfaceRules.ConditionSource.class)
public interface SurfaceRules_ConditionSourceMixin_API extends SurfaceRule.Condition {

    @Override
    default SurfaceRule then(SurfaceRule rule) {
        return (SurfaceRule) SurfaceRules.ifTrue((SurfaceRules.ConditionSource) this, (SurfaceRules.RuleSource) rule);
    }

    @Override
    default SurfaceRule.Condition not() {
        return (SurfaceRule.Condition) SurfaceRules.not((SurfaceRules.ConditionSource) this);
    }
}
