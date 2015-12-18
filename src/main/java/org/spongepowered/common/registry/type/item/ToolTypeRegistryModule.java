package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.api.data.type.ArmorTypes;
import org.spongepowered.api.data.type.ToolType;
import org.spongepowered.api.data.type.ToolTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.AdditionalRegistration;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ToolTypeRegistryModule implements CatalogRegistryModule<ToolType> {

    @RegisterCatalog(ToolTypes.class)
    private final Map<String, ToolType> armorTypeMap = new HashMap<>();

    @Override
    public Optional<ToolType> getById(String id) {
        return Optional.ofNullable(this.armorTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<ToolType> getAll() {
        return ImmutableSet.copyOf(this.armorTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        for (Item.ToolMaterial toolMaterial : Item.ToolMaterial.values()) {
            if (toolMaterial == Item.ToolMaterial.EMERALD) {
                this.armorTypeMap.put("diamond", (ToolType) (Object) toolMaterial);
            }
            this.armorTypeMap.put(toolMaterial.name().toLowerCase(), (ToolType) (Object) toolMaterial);
        }
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (Item.ToolMaterial toolMaterial : Item.ToolMaterial.values()) {
            if (!this.armorTypeMap.containsKey(toolMaterial.name().toLowerCase())) {
                this.armorTypeMap.put(toolMaterial.name().toLowerCase(), (ToolType) (Object) toolMaterial);
            }
        }
    }
}
