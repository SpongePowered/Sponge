package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemArmor;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.api.data.type.ArmorTypes;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.AdditionalRegistration;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ArmorTypeRegistryModule implements CatalogRegistryModule<ArmorType> {

    @RegisterCatalog(ArmorTypes.class)
    private final Map<String, ArmorType> armorTypeMap = new HashMap<>();

    @Override
    public Optional<ArmorType> getById(String id) {
        return Optional.ofNullable(this.armorTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<ArmorType> getAll() {
        return ImmutableSet.copyOf(this.armorTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        for (ItemArmor.ArmorMaterial armorMaterial : ItemArmor.ArmorMaterial.values()) {
            this.armorTypeMap.put(armorMaterial.name().toLowerCase(), (ArmorType) (Object) armorMaterial);
        }
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (ItemArmor.ArmorMaterial armorMaterial : ItemArmor.ArmorMaterial.values()) {
            if (!this.armorTypeMap.containsKey(armorMaterial.name().toLowerCase())) {
                this.armorTypeMap.put(armorMaterial.name().toLowerCase(), (ArmorType) (Object) armorMaterial);
            }
        }
    }
}
