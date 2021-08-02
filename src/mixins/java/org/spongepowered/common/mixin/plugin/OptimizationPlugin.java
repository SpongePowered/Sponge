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
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.common.applaunch.config.common.OptimizationCategory;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

import java.util.Map;
import java.util.function.Function;

public class OptimizationPlugin extends AbstractMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        final CommonConfig globalConfig = SpongeConfigs.getCommon().get();
        if (globalConfig.modules.optimizations) {
            final Function<OptimizationCategory, Boolean> optimizationCategoryBooleanFunction = OptimizationPlugin.mixinEnabledMappings.get(mixinClassName);
            if (optimizationCategoryBooleanFunction == null) {
                new PrettyPrinter(50).add("Could not find function for optimization patch").centre().hr()
                    .add("Missing function for class: " + mixinClassName)
                    .trace();
                return false;
            }
            return optimizationCategoryBooleanFunction.apply(globalConfig.optimizations);
        }
        return false;
    }

    // So that any additional optimizations can be added in succession.
    private static final Map<String, Function<OptimizationCategory, Boolean>> mixinEnabledMappings = ImmutableMap.<String, Function<OptimizationCategory, Boolean>> builder()
            .put("org.spongepowered.common.mixin.optimization.SpongeImplHooksMixin_Item_Pre_Merge",
                    optimizationCategory -> optimizationCategory.dropsPreMerge)
            .put("org.spongepowered.common.mixin.optimization.mcp.block.BlockRedstoneWireMixin_Eigen", optimizationCategory -> optimizationCategory.eigenRedstone.enabled)
            .put("org.spongepowered.common.accessor.block.BlockRedstoneWireAccessor_Eigen", optimizationCategory -> optimizationCategory.eigenRedstone.enabled)
            .put("org.spongepowered.common.mixin.optimization.mcp.entity.passive.TameableEntity_Optimization_Owner",
                    optimizationCategory -> optimizationCategory.cacheTameableOwners)
            .put("org.spongepowered.common.mixin.optimization.mcp.entity.item.ItemFrameEntityMixin_Optimization_Map",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.invalid.optimization.entity.EntityTrackerEntryMixin_MapOptimization",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.optimization.mcp.item.ItemMapMixin_MapOptimization",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.optimization.mcp.world.storage.MapDataMixin_MapOptimization",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.optimization.mcp.world.storage.MapInfoMixin_MapOptimization",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.optimization.mcp.server.MinecraftServerMixin_MapOptimization",
                    optimizationCategory -> optimizationCategory.optimizeMaps)
            .put("org.spongepowered.common.mixin.optimization.mcp.tileentity.TileEntityMixin_Optimization_Hopper",
                    optimizationCategory -> optimizationCategory.optimizeHoppers)
            .put("org.spongepowered.common.mixin.optimization.mcp.tileentity.HopperTileEntityMixin_Optimization_Hopper",
                    optimizationCategory -> optimizationCategory.optimizeHoppers)
            .put("org.spongepowered.common.mixin.optimization.mcp.entity.EntityMixin_UseActiveChunkForCollisions",
                    optimizationCategory -> optimizationCategory.useActiveChunksForCollisions)
            .put("org.spongepowered.common.mixin.optimization.mcp.world.WorldMixin_UseActiveChunkForCollisions",
                    optimizationCategory -> optimizationCategory.useActiveChunksForCollisions)
            .put("org.spongepowered.common.mixin.optimization.mcp.world.WorldServerMixin_UseActiveChunkForCollisions",
                    optimizationCategory -> optimizationCategory.useActiveChunksForCollisions)
            .put("org.spongepowered.common.mixin.optimization.mcp.tileentity.ChestTileEntityMixin_Optimization_TileEntity",
                    optimizationCategory -> optimizationCategory.optimizeBlockEntityTicking)
            .put("org.spongepowered.common.mixin.optimization.mcp.tileentity.EnderChestTileEntityMixin_Optimization_TileEntity",
                    optimizationCategory -> optimizationCategory.optimizeBlockEntityTicking)
            .put("org.spongepowered.common.mixin.optimization.mcp.block.LeavesBlockMixin_DisablePersistentScheduledUpdate",
                    optimizationCategory -> optimizationCategory.disableScheduledUpdatesForPersistentLeafBlocks)
            .build();

}
