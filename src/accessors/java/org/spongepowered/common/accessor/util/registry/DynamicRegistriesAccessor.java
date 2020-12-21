package org.spongepowered.common.accessor.util.registry;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.common.UntransformedAccessorError;

import java.util.Map;

@Mixin(DynamicRegistries.class)
public interface DynamicRegistriesAccessor {

    @Accessor("REGISTRIES") static Map<RegistryKey<? extends Registry<?>>, ?> accessor$REGISTRIES() {
        throw new UntransformedAccessorError();
    }
}
