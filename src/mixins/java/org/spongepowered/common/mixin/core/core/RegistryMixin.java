package org.spongepowered.common.mixin.core.core;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.data.fixer.SpongeDataCodec;

import javax.annotation.Nullable;

@Mixin(Registry.class)
public abstract class RegistryMixin {

    // @formatter:off

    @Shadow @Nullable public abstract ResourceLocation getKey(Object var1);

    // @formatter:on

    @Redirect(method = "encode", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation impl$unwrapSpongeDataCodec(Registry registry, Object var1) {
        if (var1 instanceof SpongeDataCodec) {
            return this.getKey(((SpongeDataCodec<?, ?>) var1).first());
        }

        return this.getKey(var1);
    }
}
