package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.potion.Potion;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.potion.PotionEffectTypes;
import org.spongepowered.common.registry.AdditionalRegistration;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.RegisterCatalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionEffectTypeRegistryModule implements CatalogRegistryModule<PotionEffectType> {

    private final List<PotionEffectType> potionList = new ArrayList<>();

    @RegisterCatalog(PotionEffectTypes.class)
    private final Map<String, PotionEffectType> potionEffectTypeMap = new HashMap<>();

    @Override
    public Optional<PotionEffectType> getById(String id) {
        return Optional.ofNullable(this.potionEffectTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PotionEffectType> getAll() {
        return ImmutableList.copyOf(this.potionList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerDefaults() {
        for (Potion potion : Potion.potionTypes) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType) potion;
                this.potionList.add(potionEffectType);
                this.potionEffectTypeMap.put(potion.getName(), (PotionEffectType) potion);
            }
        }
        ((Map<String, Potion>) Potion.field_180150_I).entrySet().stream()
            .filter(entry -> !this.potionEffectTypeMap.containsKey(entry.getKey().toLowerCase()))
            .forEach(entry -> this.potionEffectTypeMap.put(entry.getKey().toLowerCase(), (PotionEffectType) entry.getValue()));
    }

    @SuppressWarnings("unchecked")
    @AdditionalRegistration
    public void additionalRegistration() { // I'm guessing that this should work very well.
        ((Map<String, Potion>) Potion.field_180150_I).entrySet().stream()
            .filter(entry -> !this.potionEffectTypeMap.containsKey(entry.getKey().toLowerCase()))
            .forEach(entry -> {
                this.potionList.add((PotionEffectType) entry.getValue());
                this.potionEffectTypeMap.put(entry.getKey().toLowerCase(), (PotionEffectType) entry.getValue());
            });
    }
}
