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
import java.util.function.Supplier;

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
            return mixinEnabledMappings.get(mixinClassName).get();
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
    private static final Map<String, Supplier<Boolean>> mixinEnabledMappings = ImmutableMap.<String, Supplier<Boolean>>builder()
            .put("org.spongepowered.common.mixin.optimization.MixinSpongeImplHooks_Item_Pre_Merge",
                    () -> SpongeImpl.getGlobalConfig().getConfig().getOptimizations().doDropsPreMergeItemDrops())
            .build();

}
