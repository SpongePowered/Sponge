package org.spongepowered.common.registry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.spongepowered.api.ResourceKey;

public final class SpongeResourceKeyFactory implements ResourceKey.Factory {

    @Override
    public ResourceKey resolve(final String formatted, final String defaultNamespace) {
        if (formatted.contains(":")) {
            try {
                final ResourceLocation resourceLocation = new ResourceLocation(formatted);
                return (ResourceKey) (Object) resourceLocation;
            } catch (ResourceLocationException e) {
                throw new IllegalStateException(e);
            }
        } else {
            try {
                final ResourceLocation resourceLocation = new ResourceLocation(defaultNamespace, formatted);
                return (ResourceKey) (Object) resourceLocation;
            } catch (ResourceLocationException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}