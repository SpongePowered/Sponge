package org.spongepowered.common.mixin.core.util;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
public abstract class MixinResourceLocation implements org.spongepowered.api.resource.ResourceLocation {

    @Shadow public abstract String getResourceDomain();

    @Shadow public abstract String getResourcePath();

    @Override
    public String getDomain() {
        return getResourceDomain();
    }

    @Override
    public String getPath() {
        return getResourcePath();
    }
}
