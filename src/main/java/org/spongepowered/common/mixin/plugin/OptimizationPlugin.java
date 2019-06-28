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
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.category.OptimizationCategory;
import org.spongepowered.common.config.type.GlobalConfig;

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
        final GlobalConfig globalConfig = SpongeImpl.getGlobalConfigAdapter().getConfig();
        if (globalConfig.getModules().useOptimizations()) {
            final Function<OptimizationCategory, Boolean> optimizationCategoryBooleanFunction = mixinEnabledMappings.get(mixinClassName);
            if (optimizationCategoryBooleanFunction == null) {
                new PrettyPrinter(50).add("Could not find function for optimization patch").centre().hr()
                        .add("Missing function for class: " + mixinClassName)
                        .trace();
                return false;
            }
            return optimizationCategoryBooleanFunction.apply(globalConfig.getOptimizations());
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
    private static final Map<String, Function<OptimizationCategory, Boolean>> mixinEnabledMappings = ImmutableMap.<String, Function<OptimizationCategory, Boolean >> builder()
            .put("org.spongepowered.common.mixin.optimization.MixinSpongeImplHooks_Item_Pre_Merge",
                    OptimizationCategory::doDropsPreMergeItemDrops)
            .put("org.spongepowered.common.mixin.optimization.enchantment.MixinEnchantmentHelper_No_Source_Leak",
                    OptimizationCategory::useEnchantmentHelperFix)
            .put("org.spongepowered.common.mixin.optimization.block.MixinBlockRedstoneWire_Eigen", (optimization) -> {
                    if (optimization.usePandaRedstone() && optimization.useEigenRedstone()) {
                        // Disable panda redstone if eigen is enabled.
                        optimization.setPandaRedstone(false);
                    }
                return optimization.useEigenRedstone() && !optimization.usePandaRedstone();
            })
            .put("org.spongepowered.common.mixin.optimization.block.MixinBlockRedstoneWire_PandaRedstone", (optimization) -> {
                return optimization.usePandaRedstone() && !optimization.useEigenRedstone();
                })
            .put("org.spongepowered.common.mixin.optimization.entity.EntityMixinTameable_Cached_Owner",
                    OptimizationCategory::useCacheTameableOwners)
            .put("org.spongepowered.common.mixin.optimization.network.play.server.MixinSPacketChunkData_Async_Lighting",
                    OptimizationCategory::useAsyncLighting)
            .put("org.spongepowered.common.mixin.optimization.world.MixinChunk_Async_Lighting",
                    OptimizationCategory::useAsyncLighting)
            .put("org.spongepowered.common.mixin.optimization.world.WorldServerMixin_Async_Lighting",
                    OptimizationCategory::useAsyncLighting)
            .put("org.spongepowered.common.mixin.optimization.world.gen.MixinChunkProviderServer_Async_Lighting",
                    OptimizationCategory::useAsyncLighting)
            .put("org.spongepowered.common.mixin.optimization.world.gen.structure.MixinMapGenStructure_Structure_Saving",
                    OptimizationCategory::useStructureSave)
            .put("org.spongepowered.common.mixin.optimization.entity.item.EntityItemFrameMixin_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.entity.MixinEntityTrackerEntry_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.item.MixinItemMap_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.world.storage.MixinMapData_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.world.storage.MixinMapInfo_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.server.MixinMinecraftServer_MapOptimization",
                    OptimizationCategory::useMapOptimization)
            .put("org.spongepowered.common.mixin.optimization.tileentity.MixinTileEntity_HopperOptimization",
                    OptimizationCategory::isOptimizeHoppers)
            .put("org.spongepowered.common.mixin.optimization.tileentity.MixinTileEntityHopper_HopperOptimization",
                    OptimizationCategory::isOptimizeHoppers)
            .put("org.spongepowered.common.mixin.optimization.entity.MixinEntity_UseActiveChunkForCollisions",
                    OptimizationCategory::isUseActiveChunkForCollisions)
            .put("org.spongepowered.common.mixin.optimization.world.MixinWorld_UseActiveChunkForCollisions",
                    OptimizationCategory::isUseActiveChunkForCollisions)
            .put("org.spongepowered.common.mixin.optimization.world.MixinWorldServer_UseActiveChunkForCollisions",
                    OptimizationCategory::isUseActiveChunkForCollisions)
            .build();

}
