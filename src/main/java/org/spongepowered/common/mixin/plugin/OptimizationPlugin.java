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
package org.spongepowered.common.mixin.plugin;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class OptimizationPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        final SpongeConfig.GlobalConfig globalConfig = SpongeImpl.getGlobalConfig().getConfig();
        if (globalConfig.getModules().useOptimizations()) {
            return mixinEnabledMappings.get(mixinClassName).apply(globalConfig.getOptimizations());
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    // So that any additional optimizations can be added in succession.
    private static final Map<String, Function<SpongeConfig.OptimizationCategory, Boolean>> mixinEnabledMappings = ImmutableMap.<String, Function<SpongeConfig.OptimizationCategory, Boolean >> builder()
            .put("org.spongepowered.common.mixin.optimization.block.state.MixinStateImplementation",
                    SpongeConfig.OptimizationCategory::useBlockStateLookupPatch)
            .put("org.spongepowered.common.mixin.optimization.world.MixinWorld_Lighting",
                    SpongeConfig.OptimizationCategory::useIgnoreUloadedChunkLightingPatch)
            .put("org.spongepowered.common.mixin.optimization.world.MixinWorldServer_Lighting",
                    SpongeConfig.OptimizationCategory::useIgnoreUloadedChunkLightingPatch)
            .put("org.spongepowered.common.mixin.optimization.world.gen.MixinChunkProviderServer_Lighting",
                    SpongeConfig.OptimizationCategory::useIgnoreUloadedChunkLightingPatch)
            .put("org.spongepowered.common.mixin.optimization.MixinSpongeImplHooks_Item_Pre_Merge",
                    SpongeConfig.OptimizationCategory::doDropsPreMergeItemDrops)
            .put("org.spongepowered.common.mixin.optimization.MixinInventoryHelper",
                    SpongeConfig.OptimizationCategory::doDropsPreMergeItemDrops)
            .put("org.spongepowered.common.mixin.optimization.MixinEntity_Item_Pre_Merge",
                    SpongeConfig.OptimizationCategory::doEntityDropsPreMerge)
            .build();

}
