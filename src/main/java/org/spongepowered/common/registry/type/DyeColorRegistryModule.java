package org.spongepowered.common.registry.type;

import com.google.common.collect.Maps;
import net.minecraft.item.EnumDyeColor;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class DyeColorRegistryModule implements CatalogRegistryModule<DyeColor> {

    @RegisterCatalog(DyeColors.class)
    private final Map<String, DyeColor> dyeColorMappings = Maps.newHashMap();


    @Override
    public Optional<DyeColor> getById(String id) {
        return null;
    }

    @Override
    public Collection<DyeColor> getAll() {
        return null;
    }

    @Override
    public void registerDefaults() {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            this.dyeColorMappings.put(dyeColor.getName().toLowerCase(), (DyeColor) (Object) dyeColor);
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (!this.dyeColorMappings.containsValue((DyeColor) (Object) dyeColor)) {
                this.dyeColorMappings.put(dyeColor.getName().toLowerCase(), (DyeColor) (Object) dyeColor);
            }
        }
    }
}
